package GUI.Utils;

import GUI.Excel_To_ODM_XML;
import java.util.prefs.Preferences;

/**
 * Opens and saves registry entries of the
 * {@link GUI.MainSceneController MainSceneController} file directories and
 * options selected by the user.
 * <p>
 * The class writes and reads the file choice directories and options in the
 * registry with the use of registry keys, one for each file (DATA_KEY,
 * INSTRUMENT_KEY, EXCEL_KEY), and option (REPETITION_KEY, DEFAULT_KEY).
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class FileChoiceRecorder {

    /**
     * Registry key for the Data Dictionary directory.
     */
    private static final String DATA_KEY = "ZIH.Data";
    /**
     * Registry key for the Instrument Designation directory.
     */
    private static final String INSTRUMENT_KEY = "ZIH.Instrument";
    /**
     * Registry key for the Excel File directory.
     */
    private static final String EXCEL_KEY = "ZIH.Excel";
    /**
     * Registry key for the
     * {@link GUI.MainSceneController#repeatingRows repeatingRowsForms} boolean.
     */
    private static final String REPETITION_KEY_FORMS = "ZIH.RepetitionForms";
    /**
     * Registry key for the
     * {@link GUI.MainSceneController#repeatingRows repeatingRowsEvents} boolean.
     */
    private static final String REPETITION_KEY_EVENTS = "ZIH.RepetitionEvents";
    /**
     * Registry key for the
     * {@link GUI.MainSceneController#defaultValues defaultValues} boolean.
     */
    private static final String DEFAULT_KEY = "ZIH.Default";

    /**
     * Lets other classes know if there is already a registry entry.
     */
    private boolean exists;
    private String dataDir, instrumentDir, excelDir, repeatingRowsForms, repeatingRowsEvents, defaultValues;
    private Preferences userPref;

    /**
     * Create a userPref object and check if there is already a registry entry.
     * <p>
     * A userPref object is used to write to and read from the registry. If the
     * userPref can find a registry entry corresponding to the key for the
     * DataDictionary then it will read from the existing registry and
     * initializes {@link #exists exists} to true, otherwise false.
     */
    public FileChoiceRecorder() {
        userPref = Preferences.userNodeForPackage(Excel_To_ODM_XML.class);
        if (userPref.get(DATA_KEY, null) != null) {
            readExistingRegistry();
            exists = true;
        } else {
            exists = false;
        }
    }

    /**
     * Write to the Registry.
     */
    public void writeToRegistry() {
        userPref.put(DATA_KEY, getDataDir());
        userPref.put(INSTRUMENT_KEY, getInstrumentDir());
        userPref.put(EXCEL_KEY, getExcelDir());
        userPref.put(REPETITION_KEY_FORMS, getRepeatingRowsForms());
        userPref.put(REPETITION_KEY_EVENTS, getRepeatingRowsEvents());
        userPref.put(DEFAULT_KEY, getDefaultValues());
    }

    /**
     * @return {@link #exists exists}
     */
    public boolean checkExists() {
        return exists;
    }

    /**
     * Read from the Registry.
     */
    private void readExistingRegistry() {
        setDataDir(userPref.get(DATA_KEY, null));
        setInstrumentDir(userPref.get(INSTRUMENT_KEY, null));
        setExcelDir(userPref.get(EXCEL_KEY, null));
        setRepeatingRowsForms(userPref.get(REPETITION_KEY_FORMS, null));
        setRepeatingRowsEvents(userPref.get(REPETITION_KEY_EVENTS, null));
        setDefaultValues(userPref.get(DEFAULT_KEY, null));
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getInstrumentDir() {
        return instrumentDir;
    }

    public String getExcelDir() {
        return excelDir;
    }

    public String getRepeatingRowsForms() {
        return repeatingRowsForms;
    }
    
    public String getRepeatingRowsEvents() {
        return repeatingRowsEvents;
    }

    public String getDefaultValues() {
        return defaultValues;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public void setInstrumentDir(String instrumentDir) {
        this.instrumentDir = instrumentDir;
    }

    public void setExcelDir(String excelDir) {
        this.excelDir = excelDir;
    }

    public void setRepeatingRowsForms(String repeatingRowsForms) {
        this.repeatingRowsForms = repeatingRowsForms;
    }
    
    public void setRepeatingRowsEvents(String repeatingRowsEvents) {
        this.repeatingRowsEvents = repeatingRowsEvents;
    }

    public void setDefaultValues(String defaultValues) {
        this.defaultValues = defaultValues;
    }
}
