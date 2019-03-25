package es.upm.woa.agent.group2;

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

	public final static String WORLD = "World";
	public final static String TRIBE = "Tribe";
	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
	    
	public AgWorld() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setup()
	{
		System.out.println(getLocalName()+": has entered into the system ");
		
		//Register of the codec and the ontology to be used in the ContentManager
		//Register language and ontology this part always goes
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
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
										if(true)
										{
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
												Cell cell = new Cell();
												cell.setContent("");
												cell.setOwner(1);
												cell.setX(0);
												cell.setY(0);
												
												notify.setLocation(cell);
												notify.setNewUnit(ag);
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
        
        //AGENTS ARE NOW CREATED FROM THE WORLD
        ContainerController cc = getContainerController();
        AgentController ac1,ac2;
		try {
			ac1 = cc.createNewAgent("Tribe", "es.upm.woa.agent.group2.AgTribe", new Object[0]);
			ac2 = cc.createNewAgent("Unit", "es.upm.woa.agent.group2.AgUnit", new Object[0]);
			ac1.start();
			ac2.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	}

}
