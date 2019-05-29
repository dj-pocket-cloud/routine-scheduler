package program;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TaskEditController {

    @FXML private Button deleteAll;
    @FXML private Button unarchiveAll;

    private boolean mode; //true: add existing task to calendar, false: manage tasks

    public void initialize() {

    }

    public void setMode(boolean bool) {
        mode = bool;
        //do button visibility stuff here
        if (bool) {
            deleteAll.setVisible(false);
            deleteAll.setDisable(true);
            unarchiveAll.setVisible(false);
            unarchiveAll.setDisable(true);
        } else {
            deleteAll.setVisible(true);
            deleteAll.setDisable(false);
            unarchiveAll.setVisible(true);
            unarchiveAll.setDisable(false);
        }
    }

}
