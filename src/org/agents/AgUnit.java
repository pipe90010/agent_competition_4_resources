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

public class AgUnit extends Agent{

	public final static String WORLD = "World";
	public final static String CREATE = "Create";
	
	public AgUnit() {
		// TODO Auto-generated constructor stub
	}
	
	protected void setup()
	{
		System.out.println(getLocalName()+": has entered into the system ");
		
		
		
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
}
