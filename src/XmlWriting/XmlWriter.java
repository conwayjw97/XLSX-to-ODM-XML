package XmlWriting;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h1>ODM XML file writer.</h1>
 * <p>
 * This class is used for the writing of ODM XML files with the help of the
 * Apache Commons library, the data to be written should be handled first by the
 * {@link XmlWriting.XmlConverter XmlConverter} first before being written here.
 *
 * @author James Conway
 * @since 2018-07-18
 */
public class XmlWriter {

    private Document document;
    private File xmlFile;

    /**
     * Previous XML element to append data to.
     */
    private Element clinicalData, subjectData, studyEventData, formData, itemGroupData, itemData;

    /**
     * Get the XML file to write to and create a DocumentBuilder to write to
     * it.
     *
     * @param xmlFile The file to be written to.
     */
    public XmlWriter(File xmlFile) {
        // Create a DocumentBuilder
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.newDocument();
            this.xmlFile = xmlFile;
        } catch (ParserConfigurationException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "ParserConfigurationException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create an "ODM" root element and append a "ClinicalData" element to it.
     */
    public void createDocument() {
        // Create the root Element with all the default Attributes and append it to the file
        Element rootElement = document.createElement("ODM");
        rootElement.setAttribute("xmlns", "http://www.cdisc.org/ns/odm/v1.3");
        rootElement.setAttribute("xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xmlns:redcap", "https://projectredcap.org");
        rootElement.setAttribute("xsi:schemaLocation", "http://www.cdisc.org/ns/odm/v1.3 schema/odm/ODM1-3-1.xsd");
        rootElement.setAttribute("ODMVersion", "1.3.1");
        rootElement.setAttribute("FileOID", "000-00-0000");
        rootElement.setAttribute("FileType", "Snapshot");
        rootElement.setAttribute("Description", "PMT");
        rootElement.setAttribute("AsOfDateTime", "#");
        rootElement.setAttribute("CreationDateTime", "#");
        rootElement.setAttribute("SourceSystem", "REDCap");
        rootElement.setAttribute("SourceSystemVersion", "8.4.2");
        document.appendChild(rootElement);

        // Create the ClinicalData element and append it to the file
        clinicalData = document.createElement("ClinicalData");
        clinicalData.setAttribute("StudyOID", "Project.PMT");
        rootElement.appendChild(clinicalData);
    }

    /**
     * Create a "SubjectData" element and assign the patientID to it.
     * @param patientID 
     */
    public void createSubjectData(String patientID) {
        // Create the SubjectData element and append it to the file
        subjectData = document.createElement("SubjectData");
        subjectData.setAttribute("SubjectKey", patientID);
        clinicalData.appendChild(subjectData);
    }

    /**
     * Create a "StudyEventData" element and assign the event to it.
     * @param event 
     */
    public void createStudyEventData(String event) {
        // Create the StudyEventData element and append it to the file
        studyEventData = document.createElement("StudyEventData");
        studyEventData.setAttribute("StudyEventOID", "Event." + event);
        studyEventData.setAttribute("StudyEventRepeatKey", "1");
        studyEventData.setAttribute("redcap:UniqueEventName", event);
        subjectData.appendChild(studyEventData);
    }

    /**
     * Create a "FormData" element and assign the form and repeatKey to it.
     * @param form
     * @param repeatKey 
     */
    public void createFormData(String form, int repeatKey) {
        // Create the FormData element with its repeat key and append it to the file
        formData = document.createElement("FormData");
        formData.setAttribute("FormOID", "Form." + form);
        formData.setAttribute("FormRepeatKey", String.valueOf(repeatKey));
        studyEventData.appendChild(formData);
    }

    /**
     * Create a "GroupData" element (if (createGroupData)) and append an "ItemData" to the "GroupData" element.
     * <p>
     * Also, associate the form value with the "GroupData" element and the variable and value with the "ItemData" element.
     * @param variable
     * @param form
     * @param createGroupData
     * @param value 
     */
    public void createItemData(String variable, String form, boolean createGroupData, String value) {
        // If a new ItemGroupData element needs to be created to encase the following ItemData elements
        // then create one and append it
        if (createGroupData) {
            itemGroupData = document.createElement("ItemGroupData");
            itemGroupData.setAttribute("ItemGroupOID", form + "." + variable);
            itemGroupData.setAttribute("ItemGroupRepeatKey", "1");
            formData.appendChild(itemGroupData);
        }
        // Create the ItemData element and append it to the file
        itemData = document.createElement("ItemData");
        itemData.setAttribute("ItemOID", variable);
        if (value != null) {
            itemData.setAttribute("Value", value);
        } else {
            itemData.setAttribute("Value", "");
        }
        itemGroupData.appendChild(itemData);
    }

    /**
     * Save the XML file.
     */
    public void saveDocument() {
        // Write the document to an XML File
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFile);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "TransformerConfigurationException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "TransformerException: " + ex.getLocalizedMessage(), ButtonType.OK);
            alert.showAndWait();
            Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
