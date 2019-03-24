package org.agents;

import org.ontology.CreateUnit;
import org.ontology.GameOntology;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgWorld extends Agent{

	public final static String WORLD = "World";
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
									// If the action is EstimationRequest...
									if (conc instanceof CreateUnit)
									{
										System.out.println(myAgent.getLocalName()+": received creation request from "+(msg.getSender()).getLocalName());
										ACLMessage reply = msg.createReply();
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
						}
					}
				}
						
		});
    	}

}
