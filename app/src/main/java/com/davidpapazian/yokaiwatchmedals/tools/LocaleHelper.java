package com.davidpapazian.yokaiwatchmedals.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.StatFs;
import android.preference.PreferenceManager;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;

import java.util.Locale;
import java.util.Stack;

public class LocaleHelper {


    public static final int DE = 1;
    public static final int EN = 2;
    public static final int ES = 3;
    public static final int FR = 4;
    public static final int IT = 5;
    public static final int JP_kanji = 6;
    public static final int JP_kana = 7;
    public static final int JP_romaji = 8;
    public static final int NL = 9;
    public static final int PT = 10;
    public static final int RU = 11;
    public static final int KR = 12;
    //public static int currentLang = 2;

    public static Context onAttach(Context context) {
        String localeString = dbLangToLocale(getAppLang());
        return setLocale(context, localeString);
    }

    public static Context setLocale(Context context, String localeString) {
        setAppLang(localeToDbLang(localeString));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, localeString);
        }

        return updateResourcesLegacy(context, localeString);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String localeString) {
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String localeString) {
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }


    public static void setAppLang(String dbLang) {
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putString("appLang", dbLang).apply();
    }

    public static void setMedalLang(String dbLang) {
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putString("medalLang", dbLang).apply();
    }

    public static void setProductLang(String dbLang) {
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putString("productLang", dbLang).apply();
    }

    public static void setYokaiLang(String dbLang) {
        PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).edit().putString("yokaiLang", dbLang).apply();
    }

    public static String getAppLang() {
        return PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getString("appLang", localeToDbLang(Locale.getDefault().getLanguage()));
    }

    public static String getMedalLang() {
        return PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getString("medalLang", getAppLang());
    }

    public static String getProductLang() {
        return PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getString("productLang", getAppLang());
    }

    public static String getYokaiLang() {
        return PreferenceManager.getDefaultSharedPreferences(YWMApplication.getInstance()).getString("yokaiLang", getAppLang());
    }

    public static String getSuffixLang() {
        if (getMedalLang().equals("JP_kanji"))
            return "JP_kanji";
        else
            return getAppLang();
    }

    public static String localeToDbLang(String locale) {
        String lang;
        if (locale.contains("fr"))
            lang = "FR";
        else if (locale.contains("de"))
            lang = "DE";
        else if (locale.contains("it"))
            lang = "IT";
        else if (locale.contains("es"))
            lang = "ES";
        else if (locale.contains("ru"))
            lang = "RU";
        else if (locale.contains("ja"))
            lang = "JP_kanji";
        else
            lang = "EN";
        return lang;
    }

    public static String dbLangToLocale(String lang) {
        String locale;
        if (lang.equals("FR"))
            locale = "fr";
        else if (lang.equals("DE"))
            locale = "de";
        else if (lang.equals("IT"))
            locale = "it";
        else if (lang.equals("ES"))
            locale = "es";
        else if (lang.equals("RU"))
            locale = "ru";
        else if (lang.equals("JP") || lang.equals("JP_kanji") || lang.equals("JP_romaji"))
            locale = "ja";
        else
            locale = "en";
        return locale;
    }

    public static String getLangString(int i){
        switch (i){
            case DE:
                return "DE";
            case EN:
                return "EN";
            case ES:
                return "ES";
            case FR:
                return "FR";
            case IT:
                return "IT";
            case JP_kana:
                return "JP_kana";
            case JP_kanji:
                return "JP_kanji";
            case JP_romaji:
                return "JP_romaji";
            case NL:
                return "EN";
            case PT:
                return "EN";
            case RU:
                return "EN";
            case KR:
                return "EN";
            default:
                return "EN";
        }
    }
}