package es.upm.woa.agent.group2.common;

import java.util.UUID;

import es.upm.woa.ontology.GameOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class MessageFormatter {
    static private final Codec codec = new SLCodec();
    static private final Ontology ontology = GameOntology.getInstance();

    public static ACLMessage createMessage(int performative, String protocol, AID receiver) {
        ACLMessage msg = new ACLMessage(performative);
        msg.setProtocol(protocol);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        msg.setConversationId(UUID.randomUUID().toString());
        msg.addReceiver(receiver);

        return msg;
    }

    public static ACLMessage createReplyMessage(ACLMessage message, int performative, String protocol) {
        ACLMessage msg = message.createReply();
        msg.setPerformative(performative);
        msg.setProtocol(protocol);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());

        return msg;
    }
}
