package es.upm.woa.agent.group2.agents;

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

import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.behaviours.MovementRequestBehaviour;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.Empty;
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

public class AgUnit extends Agent{

	public final static String WORLD = "World";
	public final static String UNIT = "Unit";
	
	// -----------------------------------------------------------------
	// Building Constants
	// -----------------------------------------------------------------

	public final static String TOWNHALL = "Townhall";
	
	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
    
    private Cell currentPosition;
    
    private Unit unit;
	
    private ArrayList<Cell> discoveredCells;
    
    private Boolean isBusy;
    
	public AgUnit() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setup()
	{
		unit = new Unit(getAID());
		isBusy = false;
		discoveredCells = new ArrayList<Cell>();
		Printer.printSuccess( getLocalName(),"has entered into the system ");
		//Register of the codec and the ontology to be used in the ContentManager
		//Register language and ontology this part always goes
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        Object[] args = getArguments();
        int x,y;
        if (args != null) {
            if (args.length==2) {
                x = (Integer)args[0];
                y =  (Integer)args[1];
                Cell cell= new Cell();
                cell.setX(x);
                cell.setY(y);
                setCurrentPosition(cell);
                Printer.printSuccess( getLocalName(),"CURRENT POSITION IS SET FOR X: "+x+" and Y: "+y);
            }            

        }
        
		/*
         * BEHAVIORS------------------------------------------------------------------------------------------
         */
		
		
		/*addBehaviour(new SimpleBehaviour(this)
		{
			
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
					if (res.length > 0)
					{
						AID ag = (AID)res[0].getName();
	        				
						ACLMessage msg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "createUnit",ag);
						
						CreateUnit create = new CreateUnit();
						Action agAction = new Action(ag,create);
						// Here you pass in arguments the message and the content that it will be filled with
						getContentManager().fillContent(msg, agAction);
						send(msg);
						Printer.printSuccess( getLocalName(),": REQUEST CREATION TO THE WORLD");
					}
					else
					Printer.printSuccess( getLocalName(),"THERE ARE NO AGENTS REGISTERED WITH TYPE: "+sd.getType());
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
			
		});
		*/
        //Behavior for requesting BUILDINGS
        addBehaviour(new SimpleBehaviour(this)
		{

			@Override
			public void action() {
				// TODO Auto-generated method stub
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);
				
				try {
					// It finds agents of the required type
					DFAgentDescription[] res = new DFAgentDescription[1];
					res = DFService.search(myAgent, dfd);
					// Gets the first occurrence, if there was success
					if (res.length > 0)
					{
						AID ag = (AID)res[0].getName();
						
						ACLMessage createMsg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "CreateBuilding",ag);
						CreateBuilding createTownhall = new CreateBuilding();
						createTownhall.setBuildingType(TOWNHALL);
						
						Action agAction = new Action(ag,createTownhall);
						// Here you pass in arguments the message and the content that it will be filled with
						getContentManager().fillContent(createMsg, agAction);
						isBusy = true;
						send(createMsg);
					}
					else
						Printer.printSuccess( getLocalName(),"THERE ARE NO AGENTS REGISTERED WITH TYPE: "+sd.getType());
				}
				
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
        	
		});
        
        
		//Behavior for moving
		addBehaviour(new SimpleBehaviour(this)
		{
			
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
					if (res.length > 0)
					{
						AID ag = (AID)res[0].getName();
											
						ACLMessage createMsg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "MoveToCell",ag);
						MoveToCell createAction = new MoveToCell();
						
						Cell targetPosition = new Cell();
						targetPosition.setX(currentPosition.getX()+1);
						targetPosition.setY(currentPosition.getY()+1);						
						targetPosition.setContent(new Empty());
						createAction.setTarget(targetPosition);
						Action agAction = new Action(ag,createAction);
						// Here you pass in arguments the message and the content that it will be filled with
						getContentManager().fillContent(createMsg, agAction);
						isBusy = true;
						send(createMsg);
					}
					else
						Printer.printSuccess( getLocalName(),"THERE ARE NO AGENTS REGISTERED WITH TYPE: "+sd.getType());
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
			
		});
		
		// Adds a behavior to process the answer to a creation request
		addBehaviour(new SimpleBehaviour(this)
		{

			@Override
			public void action() {
				
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.MatchProtocol("createUnit")));
				if (msg != null)
	            {
					Printer.printSuccess( getLocalName(),"WORLD REPLIED SUCCESFULL CREATION OF UNIT");
	            }
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		// Adds a behavior to process the answer to a movement request
		addBehaviour(new SimpleBehaviour(this)
		{

			@Override
			public void action() {
					
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD), MessageTemplate.MatchPerformative(ACLMessage.REFUSE))), MessageTemplate.MatchProtocol("MoveToCell")));
				if (msg != null)
			    {
					if(msg.getPerformative()==ACLMessage.AGREE )
						Printer.printSuccess( getLocalName(),"MOVEMENT ACCEPTED");
					else if(msg.getPerformative()==ACLMessage.REFUSE )
						Printer.printSuccess( getLocalName(),"MOVEMENT REFUSED");
					if(msg.getPerformative()==ACLMessage.NOT_UNDERSTOOD )
						Printer.printSuccess( getLocalName(),"MOVEMENT NOT_UNDERSTOOD");
			     }
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
					
		});	
		
		
		// Adds a behavior to process the answer to a movement request
		addBehaviour(new SimpleBehaviour(this)
		{

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
		addBehaviour(new CyclicBehaviour(this)
		{

			public void action() {
					
				ACLMessage msg = receive(MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
						MessageTemplate.or(
								MessageTemplate.or(
										MessageTemplate.MatchProtocol("NotifyCellDetail"),
										MessageTemplate.MatchProtocol("informMove")),
								MessageTemplate.MatchProtocol("CreateBuilding")
						)));
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
								
								if (conc instanceof NotifyCellDetail) {
									
									NotifyCellDetail agActionN = (NotifyCellDetail)agAction.getAction();
			
									Cell cell= agActionN.getNewCell();
									addNewCellDiscovered(cell);
									
									Object content = cell.getContent();
									
									Printer.printSuccess( getLocalName()," Movement has been made");
									
									if(content!=null)
									{
										if(content instanceof Building) {
											Printer.printSuccess(getLocalName(), "New cell is a building");
										}
										else if(content instanceof Empty) {
											Printer.printSuccess(getLocalName(), "New cell is empty");
										}
										else if(content instanceof Resource) {
											Printer.printSuccess(getLocalName(), "New cell is a resource");
										}
										else
											Printer.printSuccess(getLocalName(), "New cell's content is unknown");
									}
								}
								else if(conc instanceof MoveToCell)
								{
									MoveToCell agActionN = (MoveToCell)agAction.getAction();
									
									Cell cell= agActionN.getTarget();
									isBusy=false;
									setCurrentPosition(cell);
									Printer.printSuccess(getLocalName(), "New position has been updated to: "+cell.getX()+" and "+cell.getY());
								}
								else if(conc instanceof CreateBuilding)
								{
									CreateBuilding agActionN = (CreateBuilding)agAction.getAction();
									String type = agActionN.getBuildingType();
									isBusy=false;
									//setCurrentPosition(cell);
									Printer.printSuccess(getLocalName(), "New Building: "+type+"  has been created succesfully ");
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
	protected void takeDown() {
		// Printout a dismissal message
		Printer.printSuccess( getLocalName(),"Agent has been exterminated");
	}

	public Cell getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Cell currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	public void addNewCellDiscovered(Cell cell) {
	
		boolean found=false;
		for (int i = 0; i < discoveredCells.size(); i++) {
			Cell currentCell = discoveredCells.get(i);
			if(currentCell.getX()==cell.getX() && currentCell.getY()==cell.getY())
			{
				found = true;
				discoveredCells.set(i, cell);
			}
				
		}
		if(!found)
			discoveredCells.add(cell);
	}
}
