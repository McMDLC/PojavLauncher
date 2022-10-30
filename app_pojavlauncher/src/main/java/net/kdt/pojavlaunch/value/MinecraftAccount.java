package net.kdt.pojavlaunch.value;


import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import net.kdt.pojavlaunch.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.*;
import android.graphics.Bitmap;
import android.util.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MinecraftAccount
{
    public String accessToken = "0"; // access token
    public String clientToken = "0"; // clientID: refresh and invalidate
    public String profileId = "00000000-0000-0000-0000-000000000000"; // profile UUID, for obtaining skin
    public String username = "Steve";
    public String selectedVersion = "1.16.5";
    public boolean isMicrosoft = false;
    public boolean isElyBy = false;
    public String msaRefreshToken = "0";
    public String skinFaceBase64;
    public long expiresAt;

    public static final String usernameToUuidUrl = "https://api.mojang.com/users/profiles/minecraft";
    public static final String usernameToUuidUrlElyBy = "https://authserver.ely.by/api/users/profiles/minecraft";

    void updateSkinFace(String uuid) {
        try {
            if (!isElyBy) {
                File skinFile = File.createTempFile("skin", ".png", new File(Tools.DIR_DATA, "cache"));
                Tools.downloadFile("https://mc-heads.net/head/" + uuid + "/100", skinFile.getAbsolutePath());
                skinFaceBase64 = Base64.encodeToString(IOUtils.toByteArray(new FileInputStream(skinFile)), Base64.DEFAULT);
                Log.i("SkinLoader", "Update skin face success");
            }
        } catch (IOException e) {
            // Skin refresh limit, no internet connection, etc...
            // Simply ignore updating skin face
            Log.w("SkinLoader", "Could not update skin face", e);
        }
    }

    public void updateSkinFace() {
        updateSkinFace(profileId);
    }

    public String save(String outPath) throws IOException {
        Tools.write(outPath, Tools.GLOBAL_GSON.toJson(this));
        return username;
    }

    public String save() throws IOException {
        return save(Tools.DIR_ACCOUNT_NEW + "/" + username + ".json");
    }

    public static MinecraftAccount parse(String content) throws JsonSyntaxException {
        return Tools.GLOBAL_GSON.fromJson(content, MinecraftAccount.class);
    }

    public static MinecraftAccount load(String name) throws JsonSyntaxException {
        if(!accountExists(name)) return null;
        try {
            MinecraftAccount acc = parse(Tools.read(Tools.DIR_ACCOUNT_NEW + "/" + name + ".json"));
            if (acc.accessToken == null) {
                acc.accessToken = "0";
            }
            if (acc.clientToken == null) {
                acc.clientToken = "0";
            }
            if (acc.profileId == null) {
                acc.profileId = "00000000-0000-0000-0000-000000000000";
            }
            if (acc.username == null) {
                acc.username = "0";
            }
            if (acc.selectedVersion == null) {
                acc.selectedVersion = "1.16.5";
            }
            if (acc.msaRefreshToken == null) {
                acc.msaRefreshToken = "0";
            }
            if (acc.skinFaceBase64 == null) {
                // acc.updateSkinFace("MHF_Steve");
            }
            return acc;
        } catch(IOException e) {
            Log.e(MinecraftAccount.class.getName(), "Caught an exception while loading the profile",e);
            return null;
        }
    }

    public Bitmap getSkinFace(){
        if(skinFaceBase64 == null){
            return Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
        }
        byte[] faceIconBytes = Base64.decode(skinFaceBase64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(faceIconBytes, 0, faceIconBytes.length);
    }

    private static boolean accountExists(String username){
        return new File(Tools.DIR_ACCOUNT_NEW + "/" + username + ".json").exists();
    }

    public static void clearTempAccount() {
        File tempAccFile = new File(Tools.DIR_DATA, "cache/tempacc.json");
        tempAccFile.delete();
    }

    public static void saveTempAccount(MinecraftAccount acc) throws IOException {
        File tempAccFile = new File(Tools.DIR_DATA, "cache/tempacc.json");
        tempAccFile.delete();
        acc.save(tempAccFile.getAbsolutePath());
    }

    public static class GetID extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(args[0] + "/" + args[1]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    return new JSONObject(Tools.read(conn.getInputStream())).getString("id");
                }
                return "00000000-0000-0000-0000-000000000000";
            } catch (JSONException | IOException e) {
                return "00000000-0000-0000-0000-000000000000";
            }
        }

    }
}