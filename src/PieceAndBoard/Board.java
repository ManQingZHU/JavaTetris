package PieceAndBoard;

public class Board	{
	private int width;
	private int height;
	private boolean[][] grid;
	private int[] widths;
	private int[] heights;
	private int maxHeight;
	private boolean DEBUG = true;
	boolean committed;
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;
		
		widths = new int[height];
		heights = new int[width];
		maxHeight = 0;
		
		
		// for backup
		xGrid = new boolean[width][height];
		xWidths = new int[height];
		xHeights = new int[width];
		xMaxHeight = 0;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return maxHeight;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			int cnt = 0;
			int maxCnt = 0;
			for(int i = 0; i < width; ++i)
			{
				cnt = 0;
				for(int j = height-1; j >= 0; --j)
					if(grid[i][j]) {
						cnt = j + 1;
						break;
					}
				
				if(heights[i] != cnt)
					throw new RuntimeException("heights[] inconsist!");
				
				maxCnt = Math.max(maxCnt, heights[i]);
			}
			if(maxCnt != maxHeight)
				throw new RuntimeException("maxHeight inconsis!");
			
			for(int j = 0; j < height; ++j)
			{
				cnt = 0;
				for(int i = 0; i < width; ++i)
					if(grid[i][j])
						cnt++;
				if(widths[j] != cnt)
					throw new RuntimeException("widths[] inconsist!");
			}
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int ret = 0;
		int[] dy = piece.getSkirt();
		for(int i = 0; i < piece.getWidth(); ++i)
			ret = Math.max(ret, heights[x+i]-dy[i]);
		return ret; 
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x];
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		return widths[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(inRangeX(x) && inRangeY(y))
			return grid[x][y];
		return true;
	}
	
	private boolean inRangeX(int x) {
		return x >= 0 && x < width;
	}
	
	private boolean inRangeY(int y) {
		return y >= 0 && y < height;
	}
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		if (!committed) throw new RuntimeException("place commit problem");
		
		backup();
		
		int result = PLACE_OK;
		TPoint[] body = piece.getBody();
		for(TPoint tp: body)
		{
			int tx = tp.x+x; 
			int ty = tp.y+y;
			if(!inRangeX(tx) || !inRangeY(ty))
				return PLACE_OUT_BOUNDS;
			if(grid[tx][ty])
				return PLACE_BAD;
			
			grid[tx][ty] = true;
			
			widths[ty]++;
			if(fullRow(ty)) result = PLACE_ROW_FILLED;
			heights[tx] = Math.max(heights[tx], ty+1);
			maxHeight = Math.max(maxHeight, ty+1);
		}
		sanityCheck();
		return result;
	}
	
	private boolean fullRow(int y)
	{
		return widths[y] == width;
	}
	
	private boolean emptyRow(int y) {
		return widths[y] == 0;
	}
	
	private void emptyThisRow(int y)
	{
		for(int i = 0; i < width; ++i)
			grid[i][y] = false;

		widths[y] = 0;
	}

	private void fallDown(int to, int from)
	{
		widths[to] = widths[from];
		widths[from] = 0;
		
		for(int i = 0; i < width; ++i) {
			grid[i][to] = grid[i][from];
			grid[i][from] = false;
		}
	}

	private void recomputeHeights()
    {
        maxHeight = 0;
        for(int i = 0; i < width; ++i) {
            heights[i] = 0;
            for (int j = height - 1; j >= 0; --j)
                if(grid[i][j])
                {
                    heights[i] = j+1;
                    break;
                }
            maxHeight = Math.max(maxHeight, heights[i]);
        }
    }

	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		backup();
		int rowsCleared = 0;
		int to, from, len = getMaxHeight();
		for(int i = 0; i <= len; ++i)
			if(fullRow(i))
			{
				emptyThisRow(i);
				rowsCleared++;
			}
		
		for(to= 0, from = 0; to <= len; ++to)
		{
			if(!emptyRow(to))
				continue;
			
			from = (to+1) > (from+1) ? (to+1):(from+1);
			while(from <= len && emptyRow(from))
				from++;
			if(from > len) break;
			fallDown(to, from);
		}
		recomputeHeights();

		sanityCheck();
		return rowsCleared;
	}
	
	private boolean[][] xGrid;
	private int[] xWidths;
	private int[] xHeights;
	private int xMaxHeight;
	
	private void backup() {
		if(committed)
		{
			System.arraycopy(widths, 0, xWidths, 0, widths.length);
			System.arraycopy(heights, 0, xHeights, 0, heights.length);
			for(int i  = 0; i < width; ++i)
				System.arraycopy(grid[i], 0, xGrid[i], 0, grid[i].length);
			xMaxHeight = maxHeight;
			committed = false;
		}
	}
	
	private static final int WIDTHS = 0;
	private static final int HEIGHTS = 1;
	private static final int GRID = 2;
	
	private void undoSwap(int index) {
		switch (index) {
		case WIDTHS:
			int[] tmp0 = widths;
			widths = xWidths;
			xWidths = tmp0;
			break;
		
		case HEIGHTS:
			int[] tmp1 = heights;
			heights = xHeights;
			xHeights = tmp1;
			break;
		
		case GRID:
			boolean[][] tmp2 = grid;
			grid = xGrid;
			xGrid = tmp2;
			break;
			
		default:
			break;
		}
	}
	
	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(!committed) {
			undoSwap(WIDTHS);
			undoSwap(HEIGHTS);
			undoSwap(GRID);
			maxHeight = xMaxHeight;
			
			sanityCheck();
			commit();
		}
	}
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


