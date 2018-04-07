package com.davidpapazian.yokaiwatchmedals.tools;

import android.preference.PreferenceManager;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;

import java.util.Comparator;
import java.util.Locale;

public class ItemComparator implements Comparator<Item> {

    public final static int SORT_BY_CODE = 0;
    public final static int SORT_BY_NAME = 1;
    public final static int SORT_BY_DATE = 2;

    public int sortBy;
    public int direction;
    private String type;

    public ItemComparator(String type) {
        this.type = type.toLowerCase(Locale.ENGLISH);
        this.sortBy = PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getInt(this.type + "_sort", getDefaultSort());
        this.direction = PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getInt(this.type + "_direction", 1);
    }

    public void sortBy(int newSortBy) {
        if (sortBy == newSortBy) {
            direction*=-1;
        } else {
            sortBy = newSortBy;
            direction = 1;
            PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putInt(type + "_sort", sortBy).apply();
        }
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putInt(type + "_direction", direction).apply();
    }

    @Override
    public int compare(Item item1, Item item2) {
        if (item1 == null)
            return item2 == null ? 0 : -1;
        else if (item2 == null)
            return 1;

        switch (sortBy){
            case SORT_BY_CODE:
                return direction*(((Medal)item1).getCodeOrder() - ((Medal)item2).getCodeOrder());
            case SORT_BY_NAME:
                return direction*(item1.getName().replace("[JP]","").replace("[EN]","").replace("[?]","").compareTo(item2.getName().replace("[JP]","").replace("[EN]","").replace("[?]","")));
            case SORT_BY_DATE:
                return direction*(((Product)item1).getDate().compareTo(((Product)item2).getDate()));
        }
        return 0;
    }

    private int getDefaultSort() {
        if (type.equals("medal"))
            return SORT_BY_CODE;
        else if (type.equals("product"))
            return SORT_BY_DATE;
        else
            return SORT_BY_NAME;
    }

}
