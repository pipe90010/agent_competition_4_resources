package es.upm.woa.group2.ontology;


import es.upm.woa.ontology.Cell;
import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NotifyNewBuilding
* @author ontology bean generator
* @version 2019/06/1, 12:40:50
*/
public class NotifyNewBuilding implements AgentAction {

   /**
* Protege name: cell
   */
   private Cell cell;
   public void setCell(Cell value) { 
    this.cell=value;
   }
   public Cell getCell() {
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
