package es.upm.woa.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
 * Protege name: Cell
 * 
 * @author ontology bean generator
 * @version 2019/03/22, 10:37:19
 */
//public class Cell implements Concept {

	/**
	 * Protege name: owner
	 */
	/*
	private int owner;

	public void setOwner(int value) {
		this.owner = value;
	}

	public int getOwner() {
		return this.owner;
	}

	/**
	 * Protege name: content
	 */
	/*
	private String content;

	public void setContent(String value) {
		this.content = value;
	}

	public String getContent() {
		return this.content;
	}

	/**
	 * Protege name: y
	 */
/*private int y;

	public void setY(int value) {
		this.y = value;
	}

	public int getY() {
		return this.y;
	}

	/**
	 * Protege name: x
	 */
/*private int x;

	public void setX(int value) {
		this.x = value;
	}

	public int getX() {
		return this.x;
	}

}*/



public class Cell implements Concept {

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
	* Protege name: content
	   */
	   private List content = new ArrayList();
	   public void addContent(Object elem) { 
	     List oldList = this.content;
	     content.add(elem);
	   }
	   public boolean removeContent(Object elem) {
	     List oldList = this.content;
	     boolean result = content.remove(elem);
	     return result;
	   }
	   public void clearAllContent() {
	     List oldList = this.content;
	     content.clear();
	   }
	   public Iterator getAllContent() {return content.iterator(); }
	   public List getContent() {return content; }
	   public void setContent(List l) {content = l; }

	   /**
	* Protege name: owner
	   */
	   private AID owner;
	   public void setOwner(AID value) { 
	    this.owner=value;
	   }
	   public AID getOwner() {
	     return this.owner;
	   }

	}
