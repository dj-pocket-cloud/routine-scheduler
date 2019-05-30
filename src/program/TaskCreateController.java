package program;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TaskCreateController {

    private static final String SECRET = "uuddlrlrba";

    @FXML private CheckBox checkbox;
    @FXML private Button cancel;
    @FXML private Button save;
    @FXML private TextField nameField;
    @FXML private TextArea notesField;
    @FXML private ToggleGroup priority;
    @FXML private RadioButton lowPriority;
    @FXML private RadioButton medPriority;
    @FXML private RadioButton highPriority;
    @FXML private Text errorText;

    private String taskName;
    private String taskDescription;
    private Main.PRIORITY taskPriority;
    private List<String> taskDates = new ArrayList<String>();
    private String dateString;
    private Task taskRef;

    public void initialize() {
        EventHandler<ActionEvent> closeWindow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage)cancel.getScene().getWindow();
                stage.close();
            }
        };
        cancel.setOnAction(closeWindow);

        EventHandler<ActionEvent> saveTask = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //TODO: differentiate between updating a task and adding a completely new one
                //TODO: currently only adding a task is implemented
                if (!nameField.getText().isBlank() && !nameField.getText().matches(SECRET)) {
                    Main.getMainController().setSaved(false);
                    nameField.setText(nameField.getText().replace("¬", ""));
                    notesField.setText(notesField.getText().replace("¬", ""));
                    if (taskRef != null) {
                        Main.getMainController().removeTask(taskRef);
                    }
                    Main.getMainController().addTask(createNew());
                    Main.getMainController().updateTable();
                    Stage stage = (Stage) save.getScene().getWindow();
                    stage.close();
                } else if (nameField.getText().matches(SECRET)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(null);
                    alert.setHeaderText(null);
                    alert.setContentText("You can now play as \uD83D\uDC68\u200D\uD83D\uDD27 Luigi.");
                    alert.showAndWait();
                } else {
                    errorText.setVisible(true);
                }
            }
        };
        save.setOnAction(saveTask);

        EventHandler<KeyEvent> onType = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                errorText.setVisible(false);
            }
        };
        nameField.setOnKeyTyped(onType);
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

    private Task createNew() {
        //take all fields and put it into a new Task object
        taskName = nameField.getText();
        taskDescription = notesField.getText();
        RadioButton selected = (RadioButton)priority.getSelectedToggle();
        switch (selected.getText()) {
            case "Low":
                taskPriority = Main.PRIORITY.LOW;
                break;
            case "High":
                taskPriority = Main.PRIORITY.HIGH;
                break;
            default:
                taskPriority = Main.PRIORITY.MED;
                break;
        }
        if (checkbox.isSelected()) {
            dateString = Main.getCurrDate()+"|false";
            taskDates.add(dateString);
        }
        Task task = new Task(taskName, taskDescription, taskPriority, taskDates, false);
        return task;
    }

    public void setTaskRef(Task task) {
        taskRef = task;
        if (task != null) {
            nameField.setText(task.getName());
            notesField.setText(task.getDescription());
            int index;
            switch (task.getPriority()+"") {
                case "LOW":
                    index = 0;
                    break;
                case "HIGH":
                    index = 2;
                    break;
                default:
                    index = 1;
                    break;
            }
            priority.selectToggle(priority.getToggles().get(index));
        }
    }
}
