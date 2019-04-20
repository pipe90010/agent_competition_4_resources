package es.upm.woa.agent.group2.agents;


import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import es.upm.woa.agent.group2.common.Printer;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.NotifyNewCellDiscovery;
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
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import es.upm.woa.agent.group5.AgWorld;

public class AgTribe extends Agent {

	public final static String TRIBE = "Tribe";

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private Tribe tribe;

	public AgTribe() {
		// TODO Auto-generated constructor stub
	}

	protected void setup() {
		tribe = new Tribe(getAID());
		Printer.printSuccess( getLocalName(),": has entered into the system");
//      Register of the codec and the ontology to be used in the ContentManager
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

//		BEHAVIOURS ****************************************************************

		addBehaviour(new CyclicBehaviour(this) {

			public void action() {
				// Waits for units creation
				/*ACLMessage msg = receive(MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
						MessageTemplate.MatchOntology(ontology.getName())));*/
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
									AID aid = agActionN.getNewUnit();
									
									Unit u = new Unit(aid, cell);
									tribe.addUnit(u, 150, 50);
									Printer.printSuccess( getLocalName(),"received unit creation from "+ (msg.getSender()).getLocalName()+" with AID: "+aid.getName()+" and its location is "+cell.getX()+" and "+cell.getY());
								}
							}
						}
						else
						{
							// If what is received is not understood
							Printer.printSuccess( getLocalName(),": Mamma mia!! non capisco il messaggio di "+(msg.getSender()).getLocalName());
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
		});
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
								if (conc instanceof NotifyNewCellDiscovery) {
									
									//casting
									NotifyNewCellDiscovery agActionN = (NotifyNewCellDiscovery)agAction.getAction();

									Cell cell= agActionN.getNewCell();
									tribe.addDiscoveredCell(cell);
									Printer.printSuccess( getLocalName(),"received unit creation from "+ (msg.getSender()).getLocalName()+" and its location is "+cell.getX()+" and "+cell.getY());	
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
		
		

	}

}
