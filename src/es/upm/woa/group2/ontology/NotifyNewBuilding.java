package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NotifyNewBuilding
* @author ontology bean generator
* @version 2019/06/1, 19:01:09
*/
public class NotifyNewBuilding implements AgentAction {

   /**
* Protege name: cell
   */
   private CellGroup2 cell;
   public void setCell(CellGroup2 value) { 
    this.cell=value;
   }
   public CellGroup2 getCell() {
     return this.cell;
   }

   /**
* Protege name: type
   */
   private String type;
   public void setType(String value) { 
    this.type=value;
   }
   public String getType() {
     return this.type;
   }

}
