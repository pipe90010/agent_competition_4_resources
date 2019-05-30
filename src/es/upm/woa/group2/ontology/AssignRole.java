package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: AssignRole
* @author ontology bean generator
* @version 2019/05/28, 23:11:11
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
