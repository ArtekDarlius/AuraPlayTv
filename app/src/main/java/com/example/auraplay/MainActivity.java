package com.example.auraplay;


import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    ProgressDialog pDialog;

    private static String url = "https://ids.aurafutures.com/api/v1/campaigns/7934/manifest/4";

    VideoView videoView;
    MediaController mc;

    File[] ficheros;

    String  result = "";
    Toolbar toolbar;
    static int playPosition=0;

    private long id;
    ProgressDialog mDialog;
    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        CreateFolder();


        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        videoView= findViewById(R.id.videoV);
        mc= new MediaController(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode== KeyEvent.KEYCODE_BACK)){
            finishAffinity();
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Download:
                Toast.makeText(getApplicationContext(),"pulsado",Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setTitle("(FUTURE OPTION IS NOT WORKING NOW!!)")
                        .setMessage("Are you sure you want to download again the files?")


                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadJSON extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL myurl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) myurl.openConnection();
                InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);

                }
                result = builder.toString();

                Log.e("Json", builder.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String s) {

            try {
                JSONObject object= new JSONObject(result);
                JSONObject objectItemsAndReviews = object.getJSONObject("itemsAndReviewsExperience");
                JSONObject objectPages = objectItemsAndReviews.getJSONObject("pagesById");
                JSONObject object264 = objectPages.getJSONObject("264743");
                JSONArray arrayThings= object264.getJSONArray("things");
                JSONObject objectSlides = arrayThings.getJSONObject(0);
                JSONArray arraySlides= objectSlides.getJSONArray("slides");

                for (int i=0; i<arraySlides.length();i++){

                    JSONObject jsonObject = arraySlides.getJSONObject(i);
                    String path=jsonObject.getString("path");
                    //System.out.println(path);

                    if (path.toLowerCase().endsWith("png")){

                    }else{
                        System.out.println(path);
                        downloadfile(path);
                    }

                }
                if (pDialog.isShowing()) pDialog.dismiss();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (!prefs.getBoolean("firstTime", false)){
                    //showDialog();
                    showDialogGIF();
                }else{
                    createPlaylist();
                    playListVideo();
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstTime", true);
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadfile(String vidurl) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean("firstTime", false)) {

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(vidurl));
            String title = URLUtil.guessFileName(vidurl, null, null);

            if (title.toLowerCase().endsWith("png")){


            }else {
                request.setTitle(title);
                request.setDescription("Downloading File please wait...");
                String cookie = CookieManager.getInstance().getCookie(vidurl);
                request.addRequestHeader("cookie", cookie);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/AuraMedia/"+title);
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                id = downloadManager.enqueue(request);

            }

        } else{
        }
    }

   private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
           if (id==downloadId){
               Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_SHORT).show();
               alertDialog.dismiss();
               createPlaylist();
               playListVideo();
           }
       }
   };

    public void showDialogGIF(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this,R.layout.dialog_gif,null);
        ImageView gif= dialogView.findViewById(R.id.iv_gif);
        Glide.with(this).load(R.drawable.logo).into(gif);
        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void CreateFolder(){
        File ruta = new File(Environment.getExternalStorageDirectory(), "/Download/AuraMedia");

        if (!ruta.exists()){
            ruta.mkdirs();

        }else{

        }
    }

    public void createPlaylist() {
        File file = new File(Environment.getExternalStorageDirectory(), "/Download/AuraMedia");
        System.out.println(file);
        ficheros = file.listFiles();
        System.out.println(ficheros.length);

        for (int i = 0; i < ficheros.length; i++){
            File file1 = ficheros[i].getAbsoluteFile();
            if (file1.getName().toLowerCase().endsWith("png")){
                System.out.println(ficheros[i]);
                String name= ficheros[i].getName();
                System.out.println(name);
                System.out.println(file);
                new File(file,name).delete();
            }
        }
        ficheros=file.listFiles();
    }

    public void playListVideo (){

        if (ficheros != null) {

            if (ficheros.length > 0) {
                String path = ficheros[playPosition].getPath();

                Uri uri = Uri.parse(path);

                videoView.setVideoURI(uri);
                videoView.setMediaController(null);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        videoView.start();
                    }
                });

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if ((playPosition + 1) <= ficheros.length) {
                            playPosition++;
                            String path = ficheros[playPosition].getPath();
                            Uri uri = Uri.parse(path);
                            videoView.setVideoURI(uri);
                            videoView.setMediaController(null);

                            videoView.start();
                            if (playPosition == (ficheros.length - 1)) {
                                playPosition = 0;
                                playPosition--;
                            }
                        }
                    }
                });
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new DownloadJSON().execute();

                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){

        }
    }
}




