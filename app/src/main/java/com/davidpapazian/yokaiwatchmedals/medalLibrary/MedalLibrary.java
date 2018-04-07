package com.davidpapazian.yokaiwatchmedals.medalLibrary;

import android.content.Intent;
import android.util.Log;

import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.gui.MainActivity;
import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Yokai;

import java.util.ArrayList;

public class MedalLibrary {

    private static MedalLibrary sMedalLibrary;

    private ArrayList<Medal> medalList = new ArrayList<>();
    private ArrayList<Integer[]> medalFilterStates = new ArrayList<>();
    private ArrayList<String> medalSerieNames = new ArrayList<>();
    private ArrayList<String> medalColorNames = new ArrayList<>();
    private ArrayList<String> medalTypeNames = new ArrayList<>();

    private ArrayList<Product> productList = new ArrayList<>();
    private ArrayList<Integer[]> productFilterStates = new ArrayList<>();
    private ArrayList<String> productTypeNames = new ArrayList<>();

    private ArrayList<Yokai> yokaiList = new ArrayList<>();
    private ArrayList<Integer[]> yokaiFilterStates = new ArrayList<>();
    private ArrayList<String> yokaiGameNames = new ArrayList<>();
    private ArrayList<String> yokaiTribeNames = new ArrayList<>();
    private ArrayList<String> yokaiTypeNames = new ArrayList<>();

    private boolean isInitiated = false;

    public static MedalLibrary getInstance(){
        if (sMedalLibrary == null) {
            Log.w("test", "MedalLibrary instantiating");
            sMedalLibrary = new MedalLibrary();
        }
        return sMedalLibrary;
    }

    public static void initialise() {
        Log.w("test", "MedalLibrary initialising");
        YWMApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
                getInstance().resetLists();
                databaseHelper.browseMedal();
                databaseHelper.browseProduct();
                databaseHelper.browseYokai();
                getInstance().isInitiated = true;
                Intent intent = new Intent();
                intent.setAction(MainActivity.MEDAL_LIBRARY_READY);
                YWMApplication.getInstance().sendBroadcast(intent);
            }
        });
    }

    public boolean isInitiated() {
        return getInstance().isInitiated;
    }

    public ArrayList<Medal> getMedalList() {
        return medalList;
    }

    public ArrayList<Integer[]> getMedalFilterStates() {
        return medalFilterStates;
    }

    public ArrayList<Product> getProductList() {
        return productList;
    }

    public ArrayList<Integer[]> getProductFilterStates() {
        return productFilterStates;
    }

    public ArrayList<Yokai> getYokaiList() {
        return yokaiList;
    }

    public ArrayList<Integer[]> getYokaiFilterStates() {
        return yokaiFilterStates;
    }

    public void setMedalList(ArrayList<Medal> newList) {
        medalList.clear();
        medalList = newList;
    }

    public void setMedalFilterStates(ArrayList<Integer[]> newList) {
        medalFilterStates.clear();
        medalFilterStates = newList;
    }

    public void setProductList(ArrayList<Product> newList) {
        productList.clear();
        productList = newList;
    }

    public void setProductFilterStates(ArrayList<Integer[]> newList) {
        productFilterStates.clear();
        productFilterStates = newList;
    }

    public void setYokaiList(ArrayList<Yokai> newList) {
        yokaiList.clear();
        yokaiList = newList;
    }

    public void setYokaiFilterStates(ArrayList<Integer[]> newList) {
        yokaiFilterStates.clear();
        yokaiFilterStates = newList;
    }

    public Medal getMedalFromId(int id) {
        for (Medal medal : medalList)
            if (medal.getId() == id)
                return medal;
        return null;
    }

    public Product getProductFromId(int id) {
        for (Product product : productList)
            if (product.getId() == id)
                return product;
        return null;
    }

    public Yokai getYokaiFromId(int id) {
        for (Yokai yokai : yokaiList)
            if (yokai.getId() == id)
                return yokai;
        return null;
    }

    public void resetLists() {
        medalSerieNames = null;
        medalColorNames = null;
        medalTypeNames = null;
        productTypeNames = null;
        yokaiGameNames = null;
        yokaiTribeNames = null;
        yokaiTypeNames = null;
    }

    public ArrayList<String> getAttributeNames(String table) {
        ArrayList<String> list;
        switch (table) {
            case "MedalSerie":
                list =  medalSerieNames;
                break;
            case "MedalColor":
                list = medalColorNames;
                break;
            case "MedalType":
                list = medalTypeNames;
                break;
            case "ProductType":
                list = productTypeNames;
                break;
            case "YokaiGame":
                list = yokaiGameNames;
                break;
            case "YokaiTribe":
                list = yokaiTribeNames;
                break;
            default: //case "YokaiType":
                list = yokaiTypeNames;
                break;
        }
        if (list == null || list.isEmpty()) {
            setAttributeNames(table);
            return getAttributeNames(table);
        } else {
            return list;
        }
    }

    public void setAttributeNames(String table) {
        switch (table) {
            case "MedalSerie":
                medalSerieNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            case "MedalColor":
                medalColorNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            case "MedalType":
                medalTypeNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            case "ProductType":
                productTypeNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            case "YokaiGame":
                yokaiGameNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            case "YokaiTribe":
                yokaiTribeNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
            default: //case "YokaiType":
                yokaiTypeNames = DatabaseHelper.getInstance().getAttributeNames(table);
                break;
        }
    }
}
