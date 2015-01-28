package edu.passwordStorrager.cloud;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Values;

public class CloudManager {
    String storageFilePath;

    public CloudManager() {
        storageFilePath = Main.propertiesApplication.getProperty("Storage") + Values.DEFAULT_STORAGE_FILE_NAME;
    }

    public void synchronize() {
        new DropBoxManager(storageFilePath).syncDropBox();
    }


}
