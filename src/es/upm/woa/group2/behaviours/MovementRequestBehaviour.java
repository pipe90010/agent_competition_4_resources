package es.upm.woa.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;

import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.common.WorldTimer;
import es.upm.woa.group2.util.Searching;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyCellDetail;
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



public class MovementRequestBehaviour extends CyclicBehaviour {
	
	private AgWorld AgWorldInstance;
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Cell[][] map;
	//private WorldTimer worldTimer;
	
	public MovementRequestBehaviour(AgWorld AgWorldInstance){
		this.AgWorldInstance=AgWorldInstance;
		this.map=AgWorldInstance.getMap();
	}
	
	@Override
	public void action() {
		// Wait for a units request to move to a new position
		ACLMessage msg = AgWorldInstance.receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("MoveToCell")));
		if (msg != null) {// && msg.getPerformative() == ACLMessage.REQUEST) {
			AID unitAID = msg.getSender();
			String senderName = (unitAID).getLocalName();					

			try {

				ContentElement movementRequest = AgWorldInstance.getContentManager().extractContent(msg);

				// validates that the movementRequest is and action and cast it to MoveToCell
				if (movementRequest instanceof Action) {
					Concept conc = ((Action) movementRequest).getAction();
					if (conc instanceof MoveToCell) {
						MoveToCell moveAction = ((MoveToCell) conc);
						
						//TODO: new logic to be fixed --> getTargetDirection
						Cell requestedPosition = null; //moveAction.getTarget();

						if (requestedPosition != null) {
							// ACLMessage reply;
							ACLMessage reply = msg.createReply();
							reply.setLanguage(codec.getName());
							reply.setOntology(ontology.getName());

							AID sender = msg.getSender();

							int indexTribe = AgWorldInstance.findTribePositionByUnitAID(sender);
							Tribe tribeSender = AgWorldInstance.getTribes().get(indexTribe);
							Unit senderUnit = AgWorldInstance.findUnitByAID(sender, tribeSender);

							Cell currentPosition = senderUnit.getPosition(); // TODO: DANIEL
																				// findUnitFromAnyTribe(unitAID).getPosition();

							// validate that current and requested positions are adjacent

							if (!AgWorldInstance.areAdjacentPositions(currentPosition, requestedPosition)) {
								Printer.printSuccess(AgWorldInstance.getLocalName(),
										"Unit " + senderName + " can't move there...");
								reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
										ACLMessage.REFUSE, "MoveToCell");
								AgWorldInstance.send(reply);
							} else {
								Printer.printSuccess(AgWorldInstance.getLocalName(), "Unit " + senderName
										+ " IS ABLE TO MOVE TO NEW POSITION, START WAIT TIME");
								
								//creates and sends a reply to the unit that requested the movement
								
								MoveToCell createAction = new MoveToCell();
								
								//TODO: new logic to be fixed --> getTargetDirection
								//createAction.setTarget(requestedPosition);
								
								Action agAction = new Action(sender, createAction);
								reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
										ACLMessage.AGREE, "MoveToCell");
								AgWorldInstance.getContentManager().fillContent(reply, agAction);
								AgWorldInstance.send(reply);
								
								senderUnit.setAction("MOVING");
								AgWorldInstance.updateUnitInTribeByUnitAID(senderUnit);
								
								//starts the unit movement
								
								try {
									
									//sets the waiting time
									long waitTime = AgWorldInstance.getWorldTimer().getMovementTime();

									AgWorldInstance.doWait(waitTime);

									Printer.printSuccess(AgWorldInstance.getLocalName(),
											"Unit " + senderName + " WAIT TIME FINISHED");

									// Find the unit based on sender AID
									Unit unit = AgWorldInstance.findUnitByAID(unitAID, tribeSender);
									ACLMessage informMsg = null;
									
									
									if (!AgWorldInstance.isGameOver()) {
										// Move the unit
										Cell cell = AgWorldInstance.moveUnitToPosition(unit, requestedPosition);

										boolean isNew = tribeSender.addDiscoveredCell(cell);
										
										
										Printer.printSuccess(AgWorldInstance.getLocalName(),
												"Unit " + senderName + "POSITION UPDATED");
										
										//INFORMS UNIT WHO REQUESTED THE MOVEMENT
										informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
												ACLMessage.INFORM, "informMove");
										AgWorldInstance.getContentManager().fillContent(informMsg, agAction);
										AgWorldInstance.send(informMsg);
										
										
										NotifyCellDetail notify = new NotifyCellDetail();
										notify.setNewCell(cell);

										// INFORMS TRIBE ABOUT NEW CELL DISCOVERY
										ACLMessage informMsgTribe = MessageFormatter.createMessage(
												AgWorldInstance.getLocalName(), ACLMessage.INFORM, "NotifyCellDetail",
												tribeSender.getId());
										Action notifyCellDiscovery = new Action(tribeSender.getId(), notify);
										AgWorldInstance.getContentManager().fillContent(informMsgTribe, notifyCellDiscovery);
										AgWorldInstance.send(informMsgTribe);

										ACLMessage informMsgUnit = MessageFormatter.createMessage(
												AgWorldInstance.getLocalName(), ACLMessage.INFORM, "NotifyCellDetail",
												senderUnit.getId());
										Action notifyCellDiscoveryUnit = new Action(senderUnit.getId(), notify);
										AgWorldInstance.getContentManager().fillContent(informMsgUnit, notifyCellDiscoveryUnit);
										AgWorldInstance.send(informMsgUnit);
										
										senderUnit.setAction(null);
										AgWorldInstance.updateUnitInTribeByUnitAID(senderUnit);
										
									} else {
										informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
												ACLMessage.FAILURE, "NotifyCellDetail");
										AgWorldInstance.send(informMsg);
										
										senderUnit.setAction(null);
										AgWorldInstance.updateUnitInTribeByUnitAID(senderUnit);
									}

								} catch (Codec.CodecException | OntologyException e) {
									e.printStackTrace();
								}
							}
						} else {
							ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
									ACLMessage.NOT_UNDERSTOOD, null);
							AgWorldInstance.send(reply);
						}
					} else {
						Printer.printSuccess(AgWorldInstance.getLocalName(), "Wrong position.");
					}
				} else {
					Printer.printSuccess(AgWorldInstance.getLocalName(), "You lost");
				}
			} catch (Codec.CodecException | OntologyException e) {
				e.printStackTrace();
			}
		} else {
			// If no message arrives
			block();
		}
	}
}