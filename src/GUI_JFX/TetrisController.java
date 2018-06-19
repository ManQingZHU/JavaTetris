package GUI_JFX;

import PieceAndBoard.Board;
import PieceAndBoard.Piece;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class TetrisController implements Initializable {
    // size of the board in blocks
    public static final int pixels = 20;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;

//	public static final int WIDTH = 6;
//	public static final int HEIGHT = 2;

    // Extra blocks at the top for pieces to start.
    // If a piece is sticking up into this area
    // when it has landed -- game over!
    public static final int TOP_SPACE = 4;

    // When this is true, plays a fixed sequence of 100 pieces
    protected boolean testMode = false;
    public final int TEST_LIMIT = 100;

    // PieceAndBoard.Board data structures
    protected Board board;
    protected Piece[] pieces;


    // The current piece in play or null
    protected Piece currentPiece;
    protected int currentX;
    protected int currentY;
    protected boolean moved;	// did the player move the piece


    // The piece we're thinking about playing
    // -- set by computeNewPosition
    // (storing this in ivars is slightly questionable style)
    protected Piece newPiece;
    protected int newX;
    protected int newY;

    // State of the game
    protected boolean gameOn;	// true if we are playing
    protected int count;		 // how many pieces played so far
    protected long startTime;	// used to measure elapsed time
    protected Random random;	 // the random generator for new pieces

    protected int score;
    protected Timer timer;
    public final int DELAY = 400;

    @FXML
    protected Pane gameFrame, controlFrame, tetrisFrame;

    @FXML
    protected JFXToggleButton startButton;

    @FXML
    protected JFXSlider speedSlider;

    @FXML
    protected JFXCheckBox testCheckBox;

    @FXML
    protected Label countLabel, scoreLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createTetrisFrame();
    }

    public void createTetrisFrame() {
        tetrisFrame.setPrefSize((WIDTH * pixels)+2, (HEIGHT+TOP_SPACE)*pixels+2);
        gameOn = false;

        pieces = Piece.getPieces();
        board = new Board(WIDTH, HEIGHT + TOP_SPACE);
        paintBoard();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->tick(DOWN));
            }
        }, 0, DELAY);

        gameFrame.requestFocus();
    }
    
    @FXML
    protected void handleKeyPressed(KeyEvent keyEvent) {
        switch(keyEvent.getCode().getChar())
        {
            case "J": case "4":
                tick(LEFT);
                break;
            case "L": case "6":
                tick(RIGHT);
                break;
            case "K": case"5":
                tick(ROTATE);
                break;
            case "N": case "0":
                tick(DROP);
                break;
                default:;
        }

    }

    @FXML
    protected void switchGameState(MouseEvent mouseEvent) {
        if(startButton.isSelected())
            startGame();
        else stopGame();
    }

    /**
     Figures a new position for the current piece
     based on the given verb (LEFT, RIGHT, ...).
     The board should be in the committed state --
     i.e. the piece should not be in the board at the moment.
     This is necessary so dropHeight() may be called without
     the piece "hitting itself" on the way down.

     Sets the ivars newX, newY, and newPiece to hold
     what it thinks the new piece position should be.
     (Storing an intermediate result like that in
     ivars is a little tacky.)
     */
    public void computeNewPosition(int verb) {
        // As a starting point, the new position is the same as the old
        newPiece = currentPiece;
        newX = currentX;
        newY = currentY;

        // Make changes based on the verb
        switch (verb) {
            case LEFT: newX--; break;

            case RIGHT: newX++; break;

            case ROTATE:
                newPiece = newPiece.fastRotation();

                // tricky: make the piece appear to rotate about its center
                // can't just leave it at the same lower-left origin as the
                // previous piece.
                newX = newX + (currentPiece.getWidth() - newPiece.getWidth())/2;
                newY = newY + (currentPiece.getHeight() - newPiece.getHeight())/2;
                break;

            case DOWN: newY--; break;

            case DROP:
                newY = board.dropHeight(newPiece, newX);

                // trick: avoid the case where the drop would cause
                // the piece to appear to move up
                if (newY > currentY) {
                    newY = currentY;
                }
                break;

            default:
                throw new RuntimeException("Bad verb");
        }

    }

    public static final int ROTATE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DROP = 3;
    public static final int DOWN = 4;

    /**
     Called to change the position of the current piece.
     Each key press calls this once with the verbs
     LEFT RIGHT ROTATE DROP for the user moves,
     and the timer calls it with the verb DOWN to move
     the piece down one square.

     Before this is called, the piece is at some location in the board.
     This advances the piece to be at its next location.

     Overriden by the brain when it plays.
     */
    public void tick(int verb) {
        if (!gameOn) return;

        if (currentPiece != null) {
            board.undo();	// remove the piece from its old position
        }

        // Sets the newXXX ivars
        computeNewPosition(verb);

        // try out the new position (rolls back if it doesn't work)
        int result = setCurrent(newPiece, newX, newY);

        // if row clearing is going to happen, draw the
        // whole board so the green row shows up
        if (result ==  Board.PLACE_ROW_FILLED) {
            repaint();
        }

        boolean failed = (result >= Board.PLACE_OUT_BOUNDS);

        // if it didn't work, put it back the way it was
        if (failed) {
            if (currentPiece != null) board.place(currentPiece, currentX, currentY);
                repaint();
        }

        /*
		 How to detect when a piece has landed:
		 if this move hits something on its DOWN verb,
		 and the previous verb was also DOWN (i.e. the player was not
		 still moving it),	then the previous position must be the correct
		 "landed" position, so we're done with the falling of this piece.
		*/
        if (failed && verb==DOWN && !moved) {	// it's landed

            int cleared = board.clearRows();
            if (cleared > 0) {
                // score goes up by 5, 10, 20, 40 for row clearing
                // clearing 4 gets you a beep!
                switch (cleared) {
                    case 1: score += 5;	 break;
                    case 2: score += 10;  break;
                    case 3: score += 20;  break;
                    case 4: score += 40; break;
                    default: score += 50;  // could happen with non-standard pieces
                }
                updateCounters();
                repaint();	// repaint to show the result of the row clearing
            }


            // if the board is too tall, we've lost
            if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
                stopGame();
            }
            // Otherwise add a new piece and keep playing
            else {
                addNewPiece();
            }
        }

        // Note if the player made a successful non-DOWN move --
        // used to detect if the piece has landed on the next tick()
        moved = (!failed && verb!=DOWN);
    }

    protected void updateCounters() {
        countLabel.setText("Pieces: " + count);
        scoreLabel.setText("Score: " + score);
    }

    /**
     Sets the internal state and starts the timer
     so the game is happening.
     */
    public void startGame() {
        // cheap way to reset the board state
        board = new Board(WIDTH, HEIGHT + TOP_SPACE);

        // draw the new board state once
        repaint();

        count = 0;
        score = 0;
        updateCounters();

        gameOn = true;

        // Set mode based on checkbox at start of game
        testMode = testCheckBox.isSelected();

        if (testMode) random = new Random(0);	// same seq every time
        else random = new Random(); // diff seq each game

        addNewPiece();
        startTime = System.currentTimeMillis();
    }

    public void stopGame() {
        gameOn = false;
        startButton.setSelected(false);

        // open summary window
        long delta = (System.currentTimeMillis() - startTime)/10;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Summary.fxml"));
        Parent summaryRoot = null;
        try {
            summaryRoot = (Parent) fxmlLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        summaryController controller = fxmlLoader.<summaryController>getController();
        controller.setTimeLabel(delta);
        controller.setCountLabel(count);
        controller.setScoreLabel(score);
        Scene summaryScene = new Scene(summaryRoot);
        Stage summaryStage = new Stage();
        summaryStage.setScene(summaryScene);
        summaryStage.show();
    }

    /**
     Selects the next piece to use using the random generator
     set in startGame().
     */
    public Piece pickNextPiece() {
        int pieceNum;

        pieceNum = (int) (pieces.length * random.nextDouble());

        Piece piece	 = pieces[pieceNum];

        return(piece);
    }


    /**
     Tries to add a new random piece at the top of the board.
     Ends the game if it's not possible.
     */
    public void addNewPiece() {
        count++;
        score++;

        if (testMode && count == TEST_LIMIT+1) {
            stopGame();
            return;
        }

        // commit things the way they are
        board.commit();
        currentPiece = null;

        Piece piece = pickNextPiece();

        if(piece == null)
        {
            stopGame();
            return;
        }

        // Center it up at the top
        int px = (board.getWidth() - piece.getWidth())/2;
        int py = board.getHeight() - piece.getHeight();

        // add the new piece to be in play
        int result = setCurrent(piece, px, py);

        // This probably never happens, since
        // the blocks at the top allow space
        // for new pieces to at least be added.
        if (result>Board.PLACE_ROW_FILLED) {
            stopGame();
        }

        updateCounters();
    }

    /**
     Given a piece, tries to install that piece
     into the board and set it to be the current piece.
     Does the necessary repaints.
     If the placement is not possible, then the placement
     is undone, and the board is not changed. The board
     should be in the committed state when this is called.
     Returns the same error code as PieceAndBoard.Board.place().
     */
    public int setCurrent(Piece piece, int x, int y) {
        int result = board.place(piece, x, y);

        if (result <= Board.PLACE_ROW_FILLED) { // SUCESS
            // repaint the rect where it used to be
            if (currentPiece != null)
                repaint();
            currentPiece = piece;
            currentX = x;
            currentY = y;
            // repaint the rect where it is now
            repaint();
        }
        else {
            board.undo();
        }

        return(result);
    }

    // width in pixels of a block
    private final float dX() {
        return( ((float)(tetrisFrame.getPrefWidth()-2)) / board.getWidth() );
    }

    // height in pixels of a block
    private final float dY() {
        return( ((float)(tetrisFrame.getPrefHeight()-2)) / board.getHeight() );
    }

    // the x pixel coord of the left side of a block
    private final int xPixel(int x) {
        return(Math.round(1 + (x * dX())));
    }

    // the y pixel coord of the top of a block
    private final int yPixel(int y) {
        return (int)(Math.round(tetrisFrame.getPrefHeight() -1 - (y+1)*dY()));
    }

    protected static final Color TETRIS_BLACK = Color.rgb(72,72,62);
    protected static final Color TETRIS_WHITE = Color.rgb(248,248,248);
    protected static final Color TETRIS_GREEN = Color.rgb(14,150,84);
    protected static final Color TETRIS_BLUE = Color.rgb(105,160,210);
    protected static final Color TETRIS_PURPLE = Color.rgb(82,100,174);

    public void paintBoard() {
        Rectangle boardEdge = new Rectangle(0, 0, tetrisFrame.getPrefWidth()-1, tetrisFrame.getPrefHeight()-1);
        boardEdge.setFill(Color.TRANSPARENT);
        boardEdge.setStroke(TETRIS_WHITE);

        int spacerY = yPixel(board.getHeight() - TOP_SPACE - 1);
        Line sepLine = new Line(0, spacerY, tetrisFrame.getPrefWidth()-1, spacerY);
        sepLine.setStroke(TETRIS_WHITE);
        tetrisFrame.getChildren().add(boardEdge);
        tetrisFrame.getChildren().add(sepLine);
    }

    public void paintTetris() {
        paintBoard();

        // Factor a few things out to help the optimizer
        final int dx = Math.round(dX()-2);
        final int dy = Math.round(dY()-2);
        final int bWidth = board.getWidth();

        int x, y;
        // Loop through and draw all the blocks
        // left-right, bottom-top
        for (x=0; x<bWidth; x++) {
            int left = xPixel(x);    // the left pixel

            // right pixel (useful for clip optimization)
            int right = xPixel(x + 1) - 1;
            Rectangle brick = null;
            // draw from 0 up to the col height
            final int yHeight = board.getColumnHeight(x);
            for (y = 0; y < yHeight; y++) {
                if (board.getGrid(x, y)) {
                    boolean filled = (board.getRowWidth(y) == bWidth);
                    brick = new Rectangle(left + 1, yPixel(y) + 1, dx, dy); // +1 to leave a white border
                    if (filled) {
                        brick.setFill(TETRIS_GREEN);
                    }
                    else brick.setFill(TETRIS_BLUE);
                    tetrisFrame.getChildren().add(brick);
                }
            }
        }
    }

    public void repaint() {
        tetrisFrame.getChildren().clear();
        paintTetris();
    }

    private final static int initialSpeedValue = 75;

    public void handleSpeedChange(MouseEvent mouseEvent) {
       double value = (speedSlider.getValue() - initialSpeedValue) / speedSlider.getMax();
       timer.cancel();
       timer.purge();

       timer = new Timer();
       timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() { Platform.runLater(() -> tick(DOWN));
                }
            },
               0, (int) (DELAY - value * DELAY));

    }
}
