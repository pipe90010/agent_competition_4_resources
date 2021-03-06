package es.upm.woa.group2.rules;

import es.upm.woa.ontology.Cell;
import es.upm.woa.group2.agent.AgWorld;
import es.upm.woa.group2.beans.Tribe;

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
	
	public boolean meetTownhallCreationCondition(Integer gold,Integer stones, Integer wood, boolean oneUnitBuilding)
	{
		return gold>=250 && stones>=150 && wood>=200 && !oneUnitBuilding;
	}
	
	public boolean meetFarmCreationCondition(Integer gold,Integer stones, Integer wood, boolean oneUnitBuilding)
	{
		return gold>=100 && stones>=25 && wood>=25 && !oneUnitBuilding;
	}
	
	public boolean meetStoreCreationCondition(Integer gold,Integer stones, Integer wood, boolean oneUnitBuilding)
	{
		return gold>=50 && stones>=50 && wood>=50 && !oneUnitBuilding;
	}
	
}
