package GUI;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Preloader;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * <h1>Controller class for the LoadingScreen window</h1>
 * This GUI class is called whenever the program requires a loading screen.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class LoadingScreenController extends Preloader {

    private Stage loadingStage;

    @FXML
    private Label infoLabel;

    /**
     * The start method is called after after the program is ready for the 
     * window to begin running.
     * <p>
     * It configures the window and then presents the Loading Screen interface
     * within it.
     *
     * @param stage Used to configure the Stage(Window) in which the Loading Screen is.
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.loadingStage = stage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("LoadingScreen.fxml"));
        try {
            loader.load();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Exception: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Parent root = loader.getRoot();
        Scene loadingScene = new Scene(root);
        loadingStage.setTitle("Loading");
        loadingStage.setScene(loadingScene);
        loadingStage.show();
        infoLabel.setText("Loading...");
    }

    /**
     * Binds the label in the Loading Screen to any messages that the window 
     * receives from Task threads
     *
     * @param task Contains the messageProperty of the Task Thread 
     */
    public void bindLabel(Task task) {
        infoLabel.textProperty().bind(task.messageProperty());
    }

}
