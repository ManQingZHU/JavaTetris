package GUI_JFX;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class summaryController implements Initializable {
    @FXML
    private Label timeLabel, countLabel, scoreLabel;

    @FXML
    private JFXButton okButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void handleClick(MouseEvent mouseEvent) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void setTimeLabel(long delta){
        timeLabel.setText("Time: "+ Double.toString(delta/100.0) + " seconds");
    }

    public void setCountLabel(int count){
        countLabel.setText("Count: "+count);
    }

    public void setScoreLabel(int score){
        scoreLabel.setText("Score: "+score);
    }
}
