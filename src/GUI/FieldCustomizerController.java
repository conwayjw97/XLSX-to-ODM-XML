package GUI;

import GUI.Utils.ProgressFileHandler;
import CsvParsing.DataDictionaryParser;
import CsvParsing.InstrumentDesigParser;
import GUI.Utils.MenuTracker;
import XlsxParsing.ExcelParser;
import XmlWriting.XmlConverter;
import java.io.File;
import static java.lang.Integer.max;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * <h1>Controller class for the FieldCustomizer window</h1>
 * <p>
 * This GUI class is called from the MainSceneController after the user presses
 * the beginButton.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class FieldCustomizerController implements Initializable {

    public static final String ITEM_EMPTY = "";
    public static final String FIELD_EMPTY = " ";
    public static final String EVENT_SHEET_PLACEHOLDER = "Choose Event for Sheet: ";
    public static final String EVENT_FIELD_PLACEHOLDER = "Choose Event for Field: ";
    public static final String FORM_SHEET_PLACEHOLDER = "Choose Form for Sheet: ";
    public static final String FORM_FIELD_PLACEHOLDER = "Choose Form for Field: ";
    public static final String VARIABLE_FIELD_PLACEHOLDER = "Choose Variable for Field: ";

    private ExcelParser excelParser;
    private InstrumentDesigParser instrumentParser;
    private DataDictionaryParser dictionaryParser;
    private MenuTracker menuTracker;
    private ProgressFileHandler progressFileHandler;
    private HashMap<String, ObservableList> eventToFormsList, formToVariablesList;
    private int globalIndex;
    private boolean notBound, repeatingRowsForms, repeatingRowsEvents, defaultValues;

    @FXML
    private TreeView<String> excelFieldTree;
    @FXML
    private ListView<String> redcapEventsList;
    @FXML
    private ListView<String> redcapFormsList;
    @FXML
    private ListView<String> redcapVariablesList;
    @FXML
    private Button toEventsButton;
    @FXML
    private Button toFormsButton;
    @FXML
    private Button toVariablesButton;
    @FXML
    private Button convertButton;
    @FXML
    private MenuItem clearProgressButton;
    @FXML
    private MenuItem openProgressButton;
    @FXML
    private MenuItem saveProgressButton;

    /**
     * Unused initialize method.
     * <p>
     * Had to be implemented as part of a class that implements Initializable.
     *
     * @param url Unused.a
     * @param rb Unused.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Pre Scene Creation constructor.
     * <p>
     * Extra constructor method to allow the MainSceneController to use the set
     * methods before the window is shown. (With initializable the window is
     * shown first)
     */
    public void begin() {
        notBound = true;
        fillFieldTree();
    }

    /**
     * Called whenever the user presses the convertButton.
     * <p>
     * This first opens a FileChooser in which the user can choose where to save
     * the converted XML, then creates a Task thread to perform all the parsing
     * and conversion work while opening a LoadingScreen to show that the
     * program is busy. After the conversion Task thread is finished it brings
     * up an Alert window to inform the user that it has finished.
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void convert(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save ODM XML File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File xmlFile = fileChooser.showSaveDialog(new Stage());

        // If a file was chosen
        if (xmlFile != null) {
            // Put all the conversion work in its own thread
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Collecting User Choices...");
                    menuTracker.collectEventListData(redcapEventsList.getItems());
                    menuTracker.collectFormListData(redcapFormsList.getItems());
                    menuTracker.collectVariableListData(redcapVariablesList.getItems());

                    updateMessage("Parsing Patient Data...");
                    if (repeatingRowsForms) {
                        excelParser.parseRepeatingRows(menuTracker.getSelectedSheetToFields());
                        updateMessage("Converting to ODM XML...");
                        XmlConverter xmlConverter = new XmlConverter(menuTracker, instrumentParser, dictionaryParser,
                                excelParser, xmlFile, defaultValues);
                        xmlConverter.convertRepeatingRows(true);
                    } else if (repeatingRowsEvents) {
                    	excelParser.parseRepeatingRows(menuTracker.getSelectedSheetToFields());
                        updateMessage("Converting to ODM XML...");
                        XmlConverter xmlConverter = new XmlConverter(menuTracker, instrumentParser, dictionaryParser,
                                excelParser, xmlFile, defaultValues);
                        xmlConverter.convertRepeatingRows(false);
                    } else {
                        excelParser.parseRepeatingColumns(menuTracker.getSelectedSheetToFields());
                        updateMessage("Converting to ODM XML...");
                        XmlConverter xmlConverter = new XmlConverter(menuTracker, instrumentParser, dictionaryParser,
                                excelParser, xmlFile, defaultValues);
                        xmlConverter.convertRepeatingColumns();
                    }
                    return null;
                }
            };
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

            // After conversion has ended, close the loading screen
            task.setOnSucceeded(e -> {
                loadingStage.hide();
                Alert alert = new Alert(AlertType.NONE, "Conversion Complete!", ButtonType.OK);
                alert.getDialogPane().setMaxSize(200, 50);
                alert.showAndWait();
            });
        }
    }

    /**
     * Called whenever the user presses the openProgressButton.
     * <p>
     * This opens a FileChooser through which the user can choose a progress
     * file. If the file is valid then the interface will be updated with the
     * progress file data.
     */
    @FXML
    private void openProgress() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Progress File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Progress Files", "*.prog"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File progressFile = fileChooser.showOpenDialog(new Stage());
        progressFileHandler = new ProgressFileHandler(progressFile);
        progressFileHandler.readProgress();
        // If the progress file contains the same files that have already been chosen in the MainScene
        if (progressFileHandler.getDictionaryFile().equals(dictionaryParser.getFile())
                && progressFileHandler.getInstrumentFile().equals(instrumentParser.getFile())
                && progressFileHandler.getXlsxFile().equals(excelParser.getFile())) {
            // Fill the relevant TreeView and ListViews based on the status code in the file
            switch (progressFileHandler.getStatus()) {
                case 1:
                    fillFieldTree(progressFileHandler.getSelectedSheetToFields());
                    break;
                case 2:
                    fillFieldTree(progressFileHandler.getSelectedSheetToFields());
                    fillEventsList(progressFileHandler.getEventListData());
                    checkAllEventsChosen();
                    break;
                case 3:
                    fillFieldTree(progressFileHandler.getSelectedSheetToFields());
                    fillEventsList(progressFileHandler.getEventListData());
                    fillFormsList(progressFileHandler.getFormListData());
                    checkAllEventsChosen();
                    checkAllFormsChosen();
                    break;
                case 4:
                    fillFieldTree(progressFileHandler.getSelectedSheetToFields());
                    fillEventsList(progressFileHandler.getEventListData());
                    fillFormsList(progressFileHandler.getFormListData());
                    fillVariablesList(progressFileHandler.getVariableListData());
                    checkAllEventsChosen();
                    checkAllFormsChosen();
                    checkAllVariablesChosen();
                    break;
                default:
                    break;
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "This progress file could not be opened"
                    + " as its contents correspond to files which haven't been selected in the Main Scene.");
            alert.showAndWait();
        }
    }

    /**
     * Called whenever the user presses the clearProgressButton.
     * <p>
     * This clears interface.
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void clearProgress(ActionEvent event) {
        excelFieldTree.scrollTo(0);
        redcapEventsList.getItems().clear();
        redcapEventsList.setDisable(true);
        redcapFormsList.getItems().clear();
        redcapFormsList.setDisable(true);
        redcapVariablesList.getItems().clear();
        redcapVariablesList.setDisable(true);
        toFormsButton.setDisable(true);
        toVariablesButton.setDisable(true);
        convertButton.setDisable(true);
        fillFieldTree();
    }

    /**
     * Called whenever the user presses the saveProgressButton.
     * <p>
     * Opens a FileChooser through which the user can decide the name and
     * directory for the progress file containing data for this current choices
     * on the interface.
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void saveProgress(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Progress File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Progress Files", "*.prog"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        if (fileChooser.showSaveDialog(new Stage()) != null) {
            progressFileHandler = new ProgressFileHandler(fileChooser.showSaveDialog(new Stage()));
            progressFileHandler.writeProgress(excelParser.getFile(), instrumentParser.getFile(),
                    dictionaryParser.getFile(), menuTracker.getSelectedSheetToFields(),
                    redcapEventsList.getItems(), redcapFormsList.getItems(), redcapVariablesList.getItems());
        }
    }

    /**
     * Fills the Excel Field TreeView.
     * <p>
     * Places all the sheets and fields of the Excel file on the TreeView as
     * CheckBoxItems.
     */
    private void fillFieldTree() {
        // Prepare the List Views
        redcapEventsList.getItems().clear();
        redcapEventsList.setDisable(true);
        redcapFormsList.getItems().clear();
        redcapFormsList.setDisable(true);
        redcapVariablesList.getItems().clear();
        redcapVariablesList.setDisable(true);
        toFormsButton.setDisable(true);
        toVariablesButton.setDisable(true);
        convertButton.setDisable(true);

        LinkedHashMap<String, ArrayList> sheetToFields = excelParser.getSheetToFields();

        menuTracker = new MenuTracker();

        // Create the root Tree Item to select all the Tree Items
        CheckBoxTreeItem<String> root;
        root = new CheckBoxTreeItem<>("All Fields");
        root.setExpanded(true);
        root.setGraphic(null);

        // Force root to be permanently expanded
        root.expandedProperty().addListener(observable -> {
            if (!root.isExpanded()) {
                root.setExpanded(true);
            }
        });

        // Set up the TreeView
        excelFieldTree.setEditable(true);
        excelFieldTree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

        // Iterate through the sheets
        for (String sheetName : sheetToFields.keySet()) {
            // For each sheet, create a tree item for it
            ArrayList<String> fields = sheetToFields.get(sheetName);
            CheckBoxTreeItem<String> sheetTree = new CheckBoxTreeItem<>(sheetName);
            sheetTree.setExpanded(true);
            // Force sheetTree to be permanently expanded
            sheetTree.expandedProperty().addListener(observable -> {
                if (!sheetTree.isExpanded()) {
                    sheetTree.setExpanded(true);
                }
            });

            // Iterate through the fields of the sheet and create a Tree Item for each of those,
            // after having created them add them to the sheet Tree Item
            for (String field : fields) {
                CheckBoxTreeItem<String> fieldTree = new CheckBoxTreeItem<>(field);
                // Add listener to collect selected items
                fieldTree.selectedProperty().addListener((obs, wasChecked, isNowChecked) -> {
                    int index = excelFieldTree.getRow(fieldTree);
                    globalIndex = excelFieldTree.getRow(sheetTree);
                    if (isNowChecked) {
                        menuTracker.fieldChecked(sheetName, field, index);
                    } else {
                        menuTracker.fieldUnchecked(sheetName, field, index);
                    }
                });

                sheetTree.getChildren().add(fieldTree);
            }

            // Add the final sheet tree item to the root Tree Item
            root.getChildren().add(sheetTree);
        }

        // Add the root to the Tree View
        excelFieldTree.setRoot(root);
        excelFieldTree.setShowRoot(true);
    }

    /**
     * Fills the Excel Field TreeView and selects the items in selectedSheets.
     * <p>
     * After calling fillFieldTree() to populate the TreeView, this method then
     * iterates through the selectedSheets parameter and selects the
     * corresponding sheets and fields on the TreeView
     *
     * @param selectedSheets LinkedHashMap containing, for each selected sheet,
     * an ArrayList of selected fields
     */
    private void fillFieldTree(LinkedHashMap<String, ArrayList> selectedSheets) {
        fillFieldTree();
        CheckBoxTreeItem<String> root = (CheckBoxTreeItem<String>) excelFieldTree.getRoot();

        // Iterate through the Sheets of the Root
        for (Iterator<TreeItem<String>> sheetIt = root.getChildren().iterator(); sheetIt.hasNext();) {
            CheckBoxTreeItem<String> sheetTree = (CheckBoxTreeItem<String>) sheetIt.next();

            // If this Sheet is in the selectedSheets HashMap
            if (selectedSheets.keySet().contains(sheetTree.getValue())) {
                ArrayList selectedFields = selectedSheets.get(sheetTree.getValue());

                // Iterate through the Fields of the Sheet
                for (Iterator<TreeItem<String>> fieldIt = sheetTree.getChildren().iterator(); fieldIt.hasNext();) {
                    CheckBoxTreeItem<String> fieldTree = (CheckBoxTreeItem<String>) fieldIt.next();

                    // If this Field is in the selectedSheets HashMap entry for this Sheet then select it
                    if (selectedFields.contains(fieldTree.getValue())) {
                        fieldTree.setSelected(true);
                    }
                }
            }
        }
    }

    /**
     * Fills the REDCap Events ListView.
     * <p>
     * Places a Placeholder item into each index corresponding to a selected
     * sheet or field to prompt the user to make a selection. It then creates a
     * List of REDCap events which can be placed into the individual item
     * ComboBoxs.
     */
    @FXML
    private void fillEventsList() {
        bindScrollPanes();
        LinkedHashMap<String, ArrayList> selectedSheets = menuTracker.getSelectedSheetToFields();

        // Prepare the List Views
        redcapEventsList.getItems().clear();
        redcapEventsList.setDisable(false);
        redcapEventsList.setEditable(true);
        redcapFormsList.getItems().clear();
        redcapFormsList.setDisable(true);
        redcapVariablesList.getItems().clear();
        redcapVariablesList.setDisable(true);
        toFormsButton.setDisable(true);
        toVariablesButton.setDisable(true);
        convertButton.setDisable(true);

        // Get Redcap Events and prepare Lists to hold data for the List View
        ArrayList<String> redcapEvents = instrumentParser.getEvents();
        LinkedHashMap<String, ArrayList> sheetToFields = excelParser.getSheetToFields();
        ObservableList defaultItems = FXCollections.observableArrayList();
        ObservableList eventsList = FXCollections.observableArrayList();

        // Put the Events into the List View
        defaultItems.add(ITEM_EMPTY);
        for (String sheetName : sheetToFields.keySet()) {
            ArrayList<String> fields = sheetToFields.get(sheetName);

            // If the sheet wasn't chosen put empty items for it
            if (selectedSheets.get(sheetName) == null || selectedSheets.get(sheetName).isEmpty()) {
                defaultItems.add(ITEM_EMPTY);
                for (int i = 0; i < fields.size(); i++) {
                    defaultItems.add(ITEM_EMPTY);
                }
            } // If the sheet was chosen fill it with event choice menus
            else {
                defaultItems.add(EVENT_SHEET_PLACEHOLDER + sheetName);
                ArrayList<String> selectedFields = selectedSheets.get(sheetName);
                for (String field : fields) {
                    if (selectedFields.contains(field)) {
                        defaultItems.add(EVENT_FIELD_PLACEHOLDER + field);
                    } else {
                        defaultItems.add(FIELD_EMPTY);
                    }
                }
            }
        }
        menuTracker.collectIndexToDefaultItem(defaultItems);

        // Collect RedCap events to be put into the menus
        for (int i = 0; i < redcapEvents.size(); i++) {
            eventsList.add(redcapEvents.get(i));
        }

        // Add the data to the List View
        redcapEventsList.setItems(defaultItems);
        redcapEventsList.setCellFactory(ComboBoxListCell.forListView(eventsList));

        excelFieldTree.scrollTo(max(0, globalIndex - 6));
        redcapEventsList.scrollTo(max(0, globalIndex - 6));
    }

    /**
     * Fills the REDCap Events and selects the items in eventListData.
     * <p>
     * After calling fillEventsList() to populate the choice ComboBoxs, this
     * method then places the eventListData ObservableList on the ListView
     *
     * @param eventListData ObservableList containing, pre-selected choices for
     * the menu
     */
    private void fillEventsList(ObservableList<String> eventListData) {
        fillEventsList();

        // Collect RedCap events to be put into the menus
        ArrayList<String> redcapEvents = instrumentParser.getEvents();
        ObservableList eventsList = FXCollections.observableArrayList();
        for (int i = 0; i < redcapEvents.size(); i++) {
            eventsList.add(redcapEvents.get(i));
        }

        // Add the data to the List View
        redcapEventsList.setItems(eventListData);
        redcapEventsList.setCellFactory(ComboBoxListCell.forListView(eventsList));
    }

    /**
     * Fills the REDCap Forms ListView.
     * <p>
     * Places a Placeholder item into each index corresponding to a selected
     * sheet or field to prompt the user to make a selection. It then attaches a
     * Listener to the ListView items, which configures the forms of the item's
     * ComboBox based on the corresponding event in the events ListView.
     */
    @FXML
    private void fillFormsList() {
        menuTracker.collectEventListData(redcapEventsList.getItems());
        populateFormsMenus();

        LinkedHashMap<String, ArrayList> sheetToFields = excelParser.getSheetToFields();
        LinkedHashMap<String, ArrayList> selectedSheets = menuTracker.getSelectedSheetToFields();
        ObservableList defaultItems = FXCollections.observableArrayList();

        // Prepare the List Views
        redcapFormsList.setDisable(false);
        redcapFormsList.getItems().clear();
        redcapVariablesList.getItems().clear();
        redcapVariablesList.setDisable(true);
        redcapFormsList.setEditable(true);
        toVariablesButton.setDisable(true);
        convertButton.setDisable(true);

        // Put the Placeholders into the List View
        defaultItems.add(ITEM_EMPTY);
        for (String sheetName : sheetToFields.keySet()) {
            ArrayList<String> fields = sheetToFields.get(sheetName);

            // If the sheet wasn't chosen put empty items for it
            if (selectedSheets.get(sheetName) == null || selectedSheets.get(sheetName).isEmpty()) {
                defaultItems.add(ITEM_EMPTY);
                for (int i = 0; i < fields.size(); i++) {
                    defaultItems.add(ITEM_EMPTY);
                }
            } // If the sheet was chosen fill it with form choice menus
            else {
                defaultItems.add(FORM_SHEET_PLACEHOLDER + sheetName);
                ArrayList<String> selectedFields = selectedSheets.get(sheetName);
                for (String field : fields) {
                    if (selectedFields.contains(field)) {
                        defaultItems.add(FORM_FIELD_PLACEHOLDER + field);
                    } else {
                        defaultItems.add(FIELD_EMPTY);
                    }
                }
            }
        }

        // Add the data to the List View
        redcapFormsList.setItems(defaultItems);

        // Add the listener to update the ComboBox with the correct forms to choose
        // whenever the user clicks on the cell
        redcapFormsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int choiceIndex = redcapFormsList.getSelectionModel().getSelectedIndex();
                if (choiceIndex >= 0) {
                    String correspondingEvent = redcapEventsList.getItems().get(choiceIndex);
                    redcapFormsList.setCellFactory(ComboBoxListCell.forListView(eventToFormsList.get(correspondingEvent)));
                }
            }
        });

        excelFieldTree.scrollTo(max(0, globalIndex - 6));
        redcapEventsList.scrollTo(max(0, globalIndex - 6));
        redcapFormsList.scrollTo(max(0, globalIndex - 6));
    }

    /**
     * Fills the Excel Field TreeView and selects the items in formListData.
     * <p>
     * After calling fillFormsList() to make all the other configurations not
     * related to the ListView's contents, this method then places the
     * formListData ObservableList on the ListView.
     *
     * @param formListData ObservableList containing, pre-selected choices for
     * the menu
     */
    private void fillFormsList(ObservableList<String> formListData) {
        fillFormsList();

        // Add the data to the List View
        redcapFormsList.setItems(formListData);

        // Add the listener to update the ComboBox with the correct forms to choose
        // whenever the user clicks on the cell
        redcapFormsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int choiceIndex = redcapFormsList.getSelectionModel().getSelectedIndex();
                if (choiceIndex >= 0) {
                    String correspondingEvent = redcapEventsList.getItems().get(choiceIndex);
                    redcapFormsList.setCellFactory(ComboBoxListCell.forListView(eventToFormsList.get(correspondingEvent)));
                }
            }
        });
    }

    /**
     * Fills the REDCap Variables ListView.
     * <p>
     * Places a Placeholder item into each index corresponding to a selected
     * field to prompt the user to make a selection, or if the selected field
     * has an eligible variable of the same name, it simply puts that variable
     * in. It then attaches a Listener to the ListView items, which configures
     * the variables of the item's ComboBox based on the corresponding form in
     * the form ListView.
     */
    @FXML
    private void fillVariablesList() {
        menuTracker.collectFormListData(redcapFormsList.getItems());
        populateVariablesMenus();

        LinkedHashMap<String, ArrayList> sheetToFields = excelParser.getSheetToFields();
        LinkedHashMap<String, ArrayList> selectedSheets = menuTracker.getSelectedSheetToFields();
        ObservableList defaultItems = FXCollections.observableArrayList();

        // Prepare the List Views
        redcapVariablesList.setDisable(false);
        redcapVariablesList.getItems().clear();
        redcapVariablesList.setEditable(true);
        convertButton.setDisable(true);

        // Put the Forms into the List View
        defaultItems.add(ITEM_EMPTY);
        int currentIndex = 1;
        for (String sheetName : sheetToFields.keySet()) {
            ArrayList<String> fields = sheetToFields.get(sheetName);

            // If the sheet wasn't chosen put empty items for it
            if (selectedSheets.get(sheetName) == null || selectedSheets.get(sheetName).isEmpty()) {
                defaultItems.add(ITEM_EMPTY);
                currentIndex++;
                for (int i = 0; i < fields.size(); i++) {
                    defaultItems.add(ITEM_EMPTY);
                    currentIndex++;
                }
            } // If the sheet was chosen fill it with event choice menus
            else {
                defaultItems.add(ITEM_EMPTY);
                currentIndex++;
                ArrayList<String> selectedFields = selectedSheets.get(sheetName);
                for (String field : fields) {
                    if (selectedFields.contains(field)) {
                        // If the chosen form for this field has a variable that's the
                        // same as the field itself then just print the field out
                        String correspondingForm = redcapFormsList.getItems().get(currentIndex);
                        if (formToVariablesList.get(correspondingForm).contains(field)) {
                            defaultItems.add(field);
                        } else {
                            defaultItems.add(VARIABLE_FIELD_PLACEHOLDER + field);
                        }
                    } else {
                        defaultItems.add(FIELD_EMPTY);
                    }
                    currentIndex++;
                }
            }
        }

        // Add the data to the List View
        redcapVariablesList.setItems(defaultItems);

        // Add the listener to update the ComboBox with the correct variables to choose
        // whenever the user clicks on the cell
        redcapVariablesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int choiceIndex = redcapVariablesList.getSelectionModel().getSelectedIndex();
                if (choiceIndex >= 0) {
                    String correspondingForm = redcapFormsList.getItems().get(choiceIndex);
                    redcapVariablesList.setCellFactory(ComboBoxListCell.forListView(formToVariablesList.get(correspondingForm)));
                }
            }
        });

        excelFieldTree.scrollTo(max(0, globalIndex - 6));
        redcapEventsList.scrollTo(max(0, globalIndex - 6));
        redcapFormsList.scrollTo(max(0, globalIndex - 6));
        redcapVariablesList.scrollTo(max(0, globalIndex - 6));

        checkAllVariablesChosen();
    }

    /**
     * Fills the REDCap Variables and selects the items in variableListData.
     * <p>
     * After calling fillVariablesList() to make all the other configurations
     * not related to the ListView's contents, this method then places the
     * eventListData ObservableList on the ListView.
     *
     * @param variableListData ObservableList containing, pre-selected choices
     * for the ListView
     */
    private void fillVariablesList(ObservableList<String> variableListData) {
        fillVariablesList();

        // Add the data to the List View
        redcapVariablesList.setItems(variableListData);

        // Add the listener to update the ComboBox with the correct variables to choose
        // whenever the user clicks on the cell
        redcapVariablesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int choiceIndex = redcapVariablesList.getSelectionModel().getSelectedIndex();
                if (choiceIndex >= 0) {
                    String correspondingForm = redcapFormsList.getItems().get(choiceIndex);
                    redcapVariablesList.setCellFactory(ComboBoxListCell.forListView(formToVariablesList.get(correspondingForm)));
                }
            }
        });
    }

    /**
     * Called whenever the user selects a ComboBox item from the Events
     * ListView.
     * <p>
     * Check whether an event for a sheet was chosen and if so, set the choice
     * for all the fields associated with the sheet to the same item, finally
     * call checkAllEventsChosen().
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void commitChosenEvent(ListView.EditEvent event) {
        int choiceIndex = event.getIndex();
        globalIndex = choiceIndex;

        // Set the new chosen value for the Event 
        //(This is a work-around for a bug in JavaFX in that the On Edit Commit 
        // listener doesn't assign new edit values)
        HashMap<Integer, String> indexToItemType = menuTracker.getIndexToItemType();
        ObservableList<String> listItems = redcapEventsList.getItems();
        listItems.set(choiceIndex, event.getNewValue().toString());

        // If a Sheet Event was chosen then assign the choice to all its fields too
        if (indexToItemType.get(choiceIndex).equals(menuTracker.SHEET_PLACEHOLDER)) {
            int index = choiceIndex + 1;
            ArrayList<Integer> itemsToChange = new ArrayList<>();

            // Collect the indices of the fields below the sheet
            do {
                if (!indexToItemType.get(index).equals(menuTracker.EMPTY_FIELD_PLACEHOLDER)) {
                    itemsToChange.add(index);
                }
                index++;
            } while (indexToItemType.get(index) != null
                    && !indexToItemType.get(index).equals(menuTracker.EMPTY_ITEM_PLACEHOLDER)
                    && !indexToItemType.get(index).equals(menuTracker.SHEET_PLACEHOLDER));

            // Change the values of the associated fields to the sheet choice
            ObservableList<String> newListItems = FXCollections.observableArrayList();
            for (int i = 0; i < listItems.size(); i++) {
                if (itemsToChange.contains(i)) {
                    newListItems.add(redcapEventsList.getItems().get(choiceIndex));
                } else {
                    newListItems.add(listItems.get(i));
                }
            }
            redcapEventsList.setItems(newListItems);
        }

        // Check whether all the events have been chosen
        checkAllEventsChosen();
    }

    /**
     * Check whether an event has been chosen for every selected field.
     * <p>
     * If so, then enable the toFormsButton to proceed to the next ListView.
     */
    private void checkAllEventsChosen() {
        if (toFormsButton.isDisabled()) {
            boolean eventListDone = true;
            // Check whether there is still an Event item that has the placeholder text
            for (String item : redcapEventsList.getItems()) {
                if (item.startsWith(EVENT_FIELD_PLACEHOLDER)) {
                    eventListDone = false;
                }
            }
            // If not, activate the toFormsButton
            if (eventListDone) {
                toFormsButton.setDisable(false);
            }
        }
    }

    /**
     * Called whenever the user selects a ComboBox item from the Forms ListView.
     * <p>
     * Check whether a form for a sheet was chosen and if so, set the choice for
     * all the fields associated with the sheet to the same item, finally call
     * checkAllFormsChosen().
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void commitChosenForm(ListView.EditEvent event) {
        int choiceIndex = event.getIndex();
        globalIndex = choiceIndex;

        // Set the new chosen value for the Form 
        //(This is a work-around for a bug in JavaFX in that the On Edit Commit 
        // listener doesn't assign new edit values)
        LinkedHashMap<Integer, String> indexToItemType = menuTracker.getIndexToItemType();
        ObservableList<String> listItems = redcapFormsList.getItems();
        listItems.set(choiceIndex, event.getNewValue().toString());
        redcapFormsList.setItems(listItems);

        // If a Sheet Event was chosen then assign the choice to all its fields too
        if (indexToItemType.get(choiceIndex).equals(menuTracker.SHEET_PLACEHOLDER)) {
            int index = choiceIndex + 1;
            ArrayList<Integer> itemsToChange = new ArrayList<>();

            // Collect the indices of the fields below the sheet
            do {
                if (!indexToItemType.get(index).equals(menuTracker.EMPTY_FIELD_PLACEHOLDER)) {
                    itemsToChange.add(index);
                }
                index++;
            } while (indexToItemType.get(index) != null
                    && !indexToItemType.get(index).equals(menuTracker.EMPTY_ITEM_PLACEHOLDER)
                    && !indexToItemType.get(index).equals(menuTracker.SHEET_PLACEHOLDER));

            // Change the values of the associated fields to the sheet choice
            ObservableList<String> newListItems = FXCollections.observableArrayList();
            for (int i = 0; i < listItems.size(); i++) {
                if (itemsToChange.contains(i)) {
                    newListItems.add(redcapFormsList.getItems().get(choiceIndex));
                } else {
                    newListItems.add(listItems.get(i));
                }
            }
            redcapFormsList.setItems(newListItems);
        }

        // Check whether all the events have been chosen
        checkAllFormsChosen();
    }

    /**
     * Check whether a form has been chosen for every selected field.
     * <p>
     * If so, then enable the toVariablesButton to proceed to the next ListView.
     */
    private void checkAllFormsChosen() {
        if (toVariablesButton.isDisabled()) {
            boolean formListDone = true;
            // Check whether there is still a Form item that has the placeholder text
            for (String item : redcapFormsList.getItems()) {
                if (item.startsWith(FORM_FIELD_PLACEHOLDER)) {
                    formListDone = false;
                }
            }
            // If not, activate the toVariablesButton
            if (formListDone) {
                toVariablesButton.setDisable(false);
            }
        }
    }

    /**
     * For every chosen event, place the corresponding forms in the
     * eventToFormsList.
     */
    private void populateFormsMenus() {
        eventToFormsList = new HashMap<>();
        ArrayList<String> chosenEvents = menuTracker.getChosenEvents();
        HashMap<String, ArrayList> eventToForm = instrumentParser.getEventToForm();
        // Iterate through the unique events chosen by the user
        for (String chosenEvent : chosenEvents) {
            ObservableList formsList = FXCollections.observableArrayList();
            ArrayList<String> correspondingForms = eventToForm.get(chosenEvent);
            // Iterate through the forms that correspond to this event
            for (int i = 0; i < correspondingForms.size(); i++) {
                formsList.add(correspondingForms.get(i));
            }
            // Associate the forms with the event
            eventToFormsList.put(chosenEvent, formsList);
        }
    }

    /**
     * Called whenever the user selects a ComboBox item from the Variables
     * ListView.
     * <p>
     * Update item and, if the program is working on Repeating Rows, remove the
     * variable so it can't be selected again. Then call
     * checkAllVariablesChosen().
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void commitChosenVariable(ListView.EditEvent event) {
        int choiceIndex = event.getIndex();
        globalIndex = choiceIndex;

        // Set the new chosen value for the Form 
        //(This is a work-around for a bug in JavaFX in that the On Edit Commit 
        // listener doesn't assign new edit values)
        ObservableList<String> listItems = redcapVariablesList.getItems();
        listItems.set(choiceIndex, event.getNewValue().toString());
        redcapVariablesList.setItems(listItems);

        // If working on Repeating Rows, remove the chosen Variable from the formToVariables list
        if (repeatingRowsForms || repeatingRowsEvents) {
            String correspondingForm = redcapFormsList.getItems().get(choiceIndex);
            ObservableList variablesList = formToVariablesList.get(correspondingForm);
            variablesList.remove(event.getNewValue().toString());
//            formToVariablesList.put(correspondingForm, variablesList);
        }

        // Check whether all the variables have been chosen
        checkAllVariablesChosen();
    }

    /**
     * Check whether a variable has been chosen for every selected field.
     * <p>
     * If so, then enable the convertButton to proceed to conversion.
     */
    private void checkAllVariablesChosen() {
        if (convertButton.isDisabled()) {
            boolean variablesListDone = true;
            // Check whether there is still a Variable item that has the placeholder text
            for (String item : redcapVariablesList.getItems()) {
                if (item.startsWith(VARIABLE_FIELD_PLACEHOLDER)) {
                    variablesListDone = false;
                }
            }
            // If not, activate the convertButton
            if (variablesListDone) {
                convertButton.setDisable(false);
            }
        }
    }

    /**
     * For every chosen form, place the corresponding variable in the
     * formToVariablesList.
     */
    private void populateVariablesMenus() {
        formToVariablesList = new HashMap<>();
        ArrayList<String> chosenForms = menuTracker.getChosenForms();
        HashMap<String, ArrayList> formToVariables = dictionaryParser.getFormToVariables();
        // Iterate through the unique forms chosen by the user
        System.out.println(chosenForms);
        for (String chosenForm : chosenForms) {
            ObservableList variablesList = FXCollections.observableArrayList();
            System.out.println(formToVariables);
            ArrayList<String> correspondingVariables = formToVariables.get(chosenForm);
            // Iterate through the variables that correspond to this form
            System.out.println(correspondingVariables);
            for (int i = 0; i < correspondingVariables.size(); i++) {
                variablesList.add(correspondingVariables.get(i));
            }
            // Associate the variables with the form
            formToVariablesList.put(chosenForm, variablesList);
        }
    }

    /**
     * Called whenever the user clicks inside the TreeView or one of the
     * ListViews.
     * <p>
     * Synchronize the selected indices of all the other Views.
     *
     * @param event Event data for the interface action.
     */
    @FXML
    private void selectIndex(MouseEvent event) {
        int index;
        // Try and get the index of the selected ListView
        try {
            ListView selectedListView = (ListView) event.getSource();
            index = selectedListView.getSelectionModel().getSelectedIndex();
        } // If an exception is thrown then it much be a TreeView instead
        catch (Exception ex) {
            TreeView selectedTreeView = (TreeView) event.getSource();
            index = selectedTreeView.getSelectionModel().getSelectedIndex();
        }
        // Synchronize the selected indices
        excelFieldTree.getSelectionModel().select(index);
        redcapEventsList.getSelectionModel().select(index);
        redcapFormsList.getSelectionModel().select(index);
        redcapVariablesList.getSelectionModel().select(index);
    }

    public void setExcelParser(ExcelParser excelParser) {
        this.excelParser = excelParser;
    }

    public void setInstrumentParser(InstrumentDesigParser instrumentParser) {
        this.instrumentParser = instrumentParser;
    }

    public void setDataDictionaryParser(DataDictionaryParser dictionaryParser) {
        this.dictionaryParser = dictionaryParser;
    }

    public void setRepeatingRowsForms(boolean repeatingRowsForms) {
        this.repeatingRowsForms = repeatingRowsForms;
    }
    
    public void setRepeatingRowsEvents(boolean repeatingRowsEvents) {
        this.repeatingRowsEvents = repeatingRowsEvents;
    }

    public void setDefaultValues(boolean defaultValues) {
        this.defaultValues = defaultValues;
    }

    /**
     * Bind the scroll panes of the excelFieldTree and redcapLists together.
     */
    public void bindScrollPanes() {
        if (notBound) {
            // Bind the ListView scroll properties
            ScrollBar bar1 = (ScrollBar) excelFieldTree.lookup(".scroll-bar");
            ScrollBar bar2 = (ScrollBar) redcapEventsList.lookup(".scroll-bar");
            ScrollBar bar3 = (ScrollBar) redcapFormsList.lookup(".scroll-bar");
            ScrollBar bar4 = (ScrollBar) redcapVariablesList.lookup(".scroll-bar");
            bar1.valueProperty().bindBidirectional(bar2.valueProperty());
            bar2.valueProperty().bindBidirectional(bar3.valueProperty());
            bar3.valueProperty().bindBidirectional(bar4.valueProperty());
            notBound = false;
        }

//        if(scrollPosition1 == null || !scrollPosition1.isBound()){
//            System.out.println("Binding");
//            scrollPosition1 = new SimpleDoubleProperty();
//            scrollPosition1.bind(bar1.valueProperty());
//            scrollPosition1.addListener(new ChangeListener() {
//                @Override
//                public void changed(ObservableValue arg0, Object arg1, Object arg2) {
//                    bar2.valueProperty().set((double) arg2);
//                    bar3.valueProperty().set((double) arg2);
//                    bar4.valueProperty().set((double) arg2);
//                }
//            }); 
//
//            scrollPosition2 = new SimpleDoubleProperty();
//            scrollPosition2.bind(bar2.valueProperty());
//            scrollPosition2.addListener(new ChangeListener() {
//                @Override
//                public void changed(ObservableValue arg0, Object arg1, Object arg2) {
//                    bar1.valueProperty().set((double) arg2);
//                    bar3.valueProperty().set((double) arg2);
//                    bar4.valueProperty().set((double) arg2);
//                }
//            });
//
//            scrollPosition3 = new SimpleDoubleProperty();
//            scrollPosition3.bind(bar3.valueProperty());
//            scrollPosition3.addListener(new ChangeListener() {
//                @Override
//                public void changed(ObservableValue arg0, Object arg1, Object arg2) {
//                    bar1.valueProperty().set((double) arg2);
//                    bar2.valueProperty().set((double) arg2);
//                    bar4.valueProperty().set((double) arg2);
//                }
//            });
//
//            scrollPosition4 = new SimpleDoubleProperty();
//            scrollPosition4.bind(bar4.valueProperty());
//            scrollPosition4.addListener(new ChangeListener() {
//                @Override
//                public void changed(ObservableValue arg0, Object arg1, Object arg2) {
//                    bar2.valueProperty().set((double) arg2);
//                    bar3.valueProperty().set((double) arg2);
//                    bar4.valueProperty().set((double) arg2);
//                }
//            });
//        }
    }
}
