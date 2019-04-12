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
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.common.MessageFormatter;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.MoveToCell;

import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.onto.*;
import jade.content.onto.basic.Action;
import jade.content.lang.sl.*;
//import es.upm.woa.agent.group5.AgWorld;

public class AgUnit extends Agent{

	public final static String WORLD = "World";
	public final static String UNIT = "Unit";
	
	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
    
    private Cell currentPosition;
    private Tribe tribe;
    
	
	public AgUnit() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setup()
	{
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
                cell.setOwner(this.getAID());
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
					
						/*ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						msg.addReceiver(ag);
						msg.setLanguage(codec.getName());
						msg.setOntology(ontology.getName());*/
        					/*ACLMessage msg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "createUnit",ag);
						
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
			
		});*/
		
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
					
						/*ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						  msg.addReceiver(ag);
						msg.setLanguage(codec.getName());
						msg.setOntology(ontology.getName());*/
						
						ACLMessage createMsg =	MessageFormatter.createMessage(getLocalName(),ACLMessage.REQUEST, "MoveToCell",ag);
						MoveToCell createAction = new MoveToCell();
						
						Cell targetPosition = new Cell();
						targetPosition.setX(1);//currentPosition.getX()+1);
						targetPosition.setY(1);//currentPosition.getY()+1);
						targetPosition.setOwner(getAID());
						targetPosition.setContent(new Empty());
						createAction.setTarget(targetPosition);
						Action agAction = new Action(ag,createAction);
						// Here you pass in arguments the message and the content that it will be filled with
						getContentManager().fillContent(createMsg, agAction);
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
		
		// Adds a behavior after a movement has done
		addBehaviour(new CyclicBehaviour(this)
		{

			public void action() {
					
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchPerformative(ACLMessage.FAILURE)), MessageTemplate.MatchProtocol("NotifyNewCellDiscovery")));
				if (msg != null)
			    {
					if(msg.getPerformative()==ACLMessage.INFORM)
						Printer.printSuccess( getLocalName()," Movement has been made");
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

	public Tribe getTribe() {
		return tribe;
	}

	public void setTribe(Tribe tribe) {
		this.tribe = tribe;
	}
	
	
	
}
