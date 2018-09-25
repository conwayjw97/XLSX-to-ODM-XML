package GUI.Utils;

import GUI.FieldCustomizerController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javafx.collections.ObservableList;

/**
 * Keeps track of all the user's choices on the
 * {@link GUI.FieldCustomizerController FieldCustomizer} window.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class MenuTracker {

    // REDO THIS PLACEHOLDER STUFF WITH ENUMS
    /**
     * Placeholder to identify ListView item with nothing on it.
     */
    public static final String EMPTY_ITEM_PLACEHOLDER = "";
    /**
     * Placeholder to identify ListView item with nothing on it, but where a
     * field would have gone otherwise.
     */
    public static final String EMPTY_FIELD_PLACEHOLDER = " ";
    /**
     * Placeholder to identify ListView item with a field on it.
     */
    public static final String FIELD_PLACEHOLDER = "Field";
    /**
     * Placeholder to identify ListView item with a sheet on it.
     */
    public static final String SHEET_PLACEHOLDER = "Sheet";

    /**
     * Maps the user's selected sheets to their selected fields to their
     * corresponding variable.
     * <p>
     * I.e: {sheet : {field : variable, field : variable, field : variable}}
     */
    private LinkedHashMap<String, LinkedHashMap> sheetToFieldToVariable;
    /**
     * Maps the user's selected sheets to their selected fields.
     * <p>
     * I.e: {sheet : [field, field, field]}
     */
    private LinkedHashMap<String, ArrayList> selectedSheetToFields;
    /**
     * Maps the selected forms to their corresponding variables.
     * <p>
     * I.e: {form : [variable, variable, variable]}
     */
    private LinkedHashMap<String, ArrayList> formToVariables;
    /**
     * Maps the ListView index to the type of item on it with the use of the
     * Placeholders.
     * <p>
     * I.e: {index : type placeholder, index : type placeholder, index : type
     * placeholder}
     */
    private LinkedHashMap<Integer, String> indexToItemType;
    /**
     * Maps the ListView index to the name of the sheet or field that
     * corresponds to it.
     * <p>
     * I.e: {index : field name, index : sheet name, index : field name}
     */
    private LinkedHashMap<Integer, String> indexToDefaultItem;
    /**
     * Maps the ListView index to the item.
     */
    private LinkedHashMap<Integer, String> indexToEvent, indexToForm, indexToVariable;
    /**
     * List of unique items chosen by the user.
     */
    private ArrayList<String> chosenEvents, chosenForms, chosenVariables;
    /**
     * Maps the item to the number of times the user has chosen it.
     */
    private HashMap<String, Integer> formToOccurence, variableToOccurence;

    /**
     * Initializes the {@link #selectedSheetToFields selectedSheetToFields}
     * LinkedHashMap to track the user's selection of sheets and fields from the
     * Excel TreeView.
     */
    public MenuTracker() {
        selectedSheetToFields = new LinkedHashMap<>();
    }

    /**
     * Add selected sheet or field to
     * {@link #selectedSheetToFields selectedSheetToFields}.
     *
     * @param sheetName Chosen sheet.
     * @param field Chosen field.
     * @param selectedIndex
     */
    public void fieldChecked(String sheetName, String field, int selectedIndex) {
        ArrayList<String> fields;
        // If this sheet has been selected before, add the selected field to it in selectedSheetToFields
        if (selectedSheetToFields.containsKey(sheetName)) {
            fields = selectedSheetToFields.get(sheetName);
        } // If not, create a new ArrayList for the fields of this sheet
        else {
            fields = new ArrayList<>();
        }
        fields.add(field);
        selectedSheetToFields.put(sheetName, fields);
    }

    /**
     * Remove selected sheet or field from
     * {@link #selectedSheetToFields selectedSheetToFields}.
     *
     * @param sheetName Chosen sheet.
     * @param field Chosen field.
     * @param selectedIndex
     */
    public void fieldUnchecked(String sheetName, String field, int selectedIndex) {
        ArrayList<String> fields;
        // If this sheet has been selected before, add the selected field to it in selectedSheetToFields
        if (selectedSheetToFields.containsKey(sheetName)) {
            fields = selectedSheetToFields.get(sheetName);
        } // If not, create a new ArrayList for the fields of this sheet
        else {
            fields = new ArrayList<>();
        }
        fields.remove(field);
        selectedSheetToFields.put(sheetName, fields);
    }

    /**
     * Fill {@link #indexToItemType indexToItemType} and
     * {@link #indexToDefaultItem indexToDefaultItem} with data from the REDCap
     * events ListView.
     *
     * @param eventListData REDCap events ListView contents.
     */
    public void collectIndexToDefaultItem(ObservableList eventListData) {
        indexToItemType = new LinkedHashMap<>();
        indexToDefaultItem = new LinkedHashMap<>();
        // Iterate through the ListView
        for (int i = 0; i < eventListData.size(); i++) {
            // Put the corresponding ItemType for the Index in the indexToItem and indexToDefaultItem LinkedHashMaps
            if (eventListData.get(i).toString().startsWith(FieldCustomizerController.EVENT_FIELD_PLACEHOLDER)) {
                indexToItemType.put(i, FIELD_PLACEHOLDER);
                indexToDefaultItem.put(i, eventListData.get(i).toString().substring(24));
            } else if (eventListData.get(i).toString().startsWith(FieldCustomizerController.EVENT_SHEET_PLACEHOLDER)) {
                indexToItemType.put(i, SHEET_PLACEHOLDER);
                indexToDefaultItem.put(i, eventListData.get(i).toString().substring(24));
            } else if (eventListData.get(i).toString().startsWith(FieldCustomizerController.FIELD_EMPTY)) {
                indexToItemType.put(i, EMPTY_FIELD_PLACEHOLDER);
            } else if (eventListData.get(i).toString().startsWith(FieldCustomizerController.ITEM_EMPTY)) {
                indexToItemType.put(i, EMPTY_ITEM_PLACEHOLDER);
            }
        }
    }

    /**
     * Fill {@link #indexToEvent indexToEvent} and
     * {@link #chosenEvents chosenEvents} with data from the REDCap events
     * ListView.
     *
     * @param eventListData REDCap events ListView contents.
     */
    public void collectEventListData(ObservableList eventListData) {
        indexToEvent = new LinkedHashMap<>();
        chosenEvents = new ArrayList<String>();
        // Iterate through the item types
        for (int i : indexToItemType.keySet()) {
            // If the current item is a field
            if (indexToItemType.get(i).equals(FIELD_PLACEHOLDER)) {
                // Collect the event if it's unique and its index
                String event = eventListData.get(i).toString().trim();
                indexToEvent.put(i, event);
                if (!chosenEvents.contains(event)) {
                    chosenEvents.add(event);
                }
            }
        }
    }

    /**
     * Fill {@link #indexToForm indexToForm} and
     * {@link #chosenForms chosenForms} with data from the REDCap forms
     * ListView.
     *
     * @param formListData REDCap forms ListView contents.
     */
    public void collectFormListData(ObservableList formListData) {
        indexToForm = new LinkedHashMap<Integer, String>();
        chosenForms = new ArrayList<String>();
        // Iterate through the item types
        for (int i : indexToItemType.keySet()) {
            // If the current item is a field
            if (indexToItemType.get(i).equals(FIELD_PLACEHOLDER)) {
                // Collect the form if it's unique and its index
                String form = formListData.get(i).toString();
                indexToForm.put(i, form);
                if (!chosenForms.contains(form)) {
                    chosenForms.add(form);
                }
            }
        }
    }

    /**
     * Fill {@link #indexToVariable indexToVariable},
     * {@link #chosenVariables chosenVariables},
     * {@link #sheetToFieldToVariable sheetToFieldToVariable},
     * {@link #formToVariables formToVariables},
     * {@link #formToOccurence formToOccurence}, and
     * {@link #variableToOccurence variableToOccurence} with data from the
     * REDCap variables ListView.
     *
     * @param variableListData REDCap variables ListView contents.
     */
    public void collectVariableListData(ObservableList variableListData) {
        String oldSheet = null, newSheet, field, variable, form = null;
        int occurence;
        LinkedHashMap<String, String> fieldToVariable = new LinkedHashMap<>();
        ArrayList<String> variables = null;
        sheetToFieldToVariable = new LinkedHashMap<>();
        indexToVariable = new LinkedHashMap<>();
        chosenVariables = new ArrayList<>();
        formToVariables = new LinkedHashMap<>();
        variableToOccurence = new LinkedHashMap<>();
        formToOccurence = new LinkedHashMap<>();

        // Collect data for indexToVariable and chosenVariables
        // Iterate through the item types
        for (int i : indexToItemType.keySet()) {
            // If the current item is a field
            if (indexToItemType.get(i).equals(FIELD_PLACEHOLDER)) {
                // Collect the variable if it's unique and its index
                variable = variableListData.get(i).toString();
                indexToVariable.put(i, variable);
                if (!chosenVariables.contains(variable)) {
                    chosenVariables.add(variable);
                }
            }
        }

        // Collect data for sheetToFieldToVariable
        // Iterate through the item types
        for (int i : indexToItemType.keySet()) {
            // If the current item is a sheet
            if (indexToItemType.get(i).equals(SHEET_PLACEHOLDER)) {
                newSheet = indexToDefaultItem.get(i);
                // If this is the first sheet, or a new sheet
                if (oldSheet == null || !oldSheet.equals(newSheet)) {
                    // If this is a new sheet, then store the fieldToVariable data for the old sheet
                    if (oldSheet != null) {
                        sheetToFieldToVariable.put(oldSheet, fieldToVariable);
                    }
                    oldSheet = newSheet;
                }
                fieldToVariable = new LinkedHashMap<>();
            }
            // If the current item is a field
            if (indexToItemType.get(i).equals(FIELD_PLACEHOLDER)) {
                // Collect the fieldToVariable data
                field = indexToDefaultItem.get(i);
                variable = variableListData.get(i).toString();
                fieldToVariable.put(field, variable);
            }
        }
        // Always store the collected data for the last sheet which is skipped in the
        // for-loop
        if (oldSheet != null) {
            sheetToFieldToVariable.put(oldSheet, fieldToVariable);
        }

        // Collect data for formToVariables
        // Iterate through all the unique chosen forms
        for (String chosenForm : chosenForms) {
            variables = new ArrayList<>();
            // Iterate through the forms
            for (int i : indexToForm.keySet()) {
                // If the form at this index is equal to the chosen form and the variable at this index hasn't been seen already
                if (indexToForm.get(i).equals(chosenForm) && !variables.contains(indexToVariable.get(i))) {
                    // Collect the variable at this index
                    variables.add(indexToVariable.get(i));
                }
            }
            // Associate the collected variables with this form
            formToVariables.put(chosenForm, variables);
        }

        // Collect data for variableToOccurence and formToOccurence
        // Iterate through the item types
        for (int i : indexToItemType.keySet()) {
            // If this item is a field
            if (indexToItemType.get(i).equals(FIELD_PLACEHOLDER)) {
                variable = variableListData.get(i).toString();

                // Find the form corresponding to this variable
                for (String iterForm : formToVariables.keySet()) {
                    if (formToVariables.get(iterForm).contains(indexToVariable.get(i))) {
                        form = iterForm;
                        break;
                    }
                }

                // Add the variable and form to their occurence maps
                if (!variableToOccurence.containsKey(variable)) {
                    variableToOccurence.put(variable, 1);
                    formToOccurence.put(form, 1);
                } // If the variable and form have already been added, then increment their occurence
                else if (variableToOccurence.containsKey(variable)) {
                    occurence = variableToOccurence.get(variable);
                    variableToOccurence.put(variable, occurence + 1);
                    formToOccurence.put(form, occurence + 1);
                }
            }
        }
    }

    public LinkedHashMap<String, ArrayList> getSelectedSheetToFields() {
        return selectedSheetToFields;
    }

    public LinkedHashMap<Integer, String> getIndexToItemType() {
        return indexToItemType;
    }

    public HashMap<Integer, String> getIndexToDefaultItem() {
        return indexToDefaultItem;
    }

    public LinkedHashMap<Integer, String> getIndexToEvent() {
        return indexToEvent;
    }

    public ArrayList<String> getChosenEvents() {
        return chosenEvents;
    }

    public LinkedHashMap<Integer, String> getIndexToForm() {
        return indexToForm;
    }

    public ArrayList<String> getChosenForms() {
        return chosenForms;
    }

    public LinkedHashMap<String, LinkedHashMap> getSheetToFieldToVariable() {
        return sheetToFieldToVariable;
    }

    public LinkedHashMap<Integer, String> getIndexToVariable() {
        return indexToVariable;
    }

    public ArrayList<String> getChosenVariables() {
        return chosenVariables;
    }

    public LinkedHashMap<String, ArrayList> getFormToVariables() {
        return formToVariables;
    }

    public HashMap<String, Integer> getVariableToOccurence() {
        return variableToOccurence;
    }

    public HashMap<String, Integer> getFormToOccurence() {
        return formToOccurence;
    }
}
