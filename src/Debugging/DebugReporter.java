package Debugging;

import GUI.MainSceneController;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>Debug file writer.</h1>
 * <p>
 * This class is used to take debug messages from other classes and write them
 * to their own debug text files for review.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class DebugReporter {

    private File debugFile;
    private BufferedWriter debugWriter = null;

    /**
     * Get the Debug File and create it in the Debug folder, then create the
     * BufferedWriter to write to it.
     *
     * @param debugFileName The debug file to be written to.
     */
    public DebugReporter(String debugFileName) {
        if (MainSceneController.DEBUG) {
            debugFile = new File("Debug\\" + debugFileName);
            System.out.println(debugFile.toString());
//            debugFile.getParentFile().mkdirs();
            try {
                debugWriter = new BufferedWriter(new FileWriter(debugFile));
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Writes a String then terminates the line.
     *
     * @param line The String to be written.
     */
    public void writeLn(String line) {
        if (MainSceneController.DEBUG) {
            try {
                debugWriter.write(line + System.lineSeparator());
                System.out.println(line);
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Terminates the line.
     */
    public void writeLn() {
        if (MainSceneController.DEBUG) {
            try {
                debugWriter.write(System.lineSeparator());
                System.out.println();
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Writes a String.
     *
     * @param line
     */
    public void write(String line) {
        if (MainSceneController.DEBUG) {
            try {
                debugWriter.write(line);
                System.out.print(line);
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Closes the Debug file.
     */
    public void close() {
        if (MainSceneController.DEBUG) {
            try {
                debugWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Writes an Exception message to the Debug file then closes it.
     *
     * @param message Exception message to be written.
     */
    public void reportException(String message) {
        if (MainSceneController.DEBUG) {
            try {
                debugWriter.write(message + System.lineSeparator());
                debugWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(DebugReporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
