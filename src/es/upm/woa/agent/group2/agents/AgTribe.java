package es.upm.woa.agent.group2.agents;


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
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgTribe extends Agent {

	public final static String TRIBE = "Tribe";

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();

	public AgTribe() {
		// TODO Auto-generated constructor stub
	}

	protected void setup() {
		System.out.println(getLocalName() + ": has entered into the system");
//      Register of the codec and the ontology to be used in the ContentManager
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		try {
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(TRIBE);
			dfd.addServices(sd);
			// Registers its description in the DF
			DFService.register(this, dfd);
			System.out.println(getLocalName() + ": registered in the DF");
			dfd = null;
			sd = null;
			doWait(10000);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

//		BEHAVIOURS ****************************************************************

		addBehaviour(new CyclicBehaviour(this) {

			public void action() {
				// Waits for units creation
				ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
						MessageTemplate.MatchOntology(ontology.getName())));
				if (msg != null) {

					try {
						ContentElement ce = null;
						if (msg.getPerformative() == ACLMessage.INFORM) {
							System.out.println(msg);
							ce = getContentManager().extractContent(msg);
							if (ce instanceof Action) {
								Action agAction = (Action) ce;
								Concept conc = agAction.getAction();
								// If the action is NotifyNewUnit
								if (conc instanceof NotifyNewUnit) {
									System.out.println(myAgent.getLocalName() + ": received unit creation from "
											+ (msg.getSender()).getLocalName());
									

								}
							}
						}
						else
						{
							// If what is received is not understood
							System.out.println(myAgent.getLocalName()+": Mamma mia!! non capisco il messaggio di "+(msg.getSender()).getLocalName());    
							ACLMessage reply = msg.createReply();
												
							//A NOT_UNDERSTOOD is sent		
							reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
							myAgent.send(reply);
							System.out.println(myAgent.getLocalName()+": Perplexity sent");
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
		});

	}

}
