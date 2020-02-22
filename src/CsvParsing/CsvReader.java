package CsvParsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * <h1>CSV file reader.</h1>
 * <p>
 * This class is used for the exact reading of CSV files with the help
 * of the Apache Commons CSV library, the data of which should then be 
 * interpreted by a Parser class for use in the program.
 *
 * @author James Conway
 * @since 2018-07-17
 */
public class CsvReader {

    private File csvFile;
    private BufferedReader reader;
    
    /**
     * ArrayList containing cells ArrayLists.
     * <p>
     * I.e: [[cell, cell, cell], [cell, cell, cell], ...]
     */
    private ArrayList<ArrayList> rows;
    private ArrayList<String> cells;
    private HashMap<String, Integer> headerToIndex;
    private HashMap<Integer, String> indexToHeader;

    /**
     * @param csvFile The file to be read.
     */
    CsvReader(File csvFile) {
        this.csvFile = csvFile;
    }

    /**
     * Iterate through the contents of the CSV file and collect its data.
     * <p>
     * While the method iterates through the file, it populates the rows 
     * ArrayList, and the headerToIndex and indexToHeader HashMaps.
     */
    public void read() {
        try {
            // Open the reader for the file and create a csvParser for it
            reader = new BufferedReader(new FileReader(csvFile));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withDelimiter(',')
                    .withTrim());

            headerToIndex = new HashMap<>();
            indexToHeader = new HashMap<>();
            List<CSVRecord> csvRows = csvParser.getRecords();

            // Iterate through the first row and get the mappings for the headers
            CSVRecord header = csvRows.get(0);
            for (int i = 0; i <= header.size() - 1; i++) {
            	System.out.println(header.get(i).trim());
                headerToIndex.put(header.get(i).trim(), i);
                indexToHeader.put(i, header.get(i).trim());
            }

            rows = new ArrayList<>();
            // Iterate through the rows after the header to retrieve the cell values
            for (int i = 1; i < csvRows.size(); i++) {
                // Put the cell values for this row into an arrayList
                cells = new ArrayList<>();
                for (String cell : csvRows.get(i)) {
                    cells.add(cell);
                }
                // Put the arrayList for the cell values into an arrayList for the rows
                rows.add(cells);
            }
        } catch (FileNotFoundException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "FileNotFoundException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(CsvReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "IOException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(CsvReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String, Integer> getHeaderToIndex() {
        return headerToIndex;
    }

    public HashMap<Integer, String> getIndexToHeader() {
        return indexToHeader;
    }

    public ArrayList<ArrayList> getRows() {
        return rows;
    }
}
