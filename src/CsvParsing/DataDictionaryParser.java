package CsvParsing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <h1>CSV Data Dictionary parser.</h1>
 * <p>
 * This class is used to parse Data Dictionary file data taken from the 
 * CsvReader into other data structures that can be used by the program.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class DataDictionaryParser {

    private File dataDictionaryFile;
    private CsvReader reader;
    private ArrayList<String> variables;
    private ArrayList<String> forms;
    
    /**
     * Maps the forms to their corresponding variables.
     * <p>
     * I.e: {form : [variable, variable, variable]}
     */
    private HashMap<String, ArrayList> formToVariables;
    /**
     * Maps variables to potential default values.
     */
    private HashMap<String, String> variableToDefault;

    /**
     * Get the Data Dictionary file and read it with a CsvReader object.
     * @param dataDictionaryFile The file to be read and parsed.
     */
    public DataDictionaryParser(File dataDictionaryFile) {
        this.dataDictionaryFile = dataDictionaryFile;
        reader = new CsvReader(dataDictionaryFile);
        reader.read();
    }

    /**
     * Iterate through the rows of the read CSV file and parse its data.
     * <p>
     * First iterate through the row items in the form column and put them into
     * the forms ArrayList, then iterate through the items in the variable 
     * column and put them into the variables ArrayList and formToVariables and
     * variableToDefault HashMaps.
     * @throws Exception In case this file is not a valid Data Dictionary.
     */
    public void parse() throws Exception {
        HashMap<String, Integer> headerToIndex = reader.getHeaderToIndex();
        if (headerToIndex.get("Form Name") == null 
        		|| headerToIndex.get("Variable / Field Name") == null
                || headerToIndex.get("Field Type") == null 
                || headerToIndex.get("Choices, Calculations, OR Slider Labels") == null
                || headerToIndex.get("Field Annotation") == null) {
            throw new Exception("Chosen Data Dictionary is not valid.");
        }
        ArrayList<String> row;
        variables = new ArrayList<>();
        forms = new ArrayList<>();
        formToVariables = new HashMap<>();
        variableToDefault = new HashMap<>();
        String previousForm = "";
        int formIndex = headerToIndex.get("Form Name");
        int variableIndex = headerToIndex.get("Variable / Field Name");
        int typeIndex = headerToIndex.get("Field Type");
        int choiceIndex = headerToIndex.get("Choices, Calculations, OR Slider Labels");
        int defaultIndex = headerToIndex.get("Field Annotation");

        // Parse form column 
        for (int i = 0; i < reader.getRows().size(); i++) {
            row = reader.getRows().get(i);
            // If this is a new form then add it. Otherwise if it's a duplicate skip it
            if (previousForm.isEmpty() || !previousForm.equals(row.get(formIndex))) {
                previousForm = row.get(formIndex);
                forms.add(row.get(formIndex));
            }
        }

        // Parse variable column
        for (int i = 0; i < reader.getRows().size(); i++) {
            row = reader.getRows().get(i);
            // If this is the first form, or a new form
            if (previousForm.isEmpty() || !previousForm.equals(row.get(formIndex))) {
                // If we're done collecting variables for this form then add them
                if (!previousForm.isEmpty()) {
                    formToVariables.put(previousForm, variables);
                }
                previousForm = row.get(formIndex);
                variables = new ArrayList<>();

            }
            // If the variable is of the Checkbox type then create as many variables
            // as it has choices for
            if (row.get(typeIndex).equals("checkbox")) {
                // Count the number of times '|' appears in the Choices field to determine
                // the number of variables
                int count = row.get(choiceIndex).length() - row.get(choiceIndex).replace("|", "").length();
                count++;
                for (int j = 0; j < count; j++) {
                    variables.add(row.get(variableIndex) + "___" + (j + 1));
                }
            } // If the variable is of the dropdown type then check for default values
            else if (row.get(typeIndex).equals("dropdown")) {
                String defaultValue = row.get(defaultIndex).trim();
                // If it has a default value, then get it 
                if(defaultValue.contains("@DEFAULT")){
                    String[] temp = defaultValue.split(" ");
                    for(String part : temp){
                        if(part.startsWith("@DEFAULT")){
                            defaultValue = part.split("'")[1];
                        }
                    }
                    variableToDefault.put(row.get(variableIndex), defaultValue);
                }
                variables.add(row.get(variableIndex));
            } // Otherwise, simply collect the variable
            else {
                variables.add(row.get(variableIndex));
            }
        }
        if (!previousForm.isEmpty()) {
            formToVariables.put(previousForm, variables);
        }
    }

    public ArrayList<String> getVariables() {
        return variables;
    }

    public ArrayList<String> getForms() {
        return forms;
    }

    public HashMap<String, ArrayList> getFormToVariables() {
        return formToVariables;
    }
    
    public HashMap<String, String> getVariableToDefault(){
        return variableToDefault;
    }

    public File getFile() {
        return dataDictionaryFile;
    }
}
