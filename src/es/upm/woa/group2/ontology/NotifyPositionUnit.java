package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NotifyPositionUnit
* @author ontology bean generator
* @version 2019/06/1, 19:01:09
*/
public class NotifyPositionUnit implements AgentAction {

   /**
* Protege name: cell_position
   */
   private CellGroup2 cell_position;
   public void setCell_position(CellGroup2 value) { 
    this.cell_position=value;
   }
   public CellGroup2 getCell_position() {
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
