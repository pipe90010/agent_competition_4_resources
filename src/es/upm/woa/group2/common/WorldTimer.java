package es.upm.woa.group2.common;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

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
		
		private int GAME_TIME;
		private int movementsCounter=0;
		
		private Date currentTime;
	// -----------------------------------------------------------------
	// Atributes
	// -----------------------------------------------------------------	
	
	private long simulationMiliseconds; 
	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------	
	
	public WorldTimer(long simulationMiliseconds) {
		this.simulationMiliseconds = simulationMiliseconds;
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        currentTime = cal.getTime();
        
		
	}
	
	public long parseTime(long time) {
		return time * simulationMiliseconds;
	}
	
	public long getMovementTime() {
		movementsCounter++;
		long time = parseTime(MOVE_CELL_TIME);
		System.out.println("...waiting time for MOVE_CELL_TIME:"+time+"ms");
		Calendar cal = Calendar.getInstance();
		
		Date newDate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date diff = new Date(newDate.getTime() - currentTime.getTime());
		
		System.out.println( "TIME DIFFERENCE"+sdf.format(diff) );
		System.out.println("Movements:"+movementsCounter);
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

	public int getGAME_TIME() {
		long time = parseTime(GAME_TIME);
		System.out.println("...waiting time for GAME_TIME:"+time+"ms");
		return GAME_TIME;
	}

	public void setGAME_TIME(int gAME_TIME) {
		GAME_TIME = gAME_TIME;
	}
	
	
}
