package PieceAndBoard;// PieceAndBoard.Piece.java

import java.util.*;

public class Piece {
	private TPoint[] body;
	private int[] skirt;
	private int width;
	private int height;
	private Piece next; // "next" rotation

	static private Piece[] pieces;	// singleton static array of first rotations

	/**
	 Defines a new piece given a PieceAndBoard.TPoint[] array of its body.
	 Makes its own copy of the array and the TPoints inside it.
	 Compute the width, height and skirt as well.
	*/
	public Piece(TPoint[] points) {
		body = new TPoint[points.length];
		width = 0; height = 0;
		for(int i = 0; i < points.length; ++i)
		{
			body[i] = new TPoint(points[i]);
			width = Math.max(width, points[i].x);
			height = Math.max(height, points[i].y);
		}
		width++; height++;
		computeSkirt();
		next = null;
	}
	
	// compute the skirt array
	private void computeSkirt() {
		skirt = new int[width];
		for(int i = 0; i < width; ++i)
			skirt[i] = height;
		
		for(TPoint tp: body)
		{
			skirt[tp.x] = Math.min(skirt[tp.x], tp.y);
		}
	}
	
	
	/**
	 * Alternate constructor, takes a String with the x,y body points
	 * all separated by spaces, such as "0 0  1 0  2 0	1 1".
	 * (provided)
	 */
	public Piece(String points) {
		this(parsePoints(points));
	}

	/**
	 Returns the width of the piece measured in blocks.
	*/
	public int getWidth() {
		return width;
	}

	/**
	 Returns the height of the piece measured in blocks.
	*/
	public int getHeight() {
		return height;
	}

	/**
	 Returns a pointer to the piece's body. The caller
	 should not modify this array.
	*/
	public TPoint[] getBody() {
		return body;
	}

	/**
	 Returns a pointer to the piece's skirt. For each x value
	 across the piece, the skirt gives the lowest y value in the body.
	 This is useful for computing where the piece will land.
	 The caller should not modify this array.
	*/
	public int[] getSkirt() {
		return skirt;
	}

	
	/**
	 Returns a new piece that is 90 degrees counter-clockwise
	 rotated from the receiver.
	 */
	public Piece computeNextRotation() {
		TPoint[] other = new TPoint[body.length];
		for(int i = 0; i < body.length; ++i)
			other[i] = new TPoint(height-1-body[i].y, body[i].x);
		return new Piece(other);
	}
	

	/**
	 Returns a pre-computed piece that is 90 degrees counter-clockwise
	 rotated from the receiver.	 Fast because the piece is pre-computed.
	 This only works on pieces set up by makeFastRotations(), and otherwise
	 just returns null.
	*/	
	public Piece fastRotation() {
		return next;
	}
	


	/**
	 Returns true if two pieces are the same --
	 their bodies contain the same points.
	 Interestingly, this is not the same as having exactly the
	 same body arrays, since the points may not be
	 in the same order in the bodies. Used internally to detect
	 if two rotations are effectively the same.
	*/
	public boolean equals(Object obj) {
		if (obj == this) return true;

		if (!(obj instanceof Piece)) return false;
		Piece other = (Piece)obj;
		
		TPoint[] otherBody = other.getBody();
		if(otherBody.length != body.length)
			return false;
		
		HashSet<TPoint> otherTPset = new HashSet<TPoint> ();
		for(TPoint tp1: otherBody)
			otherTPset.add(tp1);
		for(TPoint tp2: body)
			if(!otherTPset.contains(tp2))
				return false;
		
		return true;
	}


	// String constants for the standard 7 tetris pieces
	public static final String STICK_STR	= "0 0	0 1	 0 2  0 3";
	public static final String L1_STR		= "0 0	0 1	 0 2  1 0";
	public static final String L2_STR		= "0 0	1 0 1 1	 1 2";
	public static final String S1_STR		= "0 0	1 0	 1 1  2 1";
	public static final String S2_STR		= "0 1	1 1  1 0  2 0";
	public static final String SQUARE_STR	= "0 0  0 1  1 0  1 1";
	public static final String PYRAMID_STR	= "0 0  1 0  1 1  2 0";
	
	// Indexes for the standard 7 pieces in the pieces array
	public static final int STICK = 0;
	public static final int L1	  = 1;
	public static final int L2	  = 2;
	public static final int S1	  = 3;
	public static final int S2	  = 4;
	public static final int SQUARE	= 5;
	public static final int PYRAMID = 6;
	
	/**
	 Returns an array containing the first rotation of
	 each of the 7 standard tetris pieces in the order
	 STICK, L1, L2, S1, S2, SQUARE, PYRAMID.
	 The next (counterclockwise) rotation can be obtained
	 from each piece with the {@link #fastRotation()} message.
	*/
	public static Piece[] getPieces() {
		if (Piece.pieces==null) {
			Piece.pieces = new Piece[] {
				makeFastRotations(new Piece(STICK_STR)),
				makeFastRotations(new Piece(L1_STR)),
				makeFastRotations(new Piece(L2_STR)),
				makeFastRotations(new Piece(S1_STR)),
				makeFastRotations(new Piece(S2_STR)),
				makeFastRotations(new Piece(SQUARE_STR)),
				makeFastRotations(new Piece(PYRAMID_STR)),
			};
		}
		
		return Piece.pieces;
	}
	


	/**
	 Given the "first" root rotation of a piece, computes all
	 the other rotations and links them all together
	 in a circular list. The list loops back to the root as soon
	 as possible. Returns the root piece. fastRotation() relies on the
	 pointer structure setup here.
	*/
	private static Piece makeFastRotations(Piece root) {
		Piece p = root.computeNextRotation(), q = root;
		while(!root.equals(p))
		{
			q.setNext(p);
			q = q.fastRotation();
			p = q.computeNextRotation();
		}
		q.setNext(root);
		return root; 
	}
	
	public void setNext(Piece next)
	{
		this.next = next;
	}

	/**
	 Given a string of x,y pairs ("0 0	0 1 0 2 1 0"), parses
	 the points into a PieceAndBoard.TPoint[] array.
	 (Provided code)
	*/
	private static TPoint[] parsePoints(String string) {
		List<TPoint> points = new ArrayList<TPoint>();
		StringTokenizer tok = new StringTokenizer(string);
		try {
			while(tok.hasMoreTokens()) {
				int x = Integer.parseInt(tok.nextToken());
				int y = Integer.parseInt(tok.nextToken());
				
				points.add(new TPoint(x, y));
			}
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse x,y string:" + string);
		}

		TPoint[] array = points.toArray(new TPoint[0]);
		return array;
	}

}
