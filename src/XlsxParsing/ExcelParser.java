package XlsxParsing;

import Debugging.DebugReporter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <h1>XLSX Excel file parser.</h1>
 * <p>
 * This class is used to parse Excel file data taken from the XlsxReader into
 * other data structures that can be used by the program.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class ExcelParser {

    /**
     * ArrayList containing fields ArrayLists.
     * <p>
     * I.e: [[field, field, field], [field, field, field], ...]
     */
    private ArrayList<ArrayList> sheets;

    /**
     * Maps the sheets of the Excel to its fields.
     * <p>
     * I.e: {sheet : [field, field, field]}
     */
    private LinkedHashMap<String, ArrayList> sheetToFields;

    /**
     * Maps the sheets of the Excel to its indices to its fields.
     * <p>
     * I.e: {sheet : {index : field, index : field, index : field}}
     */
    private LinkedHashMap<String, LinkedHashMap> sheetToIndices;

    /**
     * Maps the patientID to the sheet to the field to the value it has.
     * <p>
     * I.e. {patientID : {sheet : {field : value}}}
     */
    private LinkedHashMap<String, LinkedHashMap> patientToSheets;

    private File xlsxFile;
    private HashMap<Integer, String> indexToSheet;
    private DebugReporter debugReporter;

    /**
     * Get the Excel file and read it with an XlsxReader object, they get the
     * necessary data from it.
     *
     * @param xlsxFile The file to be read and parsed.
     */
    public ExcelParser(File xlsxFile) {
        this.xlsxFile = xlsxFile;
        XlsxReader reader = new XlsxReader(xlsxFile);
        reader.readWorkbook();
        sheets = reader.getSheets();
        indexToSheet = reader.getIndexToSheet();
    }

    /**
     * Iterate through the sheets of the read XLSX file and collect its fields.
     * <p>
     * First iterate through the sheets of the Excel, then iterate through the
     * first row of each sheet to collect the fields (headers), then use them to
     * populate the {@link #sheetToFields sheetToFields} and
     * {@link #sheetToIndices sheetToIndices} LinkedHashMaps.
     */
    public void parseHeaders() {
        LinkedHashMap<Integer, String> indexToField;
        ArrayList<String> fields;
        sheetToFields = new LinkedHashMap<>();
        sheetToIndices = new LinkedHashMap<>();

        // Iterate through the sheets
        for (int i = 0; i < sheets.size(); i++) {
            fields = new ArrayList<>();
            indexToField = new LinkedHashMap<>();
            ArrayList firstRow = (ArrayList) sheets.get(i).get(0);

            // Iterate through the first row of each sheet
            for (int j = 0; j < firstRow.size(); j++) {
                // If the field is not empty, add it to the ArrayList
                if (firstRow.get(j) != null) {
                    fields.add(firstRow.get(j).toString());
                    indexToField.put(j, firstRow.get(j).toString());
                }
            }

            // Associate all the fields on this sheet with the sheet itself
            sheetToFields.put(indexToSheet.get(i), fields);
            sheetToIndices.put(indexToSheet.get(i), indexToField);
        }
    }

    /**
     * Iterate through the patientIDs and collect each patient's data
     * considering repeating column values.
     * <p>
     * First iterate through the patientIDs then for each patientID iterate
     * through the Excel sheets, if the sheet was selected by a user then
     * iterate through the rows of the sheet, if there is a row containing data
     * for the patientID then parse it, in the end filling the
     * {@link #patientToSheets patientToSheets} LinkedHashMap.
     *
     * @param selectedSheets Contains the sheets selected by the user.
     */
    public void parseRepeatingColumns(LinkedHashMap<String, ArrayList> selectedSheets) {
        LinkedHashMap<String, LinkedHashMap> sheetToFieldToValue;
        LinkedHashMap<String, String> fieldToValue;
        ArrayList rows, cells;
        ArrayList<String> patientIDs;

        patientToSheets = new LinkedHashMap<>();

        debugReporter = new DebugReporter("PatientParser.txt");

        // Collect all the patientID's
        patientIDs = collectPatientIDs(sheets);

        // Iterate through the patientIDs that were just collected
        for (int i = 0; i < patientIDs.size(); i++) {
            String patientID = patientIDs.get(i);
            sheetToFieldToValue = new LinkedHashMap<>();

            debugReporter.writeLn(System.lineSeparator() + "Looking at patientID: " + patientID);

            // Iterate through the sheets
            for (int j = 0; j < sheets.size(); j++) {
                String sheet = indexToSheet.get(j);

                // If this sheet has been selected on the GUI then consider the data in it
                if (selectedSheets.containsKey(sheet)) {
                    debugReporter.writeLn(System.lineSeparator() + "Looking at sheet: " + sheet);

                    ArrayList selectedFields = selectedSheets.get(sheet);
                    LinkedHashMap indexToField = sheetToIndices.get(sheet);
                    rows = sheets.get(j);

                    // Iterate through the rows of the current sheet
                    for (int k = 1; k < rows.size(); k++) {
                        cells = (ArrayList) rows.get(k);

                        // If we find a row with data for this patientID
                        if (cells.get(0) != null && cells.get(0).equals(patientID)) {
                            debugReporter.writeLn("Looking at row: " + k);

                            fieldToValue = new LinkedHashMap<>();

                            // Iterate through the cells of the row
                            for (int l = 0; l < cells.size(); l++) {

                                // If this cell belongs to a field that has been selected on the GUI
                                if (indexToField.get(l) != null && selectedFields.contains(indexToField.get(l).toString()) && (cells.get(l) != null)) {
                                    debugReporter.writeLn("Storing cell: " + cells.get(l).toString() + ", from field: " + indexToField.get(l).toString());
                                    fieldToValue.put(indexToField.get(l).toString(), cells.get(l).toString());
                                }
                            }

                            sheetToFieldToValue.put(sheet, fieldToValue);
                        }
                    }

                    // If data for this patientID is found in at least one sheet
                    if (!sheetToFieldToValue.isEmpty()) {
                        patientToSheets.put(patientID, sheetToFieldToValue);
                    }
                }
            }
        }

        reportFinalData(patientToSheets);
        debugReporter.close();
    }

    /**
     * Iterate through the patientIDs and collect each patient's data
     * considering repeating row values.
     * <p>
     * First iterate through the patientIDs then for each patientID iterate
     * through the Excel sheets, if the sheet was selected by a user then
     * iterate through the rows of the sheet, if there is a row containing data
     * for the patientID then parse it, in the end filling the
     * {@link #patientToSheets patientToSheets} LinkedHashMap.
     *
     * @param selectedSheets Contains the sheets selected by the user.
     */
    public void parseRepeatingRows(LinkedHashMap<String, ArrayList> selectedSheets) {
        LinkedHashMap<String, LinkedHashMap> sheetToFieldToValue;
        LinkedHashMap<String, ArrayList> fieldToValues;
        ArrayList rows, cells;
        ArrayList<String> patientIDs, values;

        patientToSheets = new LinkedHashMap<>();

        debugReporter = new DebugReporter("PatientParser.txt");

        // Collect all the patientID's 
        patientIDs = collectPatientIDs(sheets);

        // Iterate through the patientIDs that were just collected
        for (int i = 0; i < patientIDs.size(); i++) {
            String patientID = patientIDs.get(i);
            sheetToFieldToValue = new LinkedHashMap<>();

            debugReporter.writeLn(System.lineSeparator() + "Looking at patientID: " + patientID);

            // Iterate through the sheets
            for (int j = 0; j < sheets.size(); j++) {
                String sheet = indexToSheet.get(j);

                // If this sheet has been selected on the GUI then consider the data in it
                if (selectedSheets.containsKey(sheet)) {
                    debugReporter.writeLn(System.lineSeparator() + "Looking at sheet: " + sheet);

                    ArrayList selectedFields = selectedSheets.get(sheet);
                    LinkedHashMap indexToField = sheetToIndices.get(sheet);
                    rows = sheets.get(j);
                    fieldToValues = new LinkedHashMap<>();

                    // Iterate through the rows of the current sheet
                    for (int k = 1; k < rows.size(); k++) {
                        cells = (ArrayList) rows.get(k);

                        // If we find a row with data for this patientID
                        if (cells.get(0) != null && cells.get(0).equals(patientID)) {
                            debugReporter.writeLn("Looking at row: " + k);

                            // Iterate through the cells of the row
                            for (int l = 0; l < cells.size(); l++) {

                                // If this cell belongs to a field that has been selected on the GUI
                                if (indexToField.get(l) != null && selectedFields.contains(indexToField.get(l).toString()) && cells.get(l) != null) {
                                    debugReporter.writeLn("Storing cell: " + cells.get(l).toString() + ", from field: " + indexToField.get(l).toString());

                                    // If this cell is for a new field then create a new values ArrayList for it
                                    if (fieldToValues.get(indexToField.get(l).toString()) == null) {
                                        values = new ArrayList<>();
                                        values.add(cells.get(l).toString());
                                        fieldToValues.put(indexToField.get(l).toString(), values);
                                    } // If this cell is for a field that has been checked before then add it to the values ArrayList
                                    else {
                                        values = fieldToValues.get(indexToField.get(l).toString());
                                        values.add(cells.get(l).toString());
                                        fieldToValues.put(indexToField.get(l).toString(), values);
                                    }
                                }
                            }

                            sheetToFieldToValue.put(sheet, fieldToValues);
                        }
                    }

                    // If this patientID is found in at least one sheet
                    if (!sheetToFieldToValue.isEmpty()) {
                        patientToSheets.put(patientID, sheetToFieldToValue);
                    }
                }
            }
        }

        reportFinalData(patientToSheets);
        debugReporter.close();
    }

    /**
     * Collect all the unique patientIDs from the first sheet of the Excel file.
     *
     * @param sheets The sheets to parse.
     * @return List of patientIDs.
     */
    private ArrayList<String> collectPatientIDs(ArrayList<ArrayList> sheets) {
        ArrayList<String> patientIDs = new ArrayList<>();

        // Collect all the patientID's from the first sheet
        ArrayList rows = sheets.get(0);
        for (int i = 1; i < rows.size(); i++) {
            ArrayList cells = (ArrayList) rows.get(i);
            if (cells.get(0) != null) {
                patientIDs.add(cells.get(0).toString());
            }
        }

        return patientIDs;
    }

    /**
     * Write the resulting {@link #patientToSheets patientToSheets}
     * LinkedHashMap to the Debug file.
     *
     * @param patientToSheets 
     */
    private void reportFinalData(LinkedHashMap<String, LinkedHashMap> patientToSheets) {
        debugReporter.writeLn(System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + "patientToSheets Final Data" + System.lineSeparator());
        for (String patientID : patientToSheets.keySet()) {
            debugReporter.writeLn("patientID: " + patientID);
            LinkedHashMap<String, LinkedHashMap> sheetToFields = patientToSheets.get(patientID);
            for (String sheet : sheetToFields.keySet()) {
                debugReporter.writeLn("sheet: " + sheet);
                LinkedHashMap fieldToValue = sheetToFields.get(sheet);
                for (Object field : fieldToValue.keySet()) {
                    debugReporter.write("field: " + field.toString() + " - value: " + fieldToValue.get(field).toString() + "   ");
                }
            }
            debugReporter.writeLn();
        }
    }

    public LinkedHashMap<String, LinkedHashMap> getPatientToSheets() {
        return patientToSheets;
    }

    public LinkedHashMap<String, ArrayList> getSheetToFields() {
        return sheetToFields;
    }

    public LinkedHashMap<String, LinkedHashMap> getSheetToIndices() {
        return sheetToIndices;
    }

    public File getFile() {
        return xlsxFile;
    }
}
