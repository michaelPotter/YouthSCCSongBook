package com.example.michael.pdftest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.HttpURLConnection;


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

    /*
     * Steps to build an intent:
     * 1. Build the Intent
     * 2. Verify it resolves
     * 3. Start the activity if it's safe
     */
//    public void buttonPress(View view) {
//        // download a pdf
//        // Need to use a service? // Nope. Don't think so
//
//        // Verify it resolves
//        // open it
//    }
/* */// Erase the second star to use copied code
    //################################################################################Their Code
    TextView tv_loading;
    String dest_file_path = "test.pdf";
    int downloadedSize, totalsize = 0;
    String download_file_url = "http://youthscc.com/SCC_YouthSongBook_June_2013_DIGITAL.pdf";

    float per = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_loading = new TextView(this);
        setContentView(tv_loading);
//        setContentView(R.layout.activity_main);
        tv_loading.setGravity(Gravity.CENTER);
        tv_loading.setTypeface(null, Typeface.BOLD);
        downloadAndOpenPDF();
    }


    public void downloadAndOpenPDF() {
//        Uri path = Uri.fromFile(downloadFile(download_file_url));
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(path, "application/pdf");
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
////            finish();
//        } catch (ActivityNotFoundException e) {
//            tv_loading.setError("PDF Reader application is not installed");
//        }


        new Thread(new Runnable() {
            public void run() {
                Uri path = Uri.fromFile(downloadFile(download_file_url));
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setDataAndType(path, "application/pdf");
                    intent.setType("application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (ActivityNotFoundException e) {
                    tv_loading.setError("PDF Reader application is not installed");
                }
            }
        }).start();


    }

//    public File downloadFile(String downloadFileURL) {
//        File file = new File("file.pdf");
//        try {
//            URL url = new URL(downloadFileURL);
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            PrintWriter writer = new PrintWriter(file);
//
//            String inputLine = "";
//            while ((inputLine = in.readLine()) != null) {
//                writer.write(inputLine + System.getProperty("line.seperator"));
//                System.out.println(inputLine);
//            }
//            writer.close();
//            in.close();
//        } catch (MalformedURLException e) {
//            System.out.println(e);
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//        return file;
//    }

    public File downloadFile(String download_file_path) {
        File file = null;
        try {
            URL url = new URL(download_file_path);
            URLConnection urlConnection = url.openConnection();
//            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            // connect
            urlConnection.connect();

            // set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, dest_file_path);

            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are downloading
            totalsize = urlConnection.getContentLength();
            setText("Starting PDF download...");

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
            setText("Download Complete. Open PDF Application installed in the device");

        } catch (final MalformedURLException e) {
            setTextError("Some error occured. Press back and try again.",
                    Color.RED);
        } catch (final IOException e) {
            setTextError("Some error occured. Press back and try again.",
                    Color.RED);
        } catch (final Exception e) {
            setTextError(
                    "Failed to download image. Please check your internet connection.",
                    Color.RED);
        }
        return file;
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
    //##########################################################################################

//*/

}
