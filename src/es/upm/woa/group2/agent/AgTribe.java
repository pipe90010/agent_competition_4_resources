package es.upm.woa.group2.agent;


import java.util.ArrayList;
import java.util.Iterator;

import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.ontology.AssignRole;
import es.upm.woa.group2.ontology.CellGroup2;
import es.upm.woa.group2.ontology.NewResourceDiscovery;
import es.upm.woa.group2.ontology.NotifyBoundaries;
import es.upm.woa.group2.ontology.NotifyNewBuilding;
import es.upm.woa.group2.util.Format;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.EndOfGame;
import es.upm.woa.ontology.ExploitResource;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.Ground;
import es.upm.woa.ontology.InitalizeTribe;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyCellDetail;
import es.upm.woa.ontology.NotifyNewUnit;
import es.upm.woa.ontology.RegisterTribe;
import es.upm.woa.ontology.Resource;
import es.upm.woa.ontology.ResourceAccount;
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
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import es.upm.woa.agent.group5.AgWorld;
import jade.util.leap.List;

public class AgTribe extends Agent {

	public final static String TRIBE = "Tribe";
	public final static String REGISTRATION_DESK = "REGISTRATION DESK";

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Tribe tribe;
	
	private SimpleBehaviour assignRole;
	private SimpleBehaviour unitCreation;
	public String role;
	

	public AgTribe() {
		// TODO Auto-generated constructor stub
	}

	protected void setup() {
		tribe = new Tribe(getAID());
		Printer.printSuccess( getLocalName(),": has entered into the system");
//      Register of the codec and the ontology to be used in the ContentManager
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		//BEHAVIOURS ****************************************************************

		//TASK 1.2.1. 
		addBehaviour(new SimpleBehaviour(this) {
			public void action() {
				// TODO Auto-generated method stub
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(REGISTRATION_DESK);
				dfd.addServices(sd);
				
				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0)
					{
						AID ag = (AID)res[0].getName();
						
						ACLMessage createMsg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "RegisterTribe",ag);

						RegisterTribe regTribe = new RegisterTribe();
						regTribe.setTeamNumber(2);
						
						Action agAction = new Action(ag,regTribe);
						
						// Here you pass in arguments the message and the content that it will be filled with
						getContentManager().fillContent(createMsg, agAction);
						send(createMsg);	
						
						Printer.printSuccess( getLocalName(),"REGISTRATION IN THE WORLD SUCCESSFUL");
					}
					else					
					{
						Printer.printSuccess( getLocalName(),"REGISTRATION IN THE WORLD FAILED");
					}
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
			};
		});
		
				

		//INFORM unit-creation behavior
		unitCreation = new CyclicBehaviour(this) 
		 {

			public void action() {
				// Waits for units creation
				
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchPerformative(ACLMessage.FAILURE)), MessageTemplate.MatchProtocol("CreateUnit")));
				if (msg != null) {

					try {
						ContentElement ce = null;
						if (msg.getPerformative() == ACLMessage.INFORM) {
							ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// If the action is NotifyNewUnit
								if (conc instanceof NotifyNewUnit) {
									
									//casting
									NotifyNewUnit agActionN = (NotifyNewUnit)agAction.getAction();

									Cell cell= agActionN.getLocation();
									System.out.println("cell to send"+cell);
									AID aid = agActionN.getNewUnit();
								
									Unit u = new Unit(aid, cell);
									tribe.addUnit(u);
									
									//SENDS ROLE TO THE UNIT
									ACLMessage msgInform1 = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
											"AssignRole", aid);
									
									AssignRole agActionA = new AssignRole();
									
									
									if(tribe.getUnits().size()==1)
									{
										role = Unit.BUILDER_ROLE;
									}
									else if(tribe.getUnits().size()>1 && tribe.getUnits().size()<3)
									{
										if(tribe.getUnits().size()==2)
											role = Unit.EXPLORER_ROLE_UP;
										else
											role = Unit.EXPLORER_ROLE_DOWN;
									}
									else
									{
										role = Unit.EXPLOITER_ROLE;
									}
									
									agActionA.setRole(role);
									CellGroup2 cellGroup2 = Format.turnCellIntoCellGroup2(cell);
									agActionA.setLocation_Role(cellGroup2);
									agActionA.setId(aid);
									Action agActionRoleAssignation = new Action(aid, agActionA);	
									getContentManager().registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
									getContentManager().fillContent(msgInform1, agActionRoleAssignation);							
									send(msgInform1);
									
									Printer.printSuccess( getLocalName(),"received unit creation from "+ (msg.getSender()).getLocalName()+" with AID: "+aid.getName()+" and its location is "+cell.getX()+" and "+cell.getY());
								}
							}
						}
						else
						{
							// If what is received is not understood
							Printer.printSuccess( getLocalName(),": The message of "+(msg.getSender()).getLocalName() + "wasn't understood!");
							ACLMessage reply = msg.createReply();
												
							//A NOT_UNDERSTOOD is sent		
							reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
							myAgent.send(reply);
							Printer.printSuccess( getLocalName(),": Perplexity sent");
						}

					}

					catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException oe) {
						oe.printStackTrace();
					}
				} else {
					// If no message arrives
					block();
				}

			}
		};
		// Adds a behavior after a movement has done
		addBehaviour(new CyclicBehaviour(this)
		{

			@Override
			public void action() {
					
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchPerformative(ACLMessage.FAILURE)), MessageTemplate.MatchProtocol("informMove")));
				if (msg != null)
			    {
					if(msg.getPerformative()==ACLMessage.INFORM)
					{
						Printer.printSuccess( getLocalName()," Movement has been received by Tribe");	
						try {
							ContentElement ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// If the action is NotifyNewUnit
								if (conc instanceof NotifyCellDetail) {
									
									//casting
									NotifyCellDetail agActionN = (NotifyCellDetail)agAction.getAction();

									Cell cell= agActionN.getNewCell();
									
									Object content = cell.getContent();
									
									Printer.printSuccess( getLocalName()," Movement has been made");
									tribe.addDiscoveredCell(cell);
									Printer.printSuccess( getLocalName(),"received unit creation from "+ (msg.getSender()).getLocalName()+" and its location is "+cell.getX()+" and "+cell.getY());
									
									ArrayList<Unit> units = tribe.getUnits();
									
									for (Unit unit : units) {
										
										ACLMessage informMsgUnit = MessageFormatter.createMessage(getLocalName(),ACLMessage.INFORM, "NotifyNewCellDiscovery", unit.getId());
										NotifyCellDetail notify = new NotifyCellDetail();
	                            		notify.setNewCell(cell);
										Action notifyCellDiscoveryUnit = new Action(unit.getId(), notify);
	                            		getContentManager().fillContent(informMsgUnit, notifyCellDiscoveryUnit);
	                            		send(informMsgUnit);
									}
									
									if(content!=null)
									{
										if(content instanceof Building) {
											Printer.printSuccess(getLocalName(), "New cell is a building");
										}
										else if(content instanceof Ground) {
											Printer.printSuccess(getLocalName(), "New cell is empty");
										}
										else if(content instanceof Resource) {
											Printer.printSuccess(getLocalName(), "New cell is a resource");
										}
										else
											Printer.printSuccess(getLocalName(), "New cell's content is unknown");
									}
								}
							}
						} catch (CodecException | OntologyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
						
					else
						Printer.printSuccess( getLocalName()," Movement failure");
			     }
				else
					block();
			}

					
		});
		
		
		

		// Adds a behavior after a movement has done
		addBehaviour(new CyclicBehaviour(this)
		{

			public void action() {
					
				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
						MessageTemplate.or
						(
								MessageTemplate.or(
										MessageTemplate.MatchProtocol("informMove"),
										MessageTemplate.or(
												MessageTemplate.MatchProtocol("informMove"),
												MessageTemplate.or(
														MessageTemplate.MatchProtocol("ExploitResources"),
														MessageTemplate.or(MessageTemplate.MatchProtocol("NotifyNewBuilding"),
																MessageTemplate.or(MessageTemplate.MatchProtocol("RegisterTribe"),
																		MessageTemplate.or(MessageTemplate.MatchProtocol("EndOfGame"),
																				MessageTemplate.MatchProtocol("NewResourceDiscovery")
																		)
																)
														)
												)
												)),
								MessageTemplate.MatchProtocol("InitalizeTribe")
						)
						));
				if (msg != null)
			    {
					if(msg.getPerformative()==ACLMessage.INFORM)
					{
						try {
							ContentElement ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								//casting
								
								if(conc instanceof NotifyCellDetail)
								{
									//
									//casting
									NotifyCellDetail agActionN = (NotifyCellDetail)agAction.getAction();

									Cell cell= agActionN.getNewCell();									
									Object content = cell.getContent();
									
									Printer.printSuccess( getLocalName()," Movement has been made");
									tribe.addDiscoveredCell(cell);
									Printer.printSuccess( getLocalName(),"received unit creation from "+ (msg.getSender()).getLocalName()+" and its location is "+cell.getX()+" and "+cell.getY());
									
									ArrayList<Unit> units = tribe.getUnits();
									
									for (Unit unit : units) {
										
										ACLMessage informMsgUnit = MessageFormatter.createMessage(getLocalName(),ACLMessage.INFORM, "NotifyNewCellDiscovery", unit.getId());
										NotifyCellDetail notify = new NotifyCellDetail();
	                            		notify.setNewCell(cell);
										Action notifyCellDiscoveryUnit = new Action(unit.getId(), notify);
	                            		getContentManager().fillContent(informMsgUnit, notifyCellDiscoveryUnit);
	                            		send(informMsgUnit);
									}
									
									if(content!=null)
									{
										if(content instanceof Building) {
											Printer.printSuccess(getLocalName(), "New cell is a building");
										}
										else if(content instanceof Ground) {
											Printer.printSuccess(getLocalName(), "New cell is empty");
										}
										else if(content instanceof Resource) {
											Printer.printSuccess(getLocalName(), "New cell is a resource");
										}
										else
											Printer.printSuccess(getLocalName(), "New cell's content is unknown");
									}
								}
								else if(conc instanceof RegisterTribe)
								{
									RegisterTribe agActionN = (RegisterTribe)agAction.getAction();
									int teamNumber = agActionN.getTeamNumber();
									tribe.setTeamNumber(teamNumber);
								}
								else if(conc instanceof InitalizeTribe)
								{

									getContentManager().registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
									//casting
									InitalizeTribe agActionN = (InitalizeTribe)agAction.getAction();
									
									List unitLIst = agActionN.getUnitList();
									Cell cell = agActionN.getStartingPosition();
									ResourceAccount resource = agActionN.getStartingResources();
									
									tribe.convertAidToUnits(unitLIst); 
									
									tribe.setFood(resource.getFood());
									tribe.setGold(resource.getGold());
									tribe.setStones(resource.getStone());
									tribe.setWood(resource.getWood());
									tribe.setX_boundary(agActionN.getMapHeight());
									tribe.setY_boundary(agActionN.getMapWidth());
									
									//TODO new Inform for boundaries to units
									
									ArrayList<Unit> units = new ArrayList<Unit>();
									
									///tribe.setTownhall(cell);
									
									//Internal Strategy for unit role assignment
									Iterator<AID> iterator = unitLIst.iterator();
									int counter = 0;
									while (iterator.hasNext())
									{
										AID ag = iterator.next();
										counter++;
											
										AssignRole asignRoleAction = new AssignRole();
										
										
										if(counter==1)
										{
											role = Unit.BUILDER_ROLE;
										}
										else if(counter>1 && counter<=3)
										{
											if(counter==2)
												role = Unit.EXPLORER_ROLE_UP;
											else
												role = Unit.EXPLORER_ROLE_DOWN;
										}
										else
										{
											role = Unit.EXPLOITER_ROLE;
										}
										
										NotifyBoundaries notifyBoundaries = new NotifyBoundaries();
										notifyBoundaries.setX_axis(agActionN.getMapHeight());
										notifyBoundaries.setY_axis(agActionN.getMapWidth());
										
										ACLMessage informBoundaries = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
												"NotifyBoundaries", ag);
										
										Action notifyBoundariesAction = new Action(ag, notifyBoundaries);
										
										getContentManager().fillContent(informBoundaries, notifyBoundariesAction);							
										send(informBoundaries);
										
										ACLMessage msgInform2 = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
												"AssignRole", ag);
										
										asignRoleAction.setRole(role);
										asignRoleAction.setLocation_Role(Format.turnCellIntoCellGroup2(cell));
										System.out.println("cell to send"+cell);
										asignRoleAction.setId(ag);
										Action agActionRoleAssignation = new Action(ag, asignRoleAction);
										
										getContentManager().fillContent(msgInform2, agActionRoleAssignation);							
										send(msgInform2);
									}

									
									Printer.printSuccess( getLocalName(),
											"create tribe "+ (msg.getSender()).getLocalName()+": "+
													tribe.getTeamNumber()+" and its location is "+cell.getX()+" and "+cell.getY());

								}
								
								else if(conc instanceof ExploitResource)
								{
									ExploitResource agActionN = (ExploitResource)agAction.getAction();

									Iterator resources = agActionN.getAllResourceList();

									while(resources.hasNext())
									{
										Resource resource = (Resource)resources.next();
										switch (resource.getResourceType()) 
										{
											case GameOntology.RESOURCEACCOUNT_GOLD:
												
												break;
											case GameOntology.RESOURCEACCOUNT_FOOD:
												
												break;
	
											case GameOntology.RESOURCEACCOUNT_STONE:
												
												break;
	
											case GameOntology.RESOURCEACCOUNT_WOOD:
												
												break;
	
											default:
												break;
										}										
									}
								}
								else if(conc instanceof NotifyNewBuilding)
								{
									NotifyNewBuilding notifyNewBuilding = (NotifyNewBuilding) agAction.getAction();
									
									for (Unit unit : tribe.getUnits())
									{
										
										ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
												"NotifyNewBuilding", unit.getId());
										
										Action agActionNotification = new Action(unit.getId(), notifyNewBuilding);
										getContentManager().registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
										getContentManager().fillContent(msgInform, agActionNotification);
										send(msgInform);
									}
									String type = notifyNewBuilding.getType();
									Printer.printSuccess(getName(), "TRIBE inform units about the new building "+type);
									
								}
								else if(conc instanceof NewResourceDiscovery)
								{
									NewResourceDiscovery newResourceDiscovery = (NewResourceDiscovery) agAction.getAction();
									
									for (Unit unit : tribe.getUnits())
									{
										
										ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
												"NewResourceDiscovery", unit.getId());
										
										Action agActionNotification = new Action(unit.getId(), newResourceDiscovery);
										getContentManager().registerOntology(es.upm.woa.group2.ontology.GameOntology.getInstance());
										getContentManager().fillContent(msgInform, agActionNotification);
										send(msgInform);
									}
									int size = newResourceDiscovery.getCells().size();
									Printer.printSuccess(getName(), "TRIBE inform units about the new Resource Discovery "+size);
								}
								else if (conc instanceof EndOfGame) {
									getContainerController().kill();
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else
						Printer.printSuccess( getLocalName()," Movement failure");
			     }
				else
					block();
			}
					
		});
		
		
		
		

	}
}
