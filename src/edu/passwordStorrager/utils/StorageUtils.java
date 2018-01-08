package edu.passwordStorrager.utils;

import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.xmlManager.XmlParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StorageUtils {
    public static void createEmptyStorage(String folder) throws Throwable {
        File file = new File(StringUtils.fixFolder(folder) + "storage");
        if (!file.exists()) {
            if (file.createNewFile()) {
                new XmlParser().saveRecords(new ArrayList<Record>(),null);
            } else {
                throw new IOException("Can not create file: " + file.getAbsolutePath());
            }
        }
    }
}
