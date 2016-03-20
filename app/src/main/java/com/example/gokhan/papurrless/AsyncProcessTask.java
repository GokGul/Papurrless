package com.example.gokhan.papurrless;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.abbyy.ocrsdk.*;

import java.io.FileOutputStream;

/**
 * Created by Cam Geronimo on 25-2-2016.
 */
public class AsyncProcessTask extends AsyncTask<String, String, Boolean> {

    private ProgressDialog dialog;
    private MainActivity activity;

    public AsyncProcessTask(MainActivity activity){
        this.activity = activity;
        dialog = new ProgressDialog(activity);
    }

    protected void onPreExecute(){
        dialog.setMessage("Processing");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String inputFile = params[0];
        String outputFile = params[1];

        try{
            Client restClient = new Client();

            restClient.applicationId = "no more papurr";
            restClient.password = "XypcE95nJQj7hg9WDeUSZOez";

            // Obtain installation id when running the application for the first time
            SharedPreferences settings = activity.getPreferences(Activity.MODE_PRIVATE);
            String instIdName = "installationId";

            if(!settings.contains(instIdName)){
                // Get installation id from server using device id
                String deviceId = android.provider.Settings.Secure.getString(activity.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);

                // Obtain installation id from server
                publishProgress( "First run: obtaining installation id..");
                String installationId = restClient.activateNewInstallation(deviceId);
                publishProgress( "Done. Installation id is '" + installationId + "'");

                SharedPreferences.Editor editor = settings.edit();
                editor.putString(instIdName, installationId);
                editor.commit();
            }

            String installationId = settings.getString(instIdName, "");
            restClient.applicationId += installationId;

            publishProgress( "Uploading image...");

            String language = "English"; // Comma-separated list: Japanese,English or German,French,Spanish etc.

            ProcessingSettings processingSettings = new ProcessingSettings();
            processingSettings.setOutputFormat( ProcessingSettings.OutputFormat.txt );
            processingSettings.setLanguage(language);

            publishProgress("Uploading..");
            Task task = restClient.processImage(inputFile, processingSettings);

            while( task.isTaskActive() ) {
                // Note: it's recommended that your application waits
                // at least 2 seconds before making the first getTaskStatus request
                // and also between such requests for the same task.
                // Making requests more often will not improve your application performance.
                // Note: if your application queues several files and waits for them
                // it's recommended that you use listFinishedTasks instead (which is described
                // at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).

                Thread.sleep(5000);
                publishProgress( "Waiting.." );
                task = restClient.getTaskStatus(task.Id);
            }
            if( task.Status == Task.TaskStatus.Completed ) {
                publishProgress( "Downloading.." );
                FileOutputStream fos = activity.openFileOutput(outputFile, Context.MODE_PRIVATE);

                try {
                    restClient.downloadResult(task, fos);
                } finally {
                    fos.close();
                }
                publishProgress( "Ready" );
            } else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
                throw new Exception( "Not enough credits to process task. Add more pages to your application's account." );
            } else {
                throw new Exception( "Task failed" );
            }
            return true;
        }
        catch(Exception e){
            final String message = "Error: " + e.getMessage();
            publishProgress(message);
            e.printStackTrace();
            return false;
        }
    }

    protected void onPostExecute(Boolean result){
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        activity.updateResults(result);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        // TODO Auto-generated method stub
        String stage = values[0];
        dialog.setMessage(stage);
        // dialog.setProgress(values[0]);
    }
}
