package com.davidpapazian.yokaiwatchmedals.medalLibrary.Item;

import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;
import com.davidpapazian.yokaiwatchmedals.tools.LocaleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Yokai extends Item {

    private Integer[] nbInGames;
    private int typeId;
    private int tribeId;

    private ArrayList<Integer> medalIdList;
    private ArrayList<Medal> medalList;
    private Map<Integer, String> medalCodeList;

    public Yokai(int id, String nameEN, String nameJP, int tribeId, int typeId, Integer[] nbInGames){
        this.id = id;
        this.tribeId = tribeId;
        this.typeId = typeId;
        this.nbInGames = nbInGames;
        this.imageName = "yokai_" + String.valueOf(id);
        names.put("JP_kanji", nameJP);
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

    public int getNbInGame(int gameId){
        return nbInGames[id];
    }

    @Override
    public boolean hasState(Integer[] newState) {
        return (newState[0] == 0 || nbInGames[newState[0]] != 0)
                && (newState[1] == 0 || tribeId == newState[1])
                && (newState[2] == 0 || typeId == newState[2]);
    }

    public ArrayList<Medal> getMedalList() {
        if (medalList == null) {
            if (medalIdList == null) {
                medalCodeList = DatabaseHelper.getInstance().getMedalIdListFromYokai(id);
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

    public String getTypeName() {
        return getNameInLang("YokaiType", typeId);
    }

    public String getTribeName() {
        return getNameInLang("YokaiTribe", tribeId);
    }
}
