package es.upm.woa.agent.group2.common;

public class WorldTimer {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------
		//REAL HOURS
		private final static long MOVE_CELL_TIME = 6; 
		private final static long CREATE_UNIT_TIME = 15; 
		
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
		System.out.println("...waiting time: "+time+"ms");
		return time;
	}
	
	public long getCreationTime() {
		long time = parseTime(CREATE_UNIT_TIME);
		System.out.println("...waiting time: "+time+"ms");
		return time;
	}
}
