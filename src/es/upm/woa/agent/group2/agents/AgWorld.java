package es.upm.woa.agent.group2.agents;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

//import es.upm.woa.agent.group5.AgUnit;
//import es.upm.woa.agent.group5.AgTribe;
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.agent.group2.common.WorldTimer;
import es.upm.woa.agent.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyNewCellDiscovery;
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
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgWorld extends Agent {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String WORLD = "World";
	public final static String TRIBE = "Tribe";

	private final static int GOLD = 1500;
	private final static int FOOD = 500;
	//private final static int HOURS = 150;

	private final static int X_BOUNDARY = 100;
	private final static int Y_BOUNDARY = 100;
	
	// -----------------------------------------------------------------
	// Building Constants
	// -----------------------------------------------------------------
	
	public final static String TOWNHALL = "Townhall";
	
	// -----------------------------------------------------------------
	// Atributes
	// -----------------------------------------------------------------

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
	// Create the array of Tribes
	// loads the map
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private ArrayList<Tribe> tribes;
	private AgWorldRules worldRules;
	private WorldTimer worldTimer;
	private Cell[][] map;
	private Properties properties = new Properties();
	
	private boolean gameOver;
	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	public AgWorld() {
		// TODO Auto-generated constructor stub
	}

	// -----------------------------------------------------------------
	// JADE Methods
	// -----------------------------------------------------------------

	protected void setup() {
		System.out.println("Group2 - " + getLocalName() + ": has entered into the system ");

		// Register of the codec and the ontology to be used in the ContentManager
		// Register language and ontology this part always goes
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		try {
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(WORLD);
			dfd.addServices(sd);
			// Registers its description in the DF
			DFService.register(this, dfd);
			Printer.printSuccess(getLocalName(),"registered in the DF");
			/**
			 * Initialize everything
			 */
			initialize();

			/**
			 * TRIBE IS CREATED FROM THE WORLD
			 */
			Tribe tx = createTribe("TribeX");
			//Tribe ty = createTribe("TribeY");

			/**
			 * TEST UNIT IS CREATED FROM THE WORLD
			 */
			Unit u = createUnit("UnitX",tx);
			u.setPosition(tx.getTownhall());
			//TODO: Change amount of gold and food - TEST Integration
			tx.addUnit(u, 100, 10);
			tribes.add(tx);
			//tribes.add(ty);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		

		/*
		 * BEHAVIORS--------------------------------------------------------------------
		 * ----------------------
		 */
		addBehaviour(new CyclicBehaviour(this) {

			@Override
			public void action() {
				// Waits for creation requests
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchProtocol("createUnit")));
				if (msg != null) {
					try {
						ContentElement ce = null;
						if (msg.getPerformative() == ACLMessage.REQUEST) {
							ce = getContentManager().extractContent(msg);
							// We expect an action inside the message
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// If the action is CreateUnit...
								if (conc instanceof CreateUnit) {
									System.out.println("Group2 - " + myAgent.getLocalName() + ": received creation request from "
											+ (msg.getSender()).getLocalName());
									Printer.printSuccess( getLocalName(),"received creation request from "+(msg.getSender()).getLocalName());
									//Getting AID of message sender
									AID sender = msg.getSender();

									//Finding Unit by AID
									int indexTribe = findTribePositionByUnitAID(sender);
									Tribe tribeSender = tribes.get(indexTribe);
									Unit senderUnit = findUnitByAID(sender, tribeSender);
									
									//Validate unit creation
									Integer code = canCreateUnit(tribeSender, senderUnit.getPosition(),indexTribe);
									String newUnitName = "UnitY"+Math.random();
									int performative;
									switch (code) {
									case 1:

										Printer.printSuccess( getLocalName(),"received creation request from "+"creating unit:" + newUnitName);
										Unit u = createUnit(newUnitName,tribeSender);
										tribes.get(indexTribe).addUnit(u, 150, 50);
										
										performative = ACLMessage.AGREE;
										break;
									case 2:
										Printer.printSuccess( getLocalName(),"Not enough gold");
										performative = ACLMessage.REFUSE;
										break;
									case 3:
										Printer.printSuccess( getLocalName(),"Not enough food");
										performative = ACLMessage.REFUSE;
										break;
									case 4:
										Printer.printSuccess( getLocalName(),"unit" + newUnitName + "not positioned in the townhall");
										performative = ACLMessage.REFUSE;
										break;
									case 5:
										Printer.printSuccess( getLocalName(),"unit" + newUnitName + "not positioned in the right townhall");
										performative = ACLMessage.REFUSE;
										break;
									default:
										performative = ACLMessage.NOT_UNDERSTOOD;
										break;
									}
									
									ACLMessage reply = MessageFormatter.createReplyMessage(getLocalName(),msg, performative, "createUnit");
									myAgent.send(reply);

								}
							}
						}
					} catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException oe) {
						oe.printStackTrace();
					}
				}
				else {
	                // If no message arrives
	                block();
	            }
			}

		});

		

		addBehaviour(new CyclicBehaviour(this) {
	        @Override
	        public void action() {
	            // Wait for a units request to move to a new position
	        	ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchProtocol("MoveToCell")));
	            if(msg != null ) {//&& msg.getPerformative() == ACLMessage.REQUEST) {
	                AID unitAID = msg.getSender();
	                String senderName = (unitAID).getLocalName();
	                String platformName = getLocalName();

	                try {
	                	
	                    ContentElement movementRequest = getContentManager().extractContent(msg);
	                    
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
	                        	
	                        	int indexTribe = findTribePositionByUnitAID(sender);
								Tribe tribeSender = tribes.get(indexTribe);
								Unit senderUnit = findUnitByAID(sender, tribeSender);
								
								Cell currentPosition = senderUnit.getPosition(); //TODO: DANIEL findUnitFromAnyTribe(unitAID).getPosition();
								
								//validate that are adjacents 5.2.2 SCENARIO 1 & 2 ready								
																
	                            if (false) {//!areAdjacentPositions(currentPosition, requestedPosition)) {
	                                Printer.printSuccess( getLocalName(),"Unit " + senderName + " can't move there...");
	                                reply = MessageFormatter.createReplyMessage(getLocalName(),msg, ACLMessage.REFUSE, "MoveToCell");
	                                send(reply);
	                            } 
	                            else {
	                            		Printer.printSuccess( getLocalName(),"Unit " + senderName + " IS ABLE TO MOVE TO NEW POSITION, START WAIT TIME");
	                            		MoveToCell createAction = new MoveToCell();
	                            		createAction.setTarget(requestedPosition);
		                             Action agAction = new Action(sender,createAction);
	                            		reply = MessageFormatter.createReplyMessage(getLocalName(),msg, ACLMessage.AGREE, "MoveToCell");
	                            		getContentManager().fillContent(reply, agAction);
	                            		send(reply);

	                                try {
	                                    long waitTime= worldTimer.getMovementTime();
	                                    
	                                    doWait(waitTime);
	                                    
	                                    Printer.printSuccess( getLocalName(),"Unit " + senderName + " WAIT TIME FINISHED");
	                                    // Find the unit based on sender AID
		                            		Unit unit = findUnitByAID(unitAID, tribeSender);
		                            		ACLMessage informMsg =null;
		                            		if(!gameOver)
		                            		{
		                            			//Move the unit 
			                            		Cell cell = moveUnitToPosition(unit, requestedPosition);

			                            		Printer.printSuccess( getLocalName(),"Unit " + senderName + "POSITION UPDATED");
			                            		informMsg = MessageFormatter.createReplyMessage(getLocalName(),msg,ACLMessage.INFORM, "NotifyNewCellDiscovery");
			                            		getContentManager().fillContent(informMsg, agAction);
			                            		send(informMsg);
			                            		
			                            		
			                            		NotifyNewCellDiscovery notify = new NotifyNewCellDiscovery();
			                            		notify.setNewCell(cell);
			                            		
			                            		ACLMessage informMsgTribe = MessageFormatter.createMessage(getLocalName(),ACLMessage.INFORM, "informMove", tribeSender.getId());
			                            		Action notifyCellDiscovery = new Action(tribeSender.getId(), notify);
			                            		getContentManager().fillContent(informMsgTribe, notifyCellDiscovery);
			                            		send(informMsgTribe);
			                            		
			                            		ACLMessage informMsgUnit = MessageFormatter.createMessage(getLocalName(),ACLMessage.INFORM, "informMove", senderUnit.getId());
			                            		Action notifyCellDiscoveryUnit = new Action(tribeSender.getId(), notify);
			                            		getContentManager().fillContent(informMsgUnit, notifyCellDiscoveryUnit);
			                            		send(informMsgUnit);
		                            		}
		                            		else 
		                            		{
		                            			informMsg = MessageFormatter.createReplyMessage(getLocalName(),msg,ACLMessage.FAILURE, "NotifyNewCellDiscovery");
		                            			send(informMsg);
		                            		}
	                                    
	                                } catch (Codec.CodecException | OntologyException e) {
	                                    e.printStackTrace();
	                                }
	                            }
	                    		}
	                        	else {
	                        		ACLMessage reply = MessageFormatter.createReplyMessage(getLocalName(),msg, ACLMessage.NOT_UNDERSTOOD,null);
	                             send(reply);
	                        	}
	                        } else {
	                        Printer.printSuccess( getLocalName(),"Wrong position.");
	                        }
	                    } else { 
	                    Printer.printSuccess( getLocalName(),"You lost");
	                    }
	                } catch (Codec.CodecException | OntologyException e) {
	                    e.printStackTrace();
	                }
	            } else {
	                // If no message arrives
	                block();
	            }
	        }
	    });
	}

	// -----------------------------------------------------------------
	// Initialize Methods
	// -----------------------------------------------------------------

	private void initialize() {
		tribes = new ArrayList<Tribe>();
		worldRules = new AgWorldRules();
		//PASS ATRIBUTE IN MILISECONDS
		worldTimer= new WorldTimer(1000);
		this.initializeMap();
		gameOver=false;
	}

	private void initializeMap() {
		map = new Cell[X_BOUNDARY][Y_BOUNDARY];
		for (int i = 0; i < X_BOUNDARY; i++) {
			for (int j = 0; j < Y_BOUNDARY; j++) {
				map[i][j] = new Cell();
				map[i][j].setContent(new Empty());
				map[i][j].setOwner(null);
				map[i][j].setX(i);
				map[i][j].setY(j);
			}
		}
	}

	// -----------------------------------------------------------------
	// Regular Methods
	// -----------------------------------------------------------------
	
	//Reservar la siguiente celda aleatoriamente
	private Cell bookNextRandomCell(Concept conc) {
		
		//TODO: Integration test
		//return map[0][0];
		
		
		int x = new Random().nextInt(X_BOUNDARY);
		int y = new Random().nextInt(Y_BOUNDARY);
		//validate if it doesn't
		if (map[x][y].getOwner() == null) {
			//map[x][y].setContent(content);
			map[x][y].setContent(conc);
			return map[x][y];
		} else {
			return bookNextRandomCell(conc);
		}
		
	}

	private Tribe createTribe(String nickname) {
		ContainerController cc = getContainerController();
		
		 AgTribe agentTribe = new AgTribe();
		try {
			cc.acceptNewAgent(nickname, agentTribe).start();
			
			//Cell townhall = bookNextRandomCell(agentTribe.getAID(), "townhall");
			Building townhall = new Building();
			townhall.setOwner(agentTribe.getAID());
			List types =  new jade.util.leap.ArrayList();
			
			types.add(TOWNHALL);
			townhall.setType(types);
			
			
			Cell townhallCell = map[0][0];//bookNextRandomCell(townhall);
			map[townhallCell.getX()][townhallCell.getY()].setOwner(agentTribe.getAID());
			Tribe tribe = new Tribe(agentTribe.getAID(), GOLD, FOOD, townhallCell);
			return tribe;
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		if (!worldRules.hasEnoughFood(t.getFood())){
			return 5;
		}
		return 1;
	}

	private Unit createUnit(String nickname,Tribe tribe) {

		ContainerController cc = getContainerController();
		//es.upm.woa.agent.group1.AgUnit agentUnit = new es.upm.woa.agent.group1.AgUnit();
		try {
			//cc.acceptNewAgent(nickname, agentUnit).start();
			Cell position = bookNextRandomCell(new Empty());
			
			Object [] args= new Object[2];
			args[0]= position.getX();
			args[1]= position.getY();
			AgentController ac =cc.createNewAgent(nickname, AgUnit.class.getName(), args);
			ac.start();
			//TODO: CHECK IF WE NEED TO ADD THE UNIT AS A CONTENT FOR THE CELL
			position.setOwner(tribe.getId());
			Unit newUnit = new Unit(getAID(nickname), position);
			map[position.getX()][position.getY()].setOwner(tribe.getId());
			//agentUnit.setCurrentPosition(position);

			if(tribe!=null)
			{
				AID ag = tribe.getId();

				ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(),ACLMessage.INFORM, "CreateUnit", ag);
				// Creates a notifyNewUnit action
				NotifyNewUnit notify = new NotifyNewUnit();

				notify.setLocation(position);
				notify.setNewUnit(getAID(nickname));
				Action agActionNotification = new Action(ag, notify);
				getContentManager().fillContent(msgInform, agActionNotification);
				send(msgInform);
			}
			return newUnit;
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

	private int findTribePositionByUnitAID(AID aid) {
		for (int i = 0; i < tribes.size(); i++) {
			for (int j = 0; j < tribes.get(i).getUnits().size(); j++) {
				if (tribes.get(i).getUnits().get(j).getId().getName().equals(aid.getName()))
					return i;
			}
		}
		return -1;
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

	private Unit findUnitByAID(AID aid, Tribe tribe) {
		ArrayList<Unit> units = tribe.getUnits();
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getId().getName().equals(aid.getName()))
				return units.get(i);
		}
		return null;
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
            Printer.printSuccess( getLocalName(),"This position doesn't exist on our hexagonal map $$$");
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
        
        if(x > X_BOUNDARY || y > Y_BOUNDARY) return false;
        // If the coordinates are negative
        if(x < 0 || y < 0) return false;
        return ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0));
    }
    
    // TODO: Move to another class
    private Integer readIntProp(String property) {
        return Integer.parseInt(properties.getProperty(property));
    }
    private Cell moveUnitToPosition(Unit unit, Cell cell) {
    			map[cell.getX()][cell.getY()]= cell;
    			//TODO: UPDATE WITH THE NEW ONTOLOGY
    			map[cell.getX()][cell.getY()].setContent(new Empty());
    			
    			updateUnitInTribeByUnitAID(unit);
  			
    			return map[cell.getX()][cell.getY()];
    }
    
    
    
    public ArrayList<Tribe> getTribes() {
		return tribes;
	}


	public Cell[][] getMap() {
		return map;
	}

	public Properties getProperties() {
		return properties;
	}
}