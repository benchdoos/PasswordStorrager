package edu.passwordStorrager.objects;

public class Record {
    private String login = "";
    private String password = "";
    private String site = "";

    public Record() {
    }

    public Record(String login, String password, String site) {
        this.login = login;
        this.password = password;
        this.site = site;
    }

    public Record(Object login, Object password, Object site) {
        this.login = (String) login;
        this.password = (String) password;
        this.site = (String) site;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return "Record{site:[" + site + "] login:[" + login + "] pwd:[" + password + "]}";
    }
}
