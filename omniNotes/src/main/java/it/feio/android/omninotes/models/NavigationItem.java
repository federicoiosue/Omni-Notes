package it.feio.android.omninotes.models;

public class NavigationItem {

    private String text;
    private int icon;


    private int iconSelected;


    public NavigationItem(String text, int icon, int iconSelected) {
        this.text = text;
        this.icon = icon;
        this.iconSelected = iconSelected;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public int getIcon() {
        return icon;
    }


    public void setIcon(int icon) {
        this.icon = icon;
    }


    public int getIconSelected() {
        return iconSelected;
    }


    public void setIconSelected(int iconSelected) {
        this.iconSelected = iconSelected;
    }
}
