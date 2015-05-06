package com.michael.songBook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends Activity {
    TextView tv_loading;
    File file;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_loading = (TextView) findViewById(R.id.tv_loading);

        if (!isAdobeInstalled()) {
//            makeToast("Adobe Reader is required to use this app. Please install");
//            sendUserToAdobeDownload();
            showDialog();
//            finish(); // finish(); return; will kill dialog box if dialog is shown
//            return;
        } else {

            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {


                        File folder = new File(Environment.getExternalStorageDirectory(),
                                getString(R.string.folder_name));

                        if (!folder.exists()) {
                            folder.mkdir();
                        }

                        file = new File(folder, getString(R.string.dest_file_path));

                        if (!file.exists()) {
//                    makeToast("file does not exist, downloading file");
                            downloadFile(getString(R.string.download_file_url));
//                        if (!isFileSizeCorrect(
//                                file, new URL(getString(R.string.download_file_url)))) ;
//
//                        file.delete();
                        }
                        if (isNetworkAvailable()) {
                            if (!isFileSizeCorrect(file, new URL(getString(R.string.download_file_url)))) {
                                downloadFile(getString(R.string.download_file_url));
                            }
                        }

                        setText("Opening file");
                        openPDF();
                    } catch (MalformedURLException exception) {

                    }
                }
            }
            ).start();
        }
}

    /**
     * Opens the pdf in adobe reader
     */
    public void openPDF() {
        Uri path = Uri.fromFile(file);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setPackage("com.adobe.reader");
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            tv_loading.setError(getString(R.string.reader_not_installed));
        }
    }

    /**
     * Downloads the pdf on a new thread
     */
    public void downloadPDF() {
        new Thread(new Runnable() {
            public void run() {
                downloadFile(getString(R.string.download_file_url));
            }
        }).start();
    }

    /**
     * Downloads the file from the given url path
     * @param download_file_path
     * @return
     */
    public File downloadFile(String download_file_path) {
        try {
            int downloadedSize = 0;
            int totalsize = 0;
            float per = 0;

            URL url = new URL(download_file_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            // connect
            urlConnection.connect();

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
                setText(String.format("TotalPDF File size : %.2f MB\n\nDownloading PDF %d%s complete",
                        (totalsize / 1024 / 1024.0), (int) per, "%"));
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
        }
        return file;
    }

    /**
     * Returns whether the given file matches the intended file length
     * @param file
     * @param url
     * @return
     */
    public boolean isFileSizeCorrect(File file, URL url) {
        return size() == file.length();
    }


    /**
     * Returns the intended size of the pdf file
     * @return
     */
    public double size() {
        int totalsize = -1;

//        new Thread(new Runnable() {
//            public void run() {
        try {
            File sizeFile = new File(Environment.getExternalStorageDirectory(),
                    getString(R.string.folder_name) + "/" + getString(R.string.size_file));
            if (!sizeFile.exists()) {
                URL url = new URL(getString(R.string.size_file_url));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                FileOutputStream out = new FileOutputStream(sizeFile);
                InputStream in = urlConnection.getInputStream();
                byte[] buffer = new byte[1024 * 1024];
                int bufferLength = 0;

                while ((bufferLength = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bufferLength);
                }
                in.close();
                out.close();
            }

            Scanner scan = new Scanner(sizeFile);
            totalsize = Integer.parseInt(scan.next());
//            totalsize = scan.nextInt();

        } catch (MalformedURLException e) {
            makeToast(e.toString());
        } catch (IOException e) {
            makeToast(e.toString());
        }
//            }
//        }).start();
        return totalsize;
    }

    /**
     * Returns whether the adobe reader app is installed
     * @return
     */
    public boolean isAdobeInstalled() {
        PackageManager manager = getPackageManager();
        for (PackageInfo p : manager.getInstalledPackages(PackageManager.GET_ACTIVITIES)) {
            if (p.packageName.equals("com.adobe.reader")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends the user to the play store page for adobe reader
     */
    public void sendUserToAdobeDownload() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader"));
        i.setPackage("com.android.vending");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    /**
     * Sets the Textview text and color
     * @param message
     * @param color
     */
    void setTextError(final String message, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                tv_loading.setTextColor(color);
                tv_loading.setText(message);
            }
        });
    }

    /**
     * Sets the TextView text
     * @param txt
     */
    void setText(final String txt) {
        runOnUiThread(new Runnable() {
            public void run() {
                tv_loading.setText(txt);
            }
        });
    }

    /**
     * Creates a toast with the given text on the ui thread
     * @param text
     */
    public void makeToast(final String text) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks to see if a network is available
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showDialog() {

//        this.runOnUiThread(new Runnable() {
//            public void run() {
//                Looper.prepare();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.dialog_message))
                        .setTitle(getString(R.string.dialog_title));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        sendUserToAdobeDownload();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
//            }
//        });
    }

    private void savePDFToPhone() {
        File file = new File(Environment.getExternalStorageDirectory(),
                getString(R.string.folder_name) + "/" + "songbookTest.pdf");
        try {
            InputStream input = getAssets().open("SCC_YouthSongBook_DIGITAL.pdf");
            OutputStream output = new FileOutputStream(file);
            byte[] b = new byte[100];
            while (input.read() != -1) {
                output.write(b);
            }
            input.close();
            output.close();
        } catch (IOException e) {
            makeToast("error in saving pdf");
            e.printStackTrace();
        }
    }
}
