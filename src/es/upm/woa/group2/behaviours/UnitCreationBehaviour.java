package es.upm.woa.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import es.upm.woa.group2.agent.AgUnit;
import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.NotifyNewUnit;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class UnitCreationBehaviour extends CyclicBehaviour {

	private AgWorld AgWorldInstance;
	private AgWorldRules worldRules;
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Cell[][] map;


	public UnitCreationBehaviour(AgWorld AgWorldInstance) {
		this.AgWorldInstance = AgWorldInstance;
		this.map = AgWorldInstance.getMap();
	}

	@Override
	public void action() {
		// Waits for creation requests
		ACLMessage msg = AgWorldInstance.receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("createUnit")));
		if (msg != null) {
			try {
				ContentElement ce = null;
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					ce = AgWorldInstance.getContentManager().extractContent(msg);
					// We expect an action inside the message
					if (ce instanceof Action) {
						Action agAction = (Action) ce;
						Concept conc = agAction.getAction();
						// If the action is CreateUnit...
						if (conc instanceof CreateUnit) {
							System.out.println("Group2 - " + myAgent.getLocalName()
									+ ": received creation request from " + (msg.getSender()).getLocalName());
							Printer.printSuccess(AgWorldInstance.getLocalName(),
									"received creation request from " + (msg.getSender()).getLocalName());
							// Getting AID of message sender
							AID sender = msg.getSender();

							// Finding Unit by AID
							int indexTribe = AgWorldInstance.findTribePositionByUnitAID(sender);
							Tribe tribeSender = AgWorldInstance.getTribes().get(indexTribe);
							Unit senderUnit = AgWorldInstance.findUnitByAID(sender, tribeSender);

							// Validate unit creation
							Integer code = AgWorldInstance.canCreateUnit(tribeSender, senderUnit.getPosition(), indexTribe);
							String newUnitName = "Unit-" + tribeSender.getId().getName()
									+ tribeSender.getMemberSize();
							int performative;
							switch (code) {
							case 1:
								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"received creation request from " + "creating unit:" + newUnitName);
								performative = ACLMessage.AGREE;
								break;
							case 2:
								Printer.printSuccess(AgWorldInstance.getLocalName(), "Not enough gold");
								performative = ACLMessage.REFUSE;
								break;
							case 3:
								Printer.printSuccess(AgWorldInstance.getLocalName(), "Not enough food");
								performative = ACLMessage.REFUSE;
								break;
							case 4:
								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"unit" + newUnitName + "not positioned in the townhall");
								performative = ACLMessage.REFUSE;
								break;
							case 5:
								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"unit" + newUnitName + "not positioned in the right townhall");
								performative = ACLMessage.REFUSE;
								break;
							case 6:
								Printer.printSuccess(AgWorldInstance.getLocalName(), "unit" + newUnitName + "game is over");
								performative = ACLMessage.REFUSE;
								break;
							default:
								performative = ACLMessage.NOT_UNDERSTOOD;
								break;
							}

							ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
									performative, "createUnit");
							myAgent.send(reply);

							if (code == 1) {
								Unit u = AgWorldInstance.createUnit(false,newUnitName, tribeSender);
								if (u != null) {
									AgWorldInstance.getTribes().get(indexTribe).addUnit(u);
									AgWorldInstance.getTribes().get(indexTribe).deductCost(150, 50,0,0);
								}
							}
						}
					}
				}
			} catch (CodecException e) {
				e.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
		} else {
			// If no message arrives
			block();
		}
	}
}