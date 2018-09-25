package GUI.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Opens and saves progress files containing user choices on the
 * {@link GUI.FieldCustomizerController FieldCustomizer} window.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class ProgressFileHandler {

  private File progressFile, xlsxFile, instrumentFile, dictionaryFile;
  private ObservableList<String> eventListData, formListData, variableListData;
  
  /**
   * See the {@link MenuTracker#selectedSheetToFields menuTracker} class.
   */
  private LinkedHashMap<String, ArrayList> selectedSheetToFields;
  /**
   * Integer to represent on which ListView/TreeView the user was working.
   */
  private int status;

  /**
   * Get the Progress File and check if it already exists.
   * 
   * @param progressFile The progress file be read from or written to.
   */
  public ProgressFileHandler(File progressFile) {
    this.progressFile = progressFile;
  }

  /**
   * Write the user's progress to the Progress file.
   * 
   * @param xlsxFile Excel file.
   * @param instrumentFile Instrument Designation file.
   * @param dictionaryFile Data Dictionary file.
   * @param selectedSheetToFields See: {@link MenuTracker#selectedSheetToFields MenuTracker.selectedSheetToFields}.
   * @param eventListData REDCap events ListView data.
   * @param formListData REDCap forms ListView data.
   * @param variableListData REDCap variables ListView data.
   */
  public void writeProgress(File xlsxFile, File instrumentFile, File dictionaryFile,
          LinkedHashMap<String, ArrayList> selectedSheetToFields, ObservableList eventListData,
          ObservableList formListData, ObservableList variableListData) {

    // Determine the status code for the file
    // 1 = Only Excel Field tree has been filled
    // 2 = Excel Field Tree and Redcap Events list have been filled
    // 3 = Excel Field Tree, Redcap Events list, and Forms list have been filled
    // 4 = Excel Field Tree, Redcap Events list, Forms list, and Variables List have been filled
    if (eventListData.isEmpty() && formListData.isEmpty() && variableListData.isEmpty()) {
      status = 1;
    } else if (!eventListData.isEmpty() && formListData.isEmpty() && variableListData.isEmpty()) {
      status = 2;
    } else if (!eventListData.isEmpty() && !formListData.isEmpty() && variableListData.isEmpty()) {
      status = 3;
    } else if (!eventListData.isEmpty() && !formListData.isEmpty() && !variableListData.isEmpty()) {
      status = 4;
    }

    FileOutputStream fOut = null;
    try {
      // Create the Output Object Stream
      fOut = new FileOutputStream(progressFile);
      ObjectOutputStream out = new ObjectOutputStream(fOut);

      // Write the objects to the file
      out.writeObject(xlsxFile);
      out.writeObject(instrumentFile);
      out.writeObject(dictionaryFile);
      out.writeObject(status);
      out.writeObject(selectedSheetToFields);
      out.writeObject(eventListData.toArray());
      out.writeObject(formListData.toArray());
      out.writeObject(variableListData.toArray());
      out.flush();
    } catch (FileNotFoundException ex) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "FileNotFoundException: " + ex.getLocalizedMessage(), ButtonType.OK);
      alert.showAndWait();
      Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "IOException: " + ex.getLocalizedMessage(), ButtonType.OK);
      alert.showAndWait();
      Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        // After everything, close the file
        fOut.close();
      } catch (IOException ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "IOException: " + ex.getLocalizedMessage(), ButtonType.OK);
        alert.showAndWait();
        Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Read the user's progress from the Progress file.
   */
  public void readProgress() {
    FileInputStream fIn = null;
    try {
      // Create the Output Input Stream
      fIn = new FileInputStream(progressFile);
      ObjectInputStream in = new ObjectInputStream(fIn);

      // Read the objects from the file
      xlsxFile = (File) in.readObject();
      instrumentFile = (File) in.readObject();
      dictionaryFile = (File) in.readObject();
      status = (int) in.readObject();
      selectedSheetToFields = (LinkedHashMap<String, ArrayList>) in.readObject();

      // ObservableList is not Serializable so the ListView data has to be 
      // stored and read in Object[] format, then converted to ObservableLists
      Object[] eventListObject = (Object[]) in.readObject();
      if (eventListObject.length > 0) {
        eventListData = FXCollections.observableArrayList();
        for (int i = 0; i < eventListObject.length; i++) {
          eventListData.add(i, eventListObject[i].toString());
        }
      }

      Object[] formListObject = (Object[]) in.readObject();
      if (formListObject.length > 0) {
        formListData = FXCollections.observableArrayList();
        for (int i = 0; i < eventListObject.length; i++) {
          formListData.add(i, formListObject[i].toString());
        }
      }

      Object[] variableListObject = (Object[]) in.readObject();
      if (variableListObject.length > 0) {
        variableListData = FXCollections.observableArrayList();
        for (int i = 0; i < eventListObject.length; i++) {
          variableListData.add(i, variableListObject[i].toString());
        }
      }
    } catch (FileNotFoundException ex) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "FileNotFoundException: " + ex.getLocalizedMessage(), ButtonType.OK);
      alert.showAndWait();
      Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException | ClassNotFoundException ex) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "IOException/ClassNotFoundException: " + ex.getLocalizedMessage(), ButtonType.OK);
      alert.showAndWait();
      Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        fIn.close();
      } catch (IOException ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "IOException: " + ex.getLocalizedMessage(), ButtonType.OK);
        alert.showAndWait();
        Logger.getLogger(ProgressFileHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * @return Represents the existence of the Progress file.
   */
  public boolean fileExists() {
    return progressFile.exists();
  }

  public File getXlsxFile() {
    return xlsxFile;
  }

  public File getInstrumentFile() {
    return instrumentFile;
  }

  public File getDictionaryFile() {
    return dictionaryFile;
  }

  public int getStatus() {
    return status;
  }

  public LinkedHashMap<String, ArrayList> getSelectedSheetToFields() {
    return selectedSheetToFields;
  }

  public ObservableList<String> getEventListData() {
    return eventListData;
  }

  public ObservableList<String> getFormListData() {
    return formListData;
  }

  public ObservableList<String> getVariableListData() {
    return variableListData;
  }
}
