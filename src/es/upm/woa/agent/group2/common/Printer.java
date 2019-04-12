package es.upm.woa.agent.group2.common;

import jade.lang.acl.ACLMessage;

public class Printer {

	public static void printSuccess(String agent, String message ) {
        System.out.println("Group 2-: "+agent+": "+" "+message);
    }
	
	public static void printCreateProtocol(String agent,int performative, String protocol, String receiver ) {
        System.out.println("Group 2-:"+agent+" Creating message with performative: "+ACLMessage.getPerformative(performative)+" protocol: "+ protocol+ " and receiver is: "+receiver);
    }
	public static void printReplyProtocol(String agent,int performative, String protocol) {
        System.out.println("Group 2-:"+agent+" Replying with performative: "+ACLMessage.getPerformative(performative)+" protocol: "+ protocol);
    }
}
