package program;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class TaskEditController {

    @FXML private Button deleteAll;
    @FXML private Button createTask;
    @FXML private Button createTask1;
    @FXML private TableColumn archiveCol;
    @FXML private TableColumn activeTaskCol;
    @FXML private TableView <ActiveTasks> activeTable;
    @FXML private TableView <ArchivedTasks> archivedTable;
    @FXML private TableColumn deleteCol;
    @FXML private TableColumn unarchiveCol;
    @FXML private TableColumn archivedTaskCol;
    @FXML private Text activeTasksIndicator;
    @FXML private Text archivedTasksIndicator;

    private MainController mainController = Main.getMainController();

    public void initialize() {

        updateTables();

        /* // ACTIVE TABLE // */
        //set archive column of active tasks table
        archiveCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ActiveTasks, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(
                    TableColumn.CellDataFeatures<ActiveTasks, Button> arg0) {
                ActiveTasks task = arg0.getValue();
                Button button = new Button();
                button.setText("Archive");

                EventHandler<ActionEvent> archiveTask = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Archive");
                        alert.setContentText("Archiving this task will sort it lower and make it read-only.\nContinue?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            task.getTaskRef().setArchived(true);
                            updateTables();
                            mainController.updateTable();
                        }
                    }
                };
                button.setOnAction(archiveTask);

                return new SimpleObjectProperty<Button>(button);
            }
        });

        activeTaskCol.setCellValueFactory(new PropertyValueFactory<>("task"));
        activeTaskCol.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn tableColumn) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.toString());
                        }
                    }
                };
                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                            Task task = activeTable.getItems().get(((TableCell)mouseEvent.getSource()).getIndex()).getTaskRef();
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

        /* // ARCHIVED TABLE // */

        deleteCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArchivedTasks, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(
                    TableColumn.CellDataFeatures<ArchivedTasks, Button> arg0) {
                ArchivedTasks task = arg0.getValue();
                Button button = new Button();
                button.setText("Delete");

                EventHandler<ActionEvent> archiveTask = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Delete");
                        alert.setContentText("Are you sure you want to delete this task from the task list?\nThis cannot be undone.");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            mainController.removeTask(task.getTaskRef());
                            updateTables();
                            mainController.updateTable();
                        }
                    }
                };
                button.setOnAction(archiveTask);

                return new SimpleObjectProperty<Button>(button);
            }
        });

        unarchiveCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArchivedTasks, Button>, ObservableValue<Button>>() {
            @Override
            public ObservableValue<Button> call(
                    TableColumn.CellDataFeatures<ArchivedTasks, Button> arg0) {
                ArchivedTasks task = arg0.getValue();
                Button button = new Button();
                button.setText("Unarchive");

                EventHandler<ActionEvent> archiveTask = new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Unarchive");
                        alert.setContentText("This task will be sorted regularly and will be editable.\nContinue?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            task.getTaskRef().setArchived(false);
                            updateTables();
                            mainController.updateTable();
                        }
                    }
                };
                button.setOnAction(archiveTask);

                return new SimpleObjectProperty<Button>(button);
            }
        });

        archivedTaskCol.setCellValueFactory(new PropertyValueFactory<>("task"));
        archivedTaskCol.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn tableColumn) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.toString());
                        }
                    }
                };
                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                            Task task = archivedTable.getItems().get(((TableCell)mouseEvent.getSource()).getIndex()).getTaskRef();
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

        //color code rows based on priority (both tables)
        PseudoClass lowPriority = PseudoClass.getPseudoClass("lowPriority");
        PseudoClass medPriority = PseudoClass.getPseudoClass("medPriority");
        PseudoClass highPriority = PseudoClass.getPseudoClass("highPriority");
        activeTable.setRowFactory(new Callback<TableView<ActiveTasks>, TableRow<ActiveTasks>>() {
            @Override
            public TableRow<ActiveTasks> call(TableView<ActiveTasks> activeTasksTableView) {
                TableRow<ActiveTasks> row = new TableRow<>();
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        switch (newItem.getTaskRef().getPriority() + "") {
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
        archivedTable.setRowFactory(new Callback<TableView<ArchivedTasks>, TableRow<ArchivedTasks>>() {
            @Override
            public TableRow<ArchivedTasks> call(TableView<ArchivedTasks> archivedTasksTableView) {
                TableRow<ArchivedTasks> row = new TableRow<>();
                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        switch (newItem.getTaskRef().getPriority() + "") {
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

        EventHandler<ActionEvent> createNewTask = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                    Parent root = (Parent)loader.load();
                    TaskCreateController controller = loader.<TaskCreateController>getController();
                    //controller.setCheckboxVisible(true);
                    controller.setTaskRef(null);
                    Stage stage = new Stage();
                    stage.setTitle("Add New Task");
                    stage.setScene(new Scene(root, 480, 358));
                    stage.initStyle(StageStyle.UTILITY);
                    stage.sizeToScene();
                    stage.setResizable(false);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        createTask.setOnAction(createNewTask);
        createTask1.setOnAction(createNewTask);

        EventHandler<ActionEvent> deleteArchives = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Delete All");
                alert.setContentText("Are you sure you want to delete ALL archived tasks?\nThis cannot be undone.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    for (int i = 0; i < mainController.getGlobalTasks().size(); i++) {
                        if (mainController.getGlobalTasks().get(i).getArchived()) {
                            mainController.removeTask(mainController.getGlobalTasks().get(i));
                        }
                    }
                    updateTables();
                    mainController.updateTable();
                }
            }
        };
        deleteAll.setOnAction(deleteArchives);

    }

    public void updateTables() {
        activeTable.getItems().clear();
        activeTable.refresh();
        archivedTable.getItems().clear();
        archivedTable.refresh();
        int activeTasks = 0;
        int archivedTasks = 0;
        //put tasks in either table depending on their active status
        for(int i = 0; i < mainController.getGlobalTasks().size(); i++) {
            if(!mainController.getGlobalTasks().get(i).getArchived()) {
                ActiveTasks at = new ActiveTasks(mainController.getGlobalTasks().get(i).getName(),
                        mainController.getGlobalTasks().get(i));
                activeTable.getItems().add(at);
                activeTasks++;
            } else {
                ArchivedTasks at = new ArchivedTasks(mainController.getGlobalTasks().get(i).getName(),
                        mainController.getGlobalTasks().get(i));
                archivedTable.getItems().add(at);
                archivedTasks++;
            }
        }
        activeTasksIndicator.setText(activeTasks + " active tasks");
        archivedTasksIndicator.setText(archivedTasks + " archived tasks");

        //table sorting (both tables)
        activeTable.sortPolicyProperty().set(new Callback<TableView<ActiveTasks>, Boolean>() {
            @Override
            public Boolean call(TableView<ActiveTasks> activeTasksTableView) {
                Comparator<ActiveTasks> comparator = new Comparator<ActiveTasks>() {
                    @Override
                    public int compare(ActiveTasks o1, ActiveTasks o2) {
                        int o1Rank;
                        int o2Rank;
                        switch (o1.getTaskRef().getPriority()+"") {
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
                        switch (o2.getTaskRef().getPriority()+"") {
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
                };
                FXCollections.sort(activeTable.getItems(), comparator);
                return true;
            }

        });
        archivedTable.sortPolicyProperty().set(new Callback<TableView<ArchivedTasks>, Boolean>() {
            @Override
            public Boolean call(TableView<ArchivedTasks> archivedTasksTableView) {
                Comparator<ArchivedTasks> comparator = new Comparator<ArchivedTasks>() {
                    @Override
                    public int compare(ArchivedTasks o1, ArchivedTasks o2) {
                        int o1Rank;
                        int o2Rank;
                        switch (o1.getTaskRef().getPriority()+"") {
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
                        switch (o2.getTaskRef().getPriority()+"") {
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
                };
                FXCollections.sort(archivedTable.getItems(), comparator);
                return true;
            }

        });
    }

}
