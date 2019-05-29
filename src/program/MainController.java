package program;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class MainController {

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
    @FXML private ProgressIndicator progress;
    @FXML private SplitMenuButton addTask;
    @FXML private MenuItem addToWeek;
    @FXML private MenuItem addToMonth;

    private int numTasks;
    private List<Task> globalTasks = new ArrayList<Task>();
    private boolean saved;
    private String fileName;
    private String tempFileName;
    private File selectedFile;
    private File currFile;
    private FileChooser fc = new FileChooser();
    private String initialDirectory;

    public void initialize() {

        /* // INITIALIZATION // */

        Main.setMainController(this); //create a static reference to this controller
        fileName = NEWFILENAME;
        saved = true;
        updateTitle();
        progress.setVisible(false);
        fc.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Routine Scheduler File", "*.rsf")
        );
        fc.setInitialFileName(NEWFILENAME);
        //TODO: add a setting that lets the user change the initial directory
        //fc.setInitialDirectory(new File(initialDirectory));

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
                        setSaved(false);
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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskCreateController controller = loader.<TaskCreateController>getController();
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
                    controller.setMode(false); //set to managing mode
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
                    fc.setTitle("Open Task File");
                    selectedFile = fc.showOpenDialog(Main.getPStage());
                    progress.setVisible(true);

                    //load from selected file
                    if (selectedFile != null) {
                        clearTasks();
                        try {
                            Scanner scanner = new Scanner(selectedFile);
                            scanner.useDelimiter("¬");
                            while(scanner.hasNext()) {
                                String name = scanner.next();
                                name = name.replaceAll("\\r|\\n", "");
                                String description = scanner.next();
                                Main.PRIORITY priority = makePriority(scanner.next());
                                List<String> dates = makeDates(scanner.next());
                                boolean archived = scanner.nextBoolean();

                                addTask(new Task(name, description, priority, dates, archived));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        fileName = selectedFile.getName();
                        currFile = selectedFile;
                        updateTitle();
                        updateTable();
                        setSaved(true);
                    }
                }
                progress.setVisible(false);
            }
        };
        openFile.setOnAction(open);

        //start from a new file
        EventHandler<ActionEvent> loadNew = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (saved || showUnsavedDialog()) {
                    selectedFile = null;
                    currFile = null;
                    clearTasks();

                    fileName = NEWFILENAME;
                    updateTitle();
                    updateTable();
                    setSaved(true);
                }
            }
        };
        newFile.setOnAction(loadNew);

        //save the file
        EventHandler<ActionEvent> save = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (currFile == null) {
                    saveFileAs.fire();
                }
                else {
                    boolean pass = saveFile(currFile);
                    if (pass) {
                        fileName = selectedFile.getName();
                        updateTitle();
                    }
                }
            }
        };
        saveFile.setOnAction(save);

        //save the file as
        EventHandler<ActionEvent> saveAs = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                fc.setTitle("Save Task List");
                selectedFile = fc.showSaveDialog(Main.getPStage());
                if (selectedFile != null) {
                    boolean pass = saveFile(selectedFile);
                    if (pass) {
                        currFile = selectedFile;
                        fileName = selectedFile.getName();
                        updateTitle();
                    }
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
                alert.setContentText("Program author: Rachel Stevens\nWritten in Java using JavaFX and FXML");
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

    public void clearTasks() {
        globalTasks.clear();
        updateTable();
    }

    public void setSaved(boolean bool) {
        saved = bool;
        updateTitle();
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

    private boolean saveFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(int i = 0; i < globalTasks.size(); i++) {
                writer.append(globalTasks.get(i).toString());
                if (i != globalTasks.size()-1) {
                    writer.append("¬\n");
                }
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error Saving File");
            alert.setHeaderText(null);
            alert.setContentText("The file failed to save.");
            alert.showAndWait();
            return false;
        }

        saved = true;
        return true;
    }

    private void updateTitle() {
        if (saved) {
            Main.getPStage().setTitle(fileName.replace(".rsf", "") + " - Routine Scheduler");
        } else {
            Main.getPStage().setTitle("● " + fileName.replace(".rsf", "") + " - Routine Scheduler");
        }
    }

    private void updateTable() {
        progress.setVisible(true);
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
        progress.setVisible(false);
    }

    private Main.PRIORITY makePriority(String in) {
        switch (in) {
            case "LOW":
                return Main.PRIORITY.LOW;
            case "HIGH":
                return Main.PRIORITY.HIGH;
            default:
                return Main.PRIORITY.MED;
        }
    }

    private List<String> makeDates(String in) {
        List<String> dates = new ArrayList<String>();
        String[] inArray = in.split(", ");
        List<String> inList = Arrays.asList(inArray);
        dates.addAll(inList);

        return dates;
    }
}
