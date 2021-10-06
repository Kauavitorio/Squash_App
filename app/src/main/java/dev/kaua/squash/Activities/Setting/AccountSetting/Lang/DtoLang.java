package dev.kaua.squash.Activities.Setting.AccountSetting.Lang;

public class DtoLang {
    private String display, name;
    private int icon;

    public DtoLang(){}

    public DtoLang(String display, String name, int icon){
        this.display = display;
        this.name = name;
        this.icon = icon;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
