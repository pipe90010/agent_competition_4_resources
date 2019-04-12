package es.upm.woa.agent.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;

import es.upm.woa.agent.group2.agents.AgWorld;
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.util.Searching;
import es.upm.woa.ontology.Cell;
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



public class MovementRequestBehaviour extends CyclicBehaviour {
	
	private AgWorld AgWorldInstance;
	private ACLMessage msg;
	private String platformName;
	private ContentElement movementRequest;
	private ArrayList<Tribe> tribes;
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Properties properties = new Properties();
	private Cell[][] map;
	private String agentName;
	
	public MovementRequestBehaviour(AgWorld AgWorldInstance,ACLMessage msg){
		this.AgWorldInstance=AgWorldInstance;
		this.map=AgWorldInstance.getMap();
		this.properties= AgWorldInstance.getProperties();
		if(msg != null) {	
			this.platformName=AgWorldInstance.getName();
			try {
                this.movementRequest = AgWorldInstance.getContentManager().extractContent(msg);                
                }
			catch (Codec.CodecException | OntologyException e) {
            	e.printStackTrace();
		 	}		
		}
	}
	
	
	/**
     * Checks whether two positions are adjacent to each other.
     * This is relevant for unit movement, because they can only move to adjacent positions.
     * TODO: This should definitely be in a separate class...
     *
     * @param posA          First position
     * @param posB          Second position
     * @return boolean      Whether the two positions are adjacent
     */
    private boolean areAdjacentPositions(Cell posA, Cell posB) {
        // First, both positions need to be valid
        if(!isValidPosition(posA) || !isValidPosition(posB)) {
            System.out.println("$$$ This position doesn't exist on our hexagonal map $$$");
            return false;
        }

        // Second, the deltas of both dimensions have to be correct
        int deltaX = Math.abs(posA.getX() - posB.getX());
        int deltaY = Math.abs(posA.getY() - posB.getY());

        return (deltaX <= 2 && deltaY <= 1);
    }
    
    // TODO: Move to another class
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidPosition(Cell position) {
        int x = position.getX();
        int y = position.getY();

        // If the coordinates are outside of map
        
        if(x > readIntProp("map.width") || y > readIntProp("map.height")) return false;

        // If the coordinates are negative
        if(x < 0 || y < 0) return false;
        return ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0));
    }
    
    // TODO: Move to another class
    private Integer readIntProp(String property) {
        return Integer.parseInt(properties.getProperty(property));
    }
    private void moveUnitToPosition(AID aid, Cell cell) {
    			map[cell.getX()][cell.getY()]= cell;
    			//TODO: UPDATE WITH THE NEW ONTOLOGY
    			//map[cell.getX()][cell.getY()].setContent(aid.getName());
    }
	
	
	
	@Override
	public void action() {
		// Wait for a units request to move to a new position
		
		if (msg != null) {
			AID unitAID = msg.getSender();
			String senderName = (unitAID).getLocalName();			

			if (movementRequest instanceof Action) {
				Action action = (Action) movementRequest;
				Concept concept = action.getAction();

				// Validate that a cell exists: 5.2.2 Scenario 3
				if (concept instanceof Cell) {

					// ACLMessage reply;
					ACLMessage reply = msg.createReply();
					reply.setLanguage(codec.getName());
					reply.setOntology(ontology.getName());

					AID sender = reply.getSender();
					Cell requestedPosition = (Cell) concept;

					int indexTribe = Searching.findTribePositionByUnitAID(AgWorldInstance, sender);
					Tribe tribeSender = tribes.get(indexTribe);
					Unit senderUnit = Searching.findUnitByAID(sender, tribeSender);

					Cell currentPosition = senderUnit.getPosition(); // TODO: DANIEL
																		// findUnitFromAnyTribe(unitAID).getPosition();

					// validate that are adjacent 5.2.2 SCENARIO 1 & 2 ready

					if (!areAdjacentPositions(currentPosition, requestedPosition)) {
						System.out.println(platformName + "Unit " + senderName + " can't move there...");
						reply = MessageFormatter.createReplyMessage(this.agentName,msg, ACLMessage.REFUSE, "move");
						AgWorldInstance.send(reply);
					} else {
						// Next, we'll have to store the new position for this unit
						// TODO: DANIEL moveUnitToPosition(unitAID, requestedPosition);

						reply = MessageFormatter.createReplyMessage(this.agentName,msg, ACLMessage.AGREE, "move");
						AgWorldInstance.send(reply);

						// TODO (MAYBE): I'm not sure, but maybe we should inform all subscribers about
						// this change?
						Cell cell = null; // TODO: DANIEL getCellInfo(requestedPosition);

						try {
							// Send an INFORM with its new position
							Long gameHour = null; // TODO: DANIEL
													// readFloatProp("game.hour.length").longValue();
							Long movementDuration = null; // TODO: DANIEL
															// readIntProp("unit.movement.duration").longValue();

							// This should be read from the properties file but the format chosen in there
							// is really weird
							// and casting the types didn't work as expected...
							AgWorldInstance.doWait(2000);
							ACLMessage informMsg = MessageFormatter.createMessage(this.agentName,ACLMessage.INFORM, "move",
									unitAID);
							AgWorldInstance.getContentManager().fillContent(informMsg, new Action(unitAID, cell));

							AgWorldInstance.send(informMsg);
						} catch (Codec.CodecException | OntologyException e) {
							e.printStackTrace();
						}

						// try {
						// Inform all subscribers
						ArrayList<AID> subscribers = null; // TODO: DANIEL getSubscribers();
						ArrayList<Tribe> tribes = null; // TODO: DANIEL getTribes();

						for (Tribe tribe : tribes) {
							ArrayList<Cell> positions = null; // TODO: DANIEL
																// entityManager.retrieveTribeKnownPosition(tribe.getAgentAID());

							// Verify if the tribe knows this position
							if (positions.contains(requestedPosition)) {
								// Notify Tribe
								ACLMessage informMsg = null; // TODO: DANIEL
																// MessageFormatter.createMessage(ACLMessage.INFORM,
																// "subscribe", tribe.getAgentAID());
								// TODO: DANIEL getContentManager().fillContent(informMsg, new
								// Action(tribe.getAgentAID(), cell));
								AgWorldInstance.send(informMsg);

								// Notify Units
								for (Unit unit : tribe.getUnits()) {
									ACLMessage informMsgUnit = null; // TODO: DANIEL
																		// MessageFormatter.createMessage(ACLMessage.INFORM,
																		// "subscribe", unit.getUnitID());
									// TODO: DANIEL getContentManager().fillContent(informMsgUnit, new
									// Action(unit.getUnitID(), cell));
									AgWorldInstance.send(informMsgUnit);
								}
							}
						}
						// }
						// catch (Codec.CodecException | OntologyException e) {
						// e.printStackTrace();
						// }
					}
				} else {
					System.out.println("Wrong position.");
				}
			} else {
				System.out.println("You lost");
			}
		} else {
			// If no message arrives
			block();
		}
	}
}