package PieceAndBoard;

import PieceAndBoard.Board;

import org.junit.Test;

import junit.framework.TestCase;


public class BoardTest extends TestCase {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;
	Piece sti, sti_r;
	
	protected void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		sti = new Piece(Piece.STICK_STR);
		sti_r = sti.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	// Makre  more tests, by putting together longer series of 
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.
	
	public void testClearRows() {
		b.commit();
		b.place(sRotated, 1, 1);
		b.commit();
		int result = b.place(sti, 0, 1);

//		System.out.println(b.toString());
//		System.out.println();
		
		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(5, b.getMaxHeight());
		assertEquals(5, b.getColumnHeight(0));
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(3));
		assertEquals(1, b.getRowWidth(4));
		
		
		
		result = b.clearRows();
		
//		System.out.println(b.toString());
//		System.out.println();
		
		assertEquals(3, result);
		assertEquals(2, b.getMaxHeight());
		assertEquals(2, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(1, b.getColumnHeight(1));
		assertEquals(0, b.getColumnHeight(2));
		
		
	}
	
	// test for undo, the second undo shouldn't have any effect
	public void testUndo1() {
		b.commit();
		b.place(sRotated, 1, 1);
		b.commit();
		b.place(sti, 0, 1);
		b.commit();
		b.clearRows();
		b.undo();
		
//		System.out.println(b.toString());
//		System.out.println();
		
		assertEquals(5, b.getMaxHeight());
		assertEquals(5, b.getColumnHeight(0));
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(3));
		assertEquals(1, b.getRowWidth(4));
		
		b.undo();
		
//		System.out.println(b.toString());
//		System.out.println();
		
		assertEquals(5, b.getMaxHeight());
		assertEquals(5, b.getColumnHeight(0));
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(3));
		assertEquals(1, b.getRowWidth(4));
	}
	
	// test for undo, for place/clearRows/undo
	// it will go back before place
	public void testUndo2() {
		b.commit();
		b.place(sRotated, 1, 1);
		b.commit();
		b.place(sti, 0, 1);
//		b.commit();
		b.clearRows();
		b.undo();
		
//		System.out.println(b.toString());
//		System.out.println();
		
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	public void testStrangePlace() {
		b.commit();
		b.place(sRotated, 1, 1);
		b.commit();
		int result = b.place(sti_r, 0, 4);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
		b.undo();
		
		result = b.place(pyr3, 0, 0);
		assertEquals(Board.PLACE_BAD, result);
		b.undo();
		
//		System.out.println(b.toString());
//		System.out.println();
	}
	
	public void testDropHeight() {
		b.commit();
		b.place(sRotated, 1, 1);
		b.commit();
		assertEquals(1, b.dropHeight(sti, 0));
		assertEquals(4, b.dropHeight(pyr3, 0));
		assertEquals(3, b.dropHeight(pyr2, 1));
	}

	@Test
	public void testRunRuntimeException() {
		try {
			b.place(sRotated, 1, 1);
			fail("Should have thrown runtime exception but did not!");
		}
		catch(RuntimeException e) {
			final String msg = "place commit problem";
			assertEquals(msg, e.getMessage());
		}
	}


}
