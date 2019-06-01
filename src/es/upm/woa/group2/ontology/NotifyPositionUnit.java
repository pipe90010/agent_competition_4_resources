package es.upm.woa.group2.ontology;

import es.upm.woa.ontology.Cell;
import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NotifyPositionUnit
* @author ontology bean generator
* @version 2019/06/1, 15:28:22
*/
public class NotifyPositionUnit implements AgentAction {

   /**
* Protege name: cell_position
   */
   private Cell cell_position;
   public void setCell_position(Cell value) { 
    this.cell_position=value;
   }
   public Cell getCell_position() {
     return this.cell_position;
   }

   /**
* Protege name: rol
   */
   private String rol;
   public void setRol(String value) { 
    this.rol=value;
   }
   public String getRol() {
     return this.rol;
   }

}
