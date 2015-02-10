package edu.passwordStorrager.objects;

import edu.passwordStorrager.protector.Protector;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class Key {
    public static final String iCloudAcc = "ACC_iCloud";
    public static final String iCloudPwd = "PWD_iCloud";
    public static final String megaAcc = "ACC_MEGA";
    public static final String megaPwd = "PWD_MEGA";
    public static final String dropBoxAcc = "ACC_DropBox";
    public static final String dropBoxPwd = "PWD_DropBox";
    public static final String ENC_Value = "ENC";
    private String ENC = "";
    private String[] iCloud = new String[2];
    private String[] mega = new String[2];
    private String[] dropBox = new String[2];
    private boolean isEncrypted = false;


    public Key() {
        this.setICloud("", "");
        this.setMega("", "");
        this.setDropBox("", "");
    }

    public String getENC() {
        return ENC;
    }

    /**
     * Set Encryption key.
     * @param ENC Encryption Key. <pre>Warning!</pre> Key should be already encrypted.
     */
    public void setENC(String ENC) {
        this.ENC = ENC;
    }

    public void setICloud(String login, String password) {
        this.iCloud[0] = login;
        this.iCloud[1] = password;
    }

    public String getICloudLogin() {
        return iCloud[0];
    }

    public void setICloudLogin(String login) {
        this.iCloud[0] = login;
    }

    public String getICloudPassword() {
        return iCloud[1];
    }

    public void setICloudPassword(String password) {
        this.iCloud[1] = password;
    }


    public void setMega(String login, String password) {
        this.mega[0] = login;
        this.mega[1] = password;
    }

    public String getMegaLogin() {
        return mega[0];
    }

    public void setMegaLogin(String login) {
        this.mega[0] = login;
    }

    public String getMegaPassword() {
        return mega[1];
    }

    public void setMegaPassword(String password) {
        this.mega[1] = password;
    }


    public void setDropBox(String login, String password) {

        this.dropBox[0] = login;
        this.dropBox[1] = password;

    }

    public String getDropBoxLogin() {
        return dropBox[0];
    }

    public void setDropBoxLogin(String login) {

        this.dropBox[0] = login;

    }

    public String getDropBoxPassword() {
        return dropBox[1];
    }

    public void setDropBoxPassword(String password) {
        this.dropBox[1] = password;
    }


    public void encrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        if (!isEncrypted) {
            isEncrypted = true;
            iCloud[0] = Protector.encrypt(iCloud[0]);
            iCloud[1] = Protector.encrypt(iCloud[1]);
            mega[0] = Protector.encrypt(mega[0]);
            mega[1] = Protector.encrypt(mega[1]);
            dropBox[0] = Protector.encrypt(dropBox[0]);
            dropBox[1] = Protector.encrypt(dropBox[1]);
            System.out.println("Encrypted: "+ this);
        }
    }


    @Override
    public String toString() {
        return "KEY[\n\t" +
                Key.ENC_Value + " : " + getENC() + "\n\t" +
                Key.iCloudAcc + " : " + getICloudLogin() + "\n\t" +
                Key.iCloudPwd + " : " + getICloudPassword() + "\n\t" +
                Key.megaAcc + " : " + getMegaLogin() + "\n\t" +
                Key.megaPwd + " : " + getMegaPassword() + "\n\t" +
                Key.dropBoxAcc + " : " + getDropBoxLogin() + "\n\t" +
                Key.dropBoxPwd + " : " + getDropBoxPassword() + "\n" +
                "];";
    }

}
