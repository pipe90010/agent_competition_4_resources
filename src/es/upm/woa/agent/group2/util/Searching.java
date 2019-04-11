package es.upm.woa.agent.group2.util;

import java.util.ArrayList;

import es.upm.woa.agent.group2.agents.AgWorld;
import es.upm.woa.agent.group2.beans.Tribe;
import es.upm.woa.agent.group2.beans.Unit;
import jade.core.AID;

public class Searching {
	
	
	public static int findTribePositionByUnitAID(AgWorld agentWorld, AID msgSenderAID) {
		AID aid= agentWorld.getAID();
		for (int i = 0; i < agentWorld.getTribes().size(); i++) {
			for (int j = 0; j < agentWorld.getTribes().get(i).getUnits().size(); j++) {
				if (agentWorld.getTribes().get(i).getUnits().get(j).getId().getName().equals(msgSenderAID.getName()))
					return i;
			}
		}
		return -1;
	}

	public static Unit findUnitByAID(AID aid, Tribe tribe) {
		ArrayList<Unit> units = tribe.getUnits();
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getId().getName().equals(aid.getName()))
				return units.get(i);
		}
		return null;
	}
}
