package es.upm.woa.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: MoveToCell
* @author ontology bean generator
* @version 2019/04/5, 19:32:22
*/
public class MoveToCell implements AgentAction {

   /**
* Protege name: target
   */
   private Cell target;
   public void setTarget(Cell value) { 
    this.target=value;
   }
   public Cell getTarget() {
     return this.target;
   }

}
