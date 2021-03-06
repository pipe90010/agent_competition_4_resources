package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: AssignRole
* @author ontology bean generator
* @version 2019/06/1, 19:01:09
*/
public class AssignRole implements AgentAction {

   /**
* Protege name: role
   */
   private String role;
   public void setRole(String value) { 
    this.role=value;
   }
   public String getRole() {
     return this.role;
   }

   /**
* Protege name: totalUnitsByRole
   */
   private int totalUnitsByRole;
   public void setTotalUnitsByRole(int value) { 
    this.totalUnitsByRole=value;
   }
   public int getTotalUnitsByRole() {
     return this.totalUnitsByRole;
   }

   /**
* Protege name: location_Role
   */
   private CellGroup2 location_Role;
   public void setLocation_Role(CellGroup2 value) { 
    this.location_Role=value;
   }
   public CellGroup2 getLocation_Role() {
     return this.location_Role;
   }

   /**
* Protege name: id
   */
   private AID id;
   public void setId(AID value) { 
    this.id=value;
   }
   public AID getId() {
     return this.id;
   }

   /**
* Protege name: totalUnits
   */
   private int totalUnits;
   public void setTotalUnits(int value) { 
    this.totalUnits=value;
   }
   public int getTotalUnits() {
     return this.totalUnits;
   }

}
