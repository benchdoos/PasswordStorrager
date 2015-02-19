package edu.passwordStorrager.xmlManager;

import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FileUtils;
import org.apache.log4j.Logger;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class XmlParser {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    private String pathStorageFile;


    public XmlParser() {
        pathStorageFile = PasswordStorrager.propertiesApplication.getProperty("Storage") + Values.DEFAULT_STORAGE_FILE_NAME;
    }

    public ArrayList<Record> parseRecords() {
        ArrayList<Record> records = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Protector.decrypt(new FileInputStream(pathStorageFile), byteArrayOutputStream);
            byte data[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(byteArrayInputStream);
            NodeList list = document.getElementsByTagName("Record");

            records = new ArrayList<>(list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                Record record = new Record();
                record.setLogin(list.item(i).getAttributes().getNamedItem("login").getNodeValue());
                record.setPassword(list.item(i).getAttributes().getNamedItem("password").getNodeValue());
                record.setSite(list.item(i).getAttributes().getNamedItem("site").getNodeValue());
                records.add(record);
            }
        } catch (ParserConfigurationException e) {
            log.warn("Wrong configuration.", e);
        } catch (SAXException e) {
            log.warn("Can not parse file.", e);
        } catch (IOException e) {
            log.warn("File can not be decoded.", e); //no file / can not be decoded cause of wrong password
        } catch (Throwable e) {
            log.warn("Can not decrypt storage.", e);
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

            File file = new File(pathStorageFile);
            File file2 = new File(pathStorageFile + "_");
            if (file.exists()) {
                Path path = file.toPath();
                Path path2 = file.toPath();

                Files.copy(path, path2);
                file.delete();
            }

            Protector.encrypt(new ByteArrayInputStream(data), new FileOutputStream(file));
            file2.delete();
            FileUtils.setFileHidden(pathStorageFile);

        } catch (ParserConfigurationException e) {
            log.warn("Error in parser configuration while saving", e);
        } catch (TransformerConfigurationException e) {
            log.warn("Error in transforming while saving", e);
        } catch (TransformerException e) {
            log.warn("Error in transformer while saving", e);
        } catch (FileNotFoundException e) {
            log.warn("File not found : " + pathStorageFile, e);
        } catch (Throwable e) {
            log.warn("Can not encrypt (save) to file: " + pathStorageFile, e);
        }
    }

}
