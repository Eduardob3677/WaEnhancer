package com.wmods.wppenhacer;

import android.app.Activity;

import com.wmods.wppenhacer.xposed.core.WppCore;
import com.wmods.wppenhacer.xposed.core.components.AlertDialogWpp;
import com.wmods.wppenhacer.xposed.utils.Utils;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.OkHttpClient;

public class UpdateChecker implements Runnable {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Eduardob3677/WaEnhancer/releases/latest";

    private final Activity mActivity;

    public UpdateChecker(Activity activity) {
        this.mActivity = activity;
    }


    @Override
    public void run() {
        try {
            var client = new OkHttpClient();
            var request = new okhttp3.Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();
            var response = client.newCall(request).execute();
            var body = response.body();
            if (body == null) return;
            var content = body.string();
            var json = new JSONObject(content);
            var tagName = json.getString("tag_name");
            var releaseBody = json.optString("body", "No changelog available");
            var htmlUrl = json.getString("html_url");

            // Extract version identifier from tag name
            // Handles formats like: "release-92a9b375", "v1.0.0", "1.0.0", etc.
            var versionId = tagName
                    .replace("release-", "")
                    .replace("v", "")
                    .trim();

            var appInfo = mActivity.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
            if (!appInfo.versionName.toLowerCase().contains(versionId.toLowerCase().trim()) && !Objects.equals(WppCore.getPrivString("ignored_version", ""), versionId)) {
                mActivity.runOnUiThread(() -> {
                    var dialog = new AlertDialogWpp(mActivity);
                    dialog.setTitle("WAE - New version available!");
                    dialog.setMessage("Changelog:\n\n" + releaseBody);
                    dialog.setNegativeButton("Ignore", (dialog1, which) -> {
                        WppCore.setPrivString("ignored_version", versionId);
                        dialog1.dismiss();
                    });
                    dialog.setPositiveButton("Update", (dialog1, which) -> {
                        Utils.openLink(mActivity, htmlUrl);
                        dialog1.dismiss();
                    });
                    dialog.show();
                });
            }
        } catch (Exception ignored) {
        }
    }
}
