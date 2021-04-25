package com.arsylk.dcwallpaper.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.arsylk.dcwallpaper.Async.AsyncLoadAssets;
import com.arsylk.dcwallpaper.DestinyChild.*;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;

import static com.arsylk.dcwallpaper.utils.Define.*;

public class LoadAssets  {
    private static final int TAG_ASSETS = 732;

    public static void guiFullLoad(Context context, final Utils.Callback callback) {
        new AsyncLoadAssets(context, true) {
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(callback != null) callback.onCall();
            }
        }.execute();
    }

    public static Future updateChildSkills(Context context) {
        return Ion.with(context).load(String.format(REMOTE_ASSET_CHILD_SKILLS, Utils.md5(ASSET_CHILD_SKILLS))).group(TAG_ASSETS)
                .asString(Charset.forName("utf-8")).setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if(e == null) {
                            if(result.isEmpty()) {
                                Log.d("mTag:Assets", "Child skills are up-to-date!");
                                return;
                            }
                            try {
                                FileUtils.write(ASSET_CHILD_SKILLS, result, Charset.forName("utf-8"));
                                Log.d("mTag:Assets", "Child skills updated!");
                            }catch(Exception e1) {
                                e1.printStackTrace();
                            }
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void updateChildNames(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File locale = DCTools.getDCLocalePath();
        if(!prefs.getBoolean("update_child_names", true) && ASSET_EXTRACTED_CHILD_NAMES.exists()) {
            Log.d("mTag:Assets", "Child names are up-to-date!");
        }else {
            try {
                DCTools.extractChildNames(locale, context);
                prefs.edit()
                        .putString("locale_md5", prefs.getString("locale_md5", Utils.md5(locale)))
                        .putBoolean("update_child_names", false)
                        .apply();
                Log.d("mTag:Assets", "Child names updated!");
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEnglishPatch(Context context, final FutureCallback<DCLocalePatch> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Updating english patch...");
        progressDialog.show();
        Ion.with(context).load(String.format(REMOTE_ASSET_ENGLISH_PATCH, Utils.md5(ASSET_ENGLISH_PATCH))).group(TAG_ASSETS)
                .asString(Charset.forName("utf-8")).setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if(e == null) {
                    DCLocalePatch dcLocalePatch = null;
                    if(result.isEmpty()) {
                        Log.d("mTag:Assets", "English patch is up-to-date!");
                        dcLocalePatch = new DCLocalePatch(Utils.fileToJson(Define.ASSET_ENGLISH_PATCH));
                    }else {
                        try {
                            Log.d("mTag:Assets", "English patch updated!");
                            FileUtils.write(ASSET_ENGLISH_PATCH, result, Charset.forName("utf-8"));
                            dcLocalePatch = new DCLocalePatch(new JSONObject(result));
                        }catch(Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    //finish update
                    progressDialog.dismiss();
                    callback.onCompleted(null, dcLocalePatch);
                }else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void updateRussianPatch(Context context, final FutureCallback<DCLocalePatch> callback) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Updating russian patch...");
        progressDialog.show();
        Ion.with(context).load(String.format(REMOTE_ASSET_RUSSIAN_PATCH, Utils.md5(ASSET_RUSSIAN_PATCH))).group(TAG_ASSETS)
                .asString(Charset.forName("utf-8")).setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if(e == null) {
                    DCLocalePatch dcLocalePatch = null;
                    if(result.isEmpty()) {
                        Log.d("mTag:Assets", "Russian patch is up-to-date!");
                        dcLocalePatch = new DCLocalePatch(Utils.fileToJson(Define.ASSET_RUSSIAN_PATCH));
                    }else {
                        try {
                            Log.d("mTag:Assets", "Russian patch updated!");
                            FileUtils.write(ASSET_RUSSIAN_PATCH, result, Charset.forName("utf-8"));
                            dcLocalePatch = new DCLocalePatch(new JSONObject(result));
                        }catch(Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    //finish update
                    progressDialog.dismiss();
                    callback.onCompleted(null, dcLocalePatch);
                }else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static boolean updateInProgress(Context context) {
        return Ion.getDefault(context).getPendingRequestCount(TAG_ASSETS) != 0;
    }

    //singleton instances
    //wiki
    private static DCWiki wiki = null;
    public static DCWiki getDCWikiInstance() {
        if(wiki == null) {
            wiki = new DCWiki();
        }
        return wiki;
    }

    //info
    private static DCModelInfo info = null;
    public static DCModelInfo getDCModelInfoInstance() {
        if(info == null) {
            info = new DCModelInfo();
        }
        return info;
    }
}
