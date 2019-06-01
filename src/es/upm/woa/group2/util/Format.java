package es.upm.woa.group2.util;

import es.upm.woa.group2.ontology.CellGroup2;
import es.upm.woa.ontology.Building;
import es.upm.woa.ontology.Cell;
import es.upm.woa.ontology.Ground;
import es.upm.woa.ontology.Resource;
import jade.core.AID;

public class Format {

	public static CellGroup2 turnCellIntoCellGroup2(Cell cell)
	{
		CellGroup2 cellGroup2 = new CellGroup2();
		cellGroup2.setX(cell.getX());
		cellGroup2.setY(cell.getY());
		if(cell.getContent() instanceof Resource)
		{
			cellGroup2.setGoldPercentage(((Resource)cell.getContent()).getGoldPercentage());
			cellGroup2.setResourceAmount(((Resource)cell.getContent()).getResourceAmount());
			cellGroup2.setResourceType(((Resource)cell.getContent()).getResourceType());
		}
		else if (cell.getContent() instanceof Building)
		{
			cellGroup2.setBuildType(((Building)cell.getContent()).getType());
		}
		return cellGroup2;
	}
	
	public static Cell turnCellGroup2IntoCell(CellGroup2 cellGroup2, AID owner)
	{
		Cell cell = new Cell();
		cell.setX(cellGroup2.getX());
		cell.setY(cellGroup2.getY());
		if(cellGroup2.getResourceType()!=null || cellGroup2.getResourceAmount()>0)
		{
			Resource resource = new Resource();
			
			resource.setGoldPercentage(cellGroup2.getGoldPercentage());
			resource.setResourceAmount(cellGroup2.getResourceAmount());
			resource.setResourceType(cellGroup2.getResourceType());
			cell.setContent(resource);	
		}
		else if(cellGroup2.getBuildType()!=null)
		{
			Building building = new Building();
			
			building.setType(cellGroup2.getBuildType());
			building.setOwner(owner);
			cell.setContent(building);
		}
		else
		{
			cell.setContent(new Ground());
		}
			
		
		return cell;
	}
}
