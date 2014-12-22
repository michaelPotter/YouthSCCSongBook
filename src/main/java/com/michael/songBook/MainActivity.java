package com.michael.songBook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity {
/* /
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
//*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //################################################################################Their Code
    TextView tv_loading;
    int downloadedSize, totalsize = 0;
    float per = 0;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_loading = (TextView) findViewById(R.id.tv_loading);

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!isAdobeInstalled()) {
                        makeToast("Adobe Reader is required to use this app. Please install");
                        sendUserToAdobeDownload();
                        return;
                    }

                    File folder = new File(Environment.getExternalStorageDirectory(),
                            getString(R.string.folder_name));

                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    file = new File(folder, getString(R.string.dest_file_path));

                    if (!file.exists()) {
                        makeToast("file does not exist, downloading file");
                        downloadFile(getString(R.string.download_file_url));
                        if (!isFileSizeCorrect(
                                file, new URL(getString(R.string.download_file_url)))) ;

                        file.delete();
                    }

                    if (isNetworkAvailable()) {
                        if (!isFileSizeCorrect(file, new URL(getString(R.string.download_file_url)))) {
                            downloadFile(getString(R.string.download_file_url));
                        }
                    }

                    makeToast("file exists, opening");
                    openPDF();

                } catch(MalformedURLException e) {

                }
            }

        }
    ).start();


}

    public void openPDF() {
        Uri path = Uri.fromFile(file);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setPackage("com.adobe.reader");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            tv_loading.setError(getString(R.string.reader_not_installed));
        }
    }


    public void downloadPDF() {
        new Thread(new Runnable() {
            public void run() {
                downloadFile(getString(R.string.download_file_url));
            }
        }).start();


    }

    public File downloadFile(String download_file_path) {
//        File file = null;
        try {
            URL url = new URL(download_file_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            // connect
            urlConnection.connect();

            // set the path where we want to save the file
//            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
//            file = new File(SDCardRoot, getString(R.string.dest_file_path));

            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are downloading
            totalsize = 3000000; //urlConnection.getContentLength();
            setText(getString(R.string.start_download));

            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                per = ((float) downloadedSize / totalsize) * 100;
                setText("TotalPDF File size : "
                        + (totalsize / 1024)
                        + " KB\n\nDownloading PDF " + (int) per
                        + "% complete");
            }
            // close the output stream when complete
            fileOutput.close();
            setText(getString(R.string.end_download));

        } catch (final MalformedURLException e) {
            setTextError(getString(R.string.some_error),
                    Color.RED);
        } catch (final IOException e) {
            setTextError(getString(R.string.some_error),
                    Color.RED);
        } catch (final Exception e) {
            setTextError(
                    getString(R.string.failed_download),
                    Color.RED);
            setTextError(e.toString(), Color.RED);
        }
        return file;
    }

    public boolean isFileSizeCorrect(File file, URL url) {
        int totalsize = 0;
        try {

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            totalsize = urlConnection.getContentLength();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            totalsize = -1;
        }
        return totalsize == file.length();

    }


    public int size() {
        int totalsize = -1;

        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(getString(R.string.download_file_url));
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();
                    totalsize = urlConnection.getContentLength();
                    makeToast("" + urlConnection.getContentLength());
                } catch (Exception e) {
//                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
        return totalsize;
    }

    public boolean isAdobeInstalled() {
        PackageManager manager = getPackageManager();
        System.out.println(manager.getInstalledPackages(PackageManager.GET_ACTIVITIES));
        for (PackageInfo p : manager.getInstalledPackages(PackageManager.GET_ACTIVITIES)) {
            if (p.packageName.equals("com.adobe.reader")) {
                return true;
            }
        }
        return false;
    }

    public void sendUserToAdobeDownload() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader"));
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    void setTextError(final String message, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                tv_loading.setTextColor(color);
                tv_loading.setText(message);
            }
        });
    }

    void setText(final String txt) {
        runOnUiThread(new Runnable() {
            public void run() {
                tv_loading.setText(txt);
            }
        });
    }

    public void makeToast(final String text) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //##########################################################################################

}
