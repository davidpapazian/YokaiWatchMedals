package com.davidpapazian.yokaiwatchmedals.medalLibrary.Item;

import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Product extends Item {

    private int categoryId;
    private String date;
    private int nbEach;
    private int nbTotal;

    private ArrayList<Integer> medalIdList;
    private ArrayList<Medal> medalList;
    private Map<Integer, String> medalCodeList;

    public Product(int id, String nameEN, String nameJP, int categoryId, String date, int nbEach){
        this.id = id;
        this.categoryId = categoryId;
        this.date = date;
        this.nbEach = nbEach;
        names.put("EN", nameEN);
        names.put("JP_kanji", nameJP);
        this.imageName = "product_" + String.format(Locale.ENGLISH, "%04d", id);
    }

    public String getName() {
        String name;
        switch (LocaleHelper.getAppLang()) {
            case "JP_kanji":
            case "JP_romaji":
                name = names.get("JP_kanji");
                break;
            default:
                name = names.get("EN");
                break;
        }
        if (name != null && !name.equals(""))
            return name;
        else
            return names.get("JP_kanji");
    }

    public String getDate() {
        return (date == null || date.isEmpty()) ? "0000" : date;
    }

    public int getNbEach(){
        return nbEach;
    }

    public int getCategoryId(){
        return categoryId;
    }

    public int getNbTotal(){
        return medalIdList.size();
    }

    @Override
    public boolean hasState(Integer[] newState) {
        return (newState[0] == 0 || categoryId == newState[0]);
    }

    public ArrayList<Medal> getMedalList() {
        if (medalList == null) {
            if (medalIdList == null) {
                medalCodeList = DatabaseHelper.getInstance().getMedalIdListFromProduct(id);
                medalIdList = new ArrayList<>(medalCodeList.keySet());
            }
            medalList = new ArrayList<>();
            for (int id : medalIdList)
                medalList.add(MedalLibrary.getInstance().getMedalFromId(id));
        }
        return medalList;
    }

    public Map<Integer, String> getMedalCodeList() {
        return medalCodeList;
    }
}
