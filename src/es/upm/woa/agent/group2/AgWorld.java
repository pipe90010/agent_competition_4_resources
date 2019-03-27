package es.upm.woa.agent.group2;

import java.util.ArrayList;
import java.util.Random;

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

public class AgWorld extends Agent{

	// -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------
	
	public final static String WORLD = "World";
	public final static String TRIBE = "Tribe";
	
	private final static int GOLD = 150;
	private final static int FOOD = 50;
	private final static int HOURS = 150;
	
	private final static int X_BOUNDARY=100;
	private final static int Y_BOUNDARY=100;
	// -----------------------------------------------------------------
    // Atributes
    // -----------------------------------------------------------------
	
	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
    private ArrayList<Tribe> tribes;
	private Cell [][] map;    
    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
    public AgWorld() {
		// TODO Auto-generated constructor stub
	}
	
	// -----------------------------------------------------------------
    // JADE Methods
    // -----------------------------------------------------------------
	
	protected void setup()
	{
		System.out.println(getLocalName()+": has entered into the system ");
		
		//Register of the codec and the ontology to be used in the ContentManager
		//Register language and ontology this part always goes
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        /**
         * Initialize everything
         */
        initialize();
        
        /**
         * TRIBE IS CREATED FROM THE WORLD
         */
       Tribe t = createTribe("TribeX");
       
       /**
        * TEST UNIT IS CREATED FROM THE WORLD
        */
       Unit u = createUnit("UnitX");
       u.setPosition(t.getTownhall());
       t.addUnit(u,0,0);
       tribes.add(t);
        try
	    	{
	    		// Creates its own description
	    		DFAgentDescription dfd = new DFAgentDescription();
	    		ServiceDescription sd = new ServiceDescription();
	    		sd.setName(this.getName());
	    		sd.setType(WORLD);
	    		dfd.addServices(sd);
	    		// Registers its description in the DF
	    		DFService.register(this, dfd);
	    		System.out.println(getLocalName()+": registered in the DF");
	    	} 
	    	catch (FIPAException e)
	    	{
	    		e.printStackTrace();
	    	}
        
        /*
         * BEHAVIORS------------------------------------------------------------------------------------------
         */
        addBehaviour(new CyclicBehaviour(this)
    		{

				@Override
				public void action() {
					// TODO Auto-generated method stub
					// Waits for creation requests
					ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()), 
							MessageTemplate.MatchOntology(ontology.getName())));
					if(msg!=null)
					{
						try
						{
							ContentElement ce = null;
							if (msg.getPerformative() == ACLMessage.REQUEST)
							{
								ce = getContentManager().extractContent(msg);
								// We expect an action inside the message
								if (ce instanceof Action)
								{
									Action agAction = (Action) ce;
									Concept conc = agAction.getAction();
									// If the action is CreateUnit...
									if (conc instanceof CreateUnit)
									{
										System.out.println(myAgent.getLocalName()+": received creation request from "+(msg.getSender()).getLocalName());
										ACLMessage reply = msg.createReply();
										reply.setLanguage(codec.getName());
										reply.setOntology(ontology.getName());
										//TODO CONDITION FOR AGREEING
										AID sender = msg.getSender();
										int indexTribe = findTribePositionByUnitAID(sender);
										Tribe tribe = tribes.get(indexTribe);
										Unit senderUnit = findUnitByAID(sender,tribe);
										boolean can = canCreateUnit(tribe,senderUnit.getPosition());
										
										if(can)
										{
											Unit u = createUnit("UnitY");
											tribes.get(indexTribe).addUnit(u,150,50);
											DFAgentDescription dfd2 = new DFAgentDescription();
											ServiceDescription sd2 = new ServiceDescription();
											sd2.setType(TRIBE);
											dfd2.addServices(sd2);
											
											DFAgentDescription[] res = new DFAgentDescription[1];
											res = DFService.search(myAgent, dfd2);
											if (res.length > 0)
											{
												AID ag = (AID)res[0].getName();
												res = DFService.search(myAgent, dfd2);
												ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
												msgInform.addReceiver(ag);
												msgInform.setLanguage(codec.getName());
												msgInform.setOntology(ontology.getName());
												//Creates a notifyNewUnit action
												NotifyNewUnit notify = new NotifyNewUnit();
												
												notify.setLocation(u.getPosition());
												notify.setNewUnit(u.getId());
												Action agActionNotification = new Action(ag,notify);
												getContentManager().fillContent(msgInform,agActionNotification);
												send(msgInform);
												System.out.println("INFORM CREATION TO TRIBE");
											}
											
											reply.setPerformative(ACLMessage.AGREE);
											
										}
										//TODO CONDITION FOR REJECTION
										else if (false)
											reply.setPerformative(ACLMessage.REFUSE);
										else
											reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
										myAgent.send(reply);
									}
								}
							}
						}
						catch (CodecException e)
						{
							e.printStackTrace();
						}
						catch (OntologyException oe)
						{
							oe.printStackTrace();
						} catch (FIPAException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
						
		});
        
    	}
	
	// -----------------------------------------------------------------
    // Initialize Methods
    // -----------------------------------------------------------------
	
	private void initialize() {
		tribes = new ArrayList<Tribe>();
		this.initializeMap();
	}
	private void initializeMap() {
		map = new Cell[X_BOUNDARY][Y_BOUNDARY];
		for (int i = 0; i < X_BOUNDARY; i++) {
			for (int j = 0; j < Y_BOUNDARY; j++) {
				map[i][j]= new Cell();
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

	private Cell bookNextRandomCell(int owner, String content){
 	   int x = new Random().nextInt(X_BOUNDARY);
 	   int y = new Random().nextInt(Y_BOUNDARY);
 	   if(map[x][y].getOwner() == -1){
 		   map[x][y].setOwner(owner);
 		   map[x][y].setContent(content);
 		   return map[x][y];
 	   } else {
 		   return bookNextRandomCell(owner,content);
 	   }
 			   
    }
	
	private Tribe createTribe(String nickname) {
		ContainerController cc = getContainerController();
		
		AgTribe agentTribe = new AgTribe();
		try {
			cc.acceptNewAgent(nickname, agentTribe).start();
			Cell townhall = bookNextRandomCell(1,"townhall");
			Tribe tribe = new Tribe(agentTribe.getAID(), GOLD, FOOD, townhall);
			return tribe;
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean canCreateUnit(Tribe t, Cell position)
	{
		return t.getGold()>=150 && t.getFood()>=50 && t.getTownhall().getX()==position.getX() && t.getTownhall().getY()==position.getY();
	}
	
	private Unit createUnit(String nickname) {
		
		ContainerController cc = getContainerController();
		AgUnit agentUnit = new AgUnit();
		try {
			cc.acceptNewAgent(nickname, agentUnit).start();
			Cell position = bookNextRandomCell(1,"Unit");
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
	
	private Unit findUnitByAID(AID aid,Tribe tribe) {
		ArrayList<Unit> units = tribe.getUnits();
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getId().getName().equals(aid.getName()))
				return units.get(i);
		}
		return null;
	}
}
