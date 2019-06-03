package es.upm.woa.group2.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import es.upm.woa.group2.agent.AgTribe;
import es.upm.woa.group2.agent.AgUnit;
import es.upm.woa.group2.beans.Tribe;
import es.upm.woa.group2.beans.Unit;
import es.upm.woa.group2.behaviours.CreateBuildingBehaviour;
import es.upm.woa.group2.behaviours.ExploitResourceBehaviour;
import es.upm.woa.group2.behaviours.MovementRequestBehaviour;
import es.upm.woa.group2.behaviours.RegisterTribeBehaviour;
import es.upm.woa.group2.behaviours.UnitCreationBehaviour;
import es.upm.woa.group2.common.HttpRequest;
import es.upm.woa.group2.common.MessageFormatter;
import es.upm.woa.group2.common.Printer;
import es.upm.woa.group2.common.WorldTimer;
import es.upm.woa.group2.rules.AgWorldRules;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.CreateBuilding;
import es.upm.woa.ontology.CreateUnit;
import es.upm.woa.ontology.EndOfGame;
import es.upm.woa.ontology.ExploitResource;
import es.upm.woa.ontology.Ground;
import es.upm.woa.ontology.GameOntology;
import es.upm.woa.ontology.InitalizeTribe;
import es.upm.woa.ontology.MoveToCell;
import es.upm.woa.ontology.NotifyCellDetail;
import es.upm.woa.ontology.NotifyNewUnit;
import es.upm.woa.ontology.RegisterTribe;
import es.upm.woa.ontology.Resource;
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

	public int X_BOUNDARY;
	public int Y_BOUNDARY;

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
			// sd.setType(REGISTRATION_DESK);

			dfd.addServices(sd);

			// Reg Desk

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
		ExploitResourceBehaviour exploitRequestBehaviour = new ExploitResourceBehaviour(this);

		addBehaviour(registerTribeBehaviour);
		addBehaviour(unitTribeBehaviour);
		addBehaviour(createBuildingBehaviour);
		addBehaviour(movementRequestBehaviour);
		addBehaviour(exploitRequestBehaviour);

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
		worldTimer.setGAME_TIME(this.GAME_TIME);
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// Insert some method call here.
				try {
					Thread.sleep(worldTimer.getGAME_TIME());
					for (int i = 0; i < tribes.size(); i++) {
						Tribe t = tribes.get(i);
						ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
								"EndOfGame", t.getId());
						// Creates a notifyNewUnit action
						EndOfGame notify = new EndOfGame();
						Action agActionNotification = new Action(t.getId(), notify);
						getContentManager().fillContent(msgInform, agActionNotification);
						send(msgInform);
						
						for (int j = 0; j < t.getUnits().size(); j++) {
							Unit u = t.getUnits().get(j);
							ACLMessage msgInformUnit = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
									"EndOfGame", u.getId());
							// Creates a notifyNewUnit action
							EndOfGame notifyUnit = new EndOfGame();
							Action agActionNotificationUnit = new Action(u.getId(), notifyUnit);
							getContentManager().fillContent(msgInform, agActionNotificationUnit);
							send(msgInformUnit);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		//t.start();
		startRegistrationTime();
		// registrationPeriod = true;
	}

	private void initializeMap() {

		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader("map.json")) {

			JSONObject mapObj = (JSONObject) parser.parse(reader);
			
			// Iterate over employee array
			JSONArray initialPositions = (JSONArray) mapObj.get("initialPositions");
			JSONObject initialResources = (JSONObject) mapObj.get("initialResources");
			Long mapWidth = (Long) mapObj.get("mapWidth");
			Long mapHeight = (Long) mapObj.get("mapHeight");
			JSONArray tiles = (JSONArray) mapObj.get("tiles");

			if (tiles != null && tiles.size() > 0) {
				X_BOUNDARY = mapHeight.intValue();
				Y_BOUNDARY = mapWidth.intValue();
				map = new Cell[X_BOUNDARY + 2][Y_BOUNDARY + 2];

				for (int i = 0; i < tiles.size(); i++) {
					JSONObject tile = (JSONObject) tiles.get(i);
					Long xLong = (Long) tile.get("y");
					Long yLong = (Long) tile.get("x");
					String resourceType = (String) tile.get("resource");

					int x = xLong.intValue();
					int y = yLong.intValue();
					map[x][y] = new Cell();
					
					
					if(!resourceType.equals(GameOntology.GROUND))
					{
						Resource resource = new Resource();
						resource.setResourceType(resourceType);

						if (tile.containsKey("resource_amount")) {
							Long resource_amount = (Long) tile.get("resource_amount");
							resource.setResourceAmount(resource_amount.intValue());
						}
						if (tile.containsKey("gold_percentage")) {
							Long gold_percentage = (Long) tile.get("gold_percentage");
							resource.setGoldPercentage(gold_percentage.intValue());
						}
						map[x][y].setContent(resource);
					}
					else
						map[x][y].setContent(new Ground());
					
					map[x][y].setX(x);
					map[x][y].setY(y);
				}

			}
			System.out.println("mapObj.toString();"+mapObj.toString());
			JSONArray players = new JSONArray();
			players.add(2);
			
			JSONObject parameters = new JSONObject();
			parameters.put("players",players);
			parameters.put("map",mapObj );
			
			HttpRequest.sendPost("/start", parameters);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	// Regular Methods
	// -----------------------------------------------------------------

	/**
	 * Randomly books the next empty available cell
	 * 
	 * @param conc
	 * @return
	 */
	public Cell bookNextRandomCell() {

		int x = new Random().nextInt((X_BOUNDARY - 1)) + 1;
		int y = new Random().nextInt((Y_BOUNDARY - 1)) + 1;
		
		while(map[x][y]==null)
		{
			x = new Random().nextInt((X_BOUNDARY - 1)) + 1;
			y = new Random().nextInt((Y_BOUNDARY - 1)) + 1;
		}

		return map[x][y];
	}

	/*
	 * private Tribe createTribe(String nickname, int teamNumber) {
	 * //ContainerController cc = getContainerController();
	 * 
	 * AgTribe agentTribe = new AgTribe(); try { //cc.acceptNewAgent(nickname,
	 * agentTribe).start();
	 * 
	 * Building townhall = new Building(); townhall.setOwner(agentTribe.getAID());
	 * townhall.setType(TOWNHALL);
	 * 
	 * Cell townhallCell = bookNextRandomCell();
	 * 
	 * //creates the tribe and assign it an initial amount of resources and a
	 * TownHall cell Tribe tribe = new Tribe(agentTribe.getAID(), GOLD,
	 * FOOD,STONES,WOOD,teamNumber); return tribe; } catch (StaleProxyException e) {
	 * e.printStackTrace(); } return null; }
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

	public void createUnit(boolean isFirst, String nickname, Tribe tribe, Cell initialPosition) {

		ContainerController cc = getContainerController();

		try {
			Cell position;
			/*
			 * if(isFirst) position = tribe.getTownhall(); else
			 */
			if (initialPosition != null)
				position = initialPosition;
			else
				position = tribe.getTownhall();

			/*
			 * Object[] args = new Object[2]; args[0] = position.getX(); args[1] =
			 * position.getY();
			 */
			//long waitTime = worldTimer.getCreationTime();

			// there should be implemented a FSM behavior here
			// to refuse incoming request messages while the agent is in waiting state
			//if(!isFirst)
			//doWait(waitTime);

			if (!isGameOver()) {
				AgentController ac = cc.createNewAgent(nickname, AgUnit.class.getName(), null);
				ac.start();
				// TODO: CHECK IF WE NEED TO ADD THE UNIT AS A CONTENT FOR THE CELL
				Unit newUnit = new Unit(getAID(nickname), position);
				// map[position.getX()][position.getY()].setOwner(tribe.getId());
				
				JSONObject parameters = new JSONObject();
				parameters.put("player_id",2);
				parameters.put("agent_id",getAID(nickname).getLocalName());
				JSONObject tile = new JSONObject();
				tile.put("x",position.getX());
				tile.put("y",position.getY());
				parameters.put("tile", tile);
				
				HttpRequest.sendPost("/agent/create", parameters);

				if (tribe != null) {
					AID ag = tribe.getId();

					ACLMessage msgInform = MessageFormatter.createMessage(getLocalName(), ACLMessage.INFORM,
							"NotifyNewUnit", ag);
					// Creates a notifyNewUnit action
					NotifyNewUnit notify = new NotifyNewUnit();

					notify.setLocation(position);
					notify.setNewUnit(getAID(nickname));
					Action agActionNotification = new Action(ag, notify);
					getContentManager().fillContent(msgInform, agActionNotification);
					send(msgInform);
					
					tribe.addUnit(newUnit);
					
					if(!isFirst)
					{
						tribe.deductCost(150, 50,0,0);
					}
					
				}
			}
		} catch (StaleProxyException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createUnitAsync(boolean isFirst, String nickname, Tribe tribe, Cell initialPosition) {

		ContainerController cc = getContainerController();

		try {
			Cell position;
			/*
			 * if(isFirst) position = tribe.getTownhall(); else
			 */
			if (initialPosition != null)
				position = initialPosition;
			else
				position = tribe.getTownhall();

			long waitTime = worldTimer.getCreationTime();



			if (!isGameOver()) {
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						// Insert some method call here.
						try {
							Thread.sleep(waitTime);
							createUnit(isFirst,nickname, tribe, initialPosition);
							
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
				t.start();
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		for (int i = 0; i < tribes.size(); i++) {
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
		Cell tempTarget = null;
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		switch (nextMove) {
		case 1:
			tempTarget = getMirrorCellX(currentPosition,nextMove);
			if (tempTarget != null) {
				targetPosition = tempTarget;
			} else {
				targetPosition = map[x - 2][y];
			}
			break;
		case 2:
			tempTarget = getMirrorCellY(currentPosition,nextMove);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x-1][ y+1];
			}
			break;
		case 3:
			
			tempTarget = getMirrorCellY(currentPosition,nextMove);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x+1][ y+1];
			}			
		case 4:
			tempTarget = getMirrorCellX(currentPosition,nextMove);
			if (tempTarget != null) {
				targetPosition = tempTarget;
			} else {
				targetPosition = map[x - 1][y];
			}
			break;
		case 5:
			
			tempTarget = getMirrorCellY(currentPosition,nextMove);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x+1][y-1];
			}
			break;
		default:
			tempTarget = getMirrorCellY(currentPosition,6);
			if (tempTarget!=null) {
				targetPosition= tempTarget;
			}
			else {
				targetPosition = map[x-1][ y-1];
			}
			break;
		}
		return targetPosition;
	}

	public Cell getMirrorCellX(Cell position, int coordinate) {
		int x = position.getX();
		int y = position.getY();

		// validates that a position is even, else it is odd
		if (x % 2 == 0) {
			// up
			if (x - 2 <= 0 && coordinate==1)
				return map[X_BOUNDARY][y];
			// down
			if (x >= X_BOUNDARY && coordinate==4)
				return map[2][y];
			else
				return null;
		} else {
			// up
			if (x - 1 <= 0 && coordinate==1)
				return map[X_BOUNDARY - 1][y];
			// down
			if (x + 1 >= X_BOUNDARY && coordinate==4)
				return map[1][y];
			else
				return null;
		}
	}
	
	public Cell getMirrorCellY(Cell position,int coordinate) {
		int x = position.getX();
		int y = position.getY();
		
		// validates that a position is even, else it is odd
		if (y % 2 == 0) {
			// right
			if (y == Y_BOUNDARY  && coordinate==2)
				if(x-2==0)
					return map[1][1];
				else
					return map[x-1][1];
			else if (y == Y_BOUNDARY && coordinate==3)
				if (x == X_BOUNDARY)
					return map[1][1];
				else
					return map[x+1][1];
			else if (y == Y_BOUNDARY &&  coordinate==5)
				if (x == X_BOUNDARY)
					return map[1][y-1];
				else
					return map[x-1][1];
			else
				return null;
		} else {
			// coordinate 2
			if (x + 1 >= Y_BOUNDARY && coordinate ==2)
					return map[1][y + 1];			
			else
				if (x + 1 <= Y_BOUNDARY && coordinate ==3)
					return map[X_BOUNDARY][y + 1];
			
			else 
				if (x - 1 <=0 && coordinate==5)
					return map[x-1][Y_BOUNDARY];
			else 
				if(x - 1 <= 0 && coordinate ==6)
					if(y-1==0)
						return map [X_BOUNDARY][Y_BOUNDARY];
					else
						return map[X_BOUNDARY][y-1];
			else
				return null;
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
		// map[cell.getX()][cell.getY()].setContent(new Ground());

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

	public void startRegistrationTime() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// Insert some method call here.
				try {
					Thread.sleep(MAX_REGISTRATION_TIME * 1000);
					registrationPeriod = false;

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		t.start();
	}

	/*
	 * 100 × cells_explored(i) + 500 × cities_owned(i) + 250 × stores_owned(i) + 300
	 * × farms_owned(i) + 400 × units_owned(i) + 10 × gold_owned(i) + 2 ×
	 * stone_owned(i) + 1 × wood_owned(i) + 5 × food_owned(i)
	 */
	public int calculateScore(Tribe tribe) {

		int score = ((100 * tribe.getDiscoveredCells().size()) + (500 * tribe.getCities().size())
				+ (250 * tribe.getBuildingsByType(STORE).size()) + (300 * tribe.getBuildingsByType(FARM).size())
				+ (400 * tribe.getMemberSize()) + (10 * tribe.getGold()) + (2 * tribe.getStones()) + (tribe.getWood())
				+ (5 * tribe.getFood()));

		Printer.printSuccess("Team: " + tribe.getTeamNumber(), "THE CURRENT SCORE IS= " + score);
		return score;
	}

	/*
	 * cells_explored: Number of cells explored by the tribe. - cities_owned: Number
	 * of cities owned by the tribe. - stores_owned: Number of stores owned by the
	 * tribe. - farms_owned: Number of farm owned by the tribe. - units_owned:
	 * Number of units owned by the tribe. - gold_owned: Amount of gold owned by the
	 * tribe. - stone_owned: Amount of stone owned by the tribe. - wood_owned:
	 * Amount of wood owned by the tribe. - food_owned: Amount of food owned by the
	 * tribe.
	 */
	public void printFinalScore() {
		for (Tribe tribe : tribes) {
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL NUMBER OF CELLS EXPORED= " + tribe.getDiscoveredCells().size());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL NUMBER OF CITIES OWNED= " + tribe.getCities().size());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL NUMBER OF STORES OWNED= " + tribe.getBuildingsByType(STORE).size());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL NUMBER OF FARMS OWNED= " + tribe.getBuildingsByType(FARM).size());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL NUMBER OF UNITS OWNED= " + tribe.getUnits().size());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL AMOUNT OF GOLD OWNED= " + tribe.getGold());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL AMOUNT OF STONES OWNED= " + tribe.getStones());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL AMOUNT OF WOOD OWNED= " + tribe.getWood());
			Printer.printSuccess("Team: " + tribe.getTeamNumber(),
					"THE FINAL AMOUNT OF FOOD OWNED= " + tribe.getFood());
			calculateScore(tribe);
		}
	}

}