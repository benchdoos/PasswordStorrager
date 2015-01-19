package edu.passwordStorrager.xmlManager;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;

public class XmlParser {
    String pathStorageFile;


    public XmlParser() {
        pathStorageFile = Main.properties.getProperty("Storage") + Values.DEFAULT_STORAGE_FILE_NAME;
    }

    public ArrayList<Record> parseRecords() {
        ArrayList<Record> records = new ArrayList<Record>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            Protector.decrypt(new FileInputStream(pathStorageFile), byteArrayOutputStream);
            byte data[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

            builder = factory.newDocumentBuilder();
            document = builder.parse(byteArrayInputStream);
            NodeList list = document.getElementsByTagName("Record");

            records = new ArrayList<Record>(list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                Record record = new Record();
                record.setLogin(list.item(i).getAttributes().getNamedItem("login").getNodeValue());
                record.setPassword(list.item(i).getAttributes().getNamedItem("password").getNodeValue());
                record.setSite(list.item(i).getAttributes().getNamedItem("site").getNodeValue());
                records.add(record);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Can not parse file.");
        } catch (IOException e) {
            //System.err.println("No file found:" + pathStorageFile);
            e.printStackTrace();
            System.err.println("File can not be decoded"); //no file / can not be decoded cause of wrong password
        } catch (Throwable throwable) {
            System.err.println("Can not decrypt storage");
        }
        return records;
    }

    public void saveRecords(ArrayList<Record> records) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            docBuilder = docFactory.newDocumentBuilder();

            Document document = docBuilder.newDocument();
            Element passwordStorage = document.createElement("PasswordStorage");
            document.appendChild(passwordStorage);

            for (Record record : records) {
                Element rec = document.createElement("Record");
                rec.setAttribute("login", record.getLogin());
                rec.setAttribute("password", record.getPassword());
                rec.setAttribute("site", record.getSite());
                passwordStorage.appendChild(rec);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(byteArrayOutputStream);

            transformer.transform(source, result);

            byte[] data = byteArrayOutputStream.toByteArray();

            Protector.encrypt(new ByteArrayInputStream(data), new FileOutputStream(pathStorageFile));

            FileUtils.setFileHidden(pathStorageFile);

        } catch (ParserConfigurationException e) {
            System.err.println("Error in parser configuration while saving");
        } catch (TransformerConfigurationException e) {
            System.err.println("Error in transforming while saving");
        } catch (TransformerException e) {
            System.err.println("Error in transformer while saving");
        } catch (FileNotFoundException e) {
            System.err.println("File not found : " + pathStorageFile);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.err.println("Can not encrypt (save) to file: " + pathStorageFile);
        }
    }

}
