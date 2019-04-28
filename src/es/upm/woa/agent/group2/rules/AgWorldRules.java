package es.upm.woa.agent.group2.rules;

import es.upm.woa.ontology.Cell;
import es.upm.woa.agent.group2.agents.AgWorld;
import es.upm.woa.agent.group2.beans.Tribe;

public class AgWorldRules {
	public boolean hasEnoughGold(Integer gold) {
		return gold >= 150;
	}

	public boolean hasEnoughFood(Integer food) {
		return food >= 50;
	}

	public boolean isInTownhall(Integer X, Integer Y, Cell position) {
		return X == position.getX() && Y == position.getY();
	}
	
	public boolean isItsOwnTownhall(Cell senderTownhallPosition, Cell tribeTownhall) {
		 return senderTownhallPosition.getX()==tribeTownhall.getX()&&senderTownhallPosition.getY()==tribeTownhall.getY();		 
		 }
}
