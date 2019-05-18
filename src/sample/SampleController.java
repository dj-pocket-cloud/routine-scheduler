package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDate;

public class SampleController {

    @FXML private DatePicker datePicker;
    @FXML private Button nextDate;
    @FXML private Button prevDate;
    @FXML private MenuItem newTask;
    @FXML private MenuItem manageTask;
    @FXML private MenuItem quit;

    public void initialize() {

        /* // DATE PICKER // */

        //initialize the datePicker with the current system date
        LocalDate localDate = LocalDate.now();
        datePicker.setValue(localDate);
        Main.setCurrDate(localDate);

        //go forward a date
        EventHandler<ActionEvent> goToNextDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                datePicker.setValue(Main.getCurrDate().plusDays(1));
            }
        };
        nextDate.setOnAction(goToNextDate);

        //go back a date
        EventHandler<ActionEvent> goToPrevDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                datePicker.setValue(Main.getCurrDate().minusDays(1));
            }
        };
        prevDate.setOnAction(goToPrevDate);

        //update currDate variable whenever the datePicker is updated
        datePicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate localDate, LocalDate t1) {
                Main.setCurrDate(datePicker.getValue());
            }
        });

        /* // ADD TASK // */

        EventHandler<ActionEvent> addNewTask = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/taskAdd.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskAddController controller = loader.<TaskAddController>getController();
                    Stage stage = new Stage();
                    stage.setTitle("Add New Task");
                    stage.setScene(new Scene(root, 480, 358));
                    stage.sizeToScene();
                    stage.setResizable(false);
                    controller.setCheckboxVisible(true);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        newTask.setOnAction(addNewTask);

        /* // MANAGE TASKS // */

        EventHandler<ActionEvent> openTaskEditor = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/taskEditor.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskEditController controller = loader.<TaskEditController>getController();
                    Stage stage = new Stage();
                    stage.setTitle("Manage Tasks");
                    stage.setScene(new Scene(root, 335, 446));
                    stage.sizeToScene();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        manageTask.setOnAction(openTaskEditor);

        /* // FILE TAB // */

        //close the main window and quit the application
        EventHandler<ActionEvent> quitApplication = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Main.getPStage().close();
            }
        };
        quit.setOnAction(quitApplication);

    }
}
