package GUI_JFX;

import Brain.DefaultBrain;
import GUI_Swing.JBrainTetris;
import PieceAndBoard.Board;
import PieceAndBoard.Piece;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.Random;

public class BrainTetrisController extends TetrisController{
    @FXML
    protected Label adversaryLabel;

    @FXML
    protected JFXSlider adversarySlider;

    @FXML
    protected JFXCheckBox brainCheckBox, fallingCheckBox;

    protected DefaultBrain brain = new DefaultBrain();
    protected  DefaultBrain adversaryBrain = new DefaultBrain();
    protected DefaultBrain.Move goodMove = null;
    protected DefaultBrain.Move badMove = null;

    @Override
    public void tick(int verb){
        if (!gameOn) return;

        if (currentPiece != null) {
            board.undo();	// remove the piece from its old position
        }

        if(verb == JBrainTetris.DOWN && brainCheckBox.isSelected()) {
            goodMove = brain.bestMove(board, currentPiece, JBrainTetris.HEIGHT, goodMove);

            //  When can't find a move, stop the game
            if(goodMove == null) {
                stopGame();
                return;
            }

            currentPiece = goodMove.piece;
            currentX = goodMove.x;
            if (!fallingCheckBox.isSelected())
                currentY = goodMove.y;
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

    private void setAdStatus(boolean ok) {
        if(ok)
            adversaryLabel.setTextFill(TETRIS_WHITE);
        else adversaryLabel.setTextFill(TETRIS_PURPLE);
    }

    @Override
    public Piece pickNextPiece() {
        Random rand = new Random();
        int randValue = rand.nextInt(99)+1;
        double adValue = adversarySlider.getValue();

        if(randValue >= adValue) {
            setAdStatus(true);
            return super.pickNextPiece();
        }
        else {
            setAdStatus(false);

            double score = 0;
            Piece wrost = null;

            for(Piece p: pieces)
            {
                badMove = adversaryBrain.bestMove(board, p, JBrainTetris.HEIGHT, badMove);
                if(badMove == null)
                    break;

                if (badMove.score >= score) {
                    wrost = badMove.piece;
                    score = badMove.score;
                }
            }

            return wrost;
        }
    }
}
