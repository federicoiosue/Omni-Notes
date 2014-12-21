package it.feio.android.omninotes.models;

public class NavigationItem {


    private int arrayIndex;
    private String text;
    private int icon;
    private int iconSelected;


    public NavigationItem(int arrayIndex, String text, int icon, int iconSelected) {
        this.arrayIndex = arrayIndex;
        this.text = text;
        this.icon = icon;
        this.iconSelected = iconSelected;
    }


    public int getArrayIndex() {
        return arrayIndex;
    }


    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
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
