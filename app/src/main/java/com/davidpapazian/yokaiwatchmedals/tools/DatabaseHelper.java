package com.davidpapazian.yokaiwatchmedals.tools;


import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Product;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Yokai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper  extends SQLiteOpenHelper {

    public final static String CACHE_DIR = YWMApplication.CACHE_DIR;
    public final static String DB_DIR = YWMApplication.DB_DIR;
    public final static String ROOT_URL = YWMApplication.ROOT_URL;
    final static String DB_NAME = "database";
    final static String SAVE_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/Yo-kai Watch Medals";
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z", Locale.ENGLISH);

    private Context context;
    private static SQLiteDatabase myDataBase;
    private static DatabaseHelper sHelper;
    private MedalLibrary medalLibrary;
    private static boolean databaseReady = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        this.medalLibrary = MedalLibrary.getInstance();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (sHelper == null) {
            sHelper = new DatabaseHelper(YWMApplication.getInstance());
            sHelper.checkDataBase();
        }
        return sHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public synchronized void close() {
        //dbAccess--;
        //if (dbAccess == 0) {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
        //}
    }

    //opens database as only readable
    public void openDataBase() throws SQLException {
        //dbAccess++;
        myDataBase = SQLiteDatabase.openDatabase(DB_DIR + "/" + DB_NAME + ".db", null, SQLiteDatabase.OPEN_READWRITE);

    }

    //checks if dababase already exists
    private void checkDataBase() {
        YWMApplication.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Log.w("test", "checkDataBase");
                SQLiteDatabase tempDB = null;
                try {
                    String myPath = DB_DIR + "/" + DB_NAME + ".db";
                    Log.w("test", myPath);
                    tempDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
                } catch (SQLiteException e) {
                    Log.w("test", "db not found");
                }
                if (tempDB == null) {
                    Log.w("test", "db absent, so lets copy it");
                    getInstance().getReadableDatabase();
                    try {
                        getInstance().copyDataBase();
                    } catch (Exception e) {}
                } else {
                    Log.w("test", "db present");
                    tempDB.close();
                }
                onDatabaseReady();
            }
        });
    }

    //copies the entire database in storage, it happens only once
    public void copyDataBase(){
        try {
            InputStream myInput = context.getAssets().open(DB_NAME + ".sqlite");
            String outputFileName = DB_DIR + "/" + DB_NAME + ".db";
            OutputStream myOutput = new FileOutputStream(outputFileName);
            byte[] buffer = new byte[1024];
            int length;
            while((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            Log.w("test", "failed to copy");
        }
    }


    public synchronized String getName(String table, int id) {
        String name;
        openDataBase();
        Cursor myCursor = myDataBase.rawQuery("SELECT name" + LocaleHelper.getAppLang() + " FROM " + table + " WHERE _id = " + String.valueOf(id), null);
        myCursor.moveToFirst();
        name = myCursor.getString(0);
        myCursor.close();
        myDataBase.close();
        return name;

    }

    public void browseMedal() {
        browseMedalList();
        browseMedalFilterStates();
    }

    public void browseProduct() {
        browseProductList();
        browseProductFilterStates();
    }

    public void browseYokai() {
        browseYokaiList();
        browseYokaiFilterStates();
    }

    public synchronized void browseMedalListOLD() {
        MedalLibrary.getInstance().getMedalList().clear();
        ArrayList<Medal> newList = new ArrayList<>();
        openDataBase();
        try {
            String select = "_id, code, orderByCode, name, nameEN, MedalSerie, MedalColor, MedalType, Variant, Soultimate, Y1, Y2, Y3, Y4, Y5, Y6, Y7, Y8";
            String where = "WHERE code <> '0' AND code <> ''";
            Cursor myCursor = myDataBase.rawQuery("SELECT " + select + " FROM Medal " + where, null);
            myCursor.moveToFirst();
            do {
                int id = myCursor.getInt(0);
                String code = myCursor.getString(1);
                int codeOrder =  myCursor.getInt(2);
                if (codeOrder == 0)
                    codeOrder = 999999;
                String name = myCursor.getString(3);
                String nameEN = myCursor.getString(4);
                int serie = myCursor.getInt(5);
                int color = myCursor.getInt(6);
                int type = myCursor.getInt(7);
                int variant = myCursor.getInt(8);
                int soultimate = myCursor.getInt(9);
                ArrayList<Integer> yokaiList = new ArrayList<>();
                for (int i = 1; i<9; i++) {
                    int yokaiId = myCursor.getInt(9 + i);
                    if (yokaiId != 0)
                        yokaiList.add(yokaiId);
                    else
                        break;
                }
                Medal medal = new Medal(id, name, nameEN, code, codeOrder, serie, color, type, variant, soultimate, null);
                newList.add(medal);
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setMedalList(newList);
        }
        catch (Exception e) {
            Log.w("test", e.getMessage());
        }
    }

    public synchronized void browseMedalList() {
        MedalLibrary.getInstance().getMedalList().clear();
        ArrayList<Medal> newList = new ArrayList<>();
        String medalLang = LocaleHelper.getMedalLang();
        String suffixLang = LocaleHelper.getSuffixLang();
        openDataBase();
        try {
            String select = "Medal._id, Medal.code, Medal.orderByCode, Medal.MedalSerie, Medal.MedalColor, Medal.MedalType, Medal.Variant, Medal.Soultimate, " +
                    "Medal.Y1, Medal.Y2, Medal.Y3, Medal.Y4, Medal.Y5, Medal.Y6, Medal.Y7, Medal.Y8, " +
                    "Yokai.name" + medalLang + ", MedalSpecialName.name" + medalLang + ", MedalNameSuffix.name" + suffixLang;
            String from = "Medal LEFT JOIN MedalNameSuffix ON Medal.MedalNameSuffix = MedalNameSuffix._id " +
                    "LEFT JOIN Yokai ON Medal.Y1 = Yokai._id " +
                    "LEFT JOIN MedalSpecialName ON Medal.MedalSpecialName = MedalSpecialName._id";
            String where = "Medal.code <> '0' AND Medal.code <> ''";
            String orderBy = "Medal.orderByCode ASC";
            Cursor myCursor = myDataBase.rawQuery("SELECT " + select + " FROM " + from + " WHERE " + where + " ORDER BY " + orderBy, null);
            myCursor.moveToFirst();
            do {
                int id = myCursor.getInt(0);
                String code = myCursor.getString(1);
                int codeOrder =  myCursor.getInt(2);
                if (codeOrder == 0)
                    codeOrder = 999999;
                //String name = myCursor.getString(3);
                //String nameEN = myCursor.getString(4);
                int serie = myCursor.getInt(3);
                int color = myCursor.getInt(4);
                int type = myCursor.getInt(5);
                int variant = myCursor.getInt(6);
                int soultimate = myCursor.getInt(7);
                ArrayList<Integer> yokaiList = new ArrayList<>();
                for (int i = 1; i<9; i++) {
                    int yokaiId = myCursor.getInt(7 + i);
                    if (yokaiId != 0)
                        yokaiList.add(yokaiId);
                    else
                        break;
                }
                String suffix = myCursor.getString(18);
                String name;
                if (suffix.contains("---")) //case special name
                    name = suffix.replace("---", myCursor.getString(17));
                else //case name of the first yokai
                    name = myCursor.getString(16) + suffix;
                Medal medal = new Medal(id, name, name, code, codeOrder, serie, color, type, variant, soultimate, yokaiList);
                newList.add(medal);
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setMedalList(newList);
        }
        catch (Exception e) {
            Log.w("test", e.getMessage());
        }
    }

    public synchronized void browseMedalFilterStates() {
        medalLibrary.getMedalFilterStates().clear();
        ArrayList<Integer[]> newList = new ArrayList<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT DISTINCT MedalSerie, MedalColor, MedalType FROM Medal", null);
            myCursor.moveToFirst();
            do {
                newList.add(new Integer[]{myCursor.getInt(0), myCursor.getInt(1), myCursor.getInt(2)});
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setMedalFilterStates(newList);
        }
        catch (Exception e) {
            myDataBase.close();
        }
    }

    public synchronized void browseProductList() {
        medalLibrary.getProductList().clear();
        ArrayList<Product> newList = new ArrayList<>();
        openDataBase();
        try {
            String select = "_id, nameEN, nameJP_kanji, ProductType, medals, date";
            String where = "WHERE _id <> '0'";
            String order = "date ASC";
            Cursor myCursor = myDataBase.rawQuery("SELECT " + select + " FROM Product " + where + " ORDER BY " + order, null);
            myCursor.moveToFirst();
            do {
                int id = myCursor.getInt(0);
                String nameEN = myCursor.getString(1);
                String nameJP = myCursor.getString(2);
                int category = myCursor.getInt(3);
                int nbEach = myCursor.getInt(4);
                String date = myCursor.getString(5);
                Product product = new Product(id, nameEN, nameJP, category, date, nbEach);
                newList.add(product);
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setProductList(newList);
        }
        catch (SQLiteException e) {Log.e("test", e.getMessage());}
    }

    public synchronized void browseProductFilterStates() {
        medalLibrary.getProductFilterStates().clear();
        ArrayList<Integer[]> newList = new ArrayList<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT DISTINCT ProductType FROM Product", null);
            myCursor.moveToFirst();
            do {
                newList.add(new Integer[]{myCursor.getInt(0)});
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setProductFilterStates(newList);
        }
        catch (Exception e) {
            myDataBase.close();
            Log.w("testDB", e.getMessage());
        }
    }

    public synchronized void browseYokaiListOLD() {
        medalLibrary.getYokaiList().clear();
        ArrayList<Yokai> newList = new ArrayList<>();
        String select = "_id, nameEN, nameJP_kanji, YokaiTribe, YokaiType";
        ArrayList<String> games = getGamesShortNames();
        for (String game : games)
            select += ", no" + game;
        String where = "WHERE _id <> '0'";
        String order = "_id ASC";
        openDataBase();

        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT " + select + " FROM Yokai " + where + " ORDER BY " + order, null);
            Log.w("testDB", "in browseYokaiList, quesry : " + "SELECT " + select + " FROM Yokai " + where + " ORDER BY " + order);

            myCursor.moveToFirst();
            do {
                int id = myCursor.getInt(0);
                String nameEN = myCursor.getString(1);
                String nameJP = myCursor.getString(2);
                int tribeId = myCursor.getInt(3);
                int typeId = myCursor.getInt(4);
                Integer[] nbList = new Integer[games.size()+1];
                for (int i = 1; i<nbList.length; i++)
                    nbList[i] = myCursor.getInt(i+4);
                Yokai yokai = new Yokai(id, nameEN, nameJP, tribeId, typeId, nbList);
                newList.add(yokai);
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setYokaiList(newList);
        }
        catch (Exception e) {
            myDataBase.close();
            Log.w("testDB", e.getMessage());
        }
    }

    public synchronized void browseYokaiList() {
        medalLibrary.getYokaiList().clear();
        boolean onlyOnMedals = PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getBoolean("only_yokai_on_medals", true);
        ArrayList<Yokai> newList = new ArrayList<>();
        String select = "DISTINCT Yokai._id, Yokai.nameEN, Yokai.nameJP_kanji, Yokai.YokaiTribe, Yokai.YokaiType";
        ArrayList<String> games = getGamesShortNames();
        for (String game : games)
            select += ", Yokai.no" + game;
        String from;
        if (onlyOnMedals)
            from = "Yokai JOIN MEDAL ON Yokai._id IN (Medal.Y1, Medal.Y2, Medal.Y3, Medal.Y4, Medal.Y5, Medal.Y6, Medal.Y7, Medal.Y8)";
        else
        from = "Yokai";
        String where = "Yokai._id <> '0'";
        String order = "Yokai._id ASC";
        openDataBase();

        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT " + select + " FROM " + from + " WHERE " + where + " ORDER BY " + order, null);
            Log.w("testDB", "in browseYokaiList, quesry : " + "SELECT " + select + " FROM " + from + " WHERE " + where + " ORDER BY " + order);

            myCursor.moveToFirst();
            do {
                int id = myCursor.getInt(0);
                String nameEN = myCursor.getString(1);
                String nameJP = myCursor.getString(2);
                int tribeId = myCursor.getInt(3);
                int typeId = myCursor.getInt(4);
                Integer[] nbList = new Integer[games.size()+1];
                for (int i = 1; i<nbList.length; i++)
                    nbList[i] = myCursor.getInt(i+4);
                Yokai yokai = new Yokai(id, nameEN, nameJP, tribeId, typeId, nbList);
                newList.add(yokai);
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
            medalLibrary.setYokaiList(newList);
        }
        catch (Exception e) {
            myDataBase.close();
            Log.w("testDB", e.getMessage());
        }
    }

    public synchronized ArrayList<String> getGamesShortNames() {
        ArrayList<String> newList = new ArrayList<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT short FROM YokaiGame WHERE _id <> 0 ORDER BY _id ASC", null);
            myCursor.moveToFirst();
            do {
                newList.add(myCursor.getString(0));
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
            Log.w("testDB", e.getMessage());
        }
        return newList;
    }

    public synchronized void browseYokaiFilterStates() {
        medalLibrary.getYokaiFilterStates().clear();
        ArrayList<Integer[]> newList = new ArrayList<>();
        String select = "YokaiTribe, YokaiType";
        ArrayList<String> games = getGamesShortNames();
        for (String game : games)
            select += ", SUM(no" + game + ")>0";
        openDataBase();

        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT DISTINCT " + select + " FROM Yokai GROUP BY YokaiTribe, YokaiType", null);
            Log.w("testDB", "in browseYokaiFilterStates, query : " + "SELECT DISTINCT " + select + " FROM Yokai GROUP BY YokaiTribe, YokaiType");
            myCursor.moveToFirst();
            do {
                for (int i=1; i<1+games.size(); i++)
                    if (myCursor.getInt(i+1) == 1)
                        newList.add(new Integer[]{i, myCursor.getInt(0), myCursor.getInt(1)});
            } while (myCursor.moveToNext());
            myCursor.close();

            myDataBase.close();
            medalLibrary.setYokaiFilterStates(newList);
        }
        catch (Exception e) {
            myDataBase.close();
            Log.w("testDB", e.getMessage());
        }
    }

    public synchronized ArrayList<String> getAttributeNames(String table) {
        ArrayList<String> newList = new ArrayList<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT name" + LocaleHelper.getAppLang() + " FROM " + table, null);
            myCursor.moveToFirst();
            do {
                newList.add(myCursor.getString(0));
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
        }
        return newList;
    }

    public synchronized Map<Integer, String> getProductIdListFromMedal(int id) {
        Map<Integer, String> newList = new HashMap<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT product, code FROM MedalVar WHERE medalID = " + String.valueOf(id), null);
            myCursor.moveToFirst();
            do {
                newList.put(myCursor.getInt(0), myCursor.getString(1));
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
        }
        return newList;
    }

    public synchronized ArrayList<Integer> getYokaiIdListFromMedal(int id) {
        ArrayList<Integer> newList = new ArrayList<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT Y1, Y2, Y3, Y4, Y5, Y6, Y7, Y8 FROM Medal WHERE _id = " + String.valueOf(id), null);
            myCursor.moveToFirst();
            int i = 0;
            do {
                newList.add(myCursor.getInt(i));
                i++;
            } while (i <= 7 && myCursor.getInt(i) != 0);
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
        }
        return newList;
    }

    public synchronized Map<Integer, String> getMedalIdListFromProduct(int id) {
        Map<Integer, String> newList = new HashMap<>();
        openDataBase();
        try {
            Cursor myCursor = myDataBase.rawQuery("SELECT medalID, code FROM MedalVar WHERE product = " + String.valueOf(id), null);
            myCursor.moveToFirst();
            do {
                newList.put(myCursor.getInt(0), myCursor.getString(1));
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
        }
        return newList;
    }

    public synchronized Map<Integer, String> getMedalIdListFromYokai(int id) {
        Map<Integer, String> newList = new HashMap<>();
        openDataBase();
        try {
            String sId = String.valueOf(id);
            Cursor myCursor = myDataBase.rawQuery("SELECT _id, code FROM Medal WHERE Y1 = " + sId + " OR Y2 = " + sId + " OR Y3 = " + sId + " OR Y4 = " + sId + " OR Y5 = " + sId + " OR Y6 = " + sId + " OR Y7 = " + sId + " OR Y8 = " + sId, null);
            myCursor.moveToFirst();
            do {
                newList.put(myCursor.getInt(0), myCursor.getString(1));
            } while (myCursor.moveToNext());
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e) {
            myDataBase.close();
        }
        return newList;
    }

    public Medal findVariant(int id) {
        for (Medal medal : medalLibrary.getMedalList())
            if (medal.getId() == id)
                return medal;
        return null;
    }

    public Medal findSoultimate(int id) {
        for (Medal medal : medalLibrary.getMedalList())
            if (medal.getId() == id)
                return medal;
        return null;
    }

    public synchronized Object[] getSaveFileInfo() {
        Object[] info = new Object[3];
        openDataBase();
        Cursor myCursor = myDataBase.rawQuery("SELECT savedSaveFileVersion, currentSaveFileVersion, currentSaveFileName FROM save", null);
        myCursor.moveToFirst();
        info[0] = myCursor.getInt(0);
        info[1] = myCursor.getInt(1);
        info[2] = myCursor.getString(2);
        myCursor.close();
        myDataBase.close();
        return info;
    }

    public synchronized void saveCollectionFile(String name) {

        String saveType = "0";
        Object[] info = getSaveFileInfo();
        int version = (int) info[1];
        String data = "Yo-kai Watch Manualis\nsaveType\t" + saveType + "\nversion\t" + String.valueOf(version);

        openDataBase();

        String mot;

        try {
            Cursor firstCursor = myDataBase.rawQuery("SELECT _id, otherInCollection, want, favourite FROM Medal WHERE otherInCollection <> 0 or want <> 0 or favourite <> 0", null);
            firstCursor.moveToFirst();
            do {
                data += "\n" + "m" + "\t" + String.valueOf(firstCursor.getInt(0)) + "\t" + String.valueOf(firstCursor.getInt(1)) + "\t" + String.valueOf(firstCursor.getInt(2)) + "\t" + String.valueOf(firstCursor.getInt(3));}
            while (firstCursor.moveToNext());
            firstCursor.close();
        }
        catch (CursorIndexOutOfBoundsException e) {}

        try {
            Cursor secondCursor = myDataBase.rawQuery("SELECT _id, medalID, inCollection FROM MedalVar WHERE inCollection <> 0", null);
            secondCursor.moveToFirst();
            do {
                data += "\n" + "v" + "\t" + String.valueOf(secondCursor.getInt(0)) + "\t" + String.valueOf(secondCursor.getInt(2)) + "\t" + String.valueOf(secondCursor.getInt(1));}
            while (secondCursor.moveToNext());
            secondCursor.close();
        }
        catch (CursorIndexOutOfBoundsException e) {}

        myDataBase.close();

        data += "\n";

        File save = new File(SAVE_FOLDER, "save_" + name + ".txt");

        /*
        if (save.exists()) {
            save.delete();
        }
        */

        try {
            FileOutputStream out = new FileOutputStream(save);
            out.write(data.getBytes());
            out.flush();
            out.close();
        }
        catch (IOException e) {}

    }

    public synchronized double getMinimumVersion(){
        double minVer = 0.0;
        try {
            openDataBase();
            Cursor myCursor = myDataBase.rawQuery("SELECT minVer FROM date", null);
            myCursor.moveToFirst();
            minVer = Double.valueOf(myCursor.getString(0));
            myCursor.close();
            myDataBase.close();
        }
        catch (Exception e){}
        return minVer;
    }

    public synchronized Date getCurrentDatabaseDate() {
        try {
            Date date;
            openDataBase();
            Cursor myCursor = myDataBase.rawQuery("SELECT currentDate from date", null);
            myCursor.moveToFirst();
            date = dateFormat.parse(myCursor.getString(0));
            myCursor.close();
            myDataBase.close();
            return date;
        }
        catch (Exception e){}
        return null;
    }

    public synchronized void setCurrentDatabaseDate(Date date) {
        try {
            openDataBase();
            Log.w("test", "setting yokai date : " + dateFormat.format(date));
            myDataBase.execSQL("UPDATE date SET currentDate = '" + dateFormat.format(date) + "'");
            myDataBase.close();
        }
        catch (Exception e){e.printStackTrace();}
    }

    public boolean isReady() {
        return databaseReady;
    }

    public void onDatabaseReady() {
        databaseReady = true;
        MedalLibrary.initialise();
    }

}
