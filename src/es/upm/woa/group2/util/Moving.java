package es.upm.woa.group2.util;

import es.upm.woa.ontology.Cell;

public class Moving {

public Cell getTargetPosition(Cell currentPosition, int nextMove,
		Cell map[][], int Y_BOUNDARY, int X_BOUNDARY  ) {
		
		Cell targetPosition = null;
		int x = currentPosition.getX();
		int y = currentPosition.getY();
		switch (nextMove) {
		case 1:
			if(x-2>=1)
				targetPosition = map[x-2][y];
			break;
		case 2:
			if(x-->=1 && y++<=Y_BOUNDARY)
				targetPosition = map[x][y];
			break;	
		case 3:
			if(x++<=X_BOUNDARY && y++<=Y_BOUNDARY)
				targetPosition = map[x][y];
			break;
		case 4:
			if(x+2<=X_BOUNDARY)
				targetPosition = map[x+2][y];
			break;
		case 5:
			if(x++<=X_BOUNDARY && y-->=1)
				targetPosition = map[x][y];
			break;	
		default:
			if(x-->=1 && y-->=1)
				targetPosition = map[x][y];
			break;
		}
		
		return targetPosition;
	}
	
}
