package es.upm.woa.group2.behaviours;

import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.InitalizeTribe;
import es.upm.woa.ontology.RegisterTribe;
import es.upm.woa.ontology.ResourceAccount;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RegisterTribeBehaviour extends CyclicBehaviour{

	private AgWorld AgWorldInstance;
	private AgWorldRules worldRules;
	private Ontology ontology = GameOntology.getInstance();
	private Codec codec = new SLCodec();
	private Cell[][] map;
	
	public RegisterTribeBehaviour(AgWorld AgWorldInstance){
		this.AgWorldInstance=AgWorldInstance;
		worldRules = new AgWorldRules();
		this.map=AgWorldInstance.getMap();
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub

		// Wait for a units request to request a new building creation
		ACLMessage msg = AgWorldInstance.receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("RegisterTribe")));
		
		if (msg != null) {
			AID unitAID = msg.getSender();
			String senderName = (unitAID).getLocalName();					

			try {
				ContentElement registeredTribeRequest = AgWorldInstance.getContentManager().extractContent(msg);

				// validates that the movementRequest is and action and cast it to MoveToCell
				if (registeredTribeRequest instanceof Action) {
					Concept conc = ((Action) registeredTribeRequest).getAction();
					AID sender = msg.getSender();
					
					
					if (conc instanceof RegisterTribe) {
						RegisterTribe registeredTribeAction = ((RegisterTribe) conc);
						int teamNumber = registeredTribeAction.getTeamNumber();
						Action agAction = new Action(sender, registeredTribeAction);
						if (!AgWorldInstance.isRegistrationPeriod() || AgWorldInstance.findTribePositionByTeamNumber(teamNumber) != -1) {
							Printer.printSuccess(AgWorldInstance.getLocalName(),"CANNOT REGISTER TRIBE");	
							
							ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
									ACLMessage.REFUSE, "RegisterTribe");
							AgWorldInstance.getContentManager().fillContent(reply, agAction);
							AgWorldInstance.send(reply);									
						}
						else
						{
							Printer.printSuccess(AgWorldInstance.getLocalName(),"TRIBE REGISTERED!");	
							
							
							ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
									ACLMessage.AGREE, "RegisterTribe");
							AgWorldInstance.getContentManager().fillContent(reply, agAction);
							AgWorldInstance.send(reply);
							
							ACLMessage initializeInform = MessageFormatter.createMessage(AgWorldInstance.getLocalName(), ACLMessage.INFORM,
									"RegisterTribe", sender);
							Action initializeActionInform = new Action(sender, registeredTribeAction);									
							AgWorldInstance.getContentManager().fillContent(initializeInform, initializeActionInform);
							AgWorldInstance.send(initializeInform);
							
							//Create Tribe
							
							Tribe tg2 = new Tribe(sender, AgWorldInstance.GOLD, AgWorldInstance.FOOD,AgWorldInstance.STONES,AgWorldInstance.WOOD,teamNumber);
							
							/**
							 * TEST UNIT IS CREATED FROM THE WORLD
							 */
							Cell randomPosition = AgWorldInstance.bookNextRandomCell();
							AgWorldInstance.createUnit(true,"Unit"+teamNumber+"X1", tg2,randomPosition,"es.upm.woa.group"+teamNumber+".agent.AgUnit");
							AgWorldInstance.createUnit(true,"Unit"+teamNumber+"X2", tg2,randomPosition,"es.upm.woa.group"+teamNumber+".agent.AgUnit");
							AgWorldInstance.createUnit(true,"Unit"+teamNumber+"X3", tg2,randomPosition,"es.upm.woa.group"+teamNumber+".agent.AgUnit");
							
							//Unit u2 = AgWorldInstance.createUnit(true,"UnitX2", tg2,randomPosition);
							//u2.setRole(Unit.EXPLOITER_ROLE);
														
							// adds created unit to the tribe and deduct cost of each unit creation
							/*tg2.addUnit(u1);
							tg2.addUnit(u2);
							tg2.addUnit(u3);*/
							
							AgWorldInstance.getTribes().add(tg2);
							
							
							AID ag = tg2.getId();
							ACLMessage msgInform = MessageFormatter.createMessage(AgWorldInstance.getLocalName(), ACLMessage.INFORM,
									"InitalizeTribe", ag);
															
							InitalizeTribe initTribe = new InitalizeTribe();	
							ResourceAccount resource = new ResourceAccount();
							resource.setFood(AgWorldInstance.FOOD);
							resource.setGold(AgWorldInstance.GOLD);
							resource.setStone(AgWorldInstance.STONES);
							resource.setWood(AgWorldInstance.WOOD);
							
							initTribe.setStartingResources(resource);
							initTribe.setStartingPosition(randomPosition);
							initTribe.setMapHeight(AgWorldInstance.X_BOUNDARY);
							initTribe.setMapWidth(AgWorldInstance.Y_BOUNDARY);
							
							initTribe.setUnitList(tg2.getOntologyUnitsAID());
							
							Action initializeAction = new Action(sender, initTribe);									
							AgWorldInstance.getContentManager().fillContent(msgInform, initializeAction);
							AgWorldInstance.send(msgInform);
							
						}
						
						ACLMessage reply = msg.createReply();
						reply.setLanguage(codec.getName());
						reply.setOntology(ontology.getName());
					}
					else
					{
						ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg, ACLMessage.NOT_UNDERSTOOD, null);
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
