// If you ever have to set up JavaFX again: https://www.youtube.com/watch?v=h_3AfQhjziw
package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * <h1>Main class</h1>
 * <p>
 * This class contains the main, it has very little code, and is used mainly to
 * configure the settings of the MainScene window before it is opened.
 *
 * @author James Conway
 * @version 1.0
 * @since 2018-07-17
 */
public class Excel_To_ODM_XML extends Application {

    /**
     * This method loads the FXML window and configures a series of preferences,
     * such as whether the window is resizable and what its title is, before 
     * showing it to the user.
     */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.setTitle("Excel to ODM XML Converter");
        stage.getIcons().add(new Image("file:icon.png"));
        stage.show();
    }

    /**
     * The main only calls the 'launch(args)' method. 
     * <p>
     * 'launch(args)' is a native JavaFX library method which constructs an 
     * instance of the specified Application, then calls the 
     * 'start(javafx.stage.Stage)' method which is overriden here in the same 
     * class.
     */
    public static void main(String[] args) {
        launch(args);
    }

}
