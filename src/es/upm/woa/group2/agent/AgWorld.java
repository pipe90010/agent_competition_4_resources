package es.upm.woa.group2.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.behaviours.CreateBuildingBehaviour;
import es.upm.woa.group2.behaviours.MovementRequestBehaviour;
import es.upm.woa.group2.behaviours.RegisterTribeBehaviour;
import es.upm.woa.group2.behaviours.UnitCreationBehaviour;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.common.WorldTimer;
import es.upm.woa.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.Empty;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.InitalizeTribe;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyCellDetail;
import es.upm.woa.ontology.NotifyNewUnit;
import es.upm.woa.ontology.RegisterTribe;
import es.upm.woa.ontology.ResourceAccount;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgWorld extends Agent {

	// -----------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String WORLD = "World";
	public final static String TRIBE = "Tribe";

	public final static String REGISTRATION_DESK = "REGISTRATION DESK";
	public int MAX_REGISTRATION_TIME = 0;
	public int GAME_TIME = 0;
	
	
	public final static Integer GOLD = 1500;
	public final static Integer FOOD = 500;
	public final static Integer STONES = 500;
	public final static Integer WOOD = 500;

	private final static int X_BOUNDARY = 100;
	private final static int Y_BOUNDARY = 100;

	// -----------------------------------------------------------------
	// Building Constants
	// -----------------------------------------------------------------

	public final static String TOWNHALL = "Town Hall";
	public final static String FARM = "Farm";
	public final static String STORE = "Store";

	// -----------------------------------------------------------------
	// Atributes
	// -----------------------------------------------------------------

	// Codec for the SL language used and instance of the ontology
	// GameOntology that we have created this part always goes
	// Create the array of Tribes
	// loads the map
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	private ArrayList<Tribe> tribes;
	private AgWorldRules worldRules;
	private WorldTimer worldTimer;
	private Cell[][] map;
	private Properties properties = new Properties();

	private boolean gameOver;
	private boolean registrationPeriod; 
	
	
	// -----------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------

	public AgWorld() {
		// TODO Auto-generated constructor stub
	}

	// -----------------------------------------------------------------
	// JADE Methods
	// -----------------------------------------------------------------

	protected void setup() {
		System.out.println("Group2 - " + getLocalName() + ": has entered into the system ");

		 readPropertiesFile();
				
		// Register of the codec and the ontology to be used in the ContentManager
		// Register language and ontology this part always goes
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		try {
			// Creates its own description
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setName(this.getName());
			sd.setType(WORLD);
			//sd.setType(REGISTRATION_DESK);
			
			dfd.addServices(sd);
			
			//Reg Desk
			
			ServiceDescription sdReg = new ServiceDescription();
			sdReg.setName(this.getName());
			sdReg.setType(REGISTRATION_DESK);
			
			dfd.addServices(sdReg);
			
			
			// Registers its description in the DF
			DFService.register(this, dfd);
			Printer.printSuccess(getLocalName(), "registered in the DF");
			/**
			 * Initialize everything
			 */

			initialize();
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		/*
		 * BEHAVIORS--------------------------------------------------------------------
		 * ----------------------
		 */
		
		RegisterTribeBehaviour registerTribeBehaviour = new RegisterTribeBehaviour(this);
		UnitCreationBehaviour unitTribeBehaviour = new UnitCreationBehaviour(this);
		CreateBuildingBehaviour createBuildingBehaviour = new CreateBuildingBehaviour(this);
		MovementRequestBehaviour movementRequestBehaviour = new MovementRequestBehaviour(this);
	

		addBehaviour(registerTribeBehaviour);
		addBehaviour(unitTribeBehaviour);
		addBehaviour(createBuildingBehaviour);
		addBehaviour(movementRequestBehaviour);
		
	}

	private void readPropertiesFile() {
        File file = new File("woa.properties");
        try {
            properties.load(new FileInputStream(file));
            System.out.println("Properties file loaded");

            // Fill properties
            this.MAX_REGISTRATION_TIME = Integer.parseInt(properties.getProperty("reg_time"));
            this.GAME_TIME = Integer.parseInt(properties.getProperty("game_time"));
        } catch (FileNotFoundException e) {
            System.err.println("File " + file.getAbsolutePath() + " was not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	// -----------------------------------------------------------------
	// Initialize Methods
	// -----------------------------------------------------------------

	private void initialize() {
		tribes = new ArrayList<Tribe>();
		worldRules = new AgWorldRules();
		// PASS ATRIBUTE IN MILISECONDS
		worldTimer = new WorldTimer(Long.parseLong(properties.getProperty("tick_millis")));
		this.initializeMap();
		gameOver = false;
		registrationPeriod = true;
		startRegistrationTime();
		//registrationPeriod = true;
	}

	private void initializeMap() {
		map = new Cell[X_BOUNDARY][Y_BOUNDARY];
		for (int i = 1; i < X_BOUNDARY; i++) {
			for (int j = 1; j < Y_BOUNDARY; j++) {
				map[i][j] = new Cell();
				map[i][j].setContent(new Empty());
				map[i][j].setX(i);
				map[i][j].setY(j);
			}
		}
	}

	// -----------------------------------------------------------------
	// Regular Methods
	// -----------------------------------------------------------------

	/**
	 * Randomly books the next empty available cell 
	 * @param conc
	 * @return
	 */
	public Cell bookNextRandomCell() {

		int x = new Random().nextInt(X_BOUNDARY);
		int y = new Random().nextInt(Y_BOUNDARY);
		return map[x][y];
	}

	/*
	private Tribe createTribe(String nickname, int teamNumber) {
		//ContainerController cc = getContainerController();

		AgTribe agentTribe = new AgTribe();
		try {
			//cc.acceptNewAgent(nickname, agentTribe).start();

			Building townhall = new Building();
			townhall.setOwner(agentTribe.getAID());
			townhall.setType(TOWNHALL);

			Cell townhallCell = bookNextRandomCell();
									
			//creates the tribe and assign it an initial amount of resources and a TownHall cell 
			Tribe tribe = new Tribe(agentTribe.getAID(), GOLD, FOOD,STONES,WOOD,teamNumber);
			return tribe;
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/

	public Integer canCreateUnit(Tribe t, Cell position, Integer index) {
		if (!worldRules.isItsOwnTownhall(t.getTownhall(), tribes.get(index).getTownhall())) {
			return 2;
		}
		if (!worldRules.isInTownhall(t.getTownhall().getX(), t.getTownhall().getY(), position)) {
			return 3;
		}
		if (!worldRules.hasEnoughGold(t.getGold())) {
			return 4;
		}
		if (!worldRules.hasEnoughFood(t.getFood())) {
			return 5;
		}
		if (isGameOver())
			return 6;
		return 1;
	}

	public Unit createUnit(boolean isFirst,String nickname, Tribe tribe) {

		ContainerController cc = getContainerController();

		try {
			Cell position;
			/*if(isFirst)
				position = tribe.getTownhall();
			else*/
			 position = bookNextRandomCell();

			Object[] args = new Object[2];
			args[0] = position.getX();
			args[1] = position.getY();
			long waitTime = worldTimer.getCreationTime();

			//there should be implemented a FSM behavior here
			//to refuse incoming request messages while the agent is in waiting state  
			doWait(waitTime);
			
			if (!isGameOver()) {
				AgentController ac = cc.createNewAgent(nickname, AgUnit.class.getName(), args);
				ac.start();
				// TODO: CHECK IF WE NEED TO ADD THE UNIT AS A CONTENT FOR THE CELL
				Unit newUnit = new Unit(getAID(nickname), position);
				//map[position.getX()][position.getY()].setOwner(tribe.getId());

				if (tribe != null) {
					AID ag = tribe.getId();

					ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
							"CreateUnit", ag);
					// Creates a notifyNewUnit action
					NotifyNewUnit notify = new NotifyNewUnit();

					notify.setLocation(position);
					notify.setNewUnit(getAID(nickname));
					Action agActionNotification = new Action(ag, notify);
					getContentManager().fillContent(msgInform, agActionNotification);
					send(msgInform);
				}
				return newUnit;
			}
			return null;
		} catch (StaleProxyException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public int findTribePositionByUnitAID(AID aid) {
		for (int i = 0; i < tribes.size(); i++) {
			for (int j = 0; j < tribes.get(i).getUnits().size(); j++) {
				if (tribes.get(i).getUnits().get(j).getId().getName().equals(aid.getName()))
					return i;
			}
		}
		return -1;
	}
	
	public int findTribePositionByTeamNumber(int teamNumber) {
		
		for (int i = 0; i < tribes.size(); i++) 
		{
			if (tribes.get(i).getTeamNumber() == teamNumber)
				return i;
		}
		return -1;
	}
	
	private int findTribePositionByTribeAID(AID aid) {
		for (int i = 0; i < tribes.size(); i++) {
			if (tribes.get(i).getId().getLocalName().equals(aid.getLocalName()))
					return i;
		}
		return -1;
	}

	public boolean updateUnitInTribeByUnitAID(Unit unit) {
		for (int i = 0; i < tribes.size(); i++) {
			for (int j = 0; j < tribes.get(i).getUnits().size(); j++) {
				if (tribes.get(i).getUnits().get(j).getId().getName().equals(unit.getId().getName())) {
					tribes.get(i).getUnits().set(j, unit);
					
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean updateTribeByTribeAID(Tribe tribe) {
		for (int i = 0; i < tribes.size(); i++) {
			if (tribes.get(i).getId().getName().equals(tribe.getId().getName())) {
				tribes.set(i, tribe);
				return true;
			}
		}
		return false;
	}

	public Unit findUnitByAID(AID aid, Tribe tribe) {
		ArrayList<Unit> units = tribe.getUnits();
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getId().getName().equals(aid.getName()))
				return units.get(i);
		}
		return null;
	}

	/**
	 * Checks whether two positions are adjacent to each other. This is relevant for
	 * unit movement, because they can only move to adjacent positions. TODO: This
	 * should definitely be in a separate class...
	 *
	 * @param posA First position
	 * @param posB Second position
	 * @return boolean Whether the two positions are adjacent
	 */
	public boolean areAdjacentPositions(Cell posA, Cell posB) {
		// First, both positions need to be valid
		if (!isValidPosition(posA) || !isValidPosition(posB)) {
			Printer.printSuccess(getLocalName(), "This position doesn't exist on our hexagonal map $$$");
			return false;
		}

		// Second, the deltas of both dimensions have to be correct
		int deltaX = Math.abs(posA.getX() - posB.getX());
		int deltaY = Math.abs(posA.getY() - posB.getY());

		return (deltaX <= 2 && deltaY <= 1);
	}
	
	
	public Cell getTargetPosition(Cell currentPosition, int nextMove) {
		
		Cell targetPosition = null;
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		switch (nextMove) {
		case 1:
			if(x-2>=1)
				targetPosition = map[x-2][y];
			break;
		case 2:
			if(x-->=1 && y++<=Y_BOUNDARY)
				targetPosition = map[x][y];
			break;	
		case 3:
			if(x++<=X_BOUNDARY && y++<=Y_BOUNDARY)
				targetPosition = map[x][y];
			break;
		case 4:
			if(x+2<=X_BOUNDARY)
				targetPosition = map[x+2][y];
			break;
		case 5:
			if(x++<=X_BOUNDARY && y-->=1)
				targetPosition = map[x][y];
			break;	
		default:
			if(x-->=1 && y-->=1)
				targetPosition = map[x][y];
			break;
		}
		
		return targetPosition;
	}
	
	/*public Cell getMirrorCell(Cell position)
	{
		int x = position.getX();
		int y = position.getY();
		if(x%2==0)
		{
			if(x+1>=X_BOUNDARY)
				return map[2][y];
			else
				return map[X_BOUNDARY][y];
		}
		return null;
	}*/
	
	public Cell getMirrorCellX(Cell position)
	{
		int x = position.getX();
		int y = position.getY();
		
		//validates that a map is squared 		
		if(X_BOUNDARY == Y_BOUNDARY) { 
			//validates that a position is even, else it is odd 
			if(X_BOUNDARY%2==0) {
				if(x%2==0)
				{
					//validates that the movement is upside or downside
					//case 3X3
					if(x+1 > X_BOUNDARY )
						return map[2][y];				
					if(x-2==0)
						return map[ X_BOUNDARY - 1 ][y];
					else
						return null;
				}
				else
				{
					//validates that the movement is upside or downside
					//down
					if(x+1 == X_BOUNDARY )
						return map[1][y];				
					//up
					if(x-1==0)
						return map[ X_BOUNDARY ][y];
					else
						return null;
				}
			}
			else {
				if(x%2==0)
				{	
					//case 4X4
					//up
					if(x-2==0)
						return map[ X_BOUNDARY ][y];
					//down
					if(x==X_BOUNDARY)
						return map[2][y];
					else
						return null;					
				}
				else {
					//case 4X4
					//up
					if(x-1==0)
						return map[ X_BOUNDARY - 1 ][y];
					//down
					if(x+1 == X_BOUNDARY)
						return map[1][y];
					else
						return null;	
				}
			}
		}
		//When the map is not a square
		else
		{
			if(X_BOUNDARY < Y_BOUNDARY) {
				//validates that a position is even, else it is odd 
				if(x%2==0)
				{
					//validates that the movement is upside or downside
					//case 3X4
					//down
					if(x == X_BOUNDARY )//unit is in the boundary
						return map[2][y];				
					//up
					if(x-2==0) // check for unit is in second row
						return map[ X_BOUNDARY ][y];
					//case 4X5
					//down
					if(x+1 == X_BOUNDARY)//unit is in a even unit close to the boundary
						return map[ 2 ][y];
					else
						return null;
				}
				else
				{
					//validates that the movement is upside or downside					
					//case 3X4
					//down
					if(x+1 == X_BOUNDARY )
						return map[1][y];					
					//up
					if(x-1==0)
						return map[ X_BOUNDARY -1 ][y];
					//case 4X5
					//down
					if(x == X_BOUNDARY )
						return map[1][y];
					else
						return null;
				}
			}
			else {
				//validates that a position is even, else it is odd 
				if(x%2==0)
				{
					//validates that the movement is upside or downside 
					//case 4X3
					// down
					if(x+1 == X_BOUNDARY )
						return map[2][y];				
					// up
					if(x-2==0)
						return map[ X_BOUNDARY - 1 ][y];
					//case 5X4
					//down
					if(x == X_BOUNDARY )
						return map[2][y];
					else
						return null;
				}
				else
				{
					//case 4X3
					//down
					if(x == X_BOUNDARY )
						return map[1][y];
					//up
					if(x-1==0)
						return map[ X_BOUNDARY ][y];
					//case 5X4
					//down
					if(x + 1 == X_BOUNDARY )
						return map[1][y];
					else
						return null;
				}
			}
		}		
	}

	// TODO: Move to another class
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isValidPosition(Cell position) {
		int x = position.getX();
		int y = position.getY();

		// If the coordinates are outside of map

		if (x > X_BOUNDARY || y > Y_BOUNDARY)
			return false;
		// If the coordinates are negative
		if (x < 0 || y < 0)
			return false;
		return ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0));
	}

	// TODO: Move to another class
	private Integer readIntProp(String property) {
		return Integer.parseInt(properties.getProperty(property));
	}

	public Cell moveUnitToPosition(Unit unit, Cell cell) {
		map[cell.getX()][cell.getY()] = cell;
		// TODO: UPDATE WITH THE NEW ONTOLOGY
		map[cell.getX()][cell.getY()].setContent(new Empty());

		updateUnitInTribeByUnitAID(unit);

		return map[cell.getX()][cell.getY()];
	}
	
	
	public boolean isRegistrationPeriod() {
		return registrationPeriod;
	}

	public void setRegistrationPeriod(boolean registrationPeriod) {
		this.registrationPeriod = registrationPeriod;
	}

	public ArrayList<Tribe> getTribes() {
		return tribes;
	}

	public Cell[][] getMap() {
		return map;
	}

	public Properties getProperties() {
		return properties;
	}

	public boolean isGameOver() {

		return false;
	}

	public WorldTimer getWorldTimer() {
		return worldTimer;
	}

	public void setWorldTimer(WorldTimer worldTimer) {
		this.worldTimer = worldTimer;
	}
	
	public void startRegistrationTime()
	{
		Thread t = new Thread(new Runnable() {
	         @Override
	         public void run() {
	              // Insert some method call here.
	        	 try
	        	 {
	        		 Thread.sleep(10000);
	        		 registrationPeriod = false;
	        			
	        	 }
	        	 catch (Exception e) {
	     			e.printStackTrace();
	     		}
	        	 
	         }
		});
		t.start();
	}
	/*
	 * 100 × cells_explored(i) + 500 × cities_owned(i) + 250 × stores_owned(i) + 300 × farms_owned(i) + 400 × units_owned(i) + 10 × gold_owned(i) + 2 × stone_owned(i) + 1 × wood_owned(i) + 5 × food_owned(i)
	 */
	public int calculateScore(Tribe tribe)
	{
		
		int score = ((100*tribe.getDiscoveredCells().size())
				+(500*tribe.getCities().size())
				+(250*tribe.getBuildingsByType(STORE).size())
				+(300*tribe.getBuildingsByType(FARM).size())
				+(400*tribe.getMemberSize())
				+(10*tribe.getGold())
				+(2*tribe.getStones())
				+(tribe.getWood())
				+(5*tribe.getFood()));
		
		Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE CURRENT SCORE IS= "+score);
		return score;
	}
	/*
	 * cells_explored: Number of cells explored by the tribe.
- cities_owned: Number of cities owned by the tribe.
- stores_owned: Number of stores owned by the tribe.
- farms_owned: Number of farm owned by the tribe.
- units_owned: Number of units owned by the tribe.
- gold_owned: Amount of gold owned by the tribe.
- stone_owned: Amount of stone owned by the tribe.
- wood_owned: Amount of wood owned by the tribe.
- food_owned: Amount of food owned by the tribe.
	 */
	public void printFinalScore()
	{
		for (Tribe tribe: tribes) {
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL NUMBER OF CELLS EXPORED= "+tribe.getDiscoveredCells().size());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL NUMBER OF CITIES OWNED= "+tribe.getCities().size());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL NUMBER OF STORES OWNED= "+tribe.getBuildingsByType(STORE).size());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL NUMBER OF FARMS OWNED= "+tribe.getBuildingsByType(FARM).size());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL NUMBER OF UNITS OWNED= "+tribe.getUnits().size());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL AMOUNT OF GOLD OWNED= "+tribe.getGold());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL AMOUNT OF STONES OWNED= "+tribe.getStones());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL AMOUNT OF WOOD OWNED= "+tribe.getWood());
			Printer.printSuccess("Team: "+tribe.getTeamNumber(), "THE FINAL AMOUNT OF FOOD OWNED= "+tribe.getFood());
			calculateScore(tribe);
		}
	}

}