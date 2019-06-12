package program;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class TaskEditController {

    @FXML private Button deleteAll;
    @FXML private Button unarchiveAll;
    @FXML private TableColumn archiveCol;
    @FXML private TableColumn activeTaskCol;
    @FXML private TableView <ActiveTasks> activeTable;
    @FXML private TableView <ArchivedTasks> archivedTable;
    @FXML private TableColumn deleteCol;
    @FXML private TableColumn unarchiveCol;
    @FXML private TableColumn archivedTaskCol;

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
                        task.getTaskRef().setArchived(true);
                        updateTables();
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
                                //TODO: update TaskCreateController to be able to update ActiveTasks from this context
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                                Parent root = (Parent)loader.load();
                                TaskCreateController controller = loader.<TaskCreateController>getController();
                                controller.setCheckboxVisible(false);
                                controller.setTaskRef(task);
                                Stage stage = new Stage();
                                stage.setTitle("Update Task " + task.getName());
                                stage.setScene(new Scene(root, 480, 358));
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
                        //TODO: show a warning then delete the task from memory
                        mainController.removeTask(task.getTaskRef());
                        updateTables();
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
                        task.getTaskRef().setArchived(false);
                        updateTables();
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
                            Task task = activeTable.getItems().get(((TableCell)mouseEvent.getSource()).getIndex()).getTaskRef();
                            try {
                                //TODO: update TaskCreateController to be able to update ActiveTasks from this context
                                //TODO: set the fields in this context to be uneditable
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/program/taskCreate.fxml"));
                                Parent root = (Parent)loader.load();
                                TaskCreateController controller = loader.<TaskCreateController>getController();
                                controller.setCheckboxVisible(false);
                                controller.setTaskRef(task);
                                Stage stage = new Stage();
                                stage.setTitle("Update Task " + task.getName());
                                stage.setScene(new Scene(root, 480, 358));
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

    }

    public void updateTables() {
        activeTable.getItems().clear();
        activeTable.refresh();
        archivedTable.getItems().clear();
        archivedTable.refresh();
        //put tasks in either table depending on their active status
        for(int i = 0; i < mainController.getGlobalTasks().size(); i++) {
            if(!mainController.getGlobalTasks().get(i).getArchived()) {
                ActiveTasks at = new ActiveTasks(mainController.getGlobalTasks().get(i).getName(),
                        mainController.getGlobalTasks().get(i));
                activeTable.getItems().add(at);
            } else {
                ArchivedTasks at = new ArchivedTasks(mainController.getGlobalTasks().get(i).getName(),
                        mainController.getGlobalTasks().get(i));
                archivedTable.getItems().add(at);
            }
        }
    }

}
