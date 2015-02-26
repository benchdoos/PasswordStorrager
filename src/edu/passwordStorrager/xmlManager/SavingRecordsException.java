package edu.passwordStorrager.xmlManager;

import org.apache.log4j.Logger;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class SavingRecordsException extends RuntimeException {
    private static final Logger log = Logger.getLogger(getCurrentClassName());


    /**
     * Create a new <code>ParserConfigurationException</code> with
     * the <code>String</code> specified as an error message.
     *
     * @param msg The error message for the exception.
     * @param e
     */

    public SavingRecordsException(String msg, Throwable e) {
        super(msg);
        log.warn("Can not save records: " + msg, e);
    }
}
