package program;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SampleController {

    private static final String NEWFILENAME = "New List";
    private static final String VERSION = "0.01";

    @FXML private DatePicker datePicker;
    @FXML private Button nextDate;
    @FXML private Button prevDate;
    @FXML private MenuItem newTask;
    @FXML private MenuItem manageTask;
    @FXML private MenuItem quit;
    @FXML private MenuItem newFile;
    @FXML private MenuItem openFile;
    @FXML private MenuItem saveFile;
    @FXML private MenuItem saveFileAs;
    @FXML private TableView table;
    @FXML private TableColumn completedCol;
    @FXML private TableColumn taskCol;
    @FXML private MenuItem about;

    //TODO: when loading in data, put number of globalTasks at first line
    //TODO: then assign task ids based on the order they were loaded

    private int numTasks;
    private List<Task> globalTasks = new ArrayList<Task>();
    private boolean saved;
    private boolean fileIsNew;
    private String fileName;
    private String tempFileName;

    public void initialize() {

        /* // INITIALIZATION // */

        Main.setMainController(this); //create a static reference to this controller
        fileName = NEWFILENAME;
        saved = true;
        fileIsNew = true;
        updateTitle();

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
                updateTable();
            }
        };
        nextDate.setOnAction(goToNextDate);

        //go back a date
        EventHandler<ActionEvent> goToPrevDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                datePicker.setValue(Main.getCurrDate().minusDays(1));
                updateTable();
            }
        };
        prevDate.setOnAction(goToPrevDate);

        //update currDate variable whenever the datePicker is updated
        datePicker.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observableValue, LocalDate localDate, LocalDate t1) {
                Main.setCurrDate(datePicker.getValue());
                updateTable();
            }
        });

        /* // TABLE // */


        completedCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CurrentDaysTasks, CheckBox>, ObservableValue<CheckBox>>() {
            @Override
            public ObservableValue<CheckBox> call(
                    TableColumn.CellDataFeatures<CurrentDaysTasks, CheckBox> arg0) {
                CurrentDaysTasks task = arg0.getValue();
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().setValue(task.isCompleted());

                checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    public void changed(ObservableValue<? extends Boolean> ov,
                                        Boolean old_val, Boolean new_val) {
                        task.setCompleted(new_val);
                        task.getTaskRef().setCompleted(Main.currDate, new_val);
                    }
                });

                return new SimpleObjectProperty<CheckBox>(checkBox);

            }

        });
        taskCol.setCellValueFactory(new PropertyValueFactory<>("task"));

        /* // ADD TASK // */

        EventHandler<ActionEvent> addNewTask = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskAdd.fxml"));
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskEditor.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskEditController controller = loader.<TaskEditController>getController();
                    Stage stage = new Stage();
                    stage.setTitle("Manage Task");
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

        //open a csv file
        EventHandler<ActionEvent> open = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (saved || showUnsavedDialog()) {
                    //TODO: show a dialog to open a CSV after confirming that the user wants to open a new file

                    updateTitle();
                    updateTable();
                    saved = true;
                    fileIsNew = false;
                }
            }
        };
        openFile.setOnAction(open);

        //start from a new file
        EventHandler<ActionEvent> loadNew = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (saved || showUnsavedDialog()) {
                    //TODO: discard current data

                    fileName = NEWFILENAME;
                    updateTitle();
                    updateTable();
                    saved = true;
                    fileIsNew = true;
                }
            }
        };
        newFile.setOnAction(loadNew);

        //save the file
        EventHandler<ActionEvent> save = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (fileIsNew) {
                    tempFileName = openSaveDialog();
                }
                if (tempFileName != null) {
                    fileName = tempFileName;
                    tempFileName = fileName;
                    saveFile(fileName);
                    updateTitle();
                }
            }
        };
        saveFile.setOnAction(save);

        //save the file as
        EventHandler<ActionEvent> saveAs = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                tempFileName = openSaveDialog();
                if (tempFileName != null) {
                    fileName = tempFileName;
                    tempFileName = fileName;
                    saveFile(fileName);
                    updateTitle();
                }
            }
        };
        saveFileAs.setOnAction(saveAs);

        /* // MISCELLANEOUS // */

        EventHandler<ActionEvent> openAboutWindow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("About");
                alert.setHeaderText("Routine Scheduler v." + VERSION);
                alert.setContentText("Program author: Rachel Stevens\nWritten in Java using JavaFX");
                alert.showAndWait();
            }
        };
        about.setOnAction(openAboutWindow);

    } // end initialization

    public void addTask(Task task) {
        globalTasks.add(task);
        System.out.println(task.toString());
        updateTable();
    }

    public void setSaved(boolean bool) {
        saved = bool;
    }

    private boolean showUnsavedDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Task List");
        alert.setContentText("There appear to be unsaved changes.\nDo you want to proceed and discard changes?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        }

        return false;
    }

    private String openSaveDialog() {
        TextInputDialog dialog = new TextInputDialog(NEWFILENAME);
        dialog.setTitle("Save Task List");
        dialog.setHeaderText("Save As");
        dialog.setContentText("Please set the saved filename: ");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }

        return null;
    }

    private void saveFile(String fn) {
        //TODO: save the csv file with given filename

        saved = true;
        fileIsNew = false;
    }

    private void updateTitle() {
        Main.getPStage().setTitle(fileName + " - Routine Scheduler");
    }

    private void updateTable() {
        table.getItems().clear();
        for(int i = 0; i < globalTasks.size(); i++) {
            if (globalTasks.get(i).dateExists(Main.getCurrDate())) {
                CurrentDaysTasks cdt = new CurrentDaysTasks(globalTasks.get(i).getCompleted(Main.currDate),
                        globalTasks.get(i).getName(),
                        globalTasks.get(i).getPriority(),
                        globalTasks.get(i));
                table.getItems().add(cdt);
            }
        }
    }
}
