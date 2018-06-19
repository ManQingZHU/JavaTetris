package PieceAndBoard;

//PieceAndBoard.TPoint.java
/*
 This is just a trivial "struct" type class --
 it simply holds an int x/y point for use by Tetris,
 and supports equals() and toString().
 Allow public access to x/y, so this
 is not an object really.
 */
public class TPoint {
	public int x;
	public int y;

	// Creates a PieceAndBoard.TPoint based in int x,y
	public TPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	// Creates a PieceAndBoard.TPoint, copied from an existing PieceAndBoard.TPoint
	public TPoint(TPoint point) {
		this.x = point.x;
		this.y = point.y;
	}

	// Standard equals() override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof TPoint)) return false;

		TPoint pt = (TPoint)other;
		return(x==pt.x && y==pt.y);
	}

	// Standard toString() override, produce
	// human-readable String from object
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	// Standard hashCode() override
	public int hashCode() {
		return x*10+y;
	}
}
