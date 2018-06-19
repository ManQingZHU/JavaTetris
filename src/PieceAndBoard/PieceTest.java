package PieceAndBoard;

import PieceAndBoard.Piece;
import junit.framework.TestCase;

import java.util.*;

/*
  Unit test for PieceAndBoard.Piece class -- starter shell.
 */
public class PieceTest extends TestCase {
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated;
	private Piece l1, l2, l3, l4;

	protected void setUp() throws Exception {
		super.setUp();
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		l1 = new Piece(Piece.L2_STR);
		l2 = l1.computeNextRotation();
		l3 = l2.computeNextRotation();
		l4 = l3.computeNextRotation();
	}
	
	// Here are some sample tests to get you started
	
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
		
		assertEquals(3, s.getWidth());
		assertEquals(3, sRotated.getHeight());
		
		assertEquals(3, l2.getWidth());
		assertEquals(3, l1.getHeight());
		
	}
	
	
	// Test the skirt returned by a few pieces
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {1, 1,0}, l2.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0}, l1.getSkirt()));
	}
	
	public void testEquality() {
		Piece tmPiece = new Piece("0 0	1 2 1 0 1 1");
		
		assertTrue(l4.equals(l4));
		assertTrue(l1.equals(tmPiece));
		assertFalse(l2.equals(tmPiece));
		assertFalse(l3.equals(l2));
	}
	
	public void testFastRotation() {
		Piece[] pieces = Piece.getPieces();
		Piece stick = pieces[Piece.STICK];
		
		assertTrue(stick.equals(new Piece(Piece.STICK_STR)));
		assertEquals(1, stick.getWidth());
		assertEquals(4, stick.getHeight());
		
		Piece stRotated = stick.fastRotation();
		assertEquals(4, stRotated.getWidth());
		assertEquals(1, stRotated.getHeight());
		
		stRotated = stRotated.fastRotation();
		assertTrue(stick.equals(stRotated));
		
		Piece sq = pieces[Piece.SQUARE];
		Piece sqRotated = sq.fastRotation();
		
		assertTrue(sq.equals(sqRotated));
	}
	
}
