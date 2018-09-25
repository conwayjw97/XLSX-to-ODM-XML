package CsvParsing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <h1>CSV Instrument Designation parser.</h1>
 * <p>
 * This class is used to parse Instrument Designation file data taken from the 
 * CsvReader into other data structures that can be used by the program.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class InstrumentDesigParser {

    private File instrumentDesigFile;
    private CsvReader reader;
    private ArrayList<String> events;
    private ArrayList<String> forms;
    
    /**
     * Maps the forms to their corresponding variables.
     * <p>
     * I.e: {event : [form, form, form]}
     */
    private HashMap<String, ArrayList> eventToForm;

    /**
     * Get the Instrument Designation file and read it with a CsvReader object.
     * @param instrumentDesigFile The file to be read and parsed.
     */
    public InstrumentDesigParser(File instrumentDesigFile) {
        this.instrumentDesigFile = instrumentDesigFile;
        reader = new CsvReader(instrumentDesigFile);
        reader.read();
    }

    /**
     * Iterate through the rows of the read CSV file and parse its data.
     * <p>
     * First iterate through the row items in the event column and put them into
     * the events ArrayList, then iterate through the items in the form 
     * column and put them into the forms ArrayList and eventToForm HashMap.
     * @throws Exception In case this file is not a valid Instrument Designation.
     */
    public void parse() throws Exception {
        HashMap<String, Integer> headerToIndex = reader.getHeaderToIndex();
        if (headerToIndex.get("unique_event_name") == null || headerToIndex.get("form") == null) {
            throw new Exception("Chosen Instrument Designation is not valid.");
        }
        else{
            ArrayList<String> row;
            events = new ArrayList<>();
            forms = new ArrayList<>();
            eventToForm = new HashMap<>();
            String currentEvent, currentForm, previousEvent = "";

            // Parse event column 
            for (int i = 0; i < reader.getRows().size(); i++) {
                row = reader.getRows().get(i);
                // If this is a new event then add it. Otherwise if it's a duplicate skip it
                currentEvent = row.get(headerToIndex.get("unique_event_name"));
                if (previousEvent.isEmpty() || !previousEvent.equals(currentEvent)) {
                    previousEvent = currentEvent;
                    events.add(currentEvent);
                }
            }

            // Parse form column
            for (int i = 0; i < reader.getRows().size(); i++) {
                row = reader.getRows().get(i);
                currentEvent = row.get(headerToIndex.get("unique_event_name"));
                currentForm = row.get(headerToIndex.get("form"));
                // If this is the first event, or a new event
                if (previousEvent.isEmpty() || !previousEvent.equals(currentEvent)) {
                    // If this is a new event then add the previous event's forms to the
                    // eventToForm HashMap
                    if (!previousEvent.isEmpty()) {
                        eventToForm.put(previousEvent, forms);
                    }
                    previousEvent = currentEvent;
                    forms = new ArrayList<>();
                    forms.add(currentForm);
                } // If this event is the same as the last then just collect the form
                else {
                    forms.add(currentForm);
                }
            }
            // The last event is always skipped in the for loop so add its form to the
            // HashMap afterwards
            if (!previousEvent.isEmpty()) {
                eventToForm.put(previousEvent, forms);
            }
        }
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public ArrayList<String> getForms() {
        return forms;
    }

    public HashMap<String, ArrayList> getEventToForm() {
        return eventToForm;
    }

    public File getFile() {
        return instrumentDesigFile;
    }
}
