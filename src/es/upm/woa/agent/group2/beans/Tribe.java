package es.upm.woa.agent.group2.beans;

import java.util.ArrayList;

import es.upm.woa.ontology.Cell;
import jade.core.AID;

public class Tribe {

	// -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------
	
	public static final String TRIBE="Tribe";
	
	// -----------------------------------------------------------------
    // Atributes
    // -----------------------------------------------------------------
	private int gold;
	private int food;
	private Cell townhall;
	private ArrayList<Unit> units;
	private AID id;
	
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
	public Tribe(AID id, int gold, int food, Cell townhall) {
		super();
		this.id = id;
		this.gold = gold;
		this.food = food;
		this.townhall = townhall;
		this.units = new ArrayList<Unit>();
	}
	
	// -----------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------
	

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getFood() {
		return food;
	}

	public void setFood(int food) {
		this.food = food;
	}

	public Cell getTownhall() {
		return townhall;
	}

	public void setTownhall(Cell townhall) {
		this.townhall = townhall;
	}

	public ArrayList<Unit> getUnits() {
		return units;
	}
	
	public void addUnit(Unit u, int goldCost, int foodCost) {
		units.add(u);
		setGold(getGold()-goldCost);
		setFood(getFood()-foodCost);
	}

	public void setUnits(ArrayList<Unit> units) {
		this.units = units;
	}

	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}
}
