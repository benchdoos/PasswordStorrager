package edu.passwordStorrager.xmlManager;

import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.gui.SavingStatusSheet;
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

    public void saveRecords(final ArrayList<Record> records, final MainForm mainForm) throws SavingRecordsException {
        final SavingStatusSheet sheet = mainForm.savingStatusSheet;

        final Thread m = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = null;
                    docBuilder = docFactory.newDocumentBuilder();

                    Document document = docBuilder.newDocument();
                    Element passwordStorage = document.createElement("PasswordStorage");
                    document.appendChild(passwordStorage);

                    for (int i = 0; i < records.size(); i++) {
                        int k = (int) ((i * 100.0f) / (records.size() - 1));
                        if (sheet != null) {
//                            System.out.println(">D< +" + k + " " + i + " " + records.size());
                            sheet.setModal(true);
                            sheet.progressBar.setValue(k);
                        }

                        Record record = records.get(i);
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

                    if (sheet != null) {
                        sheet.progressBar.setIndeterminate(true);
                    }

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

                    if (sheet != null) {
                        sheet.setModal(false);
                        sheet.progressBar.setIndeterminate(false);
                        sheet.setVisible(false);
                    }
                } catch (ParserConfigurationException e) {
                    throw new SavingRecordsException("Error in parser configuration while saving", e);
                } catch (TransformerConfigurationException e) {
                    throw new SavingRecordsException("Error in transforming while saving", e);
                } catch (TransformerException e) {
                    throw new SavingRecordsException("Error in transformer while saving", e);
                } catch (FileNotFoundException e) {
                    throw new SavingRecordsException("File not found : " + pathStorageFile, e);
                } catch (IOException e) {
                    throw new SavingRecordsException("Can not copy file", e);
                } catch (Throwable e) {
                    throw new SavingRecordsException("Can not encrypt (save) to file: " + pathStorageFile, e);
                }
            }
        });

        if (sheet != null) {
            sheet.progressBar.setValue(0);
            sheet.setModal(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    m.start();
                }
            }).start();

            sheet.setVisible(true);
        } else {
            m.start();
        }

    }

}


