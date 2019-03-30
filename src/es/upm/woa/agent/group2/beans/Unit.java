package es.upm.woa.agent.group2.beans;

import es.upm.woa.ontology.Cell;
import jade.core.AID;

public class Unit {

	// -----------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------
	
	public static final String UNIT="Unit";
	// -----------------------------------------------------------------
    // Atributes
    // -----------------------------------------------------------------
	private AID id;
	private Cell position;
	
	// -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------
	
	public Unit(AID id, Cell position) {
		super();
		this.id = id;
		this.position = position;
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
	
	

}
