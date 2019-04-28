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
	private int stones;
	private int wood;
	private Cell townhall;
	private ArrayList<Unit> units;
	private int memberSize;
	private AID id;
	private ArrayList<Cell> discoveredCells;
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
	public Tribe(AID id, int gold, int food, int stones, int wood) {
		super();
		this.id = id;
		this.gold = gold;
		this.food = food;
		this.stones = stones;
		this.wood = wood;
		this.units = new ArrayList<Unit>();
		this.discoveredCells = new ArrayList<Cell>();
		this.memberSize = 0;
	}
	
	public Tribe(AID id) {
		this.units = new ArrayList<Unit>();
		this.discoveredCells = new ArrayList<Cell>();
		this.memberSize = 0;
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
	
	public void addUnit(Unit u) {
		units.add(u);
		setMemberSize(this.memberSize+1);
	}
	
	//Always overload the gold and food cost for world actions
	public void deductCost(int goldCost, int foodCost, int stones, int wood) {
		setGold(getGold()-goldCost);
		setFood(getFood()-foodCost);
		setFood(getStones()-stones);
		setFood(getWood()-wood);
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

	public int getMemberSize() {
		return memberSize;
	}

	public void setMemberSize(int memberSize) {
		this.memberSize = memberSize;
	}

	public ArrayList<Cell> getDiscoveredCells() {
		return discoveredCells;
	}

	public void setDiscoveredCells(ArrayList<Cell> discoveredCells) {
		this.discoveredCells = discoveredCells;
	}
	
	public int getStones() {
		return stones;
	}

	public void setStones(int stones) {
		this.stones = stones;
	}

	public int getWood() {
		return wood;
	}

	public void setWood(int wood) {
		this.wood = wood;
	}

	/**
	 * 
	 * @param cell
	 * @return
	 */
	public boolean addDiscoveredCell(Cell cell) {
		
		boolean isNew=true;
		for (int i = 0; i < discoveredCells.size(); i++) {
			Cell currentCell = discoveredCells.get(i);
			if(currentCell.getX()==cell.getX() && currentCell.getY()==cell.getY())
			{
				isNew = false;
				discoveredCells.set(i, cell);
			}
				
		}
		if(isNew)
			discoveredCells.add(cell);
		return isNew;
	}
	
	public int getUnitByIndex(Unit unitToSearch)
	{
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if(unit.getId()==unitToSearch.getId())
				return i;
		}
		return -1;
	}
	
	public Unit isAnyUnitBuilding() {
		
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if(unit.getAction()!=null && unit.getAction().equals("MOVING"))
				return unit;
		}
		return null;
	}
}
