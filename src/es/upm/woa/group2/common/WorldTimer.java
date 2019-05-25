package es.upm.woa.group2.common;

public class WorldTimer {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------
		//REAL HOURS
		private final static long MOVE_CELL_TIME = 6; 
		private final static long CREATE_UNIT_TIME = 15; 
		private final static long BUILD_TOWN_HALL_TIME = 24; 
		private final static long EXPOIT_RESOURCE_TIME = 8; 
		
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
	
	public long getExploitResourceTime() {
		long time = parseTime(EXPOIT_RESOURCE_TIME);
		System.out.println("...waiting time for EXPOIT_RESOURCE_TIME:"+time+"ms");
		return time;
	}
}
