package es.upm.woa.group2.beans;

import java.util.ArrayList;

import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import jade.core.AID;
import jade.util.leap.Iterator;

public class Tribe {

	// -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------
	
	public static final String TRIBE="Tribe";
	
	
	// -----------------------------------------------------------------
	// Building Constants
	// -----------------------------------------------------------------

	public final static String TOWNHALL = "Town Hall";
	public final static String FARM = "Farm";
	public final static String STORE = "Store";
	// -----------------------------------------------------------------
    // Atributes
    // -----------------------------------------------------------------
	private int gold;
	private int food;
	private int stones;
	private ArrayList<Building> cities;
	private int wood;
	private Cell townhall;
	private ArrayList<Unit> units;
	private int memberSize;
	private AID id;
	private ArrayList<Cell> discoveredCells;
	private int teamNumber;
	private int x_boundary;
	public int getX_boundary() {
		return x_boundary;
	}

	public void setX_boundary(int x_boundary) {
		this.x_boundary = x_boundary;
	}

	private int y_boundary;
	public int getY_boundary() {
		return y_boundary;
	}

	public void setY_boundary(int y_boundary) {
		this.y_boundary = y_boundary;
	}

	//Unit that creates other units and buildings
	private Unit unitBuilder;
	
	//Create unit collecters
	private ArrayList<Unit> unitsCollecters;
	
	//Explore map and if they find any valuable resource they will exploit it
	private ArrayList<Unit> unitsExplorers;
	
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
	public int getTeamNumber() {
		return teamNumber;
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	public Tribe(AID id, int gold, int food, int stones, int wood, int teamNumber) {
		super();
		this.id = id;
		this.gold = gold;
		this.food = food;
		this.stones = stones;
		this.wood = wood;
		this.units = new ArrayList<Unit>();
		this.discoveredCells = new ArrayList<Cell>();
		this.cities = new ArrayList<Building>();
		this.memberSize = 0;
		this.teamNumber = teamNumber;
		this.unitsCollecters = new ArrayList<Unit>();
		this.unitsExplorers = new ArrayList<Unit>();
	}
	
	public Tribe(AID id) {
		this.units = new ArrayList<Unit>();
		this.discoveredCells = new ArrayList<Cell>();
		this.memberSize = 0;
		this.id= id;
	}
	
	public Tribe() {
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

	public void convertAidToUnits(jade.util.leap.List aids) {
		Iterator iterator = aids.iterator();
		while(iterator.hasNext())
		{	
			AID aid = (AID) iterator.next();			
			Unit unit = new Unit(aid);
			this.units.add(unit); 
		}
		
		
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

	
	public ArrayList<Building> getCities() {
		return cities;
	}

	public void setCities(ArrayList<Building> cities) {
		this.cities = cities;
	}
	
	/**
	 * 
	 * @param cell
	 * @return
	 */
	public void addNewCity(Building building) {
		cities.add(building);
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
	
	public ArrayList<Building> getBuildingsByType(String type){
		
		ArrayList<Building> builds = new ArrayList<Building>();
		for (Building building : cities) {
			
			if(building.getType().equals(type))
			{
				builds.add(building);
			}
		}
		return builds;
	}
	
	public Unit isAnyUnitBuilding() {
		
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if(unit.getAction()!=null && unit.getAction().equals("BUILDING"))
				return unit;
		}
		return null;
	}
	
	public jade.util.leap.ArrayList getOntologyUnitsAID(){
		ArrayList<AID> aids = new ArrayList<AID>();
		for (Unit unit : units) {
			aids.add(unit.getId());
		}
		jade.util.leap.ArrayList list = new jade.util.leap.ArrayList(aids);
		return list;
	}

	public Unit getUnitBuilder() {
		return unitBuilder;
	}

	public void setUnitBuilder(Unit unitBuilder) {
		this.unitBuilder = unitBuilder;
	}

	public ArrayList<Unit> getUnitsCollecters() {
		return unitsCollecters;
	}
	
	public void addUnitsCollecters(Unit u)
	{
		this.unitsCollecters.add(u);
	}
	public void setUnitsCollecters(ArrayList<Unit> unitsCollecters) {
		this.unitsCollecters = unitsCollecters;
	}

	public ArrayList<Unit> getUnitsExplorers() {
		return unitsExplorers;
	}
	
	public void addUnitsExplorers(Unit u)
	{
		this.unitsCollecters.add(u);
	}
	
	public void setUnitsExplorers(ArrayList<Unit> unitsExplorers) {
		this.unitsExplorers = unitsExplorers;
	}
	
	
}
