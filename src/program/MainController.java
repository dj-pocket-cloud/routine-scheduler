package program;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    @FXML private Button manageTask;
    @FXML private MenuItem quit;
    @FXML private MenuItem newFile;
    @FXML private MenuItem openFile;
    @FXML private MenuItem saveFile;
    @FXML private MenuItem saveFileAs;
    @FXML private TableView<CurrentDaysTasks> table;
    @FXML private TableColumn completedCol;
    @FXML private TableColumn taskCol;
    @FXML private MenuItem about;
    @FXML private ProgressIndicator progress;
    @FXML private SplitMenuButton addTask;

    private int numTasks;
    private List<Task> globalTasks = new ArrayList<Task>();
    private boolean saved;
    private String fileName;
    private String tempFileName;
    private File selectedFile;
    private File currFile;
    private FileChooser fc = new FileChooser();
    private String initialDirectory;
    private TaskEditController tec;

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
        //TODO: save an internal settings file that automatically sets the internal directory
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

        //add checkbox to first column
        completedCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CurrentDaysTasks, CheckBox>, ObservableValue<CheckBox>>() {
            @Override
            public ObservableValue<CheckBox> call(
                    TableColumn.CellDataFeatures<CurrentDaysTasks, CheckBox> arg0) {
                CurrentDaysTasks task = arg0.getValue();
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().setValue(task.isCompleted());
                if (task.getTaskRef().getArchived()) {
                    checkBox.setDisable(true);
                }

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
        //set second column to be clickable, load the task when it is clicked
        taskCol.setCellValueFactory(new PropertyValueFactory<>("task"));
        taskCol.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn tableColumn) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.toString());
                            for (int i = 0; i < table.getItems().size(); i++) {
                                if (table.getItems().get(i).getTask().equals(item.toString()) && table.getItems().get(i).getTaskRef().getArchived()) {
                                    setStyle("-fx-text-fill: #7F7F7F;");
                                }
                            }
                        }
                    }
                };
                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                            Task task = table.getItems().get(((TableCell)mouseEvent.getSource()).getIndex()).getTaskRef();
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                                Parent root = (Parent)loader.load();
                                TaskCreateController controller = loader.<TaskCreateController>getController();
                                controller.setFieldsDisabled(task.getArchived());
                                controller.setTaskRef(task);
                                Stage stage = new Stage();
                                stage.setTitle("Update Task " + task.getName());
                                stage.setScene(new Scene(root, 480, 358));
                                stage.initStyle(StageStyle.UTILITY);
                                stage.sizeToScene();
                                stage.setResizable(false);
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return cell;
            }
        });

        //color code rows based on priority
        PseudoClass lowPriority = PseudoClass.getPseudoClass("lowPriority");
        PseudoClass medPriority = PseudoClass.getPseudoClass("medPriority");
        PseudoClass highPriority = PseudoClass.getPseudoClass("highPriority");
        table.setRowFactory(new Callback<TableView<CurrentDaysTasks>, TableRow<CurrentDaysTasks>>() {
            @Override
            public TableRow<CurrentDaysTasks> call(TableView<CurrentDaysTasks> currentDaysTasksTableView) {
                TableRow<CurrentDaysTasks> row = new TableRow<>();
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        switch (newItem.getPriority() + "") {
                            case "LOW":
                                row.pseudoClassStateChanged(lowPriority, true);
                                row.pseudoClassStateChanged(medPriority, false);
                                row.pseudoClassStateChanged(highPriority, false);
                                break;
                            case "HIGH":
                                row.pseudoClassStateChanged(lowPriority, false);
                                row.pseudoClassStateChanged(medPriority, false);
                                row.pseudoClassStateChanged(highPriority, true);
                                break;
                            default:
                                row.pseudoClassStateChanged(lowPriority, false);
                                row.pseudoClassStateChanged(medPriority, true);
                                row.pseudoClassStateChanged(highPriority, false);
                                break;
                        }
                    }
                });
                return row;
            }
        });

        /* // ADD TASK // */

        //open the window to add a new task
        EventHandler<ActionEvent> addNewTask = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskCreateController controller = loader.<TaskCreateController>getController();
                    //controller.setCheckboxVisible(true);
                    controller.setTaskRef(null);
                    Stage stage = new Stage();
                    stage.setTitle("Add New Task");
                    stage.setScene(new Scene(root, 480, 358));
                    stage.initStyle(StageStyle.UTILITY);
                    if (Main.getPStage().getX() + 850 < primaryScreenBounds.getMaxX()) {
                        stage.setX(Main.getPStage().getX() + 350);
                    } else {
                        stage.setX(Main.getPStage().getX() - 500);
                    }
                    stage.setY(Main.getPStage().getY());
                    stage.sizeToScene();
                    stage.setResizable(false);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        newTask.setOnAction(addNewTask);

        /* // MANAGE TASKS // */

        //open the window to manage tasks
        EventHandler<ActionEvent> openTaskEditor = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskEditor.fxml"));
                    Parent root = (Parent)loader.load();
                    tec = loader.<TaskEditController>getController();
                    Stage stage = new Stage();
                    stage.setTitle("Manage Tasks");
                    stage.setScene(new Scene(root, 335, 446));
                    stage.getScene().getStylesheets().add("program/default.css");
                    stage.initStyle(StageStyle.UTILITY);
                    if (Main.getPStage().getX() + 700 < primaryScreenBounds.getMaxX()) {
                        stage.setX(Main.getPStage().getX() + 350);
                    } else {
                        stage.setX(Main.getPStage().getX() - 350);
                    }
                    stage.setY(Main.getPStage().getY());
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

        //open a file
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
                            scanner.close();
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

        //open a basic about window
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
        //System.out.println(task.toString());
        updateTable();
    }

    public void removeTask(Task task) {
        globalTasks.remove(task);
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

    public boolean getSaved() {
        return saved;
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

    public void updateTable() {
        progress.setVisible(true);
        table.getItems().clear();
        table.refresh();
        for(int i = 0; i < globalTasks.size(); i++) {
            if (globalTasks.get(i).dateExists(Main.getCurrDate())) {
                CurrentDaysTasks cdt = new CurrentDaysTasks(globalTasks.get(i).getCompleted(Main.currDate),
                        globalTasks.get(i).getName(),
                        globalTasks.get(i).getPriority(),
                        globalTasks.get(i));
                table.getItems().add(cdt);
            }
        }
        //table sorting (rank order: active/archived -> priority -> task name)
        table.sortPolicyProperty().set(new Callback<TableView<CurrentDaysTasks>, Boolean>() {
            @Override
            public Boolean call(TableView<CurrentDaysTasks> currentDaysTasksTableView) {
                Comparator<CurrentDaysTasks> comparator = new Comparator<CurrentDaysTasks>() {
                    @Override
                    public int compare(CurrentDaysTasks o1, CurrentDaysTasks o2) {
                        int o1Rank;
                        int o2Rank;
                        int o1Archived;
                        int o2Archived;
                        if (o1.getTaskRef().getArchived()) {
                            o1Archived = 1;
                        } else {
                            o1Archived = 0;
                        }
                        if (o2.getTaskRef().getArchived()) {
                            o2Archived = 1;
                        } else {
                            o2Archived = 0;
                        }
                        if (o1Archived < o2Archived) {
                            return -1;
                        } else if (o1Archived > o2Archived) {
                            return 1;
                        } else {
                            switch (o1.getPriority() + "") {
                                case "LOW":
                                    o1Rank = 0;
                                    break;
                                case "HIGH":
                                    o1Rank = 2;
                                    break;
                                default:
                                    o1Rank = 1;
                                    break;
                            }
                            switch (o2.getPriority() + "") {
                                case "LOW":
                                    o2Rank = 0;
                                    break;
                                case "HIGH":
                                    o2Rank = 2;
                                    break;
                                default:
                                    o2Rank = 1;
                                    break;
                            }
                            if (o1Rank > o2Rank) {
                                return -1;
                            } else if (o1Rank < o2Rank) {
                                return 1;
                            } else {
                                if (o1.getTask().compareTo(o2.getTask()) < 0) {
                                    return -1;
                                } else if (o1.getTask().compareTo(o2.getTask()) > 0) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        }
                    }
                };
                FXCollections.sort(table.getItems(), comparator);
                return true;
            }

        });
        progress.setVisible(false);
    }

    //convert a priority string to the PRIORITY enum
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

    //add a string of dates to the dates list
    private List<String> makeDates(String in) {
        List<String> dates = new ArrayList<String>();
        String[] inArray = in.split(", ");
        List<String> inList = Arrays.asList(inArray);
        dates.addAll(inList);

        return dates;
    }

    public List<Task> getGlobalTasks() {
        return globalTasks;
    }

    public TaskEditController getTaskEditController() {
        return tec;
    }

    public void saveFileFromMain() {
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
}
