package es.upm.woa.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;

import org.json.simple.JSONObject;

import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.HttpRequest;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.ontology.Cell;
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
	private final int X_BOUNDARY;
	private final int Y_BOUNDARY;
	
	//private WorldTimer worldTimer;
	
	public MovementRequestBehaviour(AgWorld AgWorldInstance){
		this.AgWorldInstance=AgWorldInstance;
		this.map=AgWorldInstance.getMap();
		this.X_BOUNDARY=AgWorldInstance.X_BOUNDARY;
		this.Y_BOUNDARY=AgWorldInstance.Y_BOUNDARY;		
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
						
						//instantiates unit, positions and tribe objects based on message
						int tribePosition =AgWorldInstance.findTribePositionByUnitAID(unitAID);
						Unit senderUnit = AgWorldInstance.findUnitByAID(unitAID, AgWorldInstance.getTribes().get(tribePosition));
						Cell currentPosition = senderUnit.getPosition();
						Cell requestedPosition = getTargetPosition(currentPosition , moveAction.getTargetDirection());
						Tribe tribeSender =  AgWorldInstance.getTribes().get(tribePosition);
						
						Printer.printSuccess(AgWorldInstance.getLocalName(),
								"Unit " + senderName 
								+ " is currently in [" + currentPosition.getX() + "] [" + currentPosition.getY() + "]");
						
						Printer.printSuccess(AgWorldInstance.getLocalName(),
								"Unit " + senderName 
								+ " requested to move to cell [" + requestedPosition.getX() +
								"] [" + requestedPosition.getY() + "]");
						
						if (requestedPosition != null) {
							// ACLMessage reply;
							ACLMessage reply = msg.createReply();
							reply.setLanguage(codec.getName());
							reply.setOntology(ontology.getName());

							AID sender = msg.getSender();	

								//creates and sends a reply to the unit that requested the movement
								
								//TODO: new logic to be fixed --> getTargetDirection
								//createAction.setTarget(requestedPosition);
								
								Action agAction = new Action(sender, moveAction);
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

									//AgWorldInstance.doWait(waitTime);
									
									Thread t = new Thread(new Runnable() {
										@Override
										public void run() {
											// Insert some method call here.
											try {
												Thread.sleep(waitTime);
												Printer.printSuccess(AgWorldInstance.getLocalName(),
														"Unit " + senderName + " WAIT TIME FINISHED");

												// Find the unit based on sender AID
												Unit unit = AgWorldInstance.findUnitByAID(unitAID, tribeSender);
												ACLMessage informMsg = null;
												
												
												if (!AgWorldInstance.isGameOver()) {
													// Move the unit
													unit.setPosition(requestedPosition);
													Cell cell = AgWorldInstance.moveUnitToPosition(unit, requestedPosition);
													moveAction.setNewlyArrivedCell(cell);
													Action agActionMovement = new Action(sender, moveAction);
													
													boolean isNew = tribeSender.addDiscoveredCell(cell);
													
													
													Printer.printSuccess(AgWorldInstance.getLocalName(),
															"Unit " + senderName + "POSITION UPDATED");
													
													//INFORMS UNIT WHO REQUESTED THE MOVEMENT
													informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
															ACLMessage.INFORM, "MoveToCell");
													
													
													AgWorldInstance.getContentManager().fillContent(informMsg, agActionMovement);
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
													
													JSONObject parameters = new JSONObject();
													parameters.put("agent_id",senderUnit.getId().getLocalName());
													JSONObject tile = new JSONObject();
													tile.put("x",cell.getX());
													tile.put("y",cell.getY());
													parameters.put("tile", tile);
													HttpRequest.sendPost("/agent/move", parameters);
													
												} else {
													informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(), msg,
															ACLMessage.FAILURE, "NotifyCellDetail");
													AgWorldInstance.getContentManager().fillContent(informMsg, agAction);
													AgWorldInstance.send(informMsg);
													
													senderUnit.setAction(null);
													AgWorldInstance.updateUnitInTribeByUnitAID(senderUnit);
												}

											} catch (Exception e) {
												e.printStackTrace();
											}

										}
									});
									t.start();
									

								} catch (Exception e) {
									e.printStackTrace();
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

	private Cell getTargetPosition(Cell currentPosition, int coordinate) {
		Cell targetPosition = null;
		Cell tempTarget = null;
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		switch (coordinate) {
		case 1:
			tempTarget = getMirrorCellX(currentPosition,coordinate);
			if (tempTarget != null) {
				targetPosition = tempTarget;
			} else {
				targetPosition = map[x - 2][y];
			}
			break;
		case 2:
			tempTarget = getMirrorCellY(currentPosition,coordinate);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x-1][ y+1];
			}
			break;
		case 3:
			
			tempTarget = getMirrorCellY(currentPosition,coordinate);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x+1][ y+1];
			}
			break;
		case 4:
			tempTarget = getMirrorCellX(currentPosition,coordinate);
			if (tempTarget != null) {
				targetPosition = tempTarget;
			} else {
				targetPosition = map[x + 2][y];
			}
			break;
		case 5:			
			tempTarget = getMirrorCellY(currentPosition,coordinate);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x+1][y-1];
			}
			break;
		case 6:
			tempTarget = getMirrorCellY(currentPosition,coordinate);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x-1][ y-1];
			}
			break;
		}
		return targetPosition;
	}

	private Cell getMirrorCellX(Cell currentPosition, int coordinate) {
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		
		System.out.println("GOINGX FROM X:"+x);
		System.out.println("GOINGX FROM Y:"+y);
		System.out.println("GOINGX TO:"+coordinate);
		// validates that a position is even, else it is odd
		if (x % 2 == 0) {
			// up
			if (x - 2 <= 0 && coordinate==1)
				return map[X_BOUNDARY][y];
			// down
			if (x + 1 >= X_BOUNDARY && coordinate==4)
				return map[2][y];
			else
				return null;
		} else {
			// up
			if (x - 1 <= 0 && coordinate==1)
				return map[X_BOUNDARY - 1][y];
			// down
			if (x + 1 >= X_BOUNDARY && coordinate==4)
				return map[1][y];
			else
				return null;
		}
	}

	private Cell getMirrorCellY(Cell currentPosition, int coordinate) {
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		
		System.out.println("GOINGY FROM X:"+x);
		System.out.println("GOINGY FROM Y:"+y);
		System.out.println("GOINGY TO:"+coordinate);
		// validates that a position is even, else it is odd
		if (y % 2 == 0) {
			// right
			if (y == Y_BOUNDARY  && coordinate==2)
				if(x-2==0)
					return map[1][1];
				else
					return map[x-1][1];
			else if (y == Y_BOUNDARY && coordinate==3)
				if (x == X_BOUNDARY)
					return map[1][1];
				else if(x+1<=X_BOUNDARY)
					return map[x+1][1];
				else
					return map[x][y+1];
			else if (y == Y_BOUNDARY &&  coordinate==5)
				if (x == X_BOUNDARY)
					return map[1][y-1];
				else
					return map[x-1][1];
			else
				return null;
		} else {
			if (coordinate ==2)
					if(x - 1 <= 0 )
						return map[X_BOUNDARY][y +1];
					else
						return null;
			else 
				if  (coordinate==5) {
					if(y - 1 ==0) { 
						return map[x+1][Y_BOUNDARY];
					}
					else 
						return null; 
				}		
				if(coordinate ==6)
					if(y - 1 == 0)
						if(x-1==0)
							return map [X_BOUNDARY][Y_BOUNDARY];
						else
							return map [x-1][Y_BOUNDARY];
					else
						return map[X_BOUNDARY][y-1];
			else
				return null;
		}
	}
}