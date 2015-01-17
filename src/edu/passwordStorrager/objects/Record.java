package edu.passwordStorrager.objects;

public class Record {
    private String login;
    private String password;
    private String site;

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
