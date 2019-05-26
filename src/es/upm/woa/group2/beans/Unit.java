package es.upm.woa.group2.beans;

import es.upm.woa.ontology.Cell;
import jade.core.AID;

public class Unit {

	// -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------
	
	public static final String UNIT="Unit";
	public static final String BUILDER_ROLE="Builder";
	public static final String EXPLOITER_ROLE="Exploiter";
	public static final String EXPLORER_ROLE="Explorer";
	
	// -----------------------------------------------------------------
    // Atributes
    // -----------------------------------------------------------------
	private AID id;
	private Cell position;
	private String action;
	private String role;
	
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
	public Unit(AID id, Cell position) {
		super();
		this.id = id;
		this.position = position;
		this.action = null;
	}
	
	public Unit(AID id) {
		super();
		this.id = id;
	}
	
	// -----------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------

	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}

	public Cell getPosition() {
		return position;
	}

	public void setPosition(Cell position) {
		this.position = position;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	

}
