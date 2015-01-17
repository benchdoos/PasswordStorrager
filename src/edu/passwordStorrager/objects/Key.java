package edu.passwordStorrager.objects;

import edu.passwordStorrager.core.PasswordProtector;

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
    String ENC = "";
    String[] iCloud = new String[2];
    String[] mega = new String[2];
    String[] dropBox = new String[2];
    private boolean isFinal = false;
    private boolean isEncrypted = false;


    public Key() {
        setICloud("", "");
        setMega("", "");
        setDropBox("", "");
    }

    public String getENC() {
        return ENC;
    }

    public void setENC(String ENC) {
        if (!isFinal) {
            this.ENC = ENC;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public void setICloud(String login, String password) {
        if (!isFinal) {
            this.iCloud[0] = login;
            this.iCloud[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getICloudLogin() {
        return iCloud[0];
    }

    public void setICloudLogin(String login) {
        if (!isFinal) {
            this.iCloud[0] = login;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getICloudPassword() {
        return iCloud[1];
    }

    public void setICloudPassword(String password) {
        if (!isFinal) {
            this.iCloud[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }


    public void setMega(String login, String password) {
        if (!isFinal) {
            this.mega[0] = login;
            this.mega[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getMegaLogin() {
        return mega[0];
    }

    public void setMegaLogin(String login) {
        if (!isFinal) {
            this.mega[0] = login;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getMegaPassword() {
        return mega[1];
    }

    public void setMegaPassword(String password) {
        if (!isFinal) {
            this.mega[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }


    public void setDropBox(String login, String password) {
        if (!isFinal) {
            this.dropBox[0] = login;
            this.dropBox[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getDropBoxLogin() {
        return dropBox[0];
    }

    public void setDropBoxLogin(String login) {
        if (!isFinal) {
            this.dropBox[0] = login;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public String getDropBoxPassword() {
        return dropBox[1];
    }

    public void setDropBoxPassword(String password) {
        if (!isFinal) {
            this.dropBox[1] = password;
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public void finalise() {
        isFinal = true;
    }

    public void encrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        if (!isFinal) {
            if (!isEncrypted) {
                isEncrypted = true;

                this.ENC = PasswordProtector.hexPassword(PasswordProtector.hexPassword(ENC));
                this.iCloud[0] = PasswordProtector.encrypt(this.iCloud[0]);
                this.iCloud[1] = PasswordProtector.encrypt(this.iCloud[1]);
                this.mega[0] = PasswordProtector.encrypt(this.mega[0]);
                this.mega[1] = PasswordProtector.encrypt(this.mega[1]);
                this.dropBox[0] = PasswordProtector.encrypt(this.dropBox[0]);
                this.dropBox[1] = PasswordProtector.encrypt(this.dropBox[1]);
            }
        } else {
            throw new RuntimeException("Key can not be transformed");
        }
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public boolean isFinal() {
        return isFinal;
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
                "] finalised: " + isFinal() + ", encrypted: " + isEncrypted() + ";";
    }

}
