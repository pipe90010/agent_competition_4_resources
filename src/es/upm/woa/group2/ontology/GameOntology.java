// file: GameOntology.java generated by ontology bean generator.  DO NOT EDIT, UNLESS YOU ARE REALLY SURE WHAT YOU ARE DOING!
package es.upm.woa.group2.ontology;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.util.leap.HashMap;
import jade.content.lang.Codec;
import jade.core.CaseInsensitiveString;

/** file: GameOntology.java
 * @author ontology bean generator
 * @version 2019/06/1, 19:01:09
 */
public class GameOntology extends jade.content.onto.Ontology  {
  //NAME
  public static final String ONTOLOGY_NAME = "game";
  // The singleton instance of this ontology
  private static ReflectiveIntrospector introspect = new ReflectiveIntrospector();
  private static Ontology theInstance = new GameOntology();
  public static Ontology getInstance() {
     return theInstance;
  }


   // VOCABULARY
    public static final String ASSIGNROLE_TOTALUNITS="totalUnits";
    public static final String ASSIGNROLE_ID="id";
    public static final String ASSIGNROLE_LOCATION_ROLE="location_Role";
    public static final String ASSIGNROLE_TOTALUNITSBYROLE="totalUnitsByRole";
    public static final String ASSIGNROLE_ROLE="role";
    public static final String ASSIGNROLE="AssignRole";
    public static final String NOTIFYPOSITIONUNIT_ROL="rol";
    public static final String NOTIFYPOSITIONUNIT_CELL_POSITION="cell_position";
    public static final String NOTIFYPOSITIONUNIT="NotifyPositionUnit";
    public static final String NOTIFYBOUNDARIES_X_AXIS="x_axis";
    public static final String NOTIFYBOUNDARIES_Y_AXIS="y_axis";
    public static final String NOTIFYBOUNDARIES="NotifyBoundaries";
    public static final String NEWRESOURCEDISCOVERY_CELLS="Cells";
    public static final String NEWRESOURCEDISCOVERY="NewResourceDiscovery";
    public static final String NOTIFYNEWBUILDING_TYPE="type";
    public static final String NOTIFYNEWBUILDING_CELL="cell";
    public static final String NOTIFYNEWBUILDING="NotifyNewBuilding";
    public static final String CELLGROUP2_RESOURCEAMOUNT="resourceAmount";
    public static final String CELLGROUP2_RESOURCETYPE="resourceType";
    public static final String CELLGROUP2_X="x";
    public static final String CELLGROUP2_Y="y";
    public static final String CELLGROUP2_GOLDPERCENTAGE="goldPercentage";
    public static final String CELLGROUP2="CellGroup2";

  /**
   * Constructor
  */
  private GameOntology(){ 
    super(ONTOLOGY_NAME, BasicOntology.getInstance());
    try { 

    // adding Concept(s)
    ConceptSchema cellGroup2Schema = new ConceptSchema(CELLGROUP2);
    add(cellGroup2Schema, es.upm.woa.group2.ontology.CellGroup2.class);

    // adding AgentAction(s)
    AgentActionSchema notifyNewBuildingSchema = new AgentActionSchema(NOTIFYNEWBUILDING);
    add(notifyNewBuildingSchema, es.upm.woa.group2.ontology.NotifyNewBuilding.class);
    AgentActionSchema newResourceDiscoverySchema = new AgentActionSchema(NEWRESOURCEDISCOVERY);
    add(newResourceDiscoverySchema, es.upm.woa.group2.ontology.NewResourceDiscovery.class);
    AgentActionSchema notifyBoundariesSchema = new AgentActionSchema(NOTIFYBOUNDARIES);
    add(notifyBoundariesSchema, es.upm.woa.group2.ontology.NotifyBoundaries.class);
    AgentActionSchema notifyPositionUnitSchema = new AgentActionSchema(NOTIFYPOSITIONUNIT);
    add(notifyPositionUnitSchema, es.upm.woa.group2.ontology.NotifyPositionUnit.class);
    AgentActionSchema assignRoleSchema = new AgentActionSchema(ASSIGNROLE);
    add(assignRoleSchema, es.upm.woa.group2.ontology.AssignRole.class);

    // adding AID(s)

    // adding Predicate(s)


    // adding fields
    cellGroup2Schema.add(CELLGROUP2_GOLDPERCENTAGE, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    cellGroup2Schema.add(CELLGROUP2_Y, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
    cellGroup2Schema.add(CELLGROUP2_X, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
    cellGroup2Schema.add(CELLGROUP2_RESOURCETYPE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    cellGroup2Schema.add(CELLGROUP2_RESOURCEAMOUNT, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    notifyNewBuildingSchema.add(NOTIFYNEWBUILDING_CELL, cellGroup2Schema, ObjectSchema.OPTIONAL);
    notifyNewBuildingSchema.add(NOTIFYNEWBUILDING_TYPE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    newResourceDiscoverySchema.add(NEWRESOURCEDISCOVERY_CELLS, cellGroup2Schema, 0, 500);
    notifyBoundariesSchema.add(NOTIFYBOUNDARIES_Y_AXIS, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    notifyBoundariesSchema.add(NOTIFYBOUNDARIES_X_AXIS, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    notifyPositionUnitSchema.add(NOTIFYPOSITIONUNIT_CELL_POSITION, cellGroup2Schema, ObjectSchema.OPTIONAL);
    notifyPositionUnitSchema.add(NOTIFYPOSITIONUNIT_ROL, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    assignRoleSchema.add(ASSIGNROLE_ROLE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    assignRoleSchema.add(ASSIGNROLE_TOTALUNITSBYROLE, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    assignRoleSchema.add(ASSIGNROLE_LOCATION_ROLE, cellGroup2Schema, ObjectSchema.OPTIONAL);
    assignRoleSchema.add(ASSIGNROLE_ID, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
    assignRoleSchema.add(ASSIGNROLE_TOTALUNITS, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);

    // adding name mappings

    // adding inheritance

   }catch (java.lang.Exception e) {e.printStackTrace();}
  }
  }
