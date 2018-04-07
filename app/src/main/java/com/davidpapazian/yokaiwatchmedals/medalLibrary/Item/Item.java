package com.davidpapazian.yokaiwatchmedals.medalLibrary.Item;


import android.util.Log;

import com.davidpapazian.yokaiwatchmedals.gui.MainActivity;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Item {

    protected int id;
    protected Map<String, String> names = new HashMap<>();
    protected String imageName;
    public final static int TYPE_MEDAL = 1;
    public final static int TYPE_PRODUCT = 2;
    public final static int TYPE_YOKAI = 3;

    public int getId(){
        return id;
    }

    public abstract String getName();

    public String getImageName(){
        return imageName;
    }

    public abstract boolean hasState(Integer[] newState);

    protected String getNameInLangFromDb(Map<String,String> names, String table, int id) {
        String name = names.get(LocaleHelper.getAppLang());
        if (name == null) {
            name = DatabaseHelper.getInstance().getName(table, id);
            names.put(LocaleHelper.getAppLang(), name);
        }
        return name;
    }

    protected String getNameInLang(String table, int id) {
        return MedalLibrary.getInstance().getAttributeNames(table).get(id);
    }

    public int getType() {
        if (this instanceof Medal)
            return TYPE_MEDAL;
        else if (this instanceof Product)
            return TYPE_PRODUCT;
        else
            return TYPE_YOKAI;
    }
}