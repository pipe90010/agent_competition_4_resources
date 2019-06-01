package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CellGroup2
* @author ontology bean generator
* @version 2019/06/1, 19:01:09
*/
public class CellGroup2 implements Concept {

   /**
* Protege name: goldPercentage
   */
   private int goldPercentage;
   public void setGoldPercentage(int value) { 
    this.goldPercentage=value;
   }
   public int getGoldPercentage() {
     return this.goldPercentage;
   }

   /**
* Protege name: y
   */
   private int y;
   public void setY(int value) { 
    this.y=value;
   }
   public int getY() {
     return this.y;
   }

   /**
* Protege name: x
   */
   private int x;
   public void setX(int value) { 
    this.x=value;
   }
   public int getX() {
     return this.x;
   }

   /**
* Protege name: resourceType
   */
   private String resourceType;
   public void setResourceType(String value) { 
    this.resourceType=value;
   }
   public String getResourceType() {
     return this.resourceType;
   }

   /**
* Protege name: resourceAmount
   */
   private int resourceAmount;
   public void setResourceAmount(int value) { 
    this.resourceAmount=value;
   }
   public int getResourceAmount() {
     return this.resourceAmount;
   }

}
