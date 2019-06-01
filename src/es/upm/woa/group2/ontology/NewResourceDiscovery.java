package es.upm.woa.group2.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NewResourceDiscovery
* @author ontology bean generator
* @version 2019/06/1, 19:01:09
*/
public class NewResourceDiscovery implements AgentAction {

   /**
* Protege name: Cells
   */
   private List cells = new ArrayList();
   public void addCells(CellGroup2 elem) { 
     List oldList = this.cells;
     cells.add(elem);
   }
   public boolean removeCells(CellGroup2 elem) {
     List oldList = this.cells;
     boolean result = cells.remove(elem);
     return result;
   }
   public void clearAllCells() {
     List oldList = this.cells;
     cells.clear();
   }
   public Iterator getAllCells() {return cells.iterator(); }
   public List getCells() {return cells; }
   public void setCells(List l) {cells = l; }

}
