package es.upm.woa.ontology;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.util.leap.HashMap;
import jade.content.lang.Codec;
import jade.core.CaseInsensitiveString;

/** file: GameOntology.java
 * @author ontology bean generator
 * @version 2019/04/5, 19:32:22
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
    public static final String NOTIFYNEWCELLDISCOVERY_NEWCELL="newCell";
    public static final String NOTIFYNEWCELLDISCOVERY="NotifyNewCellDiscovery";
    public static final String NOTIFYNEWUNIT_NEWUNIT="newUnit";
    public static final String NOTIFYNEWUNIT_LOCATION="location";
    public static final String NOTIFYNEWUNIT="NotifyNewUnit";
    public static final String MOVETOCELL_TARGET="target";
    public static final String MOVETOCELL="MoveToCell";
    public static final String CREATEUNIT="CreateUnit";
    public static final String RESOURCE="Resource";
    public static final String BUILDING_OWNER="owner";
    public static final String BUILDING_TYPE="type";
    public static final String BUILDING="Building";
    public static final String EMPTY="Empty";
    public static final String CELL_OWNER="owner";
    public static final String CELL_CONTENT="content";
    public static final String CELL_Y="y";
    public static final String CELL_X="x";
    public static final String CELL="Cell";

  /**
   * Constructor
  */
  private GameOntology(){ 
    super(ONTOLOGY_NAME, BasicOntology.getInstance());
    try { 

    // adding Concept(s)
    ConceptSchema cellSchema = new ConceptSchema(CELL);
    add(cellSchema, es.upm.woa.ontology.Cell.class);
    ConceptSchema emptySchema = new ConceptSchema(EMPTY);
    add(emptySchema, es.upm.woa.ontology.Empty.class);
    ConceptSchema buildingSchema = new ConceptSchema(BUILDING);
    add(buildingSchema, es.upm.woa.ontology.Building.class);
    ConceptSchema resourceSchema = new ConceptSchema(RESOURCE);
    add(resourceSchema, es.upm.woa.ontology.Resource.class);

    // adding AgentAction(s)
    AgentActionSchema createUnitSchema = new AgentActionSchema(CREATEUNIT);
    add(createUnitSchema, es.upm.woa.ontology.CreateUnit.class);
    AgentActionSchema moveToCellSchema = new AgentActionSchema(MOVETOCELL);
    add(moveToCellSchema, es.upm.woa.ontology.MoveToCell.class);
    AgentActionSchema notifyNewUnitSchema = new AgentActionSchema(NOTIFYNEWUNIT);
    add(notifyNewUnitSchema, es.upm.woa.ontology.NotifyNewUnit.class);
    AgentActionSchema notifyNewCellDiscoverySchema = new AgentActionSchema(NOTIFYNEWCELLDISCOVERY);
    add(notifyNewCellDiscoverySchema, es.upm.woa.ontology.NotifyNewCellDiscovery.class);

    // adding AID(s)

    // adding Predicate(s)


    // adding fields
    cellSchema.add(CELL_X, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
    cellSchema.add(CELL_Y, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
    cellSchema.add(CELL_CONTENT, new ConceptSchema("Concept"), 0, ObjectSchema.UNLIMITED);
    cellSchema.add(CELL_OWNER, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    buildingSchema.add(BUILDING_TYPE, (TermSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
    buildingSchema.add(BUILDING_OWNER, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    moveToCellSchema.add(MOVETOCELL_TARGET, cellSchema, ObjectSchema.MANDATORY);
    notifyNewUnitSchema.add(NOTIFYNEWUNIT_LOCATION, cellSchema, ObjectSchema.MANDATORY);
    notifyNewUnitSchema.add(NOTIFYNEWUNIT_NEWUNIT, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    notifyNewCellDiscoverySchema.add(NOTIFYNEWCELLDISCOVERY_NEWCELL, cellSchema, ObjectSchema.MANDATORY);

    // adding name mappings

    // adding inheritance

   }catch (java.lang.Exception e) {e.printStackTrace();}
  }
  }
