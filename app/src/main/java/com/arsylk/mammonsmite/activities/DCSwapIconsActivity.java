package com.arsylk.mammonsmite.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.arsylk.mammonsmite.Async.AsyncWithDialog;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.DestinyChild.Pck;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import com.arsylk.mammonsmite.views.PickDirectoryDialog;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DCSwapIconsActivity extends AppCompatActivity  {
    static final String TAG = "Activity/DCSwapIcons";
    private Context context = DCSwapIconsActivity.this;
    private EditText defaultIconsView, customIconsView;
    private TextView iconsLog;
    private File defaultIconsLocation = null, customIconsLocation = null;
    private final static File workspace = new File(Define.BASE_DIRECTORY, "workspace");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcswap_icons);

        //TODO DEBUG
        defaultIconsLocation = new File(Define.BASE_DIRECTORY, "global_default_icon/portrait");
        customIconsLocation = new File(Define.BASE_DIRECTORY, "global_custom_icon/portrait");
        initViews();
    }

    private void initViews() {
        defaultIconsView = findViewById(R.id.dcicons_default_location);
        customIconsView = findViewById(R.id.dcicons_custom_location);
        iconsLog = findViewById(R.id.dcicons_log);

        defaultIconsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickDirectoryDialog(context, defaultIconsLocation).setCallback(new Utils.OnPostExecute<File>() {
                    @Override
                    public void onPostExecute(File file) {
                        if(file != null) {
                            if(file.isDirectory()) {
                                defaultIconsLocation = file;
                                defaultIconsView.setText(file.getAbsolutePath());
                            }
                        }
                    }
                }).show();
            }
        });
        customIconsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickDirectoryDialog(context, customIconsLocation).setCallback(new Utils.OnPostExecute<File>() {
                    @Override
                    public void onPostExecute(File file) {
                        if(file != null) {
                            if(file.isDirectory()) {
                                customIconsLocation = file;
                                customIconsView.setText(file.getAbsolutePath());
                            }
                        }
                    }
                }).show();
            }
        });


        findViewById(R.id.dcicons_swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(defaultIconsLocation != null && customIconsLocation != null) {
                    List<File> matchedIconFiles = Arrays.asList(defaultIconsLocation.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getName().endsWith(".pck")) {
                                return new File(customIconsLocation, pathname.getName()).exists();
                            }

                            return false;
                        }
                    }));
                    Collections.sort(matchedIconFiles);
                    iconsLog.append(matchedIconFiles.toString());

                    for(File defaultFile : matchedIconFiles) {
                        unpackAndSwitch(context, defaultFile, new File(customIconsLocation, defaultFile.getName()));
                    }
                }
            }
        });
    }

    private static void unpackAndSwitch(Context context, File defaultFile, File customFile) {
        new AsyncWithDialog<File, String, File>(context, true) {
            @Override
            protected File doInBackground(File... files) {
                try {
                    Pck defaultPck = DCTools.unpack(files[0], new File(workspace, "default_"+defaultFile.getName().replace(".pck", "")), 1 , null);
                    Pck customPck = DCTools.unpack(files[1], new File(workspace, "custom_"+customFile.getName().replace(".pck", "")), 1, null);

                    if(defaultPck == null || customPck == null)
                        return null;

                    for(Pck.PckFile defaultPckFile : defaultPck.getFiles()) {
                        Pck.PckFile testForCustom = customPck.getFile(defaultPckFile.getHash());
                        if(testForCustom != null) {
                            FileUtils.copyFile(testForCustom.getFile(), defaultPckFile.getFile());
                        }
                    }

                    //clear output directory
                    File generatedDir = new File(workspace, "generated");
                    File generated = new File(generatedDir, defaultPck.getSrc().getName());
                    if(!generatedDir.exists()) {
                        generatedDir.mkdirs();
                    }
                    if(generated.exists()) {
                        generated.delete();
                    }
                    DCTools.pack(defaultPck.getOutput(), generated);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }
        }.execute(defaultFile, customFile);
    }
}
