package program;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.time.LocalDate;
import java.util.Optional;

public class Main extends Application {

    static LocalDate currDate;
    private static Stage pStage;
    private static MainController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        pStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/program/mainWindow.fxml"));
        //primaryStage.setTitle("new database - Routine Scheduler");
        primaryStage.setScene(new Scene(root, 335, 600));
        primaryStage.getScene().getStylesheets().add("program/default.css");
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (!getMainController().getSaved()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Exiting Without Saving");
                    alert.setContentText("The current task list is unsaved. It will be discarded if the program is closed without saving.");

                    ButtonType buttonTypeSave = new ButtonType("Save and Exit");
                    ButtonType buttonTypeClose = new ButtonType("Exit Without Saving");
                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeClose, buttonTypeCancel);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonTypeSave) {
                        getMainController().saveFileFromMain();
                        Platform.exit();
                    } else if (result.get() == buttonTypeClose) {
                        //close without saving
                        Platform.exit();
                    } else {
                        windowEvent.consume();
                    }
                } else {
                    Platform.exit();
                }
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setMainController(MainController mc) {
        mainController = mc;
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static void setCurrDate(LocalDate date) {
        currDate = date;
    }

    public static LocalDate getCurrDate() {
        return currDate;
    }

    public static Stage getPStage() {
        return pStage;
    }

    public enum PRIORITY {
        LOW,
        MED,
        HIGH
    }
}
