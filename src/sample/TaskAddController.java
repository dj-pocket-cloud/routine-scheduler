package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class TaskAddController {

    @FXML private CheckBox checkbox;
    @FXML private Button cancel;

    public void initialize() {
        EventHandler<ActionEvent> closeWindow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage)cancel.getScene().getWindow();
                stage.close();
            }
        };
        cancel.setOnAction(closeWindow);
    }

    public void setCheckboxVisible(boolean bool) {
        if (bool) {
            checkbox.setVisible(true);
            checkbox.setDisable(false);
        } else {
            checkbox.setVisible(false);
            checkbox.setDisable(true);
        }
    }
}
