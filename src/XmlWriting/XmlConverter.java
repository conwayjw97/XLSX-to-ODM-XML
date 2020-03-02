package XmlWriting;

import CsvParsing.DataDictionaryParser;
import CsvParsing.InstrumentDesigParser;
import Debugging.DebugReporter;
import GUI.Utils.MenuTracker;
import XlsxParsing.ExcelParser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.w3c.dom.Element;

/**
 * <h1>User Choice and Parsed data converter for XML writing.</h1>
 * <p>
 * Takes the {@link GUI.Utils.MenuTracker MenuTracker} and Parser data and then
 * converts it into a form that the XmlWriter can write to the final XML file.
 * It has two main methods: 'convertRepeatingColumns' with a helper function
 * 'fieldFinderRepeatingColumns', and 'convertRepeatingRows' with a helper
 * function 'fieldFinderRepeatingRows'. The conversion process had to be divided
 * between these two main methods because of the algorithmic difference of
 * performing a conversion on data that has Repeating Columns, or Repeating
 * Rows.
 *
 * @author James Conway
 * @since 2018-07-19
 */
public class XmlConverter {

    private DebugReporter debugReporter;
    private MenuTracker menuTracker;
    private InstrumentDesigParser instrumentParser;
    private DataDictionaryParser dictionaryParser;
    private ExcelParser excelParser;
    private XmlWriter xmlWriter;
    private File xmlFile;
    private boolean defaultValues;

    /**
     * @param menuTracker Contains user's REDCap choices
     * @param instrumentParser Has EventToForm mappings
     * @param dictionaryParser Has FormToVariable and VariableToDefault mappings
     * @param excelParser Has patient data
     * @param xmlFile Name of the XML file to be created
     * @param defaultValues Determines whether there are default values
     */
    public XmlConverter(MenuTracker menuTracker, InstrumentDesigParser instrumentParser,
            DataDictionaryParser dictionaryParser, ExcelParser excelParser,
            File xmlFile, boolean defaultValues) {
        this.menuTracker = menuTracker;
        this.instrumentParser = instrumentParser;
        this.dictionaryParser = dictionaryParser;
        this.excelParser = excelParser;
        this.xmlFile = xmlFile;
        this.defaultValues = defaultValues;
        xmlWriter = new XmlWriter(xmlFile);
    }

    /**
     * Builds the XML file for when the Excel contains repeating values across
     * columns.
     * <p>
     * First iterate through the patientIDs and create a SubjectData element
     * then for each patientID iterate through the user chosen REDCap events and
     * create a StudyEventData element, for each event iterate through the
     * corresponding chosen RedCAP forms and create FormData element. Finally
     * for each form, create an ItemData element and then if necessary add their
     * default values. 
     */
    public void convertRepeatingColumns() {
        boolean createGroupData;
        ArrayList<String> visitedFields;

        // Gather necessary data
        LinkedHashMap<String, LinkedHashMap> patientToSheets = excelParser.getPatientToSheets();
        HashMap<String, ArrayList> eventToForm = instrumentParser.getEventToForm();
        LinkedHashMap<String, LinkedHashMap> sheetToFieldToVariable = menuTracker.getSheetToFieldToVariable();
        HashMap<String, Integer> formToOccurence = menuTracker.getFormToOccurence();
        HashMap<String, ArrayList> formToVariables = menuTracker.getFormToVariables();
        HashMap<String, ArrayList> formToAllVariables = dictionaryParser.getFormToVariables();
        HashMap<String, String> variableToDefault = dictionaryParser.getVariableToDefault();

        debugReporter = new DebugReporter("XmlConverter.txt");

        xmlWriter.createDocument();

        // Iterate through the parsed patientIDs
        for (String patientID : patientToSheets.keySet()) {
            debugReporter.writeLn("-----------------------------------------------------------");
            debugReporter.writeLn("Working on patientID: " + patientID);

            Element subjectData = xmlWriter.createSubjectData(patientID);

            // Iterate through the chosen events
            for (String event : menuTracker.getChosenEvents()) {
                debugReporter.writeLn("\nWorking on chosen event: " + event);

                Element studyEventData = xmlWriter.createStudyEventData(event, 1, subjectData);

                // Iterate through the chosen forms
                for (String form : menuTracker.getChosenForms()) {

                    // If this form corresponds to the current event
                    if (eventToForm.get(event).contains(form)) {
                        debugReporter.writeLn("\nWorking on chosen form: " + form);
                        visitedFields = new ArrayList<>();

                        // Iterate through the number of occurences this form has
                        for (int i = 0; i < formToOccurence.get(form); i++) {

                            // If this is the first ItemData to be put in this FormData then
                            // we use this variable to check if a new ItemGroupData element 
                            // needs to be made to put the new ItemData elements in
                            createGroupData = true;
                            Element formData = xmlWriter.createFormData(form, i + 1, studyEventData);
                            ArrayList<String> chosenVariables = formToVariables.get(form);

                            // Iterate through the chosen variables
                            for (String variable : chosenVariables) {
                                debugReporter.writeLn("\nWorking on chosen variable: " + variable);

                                // Find the corresponding variable and field              
                                HashMap<String, String> results = fieldFinderRepeatingColumns(variable, sheetToFieldToVariable,
                                        visitedFields, patientToSheets, patientID);
                                visitedFields.add(results.get("field"));

                                // Add them to the XML
                                xmlWriter.createItemData(variable, form, createGroupData, results.get("value"), formData);
                                createGroupData = false;
                            }

                            // If default values have to be added too
                            if (defaultValues) {
                                List<String> unchosenVariables = formToAllVariables.get(form);
                                unchosenVariables.removeAll(chosenVariables);
                                // Iterate through the unchosen variables
                                for (String variable : unchosenVariables) {
                                    if (variableToDefault.keySet().contains(variable)) {
                                        debugReporter.writeLn("\nWorking on unchosen variable: " + variable);

                                        // Find the corresponding variable and field   
                                        xmlWriter.createItemData(variable, form, createGroupData, variableToDefault.get(variable), formData);
                                        createGroupData = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        xmlWriter.saveDocument();

        debugReporter.writeLn("Done!");
        debugReporter.close();
    }

    /**
     * Builds the XML file for when the Excel contains repeating values across
     * rows.
     * <p>
     * First iterate through the patientIDs and create a SubjectData element
     * then for each patientID iterate through the user chosen REDCap events and
     * create a StudyEventData element, for each event iterate through the
     * corresponding chosen RedCAP forms and create FormData element. Finally
     * for each form, create the necessary number of repeating FormData elements
     * and fill them with their respective ItemData elements.
     */
    public void convertRepeatingRows(boolean formsInsteadOfEvents) {
        boolean createGroupData;

        // Gather necessary data
        LinkedHashMap<String, LinkedHashMap> patientToSheets = excelParser.getPatientToSheets();
        HashMap<String, ArrayList> eventToForm = instrumentParser.getEventToForm();
        LinkedHashMap<String, LinkedHashMap> sheetToFieldToVariable = menuTracker.getSheetToFieldToVariable();
        HashMap<String, ArrayList> formToVariables = menuTracker.getFormToVariables();
        HashMap<String, ArrayList> formToAllVariables = dictionaryParser.getFormToVariables();
        HashMap<String, String> variableToDefault = dictionaryParser.getVariableToDefault();

        debugReporter = new DebugReporter("XmlConverter.txt");

        xmlWriter.createDocument();

        // Iterate through the parsed patientIDs
        for (String patientID : patientToSheets.keySet()) {
            debugReporter.writeLn("-----------------------------------------------------------");
            debugReporter.writeLn("Working on patientID: " + patientID);

            Element subjectData = xmlWriter.createSubjectData(patientID);
            
            // If this is for repeating form rows
            if(formsInsteadOfEvents){
	            // Iterate through the chosen events
	            for (String event : menuTracker.getChosenEvents()) {
	                debugReporter.writeLn("\nWorking on chosen event: " + event);
	                Element studyEventData = xmlWriter.createStudyEventData(event, 1, subjectData);
	
	                // Iterate through the chosen forms
	                for (String form : menuTracker.getChosenForms()) {
	
	                    // If this form corresponds to the current event
	                    if (eventToForm.get(event).contains(form)) {
	                        debugReporter.writeLn("\nWorking on chosen form: " + form);
	                        ArrayList<String> chosenVariables = formToVariables.get(form);
	                        int repeatingForms = 0;
	
	                        // Iterate through the chosen variables to count how many FormData elements are needed
	                        // for the repeating values
	                        debugReporter.writeLn("\nCounting FormData elements to be made.");
	                        for (String variable : chosenVariables) {
	                            debugReporter.writeLn("\nWorking on chosen variable: " + variable);
	
	                            ArrayList<String> values = fieldFinderRepeatingRows(variable, sheetToFieldToVariable,
	                                    patientToSheets, patientID);
	                            if (values.size() > repeatingForms) {
	                                repeatingForms = values.size();
	                            }
	                        }
	
	                        // Create a FormData element for every repeating value and fill it with the relevant data
	                        debugReporter.writeLn("\nCreating FormData elements.");
	                        for (int i = 0; i < repeatingForms; i++) {
	                            // If this is the first ItemData to be put in this FormData then
	                            // we use this variable to check if a new ItemGroupData element 
	                            // needs to be made to put the new ItemData elements in
	                            createGroupData = true;
	                            Element formData = xmlWriter.createFormData(form, i + 1, studyEventData);
	
	                            for (String variable : chosenVariables) {
	                                debugReporter.writeLn("\nWorking on chosen variable: " + variable);
	                                // Find the corresponding variable and field     
	                                ArrayList<String> values = fieldFinderRepeatingRows(variable, sheetToFieldToVariable,
	                                        patientToSheets, patientID);
	                                // If the values List isn't empty, and if it isn't smaller than the current iteration, and
	                                // the value itself isn't null, add it to the XML
	                                if (!values.isEmpty() && values.size() > i && values.get(i) != null) {
	                                    xmlWriter.createItemData(variable, form, createGroupData, values.get(i), formData);
	                                    createGroupData = false;
	                                }
	                            }
	
	                            // If default values have to be added too
	                            if (defaultValues) {
	                                List<String> unchosenVariables = formToAllVariables.get(form);
	                                unchosenVariables.removeAll(chosenVariables);
	                                // Iterate through the unchosen variables
	                                for (String variable : unchosenVariables) {
	                                    if (variableToDefault.keySet().contains(variable)) {
	                                        debugReporter.writeLn("\nWorking on unchosen variable: " + variable);
	
	                                        // Add the default value variable to the XML
	                                        xmlWriter.createItemData(variable, form, createGroupData, variableToDefault.get(variable), formData);
	                                        createGroupData = false;
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
            }
            
            // Otherwise, this is for repeating event rows
            else{
            	System.out.println("patientToSheets: " + patientToSheets.toString());
            	// Iterate through the chosen events
	            for (String event : menuTracker.getChosenEvents()) {
	                debugReporter.writeLn("\nWorking on chosen event: " + event);
	                List<Element> studyEvents = new ArrayList<Element>();
	
	                // Iterate through the chosen forms
	                for (String form : menuTracker.getChosenForms()) {
	                	
	                    // If this form corresponds to the current event
	                    if (eventToForm.get(event).contains(form)) {
	                        debugReporter.writeLn("\nWorking on chosen form: " + form);
	                        ArrayList<String> chosenVariables = formToVariables.get(form);
	                        int repeatingForms = 0;
	
	                        // Iterate through the chosen variables to count how many FormData elements are needed
	                        // for the repeating values
	                        debugReporter.writeLn("\nCounting FormData elements to be made.");
	                        for (String variable : chosenVariables) {
	                            debugReporter.writeLn("\nWorking on chosen variable: " + variable);
	
	                            ArrayList<String> values = fieldFinderRepeatingRows(variable, sheetToFieldToVariable,
	                                    patientToSheets, patientID);
	                            if (values.size() > repeatingForms) {
	                                repeatingForms = values.size();
	                            }
	                        }
	                        
	                        // Create a FormData element for every repeating value and fill it with the relevant data
	                        debugReporter.writeLn("\nCreating FormData elements.");
	                        for (int i = 0; i < repeatingForms; i++) {
	                            // If this is the first ItemData to be put in this FormData then
	                            // we use this variable to check if a new ItemGroupData element 
	                            // needs to be made to put the new ItemData elements in
	                            createGroupData = true;
	                            Element formData;
	                            if(studyEvents.isEmpty() || studyEvents.size() <= i){
	                            	debugReporter.writeLn("\nCreating new study event: " + (i+1));
	                            	Element studyEventData = xmlWriter.createStudyEventData(event, i+1, subjectData);
	                            	studyEvents.add(studyEventData);
	                            	formData = xmlWriter.createFormData(form, 1, studyEventData);
	                            }
	                            else{
	                            	formData = xmlWriter.createFormData(form, 1, studyEvents.get(i));
	                            }
	
	                            for (String variable : chosenVariables) {
	                                debugReporter.writeLn("\nWorking on chosen variable: " + variable);
	                                // Find the corresponding variable and field   
	                                ArrayList<String> values = fieldFinderRepeatingRows(variable, sheetToFieldToVariable,
	                                        patientToSheets, patientID);
	                                System.out.println("variable: " + variable);
	                                System.out.println("values: " + values);
	                                // If the values List isn't empty, and if it isn't smaller than the current iteration, and
	                                // the value itself isn't null, add it to the XML
	                                if (!values.isEmpty() && values.size() > i && values.get(i) != null && !values.get(i).equals("&#10;")) {
	                                    xmlWriter.createItemData(variable, form, createGroupData, values.get(i), formData);
	                                    createGroupData = false;
	                                }
	                            }
	
	                            // If default values have to be added too
	                            if (defaultValues) {
	                                List<String> unchosenVariables = formToAllVariables.get(form);
	                                unchosenVariables.removeAll(chosenVariables);
	                                // Iterate through the unchosen variables
	                                for (String variable : unchosenVariables) {
	                                    if (variableToDefault.keySet().contains(variable)) {
	                                        debugReporter.writeLn("\nWorking on unchosen variable: " + variable);
	
	                                        // Add the default value variable to the XML
	                                        xmlWriter.createItemData(variable, form, createGroupData, variableToDefault.get(variable), formData);
	                                        createGroupData = false;
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	            }
            }
        }

        xmlWriter.saveDocument();

        debugReporter.writeLn("Done!");
        debugReporter.close();
    }

    /**
     * Finds the Excel value of a given variable considering repeating columns.
     *
     * @param variable The variable to find
     * @param sheetToFieldToVariable Contains the FieldToVariable mapping.
     * @param visitedFields List of fields already visited.
     * @param patientToSheets Contains Excel file data.
     * @param patientID The current patient ID.
     * @return A HashMap containing the visited field and the value found.
     */
    public HashMap<String, String> fieldFinderRepeatingColumns(String variable, LinkedHashMap<String, LinkedHashMap> sheetToFieldToVariable,
            ArrayList<String> visitedFields, LinkedHashMap<String, LinkedHashMap> patientToSheets, String patientID) {
        HashMap<String, String> results = new HashMap<>();

        // Iterate through the sheets
        for (String sheet : sheetToFieldToVariable.keySet()) {
            LinkedHashMap<String, String> fieldToVariable = sheetToFieldToVariable.get(sheet);
            // Iterate through the fields of the sheet
            for (String field : fieldToVariable.keySet()) {
                // If the field corresponds to the given variable and hasn't been previously visited
                if (fieldToVariable.get(field).equals(variable) && !visitedFields.contains(field)) {
                    LinkedHashMap<String, LinkedHashMap> excelSheetToFields = patientToSheets.get(patientID);
                    LinkedHashMap<String, String> excelFieldToVariable = excelSheetToFields.get(sheet);
                    // If this field's value is not null, return it
                    if (excelFieldToVariable != null) {
                        debugReporter.writeLn("Corresponding field found on sheet: " + sheet + ", field: " + field + ", values: " + excelFieldToVariable.get(field));
                        results.put("value", excelFieldToVariable.get(field));
                        results.put("field", field);
                        return results;
                    }
                }
            }
        }
        results.put("value", null);
        results.put("field", null);
        return results;
    }

    /**
     * Finds the Excel value of a given variable considering repeating rows.
     *
     * @param variable The variable to find
     * @param sheetToFieldToVariable Contains the FieldToVariable mapping.
     * @param patientToSheets Contains Excel file data.
     * @param patientID The current patient ID.
     * @return A HashMap containing the visited field and the value found.
     */
    public ArrayList<String> fieldFinderRepeatingRows(String variable, LinkedHashMap<String, LinkedHashMap> sheetToFieldToVariable,
            LinkedHashMap<String, LinkedHashMap> patientToSheets, String patientID) {
        ArrayList<String> values;

        // Iterate through the sheets
        for (String sheet : sheetToFieldToVariable.keySet()) {
            LinkedHashMap<String, String> fieldToVariable = sheetToFieldToVariable.get(sheet);
            // Iterate through the fields of the sheet
            for (String field : fieldToVariable.keySet()) {
                // If the field corresponds to the given variable and hasn't been previously visited
                if (fieldToVariable.get(field).equals(variable)) {
                    LinkedHashMap<String, LinkedHashMap> excelSheetToFields = patientToSheets.get(patientID);
                    LinkedHashMap<String, ArrayList> excelFieldToVariables = excelSheetToFields.get(sheet);
                    // If this field's value is not null, return it
                    if (excelFieldToVariables != null) {
                        values = new ArrayList<>();
                        if (excelFieldToVariables.get(field) != null) {
                            values = excelFieldToVariables.get(field);
                        }
                        debugReporter.writeLn("Corresponding field found on sheet: " + sheet + ", field: " + field + ", values: " + values);
                        return values;
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}
