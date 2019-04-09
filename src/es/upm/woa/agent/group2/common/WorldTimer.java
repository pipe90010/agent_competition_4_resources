package es.upm.woa.agent.group2.common;

public class WorldTimer {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------
		//REAL HOURS
		private final static long MOVE_CELL_TIME = 6; 
		
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
		return  parseTime(MOVE_CELL_TIME);
	}

}
