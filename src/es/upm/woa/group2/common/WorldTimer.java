package es.upm.woa.group2.common;

public class WorldTimer {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------
		//REAL HOURS
		private final static long MOVE_CELL_TIME = 6; 
		private final static long CREATE_UNIT_TIME = 15; 
		private final static long BUILD_TOWN_HALL_TIME = 25;
		private final static long BUILD_FARM_TIME = 12;
		private final static long BUILD_STORE_TIME  = 12;
		private final static long EXPLOIT_ORE_TIME = 8;
		private final static long EXPLOIT_WOOD_TIME = 10;
		
	// -----------------------------------------------------------------
	// Atributes
	// -----------------------------------------------------------------	
	
	private long simulationMiliseconds; 
	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------	
	
	public WorldTimer(long simulationMiliseconds) {
		this.simulationMiliseconds = simulationMiliseconds;
	}
	
	public long parseTime(long time) {
		return time * simulationMiliseconds;
	}
	
	public long getMovementTime() {
		long time = parseTime(MOVE_CELL_TIME);
		System.out.println("...waiting time for MOVE_CELL_TIME:"+time+"ms");
		return time;
	}
	
	public long getCreationTime() {
		long time = parseTime(CREATE_UNIT_TIME);
		System.out.println("...waiting time for CREATE_UNIT_TIME:"+time+"ms");
		return time;
	}
	
	public long getBuildTownhallTime() {
		long time = parseTime(BUILD_TOWN_HALL_TIME);
		System.out.println("...waiting time for BUILD_TOWN_HALL_TIME:"+time+"ms");
		return time;
	}
	
	public long getBuildFarmTime() {
		long time = parseTime(BUILD_FARM_TIME);
		System.out.println("...waiting time for BUILD_FARM_TIME:"+time+"ms");
		return time;
	}
	
	public long getBuildStoreTime() {
		long time = parseTime(BUILD_STORE_TIME);
		System.out.println("...waiting time for BUILD_STORE_TIME:"+time+"ms");
		return time;
	}
	
	public long getExploitResourceTime() {
		long time = parseTime(EXPLOIT_ORE_TIME);
		System.out.println("...waiting time for EXPOIT_RESOURCE_TIME:"+time+"ms");
		return time;
	}
	
	public long getExploitWoodTime() {
		long time = parseTime(EXPLOIT_WOOD_TIME);
		System.out.println("...waiting time for EXPOIT_WOOD_TIME:"+time+"ms");
		return time;
	}
}
