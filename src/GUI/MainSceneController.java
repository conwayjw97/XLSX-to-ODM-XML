package GUI;

import CsvParsing.DataDictionaryParser;
import CsvParsing.InstrumentDesigParser;
import GUI.Utils.FileChoiceRecorder;
import XlsxParsing.ExcelParser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * <h1>Controller class for the MainScene window</h1>
 * <p>
 * This is the first GUI class to be called once the program has begun
 * execution.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class MainSceneController implements Initializable {

    public static boolean DEBUG = false;
    private File dataFile, instrumentFile, excelFile;
    private FileChoiceRecorder fileRecorder;
    private ExcelParser excelParser;
    private InstrumentDesigParser instrumentParser;
    private DataDictionaryParser dataDicionaryParser;
    private boolean repeatingRowsForms = true, repeatingRowsEvents = false, defaultValues = false;

    @FXML
    private TextField dataDictionaryField;
    @FXML
    private TextField excelField;
    @FXML
    private TextField instrumentDesigField;
    @FXML
    private Button beginButton;
    @FXML
    private Button newDataDictionaryButton;
    @FXML
    private Button newExcelButton;
    @FXML
    private Button newInstrumentDesigButton;
    @FXML
    private MenuItem clearInterfaceButton;
    @FXML
    private CheckMenuItem debugCheck;
    @FXML
    private ToggleButton rowSelectorForms;
    @FXML
    private ToggleButton rowSelectorEvents;
    @FXML
    private ToggleButton columnSelector;
    @FXML
    private CheckBox defaultValuesCheck;

    /**
     * The initialize method is called after all @FXML annotated members have
     * been injected.
     * <p>
     * It creates a FileChoiceRecorder object and then calls 
     * 'checkFileChoiceRecorder' to check whether there is an existing registry
     * entry for the three file directory choices.
     * @param url Unused.
     * @param rb Unused.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataFile = instrumentFile = excelFile = null;
        fileRecorder = new FileChoiceRecorder();
        checkFileChoiceRecorder();
    }

    /**
     * Called whenever the user presses a clearInterfaceButton button.
     * <p>
     * Resets the window, clearing all file directory fields and then disabling
     * the beginButton.
     * @param event Event data for the interface action.
     */
    @FXML
    private void clearFileInterface(ActionEvent event) {
        dataDictionaryField.setText("");
        dataDictionaryField.setEditable(true);
        instrumentDesigField.setText("");
        instrumentDesigField.setEditable(true);
        excelField.setText("");
        excelField.setEditable(true);
        beginButton.setDisable(true);
    }

    /**
     * Called whenever the user presses a newFile button.
     * <p>
     * This then opens a FileChooser and lets the user choose the appropriate 
     * file, finally calling 'checkReady'.
     * @param event Event data for the interface action.
     */
    @FXML
    private void newFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Button pressedButton = (Button) event.getSource();
        if (pressedButton.equals(newDataDictionaryButton)) {
            fileChooser.setTitle("Open Data Dictionary");
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Spreadsheet Files", "*.csv"));
            if (dataFile != null) {
                fileChooser.setInitialDirectory(new File(dataFile.getParent()));
            }
            dataDictionaryField.setText(fileChooser.showOpenDialog(new Stage()).getAbsolutePath());
        } else if (pressedButton.equals(newInstrumentDesigButton)) {
            fileChooser.setTitle("Open Instrument Designations");
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Spreadsheet Files", "*.csv"));
            if (instrumentFile != null) {
                fileChooser.setInitialDirectory(new File(instrumentFile.getParent()));
            }
            instrumentDesigField.setText(fileChooser.showOpenDialog(new Stage()).getAbsolutePath());
        } else if (pressedButton.equals(newExcelButton)) {
            fileChooser.setTitle("Open Excel File");
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Spreadsheet Files", "*.xlsx"));
            if (excelFile != null) {
                fileChooser.setInitialDirectory(new File(excelFile.getParent()));
            }
            excelField.setText(fileChooser.showOpenDialog(new Stage()).getAbsolutePath());
        }
        checkReady();
    }

    /**
     * Determines whether the user has chosen all the necessary files, and if so
     * activating the beginButton.
     */
    private void checkReady() {
        if (!dataDictionaryField.getText().equals("")
                && (!instrumentDesigField.getText().equals(""))
                && (!excelField.getText().equals(""))) {
            dataFile = new File(dataDictionaryField.getText());
            instrumentFile = new File(instrumentDesigField.getText());
            excelFile = new File(excelField.getText());

            fileRecorder.setDataDir(dataFile.getAbsolutePath());
            fileRecorder.setInstrumentDir(instrumentFile.getAbsolutePath());
            fileRecorder.setExcelDir(excelFile.getAbsolutePath());
            fileRecorder.setRepeatingRowsForms(String.valueOf(repeatingRowsForms));
            fileRecorder.setRepeatingRowsEvents(String.valueOf(repeatingRowsEvents));
            fileRecorder.setDefaultValues(String.valueOf(defaultValues));
            fileRecorder.writeToRegistry();
            beginButton.setDisable(false);
        } else {
            beginButton.setDisable(true);
        }
    }

    /**
     * Called whenever the user presses the beginButton.
     * <p>
     * Parses the chosen files, then opens the FieldCustomizer window if 
     * no exceptions are thrown by the parsers.
     * @param event Event data for the interface action.
     */
    @FXML
    private void openFieldCustomizer(ActionEvent event) {
        // Put parsing methods into their own thread
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Opening Files...");
                dataFile = new File(dataDictionaryField.getText());
                instrumentFile = new File(instrumentDesigField.getText());
                excelFile = new File(excelField.getText());

                updateMessage("Parsing Excel Fields...");
                excelParser = new ExcelParser(excelFile);
                excelParser.parseHeaders();

                updateMessage("Parsing Instrument Designation...");
                instrumentParser = new InstrumentDesigParser(instrumentFile);
                instrumentParser.parse();

                updateMessage("Parsing Data Dictionary...");
                dataDicionaryParser = new DataDictionaryParser(dataFile);
                dataDicionaryParser.parse();

                updateMessage("Done!");

                return null;
            }
        };

        // Start the Task thread
        new Thread(task).start();

        // Load the Loading Screen Scene
        FXMLLoader loadingScreenLoader = new FXMLLoader();
        loadingScreenLoader.setLocation(getClass().getResource("LoadingScreen.fxml"));
        try {
            loadingScreenLoader.load();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Exception: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Configure the LoadingScreen
        LoadingScreenController loadingScreenController = loadingScreenLoader.<LoadingScreenController>getController();
        loadingScreenController.bindLabel(task);

        // Show the Loading Screen Window
        Parent loadingRoot = loadingScreenLoader.getRoot();
        Scene loadingScene = new Scene(loadingRoot);
        Stage loadingStage = new Stage();
        loadingStage.setTitle("Please Wait");
        loadingStage.setResizable(false);
        loadingStage.getIcons().add(new Image("file:icon.png"));
        loadingStage.setScene(loadingScene);
        loadingStage.show();

        // Add a listener to handle exceptions being caught inside the Task
        task.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                task.cancel();
                loadingStage.close();
                Exception ex = (Exception) newValue;
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + ex.getLocalizedMessage(), ButtonType.OK);
                alert.showAndWait();
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        task.setOnSucceeded(e -> {
            // Load the Customizer Scene
            FXMLLoader fieldCustomizerLoader = new FXMLLoader();
            fieldCustomizerLoader.setLocation(getClass().getResource("FieldCustomizer.fxml"));
            try {
                fieldCustomizerLoader.load();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Exception: " + ex.getLocalizedMessage(), ButtonType.OK);
                alert.showAndWait();
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Configure the FieldCustomizer 
            FieldCustomizerController fieldCusController = fieldCustomizerLoader.<FieldCustomizerController>getController();
            fieldCusController.setInstrumentParser(instrumentParser);
            fieldCusController.setExcelParser(excelParser);
            fieldCusController.setDataDictionaryParser(dataDicionaryParser);
            fieldCusController.setRepeatingRowsForms(repeatingRowsForms);
            fieldCusController.setRepeatingRowsEvents(repeatingRowsEvents);
            fieldCusController.setDefaultValues(defaultValues);
            fieldCusController.begin();

            // Show the FieldCustomizer Window
            Parent fieldRoot = fieldCustomizerLoader.getRoot();
            Scene customizerScene = new Scene(fieldRoot);
            Stage customizerStage = new Stage();
            customizerStage.setTitle("Excel to ODM XML Converter");
            customizerStage.getIcons().add(new Image("file:icon.png"));
            customizerStage.setScene(customizerScene);
            customizerStage.show();
            loadingStage.hide();
        });
    }

    /**
     * Determines whether the computer's registry contains entries for the 
     * necessary file directories and options using the fileRecorder object. 
     * If so then it takes the saved data and sets the interface accordingly, 
     * then enables the beginButton.
     */
    private void checkFileChoiceRecorder() {
        if (fileRecorder.checkExists()) {
            dataDictionaryField.setText(fileRecorder.getDataDir());
            dataFile = new File(fileRecorder.getDataDir());
            instrumentDesigField.setText(fileRecorder.getInstrumentDir());
            instrumentFile = new File(fileRecorder.getInstrumentDir());
            excelField.setText(fileRecorder.getExcelDir());
            excelFile = new File(fileRecorder.getExcelDir());
            repeatingRowsForms = Boolean.parseBoolean(fileRecorder.getRepeatingRowsForms());
            repeatingRowsEvents = Boolean.parseBoolean(fileRecorder.getRepeatingRowsEvents());
            defaultValues = Boolean.parseBoolean(fileRecorder.getDefaultValues());
            
            rowSelectorForms.setSelected(false);
            rowSelectorEvents.setSelected(false);
            columnSelector.setSelected(false);
            if (repeatingRowsForms) {
                rowSelectorForms.setSelected(true);
            } else if (repeatingRowsEvents) {
                rowSelectorEvents.setSelected(true);
            } else {
                columnSelector.setSelected(true);
            }
            
            if (defaultValues) {
                defaultValuesCheck.setSelected(true);
            }

            beginButton.setDisable(false);
        }
    }

    /**
     * Called whenever the user presses the debugCheck.
     * <p>
     * Sets the DEBUG boolean accordingly.
     * @param event Event data for the interface action.
     */
    @FXML
    private void toggleDebug(ActionEvent event) {
        if (debugCheck.isSelected()) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    /**
     * Called whenever the user presses the defaultValuesCheck.
     * <p>
     * Sets the defaultValues boolean accordingly.
     * @param event Event data for the interface action.
     */
    @FXML
    private void toggleDefaultValues(ActionEvent event) {
        if (defaultValuesCheck.isSelected()) {
            defaultValues = true;
        } else {
            defaultValues = false;
        }
        checkReady();
    }

    /**
     * Called whenever the user presses the columnSelector or rowSelector.
     * <p>
     * Sets the repeatingRows boolean accordingly, then unselects the other
     * button that wasn't pressed. 
     * @param event Event data for the interface action.
     */
    @FXML
    private void toggleRepeatingValues(ActionEvent event) {
        ToggleButton pressedButton = (ToggleButton) event.getSource();
        if (pressedButton.equals(rowSelectorForms)) {
            rowSelectorForms.setSelected(true);
            rowSelectorEvents.setSelected(false);
            columnSelector.setSelected(false);
            repeatingRowsForms = true;
            repeatingRowsEvents = false;
        }else if (pressedButton.equals(rowSelectorEvents)) {
        	rowSelectorForms.setSelected(false);
            rowSelectorEvents.setSelected(true);
            columnSelector.setSelected(false);
            repeatingRowsForms = false;
            repeatingRowsEvents = true;
        } else if (pressedButton.equals(columnSelector)) {
        	rowSelectorForms.setSelected(false);
            rowSelectorEvents.setSelected(false);
            columnSelector.setSelected(true);
            repeatingRowsForms = false;
            repeatingRowsEvents = false;
        }
        checkReady();
    }
}
