package es.upm.woa.group2.behaviours;

import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.Ground;
import es.upm.woa.ontology.GameOntology;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CreateBuildingBehaviour extends CyclicBehaviour{

	private AgWorld AgWorldInstance;
	private AgWorldRules worldRules;
	private Ontology ontology = GameOntology.getInstance();
	private Codec codec = new SLCodec();
	private Cell[][] map;
	
	public CreateBuildingBehaviour(AgWorld AgWorldInstance){
		this.AgWorldInstance=AgWorldInstance;
		worldRules = new AgWorldRules();
		this.map=AgWorldInstance.getMap();
	}
	
	public void action() {
		// Wait for a units request to request a new building creation
		ACLMessage msg = AgWorldInstance.receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("CreateBuilding")));
		if (msg != null) {
			AID unitAID = msg.getSender();
			String senderName = (unitAID).getLocalName();					

			try {
				ContentElement createBuildingRequest = AgWorldInstance.getContentManager().extractContent(msg);

				// validates that the movementRequest is and action and cast it to MoveToCell
				if (createBuildingRequest instanceof Action) {
					Concept conc = ((Action) createBuildingRequest).getAction();
					AID sender = msg.getSender();
					int indexTribe = AgWorldInstance.findTribePositionByUnitAID(sender);
					Tribe tribeSender = AgWorldInstance.getTribes().get(indexTribe);
					Unit unit = AgWorldInstance.findUnitByAID(unitAID, tribeSender);
					
					if (conc instanceof CreateBuilding) {
						CreateBuilding createBuildingAction = ((CreateBuilding) conc);
						String buildingType = createBuildingAction.getBuildingType();
						
						ACLMessage reply = msg.createReply();
						reply.setLanguage(codec.getName());
						reply.setOntology(ontology.getName());
						
						if(buildingType.equals(AgWorldInstance.TOWNHALL)) {
							
							boolean oneUnitBilding = tribeSender.isAnyUnitBuilding()!=null;
							boolean meetConditions = worldRules.meetTownhallCreationCondition(tribeSender.getGold(), tribeSender.getStones(), tribeSender.getWood(),  oneUnitBilding);
							
							if(meetConditions && unit.getPosition().getContent() instanceof Ground)
							{
								Action agAction = new Action(sender, createBuildingAction);
								reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
										ACLMessage.AGREE, "CreateBuilding");
								AgWorldInstance.getContentManager().fillContent(reply, agAction);
								AgWorldInstance.send(reply);
								
								unit.setAction("BUILDING");
								AgWorldInstance.updateUnitInTribeByUnitAID(unit);
								
								
								Cell currentPosition = unit.getPosition();
								Building townHall = new Building();
								townHall.setType(AgWorldInstance.TOWNHALL);
								map[currentPosition.getX()][currentPosition.getY()].setContent(townHall);
								tribeSender.addNewCity(townHall);
								
								long time = AgWorldInstance.getWorldTimer().getBuildTownhallTime();
								Printer.printSuccess(AgWorldInstance.getLocalName(), "Townhall from "+tribeSender.getId().getLocalName()+" creation time started");
								AgWorldInstance.doWait(time);
								Printer.printSuccess(AgWorldInstance.getLocalName(), "Townhall from "+tribeSender.getId().getLocalName()+" creation time finished");
								
								//updateTribeByTribeAID(tribeSender);
								
								
								Unit unitUpdated = AgWorldInstance.findUnitByAID(unitAID, tribeSender);
								if(unitUpdated.getAction().equals("BUILDING"))
								{
									//INFORMS TRIBE ABOUT CELL UPDATE WITH TOWNHALL
									ACLMessage informMsgTribe = MessageFormatter.createMessage(
											AgWorldInstance.getLocalName(), ACLMessage.INFORM, "informBuildingCreation",
											unitUpdated.getId());
									AgWorldInstance.getContentManager().fillContent(informMsgTribe, agAction);
									AgWorldInstance.send(informMsgTribe);
									tribeSender.setTownhall(currentPosition);
									tribeSender.deductCost(250,0,150,200);
								}
								else
								{
									ACLMessage informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
											ACLMessage.FAILURE, "NotifyCellDetail");
									AgWorldInstance.send(informMsg);
								}
								
								unit.setAction(null);
								//UPDATES TRIBES ARRAY
								AgWorldInstance.updateUnitInTribeByUnitAID(unit);
								AgWorldInstance.updateTribeByTribeAID(tribeSender);		
								
							}
							else
							{
								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"Unit " + senderName + " CANNOT CREATE BUILDING");
								reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
										ACLMessage.REFUSE, "CreateBuilding");
								AgWorldInstance.send(reply);
							}
						}
						
					}
					else
					{
						Printer.printSuccess(AgWorldInstance.getLocalName(),
								"CREATE BUILDING MESSAGE NOT UNDERSTOOD");
						ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
								ACLMessage.NOT_UNDERSTOOD, null);
						AgWorldInstance.send(reply);
					}
						
				}
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}
