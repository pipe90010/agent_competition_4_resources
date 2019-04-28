package es.upm.woa.agent.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import es.upm.woa.agent.group2.agents.AgUnit;
import es.upm.woa.agent.group2.agents.AgWorld;
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.agent.group2.rules.AgWorldRules;
import es.upm.woa.agent.group2.util.Searching;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.Empty;
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
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class UnitCreationBehaviour extends CyclicBehaviour {

	private AgWorld AgWorldInstance;
	private ACLMessage msg;
	private String platformName;
	private AgWorldRules worldRules;
	private ContentElement movementRequest;
	private ArrayList<Tribe> tribes;
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Properties properties = new Properties();
	private Cell[][] map;

	private final static int X_BOUNDARY = 100;
	private final static int Y_BOUNDARY = 100;

	public UnitCreationBehaviour(AgWorld AgWorldInstance, ACLMessage msg) {
		this.AgWorldInstance = AgWorldInstance;
		this.map = AgWorldInstance.getMap();
		this.properties = AgWorldInstance.getProperties();
		if (msg != null) {
			this.platformName = AgWorldInstance.getName();
			try {
				this.movementRequest = AgWorldInstance.getContentManager().extractContent(msg);
			} catch (Codec.CodecException | OntologyException e) {
				e.printStackTrace();
			}
		}
	}

	private Integer canCreateUnit(Tribe t, Cell position, Integer index) {
		if (!worldRules.isItsOwnTownhall(t.getTownhall(), tribes.get(index).getTownhall())) {
			return 2;
		}
		if (!worldRules.isInTownhall(t.getTownhall().getX(), t.getTownhall().getY(), position)) {
			return 3;
		}
		if (!worldRules.hasEnoughGold(t.getGold())) {
			return 4;
		}
		if (!worldRules.hasEnoughFood(t.getFood())) {
			return 5;
		}
		if(AgWorldInstance.isGameOver())
			return 6;
		return 1;
	}

	@Override
	public void action() {
		// Waits for creation requests

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
							int indexTribe = Searching.findTribePositionByUnitAID(AgWorldInstance, sender);
							Tribe tribeSender = tribes.get(indexTribe);
							Unit senderUnit = Searching.findUnitByAID(sender, tribeSender);

							// Validate unit creation
							Integer code = canCreateUnit(tribeSender, senderUnit.getPosition(), indexTribe);
							String newUnitName = "UnitY" + Math.random();
							int performative;
							switch (code) {
							case 1:

								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"received creation request from " + "creating unit:" + newUnitName);
								//Unit u = createUnit(newUnitName, tribeSender);
								//tribes.get(indexTribe).addUnit(u, 150, 50);
								
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
								Printer.printSuccess( AgWorldInstance.getLocalName(),"unit" + newUnitName + "game is over");
								performative = ACLMessage.REFUSE;
								break;		
							default:
								performative = ACLMessage.NOT_UNDERSTOOD;
								break;
							}

							ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
									performative, "createUnit");
							myAgent.send(reply);
							if(code==1)
							{
								Unit u = createUnit(newUnitName, tribeSender);
								if(u!=null)
								{
									tribes.get(indexTribe).addUnit(u);
									tribes.get(indexTribe).deductCost(150, 50);
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

	private Unit createUnit(String nickname, Tribe tribe) {

		ContainerController cc = AgWorldInstance.getContainerController();
		// es.upm.woa.agent.group1.AgUnit agentUnit = new
		// es.upm.woa.agent.group1.AgUnit();
		try {
			// cc.acceptNewAgent(nickname, agentUnit).start();
			Cell position = bookNextRandomCell(new Empty());

			Object[] args = new Object[2];
			args[0] = position.getX();
			args[1] = position.getY();
			
			long waitTime= AgWorldInstance.getWorldTimer().getCreationTime();
            
			AgWorldInstance.doWait(waitTime);
            if(!AgWorldInstance.isGameOver())
            {
				AgentController ac = cc.createNewAgent(nickname, AgUnit.class.getName(), args);
				ac.start();
				// TODO: CHECK IF WE NEED TO ADD THE UNIT AS A CONTENT FOR THE CELL
				Unit newUnit = new Unit(AgWorldInstance.getAID(nickname), position);
				// agentUnit.setCurrentPosition(position);
	
				if (tribe != null) {
					AID ag = tribe.getId();
	
					ACLMessage msgInform = MessageFormatter.createMessage(AgWorldInstance.getLocalName(), ACLMessage.INFORM,
							"CreateUnit", ag);
					// Creates a notifyNewUnit action
					NotifyNewUnit notify = new NotifyNewUnit();
	
					notify.setLocation(position);
					notify.setNewUnit(AgWorldInstance.getAID(nickname));
					Action agActionNotification = new Action(ag, notify);
					AgWorldInstance.getContentManager().fillContent(msgInform, agActionNotification);
					AgWorldInstance.send(msgInform);
				}
				return newUnit;
            }
            return null;
		} catch (StaleProxyException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Cell bookNextRandomCell(Concept conc) {
		int x = new Random().nextInt(X_BOUNDARY);
		int y = new Random().nextInt(Y_BOUNDARY);
			return map[x][y];
		
	}
}