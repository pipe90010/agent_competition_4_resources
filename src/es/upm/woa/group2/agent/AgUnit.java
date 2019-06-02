package es.upm.woa.group2.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.ontology.AssignRole;
import es.upm.woa.group2.ontology.CellGroup2;
import es.upm.woa.group2.ontology.NewResourceDiscovery;
import es.upm.woa.group2.ontology.NotifyBoundaries;
import es.upm.woa.group2.ontology.NotifyNewBuilding;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.util.Format;
import es.upm.woa.group2.util.Moving;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CellContent;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.ExploitResource;
import es.upm.woa.ontology.Ground;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyCellDetail;
import es.upm.woa.ontology.NotifyNewUnit;
import es.upm.woa.ontology.Resource;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.onto.*;
import jade.content.onto.basic.Action;
import jade.content.lang.sl.*;
//import es.upm.woa.agent.group5.AgWorld;

public class AgUnit extends Agent {

	public final static String WORLD = "World";
	public final static String UNIT = "Unit";

	// -----------------------------------------------------------------
	// Building Constants
	// -----------------------------------------------------------------

	public final static String TOWNHALL = "Town Hall";
	public final static String FARM = "Farm";
	public final static String STORE = "Store";

	public String typeOfBuildingCreated = "Town Hall";

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();

	private Cell map;
	private Cell currentPosition;
	int X_BOUNDARY;
	int Y_BOUNDARY;

	private Unit unit;
	private Tribe tribe;

	private ArrayList<Cell> discoveredCells;

	private Boolean isBusy;

	private SimpleBehaviour movement;
	private SimpleBehaviour exploitResource;
	private SimpleBehaviour createBuilding;
	private SimpleBehaviour createUnit;
	private int retornar;

	public AgUnit() {
		// TODO Auto-generated constructor stub
	}

	protected void setup() {
		unit = new Unit(getAID());
		isBusy = false;
		discoveredCells = new ArrayList<Cell>();
		tribe = new Tribe();

		Printer.printSuccess(getLocalName(), "has entered into the system ");
		// Register of the codec and the ontology to be used in the ContentManager
		// Register language and ontology this part always goes
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		/*
		 * BEHAVIORS--------------------------------------------------------------------
		 * ----------------------
		 */

		// Behavior for requesting UNIT CREATION
		createUnit = new SimpleBehaviour(this) {

			@Override
			public void action() {

				// Creates the description for the type of agent to be searched
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);

				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0) {
						AID ag = (AID) res[0].getName();

						ACLMessage msg = MessageFormatter.createMessage(getLocalName(), ACLMessage.REQUEST,
								"createUnit", ag);

						CreateUnit create = new CreateUnit();
						Action agAction = new Action(ag, create);
						// Here you pass in arguments the message and the content that it will be filled
						// with
						getContentManager().fillContent(msg, agAction);
						send(msg);
						Printer.printSuccess(getLocalName(), ": REQUEST CREATION TO THE WORLD");
					} else
						Printer.printSuccess(getLocalName(),
								"THERE ARE NO AGENTS REGISTERED WITH TYPE: " + sd.getType());
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return true;
			}

		};

		// Behavior for requesting BUILDINGS
		createBuilding = new SimpleBehaviour(this) {

			@Override
			public void action() {
				// TODO Auto-generated method stub
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);
				getContentManager().registerOntology(ontology);

				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0) {
						AID ag = (AID) res[0].getName();

						ACLMessage createMsg = MessageFormatter.createMessage(getLocalName(), ACLMessage.REQUEST,
								"CreateBuilding", ag);
						CreateBuilding createTownhall = new CreateBuilding();
						createTownhall.setBuildingType(typeOfBuildingCreated);

						Action agAction = new Action(ag, createTownhall);
						// Here you pass in arguments the message and the content that it will be filled
						// with
						getContentManager().fillContent(createMsg, agAction);
						isBusy = true;
						send(createMsg);
					} else
						Printer.printSuccess(getLocalName(),
								"THERE ARE NO AGENTS REGISTERED WITH TYPE: " + sd.getType());
				}

				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return true;
			}

		};

		// Behavior for moving

		movement = new SimpleBehaviour(this) {

			@Override
			public void action() {

				// Creates the description for the type of agent to be searched
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);

				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0) {
						AID ag = (AID) res[0].getName();

						ACLMessage createMsg = MessageFormatter.createMessage(getLocalName(), ACLMessage.REQUEST,
								"MoveToCell", ag);
						MoveToCell createAction = new MoveToCell();

						// Moving movement = new Moving();
						int cellNumber = -1;// new Random().nextInt((5) + 1); // [0...6]

						if (unit.getRole().equals(Unit.EXPLORER_ROLE_UP)
								|| unit.getRole().equals(Unit.EXPLORER_ROLE_DOWN)) {
							explore();
							cellNumber = retornar;
						} else if (unit.getRole().equals(Unit.EXPLOITER_ROLE)) {
							cellNumber = getClosestAvailableResourceDirection();
						} else {
							if (tribe.getTownhall() != null) {
								if (tribe.getTownhall().getX() == currentPosition.getX()
										&& tribe.getTownhall().getY() == currentPosition.getY()) {
									cellNumber = new Random().nextInt((5) + 1);
								} else {
									cellNumber = goToTownHall();
									System.out.println("GO TO TOWNHALL" + cellNumber);
								}
							} else
								cellNumber = new Random().nextInt((5) + 1);

						}

						createAction.setTargetDirection(cellNumber);
						Action agAction = new Action(ag, createAction);
						// Here you pass in arguments the message and the content that it will be filled
						// with
						getContentManager().registerOntology(ontology);
						getContentManager().fillContent(createMsg, agAction);
						isBusy = true;
						send(createMsg);
					} else
						Printer.printSuccess(getLocalName(),
								"THERE ARE NO AGENTS REGISTERED WITH TYPE: " + sd.getType());
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return true;
			}

		};

//Behavior for exploitResource

		exploitResource = new SimpleBehaviour(this) {

			@Override
			public void action() {

				// Creates the description for the type of agent to be searched
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);

				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0) {
						AID ag = (AID) res[0].getName();

						ACLMessage createMsg = MessageFormatter.createMessage(getLocalName(), ACLMessage.REQUEST,
								"ExploitResources", ag);
						Action agAction = new Action(ag, new ExploitResource());
						getContentManager().fillContent(createMsg, agAction);
						send(createMsg);
					} else
						Printer.printSuccess(getLocalName(),
								"THERE ARE NO AGENTS REGISTERED WITH TYPE: " + sd.getType());
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return true;
			}

		};
		// Adds a behavior to process the answer to a creation request
		addBehaviour(new SimpleBehaviour(this) {

			@Override
			public void action() {

				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
						MessageTemplate.MatchProtocol("createUnit")));
				if (msg != null) {
					Printer.printSuccess(getLocalName(), "WORLD REPLIED SUCCESFULL CREATION OF UNIT");
				}
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}

		});

		// Adds a behavior to process the answer to a movement request
		addBehaviour(new SimpleBehaviour(this) {

			@Override
			public void action() {

				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
								MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD),
										MessageTemplate.MatchPerformative(ACLMessage.REFUSE))),
						MessageTemplate.MatchProtocol("MoveToCell")));
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.AGREE)
						Printer.printSuccess(getLocalName(), "MOVEMENT ACCEPTED");
					else if (msg.getPerformative() == ACLMessage.REFUSE)
						Printer.printSuccess(getLocalName(), "MOVEMENT REFUSED");
					if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD)
						Printer.printSuccess(getLocalName(), "MOVEMENT NOT_UNDERSTOOD");
				}
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}

		});

		// Adds a behavior to process the answer to a create building request
		addBehaviour(new SimpleBehaviour(this) {

			@Override
			public void action() {

				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
								MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD),
										MessageTemplate.MatchPerformative(ACLMessage.REFUSE))),
						MessageTemplate.MatchProtocol("CreateBuilding")));
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.AGREE)
						Printer.printSuccess(getLocalName(), "CREATION ACCEPTED");
					else if (msg.getPerformative() == ACLMessage.REFUSE)
						Printer.printSuccess(getLocalName(), "CREATION REFUSED");
					if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD)
						Printer.printSuccess(getLocalName(), "CREATION NOT_UNDERSTOOD");
				}
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}

		});

		// Adds a behavior after a movement has done
		addBehaviour(new CyclicBehaviour(this) {

			public void action() {

				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
								MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
						MessageTemplate.or(
								MessageTemplate.or(MessageTemplate.MatchProtocol("NotifyCellDetail"), MessageTemplate
										.or(MessageTemplate.MatchProtocol("InformOriginPosition"), MessageTemplate.or(
												MessageTemplate.MatchProtocol("informMove"),
												MessageTemplate.or(MessageTemplate.MatchProtocol("ExploitResources"),
														MessageTemplate.or(
																MessageTemplate.MatchProtocol("informBuildingCreation"),
																MessageTemplate.MatchProtocol("NotifyNewUnit")))))),
								MessageTemplate.MatchProtocol("CreateBuilding"))));

				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						try {
							getContentManager().registerOntology(ontology);
							ContentElement ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// casting

								if (conc instanceof NotifyCellDetail) {

									NotifyCellDetail agActionN = (NotifyCellDetail) agAction.getAction();

									Cell cell = agActionN.getNewCell();
									addNewCellDiscovered(cell);

									Object content = cell.getContent();

									Printer.printSuccess(getLocalName(), " Movement has been made");

									if (content != null) {
										if (content instanceof Building) {
											Printer.printSuccess(getLocalName(), "New cell is a building");
										} else if (content instanceof Ground) {
											Printer.printSuccess(getLocalName(), "New cell is empty");
										} else if (content instanceof Resource) {
											Printer.printSuccess(getLocalName(), "New cell is a resource");
										} else
											Printer.printSuccess(getLocalName(), "New cell's content is unknown");
									}
								} else if (conc instanceof MoveToCell) {
									MoveToCell agActionN = (MoveToCell) agAction.getAction();

									// TODO: new logic to be fixed --> getTargetDirection
									int targetDirection = agActionN.getTargetDirection();
									isBusy = false;

									Cell newPosition = agActionN.getNewlyArrivedCell();
									setCurrentPosition(newPosition);

									if (!existsBuildingType(STORE) && newPosition.getContent() instanceof Resource) {
										NewResourceDiscovery newResource = new NewResourceDiscovery();
										newResource.addCells(Format.turnCellIntoCellGroup2(newPosition));

										ACLMessage informMsgUnit = MessageFormatter.createMessage(getLocalName(),
												ACLMessage.INFORM, "NewResourceDiscovery", tribe.getId());

										getContentManager().registerOntology(
												es.upm.woa.group2.ontology.GameOntology.getInstance());

										Action notifyNewResource = new Action(tribe.getId(), newResource);

										getContentManager().fillContent(informMsgUnit, notifyNewResource);
										send(informMsgUnit);

									}

									if (unit.getRole().equals(Unit.BUILDER_ROLE)) {

										if (currentPosition.getContent() instanceof Ground) {

											String nextBuilding = getNextBuildingToCreate();
											if (nextBuilding != null) {
												typeOfBuildingCreated = nextBuilding;
												addBehaviour(createBuilding);
											} else
												addBehaviour(movement);

										} else {

											if (existsBuildingType(FARM)
													&& currentPosition.getContent() instanceof Building
													&& ((Building) currentPosition.getContent()).getType()
															.equals(TOWNHALL)) {
												addBehaviour(createUnit);
											} else
												addBehaviour(movement);
										}

									} else {
										if (unit.getRole().equals(Unit.EXPLOITER_ROLE)) {

											if (newPosition.getContent() instanceof Resource) {
												Resource resource = (Resource) newPosition.getContent();
												if (resource.getGoldPercentage() > 0) {
													addBehaviour(exploitResource);
												} else if (resource.getResourceType()
														.equals(GameOntology.RESOURCEACCOUNT_WOOD)) {
													addBehaviour(exploitResource);
												}
											}
											if (newPosition.getContent() instanceof Building
													&& ((Building) newPosition.getContent()).equals(FARM)) {
												addBehaviour(exploitResource);
											}
										} else {
											addBehaviour(movement);
										}
									}

									Printer.printSuccess(getLocalName(), "New position has been updated to: "
											+ newPosition.getX() + " and " + newPosition.getY());

								} else if (conc instanceof CreateBuilding) {
									CreateBuilding agActionN = (CreateBuilding) agAction.getAction();
									String type = agActionN.getBuildingType();
									isBusy = false;

									Building building = new Building();
									building.setOwner(tribe.getId());
									building.setType(type);
									currentPosition.setContent(building);

									Printer.printSuccess(getLocalName(),
											"New Building: " + type + "  has been created succesfully ");

									ACLMessage informMsgUnit = MessageFormatter.createMessage(getLocalName(),
											ACLMessage.INFORM, "NotifyNewBuilding", tribe.getId());

									getContentManager()
											.registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
									NotifyNewBuilding notifyNewBuilding = new NotifyNewBuilding();
									notifyNewBuilding.setType(type);
									notifyNewBuilding.setCell(Format.turnCellIntoCellGroup2(currentPosition));

									Action notifyCreateBuilding = new Action(tribe.getId(), notifyNewBuilding);

									getContentManager().fillContent(informMsgUnit, notifyCreateBuilding);
									send(informMsgUnit);

									addBehaviour(movement);

								} else if (conc instanceof NotifyNewUnit) {
									NotifyNewUnit agActionN = (NotifyNewUnit) agAction.getAction();

									tribe.setId(msg.getSender());

									Cell cell = agActionN.getLocation();

									setCurrentPosition(cell);

									Printer.printSuccess(getLocalName(),
											"Origin Position has been set: " + cell.getX() + ", " + cell.getY());
								}

								else if (conc instanceof ExploitResource) {
									ExploitResource agActionN = (ExploitResource) agAction.getAction();

									Iterator resources = agActionN.getAllResourceList();

									ACLMessage reply = msg.createReply();
									reply.setLanguage(codec.getName());
									reply.setOntology(ontology.getName());

									Action explotationResources = new Action(tribe.getId(), agActionN);

									ACLMessage informUnit = MessageFormatter.createMessage(getLocalName(),
											ACLMessage.INFORM, "informExploitResources", msg.getSender());

									Printer.printSuccess(getLocalName(), "The resource has been exploited");

									getContentManager().fillContent(informUnit, explotationResources);
									send(informUnit);

								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else
						Printer.printSuccess(getLocalName(), " Movement failure");
				} else
					block();
			}

		});

		// Adds a behavior after a movement has done
		addBehaviour(new CyclicBehaviour(this) {

			public void action() {
				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
								MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
						MessageTemplate.or(MessageTemplate.MatchProtocol("AssignRole"),
								MessageTemplate.or(MessageTemplate.MatchProtocol("NotifyNewBuilding"),
										MessageTemplate.or(MessageTemplate.MatchProtocol("NotifyBoundaries"),
												MessageTemplate.MatchProtocol("NewResourceDiscovery"))))));

				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						try {
							getContentManager().registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
							ContentElement ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// casting

								if (conc instanceof AssignRole) {

									getContentManager().registerOntology(ontology);
									AssignRole agActionN = (AssignRole) agAction.getAction();

									unit.setRole(agActionN.getRole());

									if (unit.getRole().equals(Unit.EXPLORER_ROLE_DOWN))
										unit.setWay(Unit.BAJANDO);
									else {
										if (unit.getRole().equals(Unit.EXPLORER_ROLE_UP)) {
											unit.setWay(Unit.SUBIENDO);
										}
									}

									tribe.setId(msg.getSender());

									getContentManager().registerOntology(ontology);
									Cell cell = Format.turnCellGroup2IntoCell(agActionN.getLocation_Role(),
											tribe.getId());

									System.out.println("cell" + cell);

									setCurrentPosition(cell);

									if (unit.getRole().equals(Unit.BUILDER_ROLE)
											&& cell.getContent() instanceof Ground) {
										typeOfBuildingCreated = TOWNHALL;
										addBehaviour(createBuilding);
									} else {
										addBehaviour(movement);
									}
									Printer.printSuccess(getLocalName(), "The assigned role is " + agActionN.getRole());
								} else if (conc instanceof NotifyNewBuilding) {

									NotifyNewBuilding agActionN = (NotifyNewBuilding) agAction.getAction();
									String type = agActionN.getType();
									Cell buildingPosition = Format.turnCellGroup2IntoCell(agActionN.getCell(),
											tribe.getId());

									if (type.equals(Tribe.TOWNHALL)) {
										tribe.setTownhall(buildingPosition);
									}
									getContentManager().registerOntology(ontology);
									Building build = (Building) buildingPosition.getContent();
									tribe.addNewCity(build);

									Printer.printSuccess(getLocalName(),
											"Unit has been informed about new building " + type);

								} else if (conc instanceof NewResourceDiscovery) {

									NewResourceDiscovery newResourceDiscovery = (NewResourceDiscovery) agAction
											.getAction();

									Iterator<CellGroup2> cellsDiscovery = newResourceDiscovery.getAllCells();
									while (cellsDiscovery.hasNext()) {
										Cell newCellD = Format.turnCellGroup2IntoCell(cellsDiscovery.next(),
												tribe.getId());
										tribe.addNewResourcesDiscovery(newCellD);
									}

									Printer.printSuccess(getLocalName(),
											"Unit has receive total new Resource Discovery of "
													+ tribe.getNewResourcesDiscovery().size());

								} else if (conc instanceof NotifyBoundaries) {
									NotifyBoundaries notifyBoundaries = (NotifyBoundaries) agAction.getAction();

									X_BOUNDARY = notifyBoundaries.getX_axis();
									Y_BOUNDARY = notifyBoundaries.getY_axis();
									System.out.println(
											"=============================X_BOUNDARY Y_BOUNDARY==========================");

								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	protected void takeDown() {
		// Printout a dismissal message
		Printer.printSuccess(getLocalName(), "Agent has been exterminated");
	}

	public Cell getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Cell currentPosition) {
		this.currentPosition = currentPosition;
	}

	public void addNewCellDiscovered(Cell cell) {

		boolean found = false;
		for (int i = 0; i < discoveredCells.size(); i++) {
			Cell currentCell = discoveredCells.get(i);
			if (currentCell.getX() == cell.getX() && currentCell.getY() == cell.getY()) {
				found = true;
				discoveredCells.set(i, cell);
			}

		}
		if (!found)
			discoveredCells.add(cell);
	}

	public int getClosestAvailableResourceDirection() {
		ArrayList<Cell> availableResources = tribe.getNewResourcesDiscovery();

		Cell nextClosestCell = null;
		for (Cell cell : availableResources) {
			if (nextClosestCell != null) {
				int xDifferenceNextClosest = java.lang.Math.abs(currentPosition.getX() - nextClosestCell.getX());
				int yDifferenceNextClosest = java.lang.Math.abs(currentPosition.getY() - nextClosestCell.getY());

				int xDifferenceCell = java.lang.Math.abs(currentPosition.getX() - cell.getX());
				int yDifferenceCell = java.lang.Math.abs(currentPosition.getY() - cell.getY());

				if (xDifferenceCell < xDifferenceNextClosest) {
					if (yDifferenceCell <= yDifferenceNextClosest) {
						nextClosestCell = cell;
					}

				} else if (yDifferenceCell < yDifferenceNextClosest) {
					if (xDifferenceCell <= xDifferenceNextClosest) {
						nextClosestCell = cell;
					}
				}
			} else {
				nextClosestCell = cell;
			}
		}

		int direction = new Random().nextInt((5) + 1);
		if (nextClosestCell != null) {
			int xDifferenceNextClosest = currentPosition.getX() - nextClosestCell.getX();
			int yDifferenceNextClosest = currentPosition.getY() - nextClosestCell.getY();

			// move left
			if (yDifferenceNextClosest > 0) {
				if (xDifferenceNextClosest > 0)
					direction = 6;
				else
					direction = 5;

			} // move right
			else if (yDifferenceNextClosest < 0) {
				if (xDifferenceNextClosest > 0)
					direction = 2;
				else
					direction = 3;
			} else if (yDifferenceNextClosest == 0) {
				if (xDifferenceNextClosest > 0)
					direction = 1;
				else
					direction = 4;
			}
		}
		return direction;
	}

	public int goToTownHall() {
		Cell townHall = tribe.getTownhall();

		int direction = -1;
		int xDifferenceNextClosest = currentPosition.getX() - townHall.getX();
		int yDifferenceNextClosest = currentPosition.getY() - townHall.getY();

		// move left
		if (yDifferenceNextClosest > 0) {
			if (xDifferenceNextClosest > 0)
				direction = 6;
			else
				direction = 5;

		} // move right
		else if (yDifferenceNextClosest < 0) {
			if (xDifferenceNextClosest > 0)
				direction = 2;
			else
				direction = 3;
		} else if (yDifferenceNextClosest == 0) {
			if (xDifferenceNextClosest > 0)
				direction = 1;
			else
				direction = 4;
		}
		return direction;
	}

	public int subir1(int x) {
		if (x % 2 == 0) {
			if (x - 2 <= 0) {
				unit.setWay(Unit.BAJANDO);
				return 6;
			} else
				return 1;
		} else {
			if (x - 1 <= 0) {
				unit.setWay(Unit.BAJANDO);
				return 5;
			} else
				return 1;
		}
	}

	public int bajar1(int x) {
		if (x % 2 == 0) {
			if (x + 1 >= X_BOUNDARY) {
				unit.setWay(Unit.SUBIENDO);
				return 2;
			} else
				return 4;
		} else {
			if (x + 2 >= X_BOUNDARY) {
				unit.setWay(Unit.SUBIENDO);
				return 3;
			} else
				return 4;
		}
	}

	public int subir2(int x) {
		if (x % 2 == 0) {
			if (x - 2 <= 0) {
				unit.setWay(Unit.BAJANDO);
				return 6;
			} else
				return 1;
		} else {
			if (x - 1 <= 0) {
				unit.setWay(Unit.BAJANDO);
				return 5;
			} else
				return 1;
		}
	}

	public int bajar2(int x) {
		if (x % 2 == 0) {
			if (x + 1 >= X_BOUNDARY) {
				unit.setWay(Unit.SUBIENDO);
				return 2;
			} else
				return 4;
		} else {
			if (x + 2 >= X_BOUNDARY) {
				unit.setWay(Unit.SUBIENDO);
				return 3;
			} else
				return 4;
		}
	}

	public void explore() {
		int x = currentPosition.getX();
		if (unit.getRole().equals(Unit.EXPLORER_ROLE_UP)) {
			if (unit.getWay().equals(Unit.SUBIENDO))
				retornar = subir1(x);
			else
				if(unit.getWay().equals(Unit.BAJANDO))
				retornar = bajar1(x);

		} else {
			if (unit.getRole().equals(Unit.EXPLORER_ROLE_DOWN)) {
				if (unit.getWay().equals(Unit.BAJANDO))
					retornar = bajar2(x);
				else
					if(unit.getWay().equals(Unit.BAJANDO))
					retornar = subir2(x);
			}
		}
	}

	public boolean existsBuildingType(String type) {
		ArrayList<Building> townHallBuildings = tribe.getBuildingsByType(type);
		return townHallBuildings.size() > 0;
	}

	public String getNextBuildingToCreate() {
		if (existsBuildingType(TOWNHALL)) {
			if (existsBuildingType(STORE)) {
				if (existsBuildingType(FARM)) {
					return null;
				} else {
					return FARM;
				}
			} else {
				return STORE;
			}
		} else
			return TOWNHALL;
	}

}
