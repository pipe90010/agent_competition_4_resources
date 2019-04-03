package es.upm.woa.agent.group2.agents;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.rules.AgWorldRules;
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
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgWorld extends Agent {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------

	public final static String WORLD = "World";
	public final static String TRIBE = "Tribe";

	private final static int GOLD = 150;
	private final static int FOOD = 50;
	private final static int HOURS = 150;

	private final static int X_BOUNDARY = 100;
	private final static int Y_BOUNDARY = 100;
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
	private Cell[][] map;
	private Properties properties = new Properties();
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
		System.out.println(getLocalName() + ": has entered into the system ");

		// Register of the codec and the ontology to be used in the ContentManager
		// Register language and ontology this part always goes
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		/**
		 * Initialize everything
		 */
		initialize();

		/**
		 * TRIBE IS CREATED FROM THE WORLD
		 */
		Tribe tx = createTribe("TribeX");
		Tribe ty = createTribe("TribeY");

		/**
		 * TEST UNIT IS CREATED FROM THE WORLD
		 */
		Unit u = createUnit("UnitX");
		u.setPosition(tx.getTownhall());
		tx.addUnit(u, 0, 0);
		tribes.add(tx);
		tribes.add(ty);
		try {
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(WORLD);
			dfd.addServices(sd);
			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName() + ": registered in the DF");
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
				// TODO Auto-generated method stub
				// Waits for creation requests
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
						MessageTemplate.MatchOntology(ontology.getName())));
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
									System.out.println(myAgent.getLocalName() + ": received creation request from "
											+ (msg.getSender()).getLocalName());
									ACLMessage reply = msg.createReply();
									reply.setLanguage(codec.getName());
									reply.setOntology(ontology.getName());
									// TODO CONDITION FOR AGREEING
									
									//Getting AID of message sender
									AID sender = msg.getSender();

									//Finding Unit by AID
									int indexTribe = findTribePositionByUnitAID(sender);
									Tribe tribeSender = tribes.get(indexTribe);
									Unit senderUnit = findUnitByAID(sender, tribeSender);
									
									//Validate unit creation
									Integer code = canCreateUnit(tribeSender, senderUnit.getPosition(),indexTribe);
									String newUnitName = "UnitY";

									switch (code) {
									case 1:

										System.out.println("creating unit:" + newUnitName);
										Unit u = createUnit(newUnitName);
										tribes.get(indexTribe).addUnit(u, 150, 50);
										DFAgentDescription dfd2 = new DFAgentDescription();
										ServiceDescription sd2 = new ServiceDescription();
										sd2.setType(TRIBE);
										dfd2.addServices(sd2);

										DFAgentDescription[] res = new DFAgentDescription[1];
										res = DFService.search(myAgent, dfd2);
										if (res.length > 0) {
											AID ag = (AID) res[0].getName();
											res = DFService.search(myAgent, dfd2);
											ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
											msgInform.addReceiver(ag);
											msgInform.setLanguage(codec.getName());
											msgInform.setOntology(ontology.getName());
											// Creates a notifyNewUnit action
											NotifyNewUnit notify = new NotifyNewUnit();

											notify.setLocation(u.getPosition());
											notify.setNewUnit(u.getId());
											Action agActionNotification = new Action(ag, notify);
											getContentManager().fillContent(msgInform, agActionNotification);
											send(msgInform);
											System.out.println("INFORM CREATION TO TRIBE");
										}

										reply.setPerformative(ACLMessage.AGREE);
										break;
									case 2:
										System.out.println("Not enough gold");
										reply.setPerformative(ACLMessage.REFUSE);
										break;
									case 3:
										System.out.println("Not enough food");
										reply.setPerformative(ACLMessage.REFUSE);
										break;
									case 4:
										System.out.println("unit" + newUnitName + "not positioned in the townhall");
										reply.setPerformative(ACLMessage.REFUSE);
										break;
									case 5:
										System.out.println("unit" + newUnitName + "not positioned in the right townhall");
										reply.setPerformative(ACLMessage.REFUSE);
										break;
									default:
										reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
										break;
									}
									myAgent.send(reply);

								}
							}
						}
					} catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException oe) {
						oe.printStackTrace();
					} catch (FIPAException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		

	    Behaviour handleMovementRequests = new CyclicBehaviour() {
	        @Override
	        public void action() {
	            // Wait for a units request to move to a new position
	            ACLMessage msg = receive(
	                MessageTemplate.and(
	                    MessageTemplate.MatchProtocol("move"),
	                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
	                )
	            );

	            if(msg != null) {
	                AID unitAID = msg.getSender();
	                String senderName = (unitAID).getLocalName();
	                String platformName = getLocalName();

	                try {
	                    ContentElement movementRequest = getContentManager().extractContent(msg);

	                    if (movementRequest instanceof Action) {
	                        Action action = (Action) movementRequest;
	                        Concept concept = action.getAction();
	                        
	                        
	                        //Validate that a cell exists: 5.2.2 Scenario 3
	                        if (concept instanceof Cell) {
	                        	
	                        	//ACLMessage reply;
	                        	ACLMessage reply = msg.createReply();
								reply.setLanguage(codec.getName());
								reply.setOntology(ontology.getName());
								
								
								AID sender = reply.getSender();
	                        	Cell requestedPosition = (Cell) concept;
	                        	
	                        	
	                        	int indexTribe = findTribePositionByUnitAID(sender);
								Tribe tribeSender = tribes.get(indexTribe);
								Unit senderUnit = findUnitByAID(sender, tribeSender);
								
								Cell currentPosition = senderUnit.getPosition(); //TODO: DANIEL findUnitFromAnyTribe(unitAID).getPosition();
								
								//validate that are adjacents 5.2.2 SCENARIO 1 & 2								
																
	                            if (!areAdjacentPositions(currentPosition, requestedPosition)) {
	                                System.out.println(platformName + "Unit " + senderName + " can't move there...");
	                                reply = MessageFormatter.createReplyMessage(msg, ACLMessage.REFUSE, "move");
	                                send(reply);
	                            } else {
	                                // Next, we'll have to store the new position for this unit
	                            	//TODO: DANIEL moveUnitToPosition(unitAID, requestedPosition);

	                                reply = MessageFormatter.createReplyMessage(msg, ACLMessage.AGREE, "move");
	                                send(reply);

	                                // TODO (MAYBE): I'm not sure, but maybe we should inform all subscribers about this change?
	                                Cell cell = null; //TODO: DANIEL getCellInfo(requestedPosition);

	                                try {
	                                    // Send an INFORM with its new position
	                                    Long gameHour = null; //TODO: DANIEL readFloatProp("game.hour.length").longValue();
	                                    Long movementDuration = null; //TODO: DANIEL readIntProp("unit.movement.duration").longValue();

	                                    // This should be read from the properties file but the format chosen in there is really weird
	                                    // and casting the types didn't work as expected...
	                                    doWait(2000);
	                                    ACLMessage informMsg = MessageFormatter.createMessage(ACLMessage.INFORM, "move", unitAID);
	                                    getContentManager().fillContent(informMsg, new Action(unitAID, cell));

	                                    send(informMsg);
	                                } catch (Codec.CodecException | OntologyException e) {
	                                    e.printStackTrace();
	                                }

	                                //try {
	                                    // Inform all subscribers
	                                    ArrayList<AID> subscribers = null; //TODO: DANIEL getSubscribers();
	                                    ArrayList<Tribe> tribes = null; //TODO: DANIEL getTribes();

	                                    for(Tribe tribe : tribes) {
	                                        ArrayList<Cell> positions = null; //TODO: DANIEL entityManager.retrieveTribeKnownPosition(tribe.getAgentAID());

	                                        // Verify if the tribe knows this position
	                                        if (positions.contains(requestedPosition)) {
	                                            // Notify Tribe
	                                            ACLMessage informMsg = null; //TODO: DANIEL  MessageFormatter.createMessage(ACLMessage.INFORM, "subscribe", tribe.getAgentAID());
	                                          //TODO: DANIEL getContentManager().fillContent(informMsg, new Action(tribe.getAgentAID(), cell));
	                                            send(informMsg);

	                                            // Notify Units
	                                            for (Unit unit : tribe.getUnits()) {
	                                            	ACLMessage informMsgUnit = null; //TODO: DANIEL MessageFormatter.createMessage(ACLMessage.INFORM, "subscribe", unit.getUnitID());
	                                              //TODO: DANIEL getContentManager().fillContent(informMsgUnit, new Action(unit.getUnitID(), cell));
	                                                send(informMsgUnit);
	                                            }
	                                        }
	                                    }
	                                //}
	                                //catch (Codec.CodecException | OntologyException e) {
	                                //    e.printStackTrace();
	                                //}
	                            }
	                        } else { System.out.println("Wrong position."); }
	                    } else { System.out.println("You lost"); }
	                } catch (Codec.CodecException | OntologyException e) {
	                    e.printStackTrace();
	                }
	            } else {
	                // If no message arrives
	                block();
	            }
	        }
	    };
	}

	// -----------------------------------------------------------------
	// Initialize Methods
	// -----------------------------------------------------------------

	private void initialize() {
		tribes = new ArrayList<Tribe>();
		worldRules = new AgWorldRules();
		this.initializeMap();
	}

	private void initializeMap() {
		map = new Cell[X_BOUNDARY][Y_BOUNDARY];
		for (int i = 0; i < X_BOUNDARY; i++) {
			for (int j = 0; j < Y_BOUNDARY; j++) {
				map[i][j] = new Cell();
				map[i][j].setContent(null);
				map[i][j].setOwner(-1);
				map[i][j].setX(i);
				map[i][j].setY(j);
			}
		}
	}

	// -----------------------------------------------------------------
	// Regular Methods
	// -----------------------------------------------------------------

	private Cell bookNextRandomCell(int owner, String content) {
		int x = new Random().nextInt(X_BOUNDARY);
		int y = new Random().nextInt(Y_BOUNDARY);
		if (map[x][y].getOwner() == -1) {
			map[x][y].setOwner(owner);
			map[x][y].setContent(content);
			return map[x][y];
		} else {
			return bookNextRandomCell(owner, content);
		}
	}

	private Tribe createTribe(String nickname) {
		ContainerController cc = getContainerController();

		AgTribe agentTribe = new AgTribe();
		try {
			cc.acceptNewAgent(nickname, agentTribe).start();
			Cell townhall = bookNextRandomCell(1, "townhall");
			Tribe tribe = new Tribe(agentTribe.getAID(), GOLD, FOOD, townhall);
			return tribe;
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Integer canCreateUnit(Tribe t, Cell position, Integer index) {

		if (worldRules.isItsOwnTownhall(t.getTownhall(), tribes.get(index).getTownhall())) {
			return 2;
		}
		if (worldRules.isInTownhall(t.getTownhall().getX(), t.getTownhall().getY(), position)) {
			return 3;
		}
		if (worldRules.hasEnoughGold(t.getGold())) {
			return 4;
		}
		if (worldRules.hasEnoughFood(t.getFood())){
			return 5;
		}
		return 1;
	}

	private Unit createUnit(String nickname) {

		ContainerController cc = getContainerController();
		AgUnit agentUnit = new AgUnit();
		try {
			cc.acceptNewAgent(nickname, agentUnit).start();
			Cell position = bookNextRandomCell(1, "Unit");
			Unit newUnit = new Unit(agentUnit.getAID(), position);
			return newUnit;
		} catch (StaleProxyException e) {
			// TODO: handle exception
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
}
