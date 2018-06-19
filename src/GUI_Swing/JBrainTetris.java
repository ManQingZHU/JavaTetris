package GUI_Swing;

import Brain.DefaultBrain;
import PieceAndBoard.Board;
import PieceAndBoard.Piece;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class JBrainTetris extends JTetris {
    protected DefaultBrain brain = new DefaultBrain();
    protected JCheckBox brainMode;
    protected JCheckBox animateFalling;
    protected DefaultBrain.Move goodMove = null;

    protected JSlider adversary;
    protected JLabel adStatus;
    protected  DefaultBrain adversaryBrain = new DefaultBrain();
    protected DefaultBrain.Move badMove = null;
    /**
     * Creates a new GUI_Swing.JTetris where each tetris square
     * is drawn with the given number of pixels.
     *
     * @param pixels
     */
    JBrainTetris(int pixels) {
        super(pixels);
    }

    @Override
    public void tick(int verb) {
        if (!gameOn) return;

        if (currentPiece != null) {
            board.undo();
        }

        if(verb == JBrainTetris.DOWN && brainMode.isSelected()) {
            goodMove = brain.bestMove(board, currentPiece, JBrainTetris.HEIGHT, goodMove);

            if(goodMove == null) {
                stopGame();
                return;
            }

            currentPiece = goodMove.piece;
            currentX = goodMove.x;
            if (!animateFalling.isSelected())
                currentY = goodMove.y;
        }

        computeNewPosition(verb);

        int result = setCurrent(newPiece, newX, newY);

        if (result ==  Board.PLACE_ROW_FILLED) {
            repaint();
        }


        boolean failed = (result >= Board.PLACE_OUT_BOUNDS);

        if (failed) {
            if (currentPiece != null) board.place(currentPiece, currentX, currentY);
            repaintPiece(currentPiece, currentX, currentY);
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
                switch (cleared) {
                    case 1: score += 5;	 break;
                    case 2: score += 10;  break;
                    case 3: score += 20;  break;
                    case 4: score += 40; Toolkit.getDefaultToolkit().beep(); break;
                }
                updateCounters();
                repaint();
            }

            if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
                stopGame();
            }
            else {
                addNewPiece();
            }
        }

        moved = (!failed && verb!=DOWN);
    }

    @Override
    public JComponent createControlPanel() {
        JPanel panel =  (JPanel) super.createControlPanel();

        panel.add(Box.createVerticalStrut(12));
        panel.add(new JLabel("Brain:"));
        brainMode = new JCheckBox("Brain active");
        panel.add(brainMode);
        animateFalling = new JCheckBox("Animate falling", true);
        panel.add(animateFalling);

        JPanel little = new JPanel();
        panel.add(Box.createVerticalStrut(12));
        little.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, 100, 0);
        adversary.setPreferredSize(new Dimension(100, 15));
        little.add(adversary);
        panel.add(little);

        panel.add(Box.createVerticalStrut(12));
        adStatus = new JLabel("ok");
        panel.add(adStatus);

        return panel;
    }

    private void setAdStatus(boolean ok) {
        if(ok)
            adStatus.setText("ok");
        else adStatus.setText("*ok*");
    }

    @Override
    public Piece pickNextPiece() {
        Random rand = new Random();
        int randValue = rand.nextInt(99)+1;
        int adValue = adversary.getValue();

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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        JBrainTetris jbTetris = new JBrainTetris(16);
        JFrame frame = JBrainTetris.createFrame(jbTetris);
        frame.setVisible(true);
    }
}
