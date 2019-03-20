package org.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.ontology.GameOntology;

import jade.content.lang.Codec;
import jade.content.lang.Codec.*;
import jade.content.onto.*;
import jade.content.lang.sl.*;

public class AgUnit extends Agent{

	public final static String WORLD = "World";
	public final static String UNIT = "Unit";
	public final static String CREATE = "Create Unit";
	
	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created
    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
	
	public AgUnit() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setup()
	{
		System.out.println(getLocalName()+": has entered into the system ");
		
		//Register of the codec and the ontology to be used in the ContentManager
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
		try {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(UNIT);
			dfd.addServices(sd);
			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName()+": registered in the DF");
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		
		
		addBehaviour(new SimpleBehaviour(this)
		{
			
			@Override
			public void action() {
				
				// Creates the description for the type of agent to be searched
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(WORLD);
				dfd.addServices(sd);
				AID ag= dfd.getName();
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setContent(CREATE);
				msg.addReceiver(ag);
				send(msg);
				System.out.println(getLocalName()+": REQUEST CREATION TO THE WORLD");
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		addBehaviour(new SimpleBehaviour(this)
		{

			@Override
			public void action() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
	}
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agent "+getLocalName()+" has terminating");
	}
}
