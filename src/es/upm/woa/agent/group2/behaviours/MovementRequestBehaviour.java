package es.upm.woa.agent.group2.behaviours;

import java.util.ArrayList;
import java.util.Properties;

import es.upm.woa.agent.group2.agents.AgWorld;
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.agent.group2.common.WorldTimer;
import es.upm.woa.agent.group2.util.Searching;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyNewCellDiscovery;
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
	//private WorldTimer worldTimer;
	
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
	
	
    private Cell moveUnitToPosition(Unit unit, Cell cell) {
		map[cell.getX()][cell.getY()]= cell;
		//TODO: UPDATE WITH THE NEW ONTOLOGY
		map[cell.getX()][cell.getY()].setContent(new Empty());
		
		updateUnitInTribeByUnitAID(unit);
		
		return map[cell.getX()][cell.getY()];
		}
    
    private boolean updateUnitInTribeByUnitAID(Unit unit) {
		for (int i = 0; i < tribes.size(); i++) {
			for (int j = 0; j < tribes.get(i).getUnits().size(); j++) {
				if (tribes.get(i).getUnits().get(j).getId().getName().equals(unit.getId().getName()))
				{
					tribes.get(i).getUnits().get(j).setPosition(unit.getPosition());
					tribes.get(i).getUnits().get(j).setId(unit.getId());
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void action() {
		// Wait for a units request to move to a new position
		if(msg != null ) {//&& msg.getPerformative() == ACLMessage.REQUEST) {
            AID unitAID = msg.getSender();
            String senderName = (unitAID).getLocalName();
            String platformName = AgWorldInstance.getLocalName();

            try {
            	
                ContentElement movementRequest = AgWorldInstance.getContentManager().extractContent(msg);
                
                if (movementRequest instanceof Action) {
                	Concept conc = ((Action) movementRequest).getAction();
                	if(conc instanceof MoveToCell)
                	{
                		MoveToCell moveAction = ((MoveToCell) conc);
                    	Cell requestedPosition = moveAction.getTarget();
                    
                    	if(requestedPosition!=null)
                    	{
                    	//ACLMessage reply;
                    	ACLMessage reply = msg.createReply();
						reply.setLanguage(codec.getName());
						reply.setOntology(ontology.getName());
						
						AID sender = msg.getSender();
                    	
                    	int indexTribe = Searching.findTribePositionByUnitAID(AgWorldInstance,sender);
						Tribe tribeSender = tribes.get(indexTribe);
						Unit senderUnit = Searching.findUnitByAID(sender, tribeSender);
						
						Cell currentPosition = senderUnit.getPosition(); //TODO: DANIEL findUnitFromAnyTribe(unitAID).getPosition();
						
						//validate that are adjacents 5.2.2 SCENARIO 1 & 2 ready								
														
                        if (false) {//!areAdjacentPositions(currentPosition, requestedPosition)) {
                            Printer.printSuccess( AgWorldInstance.getLocalName(),"Unit " + senderName + " can't move there...");
                            reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(),msg, ACLMessage.REFUSE, "MoveToCell");
                            AgWorldInstance.send(reply);
                        } 
                        else {
                        		Printer.printSuccess( AgWorldInstance.getLocalName(),"Unit " + senderName + " IS ABLE TO MOVE TO NEW POSITION, START WAIT TIME");
                        		MoveToCell createAction = new MoveToCell();
                        		createAction.setTarget(requestedPosition);
                             Action agAction = new Action(sender,createAction);
                        		reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(),msg, ACLMessage.AGREE, "MoveToCell");
                        		AgWorldInstance.getContentManager().fillContent(reply, agAction);
                        		AgWorldInstance.send(reply);

                            try {
                                long waitTime= AgWorldInstance.getWorldTimer().getMovementTime();
                                
                                AgWorldInstance.doWait(waitTime);
                                
                                Printer.printSuccess( AgWorldInstance.getLocalName(),"Unit " + senderName + " WAIT TIME FINISHED");
                                // Find the unit based on sender AID
                            		Unit unit = Searching.findUnitByAID(unitAID, tribeSender);
                            		ACLMessage informMsg =null;
                            		if(!AgWorldInstance.isGameOver())
                            		{
                            			//Move the unit 
	                            		Cell cell = moveUnitToPosition(unit, requestedPosition);
	                            		
	                            		tribeSender.addDiscoveredCell(cell);
	                            		
	                            		Printer.printSuccess( AgWorldInstance.getLocalName(),"Unit " + senderName + "POSITION UPDATED");
	                            		informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(),msg,ACLMessage.INFORM, "NotifyNewCellDiscovery");
	                            		AgWorldInstance.getContentManager().fillContent(informMsg, agAction);
	                            		AgWorldInstance.send(informMsg);
	                            		
	                            		
	                            		NotifyNewCellDiscovery notify = new NotifyNewCellDiscovery();
	                            		notify.setNewCell(cell);
	                            		
	                            		ACLMessage informMsgTribe = MessageFormatter.createMessage(AgWorldInstance.getLocalName(),ACLMessage.INFORM, "informMove", tribeSender.getId());
	                            		Action notifyCellDiscovery = new Action(tribeSender.getId(), notify);
	                            		AgWorldInstance.getContentManager().fillContent(informMsgTribe, notifyCellDiscovery);
	                            		AgWorldInstance.send(informMsgTribe);
	                            		
	                            		ACLMessage informMsgUnit = MessageFormatter.createMessage(AgWorldInstance.getLocalName(),ACLMessage.INFORM, "informMove", senderUnit.getId());
	                            		Action notifyCellDiscoveryUnit = new Action(tribeSender.getId(), notify);
	                            		AgWorldInstance.getContentManager().fillContent(informMsgUnit, notifyCellDiscoveryUnit);
	                            		AgWorldInstance.send(informMsgUnit);
                            		}
                            		else 
                            		{
                            			informMsg = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(),msg,ACLMessage.FAILURE, "NotifyNewCellDiscovery");
                            			AgWorldInstance.send(informMsg);
                            		}
                                
                            } catch (Codec.CodecException | OntologyException e) {
                                e.printStackTrace();
                            }
                        }
                		}
                    	else {
                    		ACLMessage reply = MessageFormatter.createReplyMessage(AgWorldInstance.getLocalName(),msg, ACLMessage.NOT_UNDERSTOOD,null);
                    		AgWorldInstance.send(reply);
                    	}
                    } else {
                    Printer.printSuccess( AgWorldInstance.getLocalName(),"Wrong position.");
                    }
                } else { 
                Printer.printSuccess(AgWorldInstance.getLocalName(),"You lost");
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