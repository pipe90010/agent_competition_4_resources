package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NotifyBoundaries
* @author ontology bean generator
* @version 2019/06/1, 15:28:22
*/
public class NotifyBoundaries implements AgentAction {

   /**
* Protege name: y_axis
   */
   private int y_axis;
   public void setY_axis(int value) { 
    this.y_axis=value;
   }
   public int getY_axis() {
     return this.y_axis;
   }

   /**
* Protege name: x_axis
   */
   private int x_axis;
   public void setX_axis(int value) { 
    this.x_axis=value;
   }
   public int getX_axis() {
     return this.x_axis;
   }

}
