package es.upm.woa.agent.group2.rules;

import es.upm.woa.ontology.Cell;

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

}
