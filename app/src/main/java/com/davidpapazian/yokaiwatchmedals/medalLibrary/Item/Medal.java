package com.davidpapazian.yokaiwatchmedals.medalLibrary.Item;

import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Medal extends Item {

    private String code;
    private int codeOrder;
    private int serieId;
    private int colorId;
    private int typeId;

    private int variantId;
    private Medal variant;

    private int soultimateId;
    private Medal soultimate;

    private ArrayList<Integer> productIdList;
    private Map<Integer,String> productCodeList;
    private ArrayList<Product> productList;

    private ArrayList<Integer> yokaiIdList;
    private ArrayList<Yokai> yokaiList;

    public Medal(int id, String name, String nameEN, String code, int codeOrder, int serieId, int colorId, int typeId, int variantId, int soultimateId, ArrayList<Integer> yokaiIdList) {
        this.id = id;
        this.code = code;
        this.codeOrder = codeOrder;
        this.serieId = serieId;
        this.colorId = colorId;
        this.typeId = typeId;
        this.variantId = variantId;
        this.soultimateId = soultimateId;
        this.yokaiIdList = yokaiIdList;
        this.imageName = "medal_" + code.toLowerCase().replace("(","_").replace("-","_").replace(")","");
        names.put("JP_kanji", name);
        names.put("EN", nameEN);
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

    public String getCode(){
        return code;
    }

    public int getCodeOrder() {
        return codeOrder;
    }

    public int getSerieId(){
        return serieId;
    }

    public String getSerieName() {
        return getNameInLang("MedalSerie", serieId);
    }

    public int getColorId(){
        return colorId;
    }

    public String getColorName() {
        return getNameInLang("MedalColor", colorId);
    }

    public int getTypeId(){
        return typeId;
    }

    public String getTypeName() {
        return getNameInLang("MedalType", typeId);
    }

    public String getImageName(){
        return imageName;
    }

    public int getVariantId(){
        return variantId;
    }

    public Medal getVariant(){
        if (variantId == 0)
            return null;
        if (variant == null) {
            Medal foundVariant = DatabaseHelper.getInstance().findVariant(variantId);
            if (foundVariant != null)
                variant = foundVariant;
        }
        return variant;
    }

    public int getSoultimateId(){
        return soultimateId;
    }

    public Medal getSoultimate(){
        if (soultimateId == 0)
            return null;
        if (soultimate == null) {
            Medal foundSoultimate = DatabaseHelper.getInstance().findSoultimate(soultimateId);
            if (foundSoultimate != null)
                soultimate = foundSoultimate;
        }
        return soultimate;
    }

    @Override
    public boolean hasState(Integer[] newState) {
        return (newState[0] == 0 || serieId == newState[0])
                && (newState[1] == 0 || colorId == newState[1])
                && (newState[2] == 0 || typeId == newState[2]);
    }

    public ArrayList<Product> getProductList() {
        if (productList == null) {
            if (productIdList == null) {
                productCodeList = DatabaseHelper.getInstance().getProductIdListFromMedal(id);
                productIdList = new ArrayList<>(productCodeList.keySet());
            }
            productList = new ArrayList<>();
            for (int id : productIdList)
                productList.add(MedalLibrary.getInstance().getProductFromId(id));
        }
        return productList;
    }

    public Map<Integer, String> getProductCodeList() {
        return productCodeList;
    }

    public ArrayList<Yokai> getYokaiList() {
        if (yokaiList == null) {
            if (yokaiIdList == null) {
                yokaiIdList = DatabaseHelper.getInstance().getYokaiIdListFromMedal(id);
            }
            yokaiList = new ArrayList<>();
            for (int id : yokaiIdList)
                yokaiList.add(MedalLibrary.getInstance().getYokaiFromId(id));
        }
        return yokaiList;
    }
}
