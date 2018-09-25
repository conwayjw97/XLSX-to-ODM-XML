package XlsxParsing;

import Debugging.DebugReporter;
import GUI.MainSceneController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * <h1>XLSX file reader.</h1>
 * <p>
 * This class is used mainly for the exact reading of XLSX files with the help
 * of the Apache POI library, the data of which should then be 
 * interpreted by a Parser class for use in the program.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class XlsxReader {
    
    /**
     * ArrayList containing rows ArrayLists, which contain cells ArrayLists.
     * <p>
     * I.e: [[[cell, cell, cell], [cell, cell, cell], ...], [[cell, cell, cell], [cell, cell, cell], ...]]
     */
    private ArrayList<ArrayList> sheets;
    private ArrayList<ArrayList> rows;
    private ArrayList<String> cells;
    private HashMap<String, Integer> sheetToIndex;
    private HashMap<Integer, String> indexToSheet;
    private XSSFWorkbook wb;
    private DebugReporter debugReporter;

    /**
     * Get the XLSX file to be read and create an XSSFWorkbook with it.
     * <p>
     * XSSFWorkbooks are used to read the contents of XLSX files.
     * @param xlsxFile The file to be read.
     */
    XlsxReader(File xlsxFile) {
        try {
            // Open the Excel workbook
            wb = new XSSFWorkbook(new FileInputStream(xlsxFile));
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "IOException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(XlsxReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Iterate through the contents of the XLSX file and collect its data.
     * <p>
     * While the method iterates through the file, it populates the sheets 
     * ArrayList, and the sheetToIndex and indexToSheet HashMaps.
     */
    public void readWorkbook() {
        sheets = new ArrayList<>();
        sheetToIndex = new HashMap<>();
        indexToSheet = new HashMap<>();
        DataFormatter dataFormatter = new DataFormatter();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        if (MainSceneController.DEBUG) {
            debugReporter = new DebugReporter("XlsxReader.txt");
        }

        // Iterate through the sheets 
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            rows = new ArrayList<>();
            XSSFSheet sheet = wb.getSheetAt(i);
            sheetToIndex.put(sheet.getSheetName(), i);
            indexToSheet.put(i, sheet.getSheetName());

            if (MainSceneController.DEBUG) {
                debugReporter.writeLn("\nLooking at sheet: " + sheet.getSheetName());
            }

            // Iterate through the rows of the sheet
            for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                cells = new ArrayList<>();
                XSSFRow row = sheet.getRow(j);

                if (MainSceneController.DEBUG) {
                    debugReporter.writeLn("\nLooking at row: " + j);
                }

                if (row != null && row.getLastCellNum() > 0) {

                    // Iterate through the columns of the row and collect their values
                    for (int k = 0; k < row.getLastCellNum(); k++) {
                        if (row.getCell(k) != null && !row.getCell(k).toString().isEmpty()) {
                            if (MainSceneController.DEBUG) {
                                debugReporter.writeLn("Storing cell " + k + ": " + row.getCell(k).toString());
                            }

                            // Some Excel entries contained '--' instead of an empty cell
                            // so I had to include this condition for it
                            if (row.getCell(k).toString().startsWith("--")) {
                                cells.add(null);
                            } else // If the entry is a date then convert it to the yyyy-mm-dd format
                            {
                                if (row.getCell(k).getCellTypeEnum() == CellType.NUMERIC && DateUtil.isCellDateFormatted(row.getCell(k))) {
                                    String timeCheck = row.getCell(k).getCellStyle().getDataFormatString();
                                    // If this format mentions hours then it must be a Time cell
                                    if (timeCheck.toLowerCase().contains("h")) {
                                        cells.add(timeFormat.format(row.getCell(k).getDateCellValue()));
                                    } // Otherwise we just consider it a date cell
                                    else {
                                        cells.add(dateFormat.format(row.getCell(k).getDateCellValue()));
                                    }
                                } else {
                                    cells.add(dataFormatter.formatCellValue(row.getCell(k)));
                                }
                            }
                        } else {
                            cells.add(null);
                        }
                    }

                    rows.add(cells);
                }
            }

            sheets.add(rows);
        }
        try {
            wb.close();
        } catch (IOException ex) {
            Logger.getLogger(XlsxReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (MainSceneController.DEBUG) {
            debugReporter.close();
        }
    }

    public ArrayList<ArrayList> getSheets() {
        return sheets;
    }

    public HashMap<Integer, String> getIndexToSheet() {
        return indexToSheet;
    }

    public HashMap<String, Integer> getSheetToIndex() {
        return sheetToIndex;
    }
}
