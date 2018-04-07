package com.davidpapazian.yokaiwatchmedals.tools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.davidpapazian.yokaiwatchmedals.YWMApplication;
import com.davidpapazian.yokaiwatchmedals.gui.MainActivity;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Medal;
import com.davidpapazian.yokaiwatchmedals.medalLibrary.MedalLibrary;
import com.davidpapazian.yokaiwatchmedals.tools.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateHandler {

    public static boolean updateFetched = false;
    public final static int CANCEL_DELAY = 10000;
    public final static String CACHE_DIR = YWMApplication.CACHE_DIR;
    public final static String DB_DIR = YWMApplication.DB_DIR;
    public final static String ROOT_URL = YWMApplication.ROOT_URL;
    public static boolean fetchImages = false;
    public static boolean zip = false;
    public static boolean test = true;

    public static void fetchDataUpdate(final Context context) {

        if (fetchFirstTime(context))
            return;

        Log.w("in UpdateHandler", "fetching update");

        if (!updateFetched) {
            updateFetched = true;
            AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
                boolean dbUpdate = false;
                Date newestDate;
                Set<String> medal_DB = new HashSet<>();
                Set<String> medal_500 = new HashSet<>();
                Set<String> medal_120 = new HashSet<>();
                Set<String> product_DB = new HashSet<>();
                Set<String> product_500 = new HashSet<>();
                Set<String> product_120 = new HashSet<>();
                Set<String> yokai_DB = new HashSet<>();
                Set<String> yokai_500 = new HashSet<>();
                Set<String> yokai_120 = new HashSet<>();

                List<String> filesToDownload = new ArrayList<>();

                Handler handler = new Handler();
                TaskCanceler taskCanceler = new TaskCanceler(this);

                @Override
                protected Integer doInBackground(Void... params) {
                    int dlSize = 0;

                    Date currentYokaiDate = DatabaseHelper.getInstance().getCurrentDatabaseDate();
                    Log.w("in UpdateHandler", "old db : " + DatabaseHelper.dateFormat.format(currentYokaiDate));
                    handler.postDelayed(taskCanceler, CANCEL_DELAY);  //cancels task after 5 seconds
                    HttpURLConnection connection;

                    //fetch database update
                    try {   //compare "last-modified" date of online file with the date on the "date" table in local db
                        URL yokaiUrl = new URL(ROOT_URL + "/databases/database.sqlite");
                        connection = (HttpURLConnection) yokaiUrl.openConnection();
                        connection.connect();
                        newestDate = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(connection.getHeaderField("Last-Modified"));
                        Log.w("in UpdateHandler", "new db : " + DatabaseHelper.dateFormat.format(newestDate));
                        if (newestDate.after(currentYokaiDate)) {
                            Log.w("in UpdateHandler", "update available, time diff : " + String.valueOf(newestDate.compareTo(currentYokaiDate)));
                            dbUpdate = true;
                            dlSize += connection.getContentLength();
                        }
                    } catch (Exception e) {
                    }

                    try {
                        for (Medal medal : MedalLibrary.getInstance().getMedalList())
                            medal_DB.add(medal.getImageName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        for (File file : (new File(CACHE_DIR + "/medal_120/")).listFiles())
                            medal_120.add(file.getName().replace(".png", ""));
                        for (File file : (new File(CACHE_DIR + "/medal_500/")).listFiles())
                            medal_500.add(file.getName().replace(".png", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*
                    deleteUnusedImages("medal_120", medal_DB, medal_120);
                    deleteUnusedImages("medal_500", medal_DB, medal_500);
                    deleteUnusedImages("product_120", product_DB, product_120);
                    deleteUnusedImages("product_500", product_DB, product_500);
                    deleteUnusedImages("yokai_120", yokai_DB, yokai_120);
                    deleteUnusedImages("yokai_500", yokai_DB, yokai_500);
                    */

                    List<String> dirs = new ArrayList<>();
                    dirs.add("medal_120");
                    dirs.add("product_120");
                    dirs.add("yokai_120");
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("medal_500_enabled", false))
                        dirs.add("medal_500");
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("product_500_enabled", false))
                        dirs.add("product_500");
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("yokai_500_enabled", false))
                        dirs.add("yokai_500");

                    for (String dir : dirs) {
                        //check which updates have already downloaded
                        int i = 0;
                        while (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(dir + "_" + String.valueOf(i) + "_downloaded", false))
                            i++;
                        //fetch sizes
                        String zipName = "";
                        while (true) {
                            try {
                                zipName = dir + "_" + String.valueOf(i);
                                Log.w("in UpdateHandler", "starting fetching " + zipName);
                                URL url = new URL(ROOT_URL + "/" + zipName + ".zip");
                                connection = (HttpURLConnection) url.openConnection();
                                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                    Log.w("in UpdateHandler", zipName + " not found");
                                    break;
                                }
                                connection.connect();
                                dlSize += connection.getContentLength();
                                filesToDownload.add(zipName);
                                i++;
                            } catch (Exception e) {
                                Log.w("in UpdateHandler", zipName + " not found");
                                break;
                            }
                        }
                    }

                    if (taskCanceler != null && handler != null) {   //cancels the cancellation
                        handler.removeCallbacks(taskCanceler);
                        handler = null;
                        taskCanceler = null;
                    }
                    Log.w("in UpdateHandler", "updated fetched, size : " + String.valueOf(dlSize));
                    return dlSize;
                }

                @Override
                protected void onCancelled() {
                    onDatabaseUpdateFinished(false);
                }

                @Override
                protected void onPostExecute(final Integer total) {
                    if (total != 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Data update available: " + String.valueOf((int) Math.floor(total / 1024)) + " kB");
                        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadUpdates(context, dbUpdate, newestDate, filesToDownload, (long) total);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onDatabaseUpdateFinished(false);
                            }
                        });
                        builder.show();

                    } else {
                        onDatabaseUpdateFinished(false);
                    }
                }

            };


            task.execute();
        }
    }

    public static void downloadUpdates(final Context context, final boolean dbUpdate, final Date date, final List<String> filesToDownload, final long total) {
        final ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Downloading");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        final AsyncTask<Void, Integer, Boolean> task = new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                long progress = 0;
                InputStream input = null;
                OutputStream output = null;
                URLConnection connection = null;
                if (dbUpdate) {
                    try {
                        URL url = new URL(ROOT_URL + "/databases/database.sqlite");
                        connection = url.openConnection();
                        connection.connect();
                        input = connection.getInputStream();
                        output = new FileOutputStream(DB_DIR + "/database_new.db");

                        byte data[] = new byte[4096];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            if (isCancelled()) {
                                input.close();
                                return null;
                            }
                            progress += count;
                            publishProgress((int) (progress * 100 / total));
                            output.write(data, 0, count);
                        }

                        /* // COLLECTION
                        Object[] info = getSaveFileInfo();
                        boolean collectionEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("collection_enabled", false);
                        final int sVersion = (int) info[0];
                        final int cVersion = (int) info[1];
                        final String name = (String) info[2];
                        if (collectionEnabled && !name.equals("none")) {
                            saveCollectionFile("temp");
                        }
                        */

                        File oldFile = new File(DB_DIR + "/database.db");
                        File newOldFile = new File(DB_DIR + "/database_old.db");
                        File oldnewFile = new File(DB_DIR + "/database_new.db");
                        File newFile = new File(DB_DIR + "/database.db");
                        oldFile.renameTo(newOldFile);
                        oldnewFile.renameTo(newFile);

                        //verify whether the app version is recent enough to be able to read the new database
                        double minVer = DatabaseHelper.getInstance().getMinimumVersion();
                        double version = 0.0;
                        try {
                            version = Double.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName.replace("beta ", ""));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (version >= minVer) {
                            DatabaseHelper.getInstance().setCurrentDatabaseDate(date);
                            newOldFile.delete();
                        } else { //delete downloaded file
                            (new File(DB_DIR + "/database.db")).delete();
                            (new File(DB_DIR + "/database_old.db")).renameTo(new File(DB_DIR + "/database.db"));
                        }

                        /* // COLLECTION
                        if (collectionEnabled && !name.equals("none")) {
                            CollectionHandler newHandler = new CollectionHandler(context, this);
                            newHandler.loadCollectionFile("temp");
                            newHandler.setSaveFileInfo(sVersion, cVersion, name);
                            newHandler.deleteCollectionFile("temp");
                        }
                        */

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (output != null) {
                                output.close();
                            }
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException ignored) {
                        }
                    }
                }

                if (!filesToDownload.isEmpty()) {
                    for (String zipName : filesToDownload) {
                        //long size = 0;
                        Log.w("in UpdateHandler", "donwloading " + zipName);
                        try {
                            URL url = new URL(ROOT_URL + "/" + zipName + ".zip");
                            ZipInputStream is = new ZipInputStream(url.openStream());
                            ZipEntry file = is.getNextEntry();
                            String[] nameSplit = zipName.split("_");
                            String parent = nameSplit[0] + "_" + nameSplit[1];
                            String pngName;
                            //download every png in zip
                            while (file != null) {
                                pngName = file.getName().replace(".png", "");
                                output = new FileOutputStream(CACHE_DIR + "/" + parent + "/" + pngName);
                                byte data[] = new byte[4096];
                                int count;
                                while ((count = is.read(data)) != -1) {
                                    if (isCancelled()) {
                                        input.close();
                                        return null;
                                    }
                                    progress += count;
                                    publishProgress((int) (progress * 100 / total));
                                    output.write(data, 0, count);
                                }
                                is.closeEntry();
                                output.close();
                                //size += new File(CACHE_DIR + "/" + parent + "/" + pngName).length();
                                file = is.getNextEntry();
                            }
                            Log.w("in UpdateHandler", "downloaded " + zipName);
                            //PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(zipName + "_size", size).apply();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(zipName + "_downloaded", true).apply();
                        } catch (Exception e) {
                            Log.w("in UpdateHandler", "error while downloading " + zipName);
                            Log.w("in UpdateHandler", e.getMessage());
                        }
                    }
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                // if we get here, length is known, now set indeterminate to false
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Boolean b) {
                mProgressDialog.dismiss();
                if (b)
                    Toast.makeText(YWMApplication.getInstance(), "database and images updated", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(YWMApplication.getInstance(), "database not updated", Toast.LENGTH_SHORT).show();
                onDatabaseUpdateFinished(true);
            }

        };
        task.execute();


        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                task.cancel(true);
                try {
                    File oldFile = new File(DB_DIR + "/database_old.db");
                    File newOldFile = new File(DB_DIR + "/database.db");
                    oldFile.renameTo(newOldFile);
                } catch (Exception e) {
                }

                Toast.makeText(YWMApplication.getInstance(), "database not updated", Toast.LENGTH_SHORT).show();
                onDatabaseUpdateFinished(false);
            }
        });
    }

    public static class TaskCanceler implements Runnable {
        private AsyncTask task;

        public TaskCanceler(AsyncTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (task.getStatus() == AsyncTask.Status.RUNNING)
                task.cancel(true);
            Toast.makeText(YWMApplication.getInstance(), "connection too slow, try later", Toast.LENGTH_SHORT).show();
        }
    }

    public static void onDatabaseUpdateFinished(boolean refresh) {
        if (refresh) {
            MedalLibrary.initialise();
            Intent intent = new Intent();
            intent.setAction(MainActivity.DATABASE_UPDATE_FINISHED);
            YWMApplication.getInstance().sendBroadcast(intent);
        }
    }

    public static void deleteUnusedImages(String directory, Set<String> db_list, Set<String> dl_list) {
        Set<String> list = new HashSet<>(dl_list);
        list.removeAll(db_list);
        for (String name : list) {
            (new File(CACHE_DIR + directory + "/" + name)).delete();
            Log.w("in UpdateHandler", "deleting " + name + " in " + directory);
        }
    }

    public static boolean fetchFirstTime(final Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("first_time", true)) {

            final CharSequence[] titles = {"Medal images in HQ", "Product images in HQ", "Yokai images in HQ"};
            final String[] keys = {"medal_500_enabled", "product_500_enabled", "yokai_500_enabled"};
            final boolean[] checkedStates = new boolean[titles.length];

            new AlertDialog.Builder(context).setTitle("Welcome") //context.getString(R.string.first_time_title))
                    .setMultiChoiceItems(titles,
                            checkedStates,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int pos, boolean b) {
                                    checkedStates[pos] = b;
                                }
                            })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            for (int i = 0; i < titles.length; i++)
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(keys[i], checkedStates[i]).apply();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("first_time", false).apply();
                            fetchDataUpdate(context);
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            System.exit(0);
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }
}
