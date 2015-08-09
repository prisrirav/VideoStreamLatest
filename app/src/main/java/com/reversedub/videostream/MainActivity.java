package com.reversedub.videostream;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private MediaController mediaController;
    private boolean audioPlayToggle = false;
    private VideoViewWrapper videoViewWrapper = null;
    private MediaRecorderWrapper mediaRecorderWrapper = null;
    Button videoButton = null;
    Intent mServiceIntent;
    Context context;
    String mFileName = null;
    private final static String videoFile = "reversedub/video.mp4";
    private final static String audioOutFile = "reversedub/audioout.m4a";
    private final static String videoOutfile = "reversedub/videoMerged.mp4";
    String externalDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

    private enum VideoButtonText{
        Record,
        Cancel,
        Merge
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoView vidView = (VideoView)findViewById(R.id.myVideo);
        videoViewWrapper = new VideoViewWrapper(vidView, getFullPath(videoFile));

        if(!audioPlayToggle) {
            //videoViewWrapper.FirstRender();
        }

        context = this;
        mFileName = getFullPath(audioOutFile);
        DeleteFileIfExists(mFileName);
        DeleteFileIfExists(getFullPath(videoOutfile));

        mediaRecorderWrapper = new MediaRecorderWrapper(audioOutFile);

        videoButton = (Button) findViewById(R.id.controlBtn);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = videoButton.getText().toString();

                if (VideoButtonText.Record.toString().equalsIgnoreCase(buttonText)) {
                    mediaRecorderWrapper.onRecord(true);
                    videoViewWrapper.onPlay(true);
                    videoButton.setText(VideoButtonText.Cancel.toString());
                } else if (VideoButtonText.Cancel.toString().equalsIgnoreCase(buttonText)){
                    mediaRecorderWrapper.onRecord(false);
                    videoViewWrapper.onPlay(false);
                    videoButton.setText(VideoButtonText.Record.toString());
                    mediaRecorderWrapper.onPlay(true);
                    DeleteFileIfExists(mFileName);
                }
                else if (VideoButtonText.Merge.toString().equalsIgnoreCase(buttonText))
                {
                    Intent mergeActivityIntent = new Intent(MainActivity.this, MergedVideoPlayActivity.class);

                    mergeActivityIntent.putExtra(MergedVideoPlayActivity.MERGED_FILE_KEYNAME, getFullPath(videoOutfile));
                    startActivity(mergeActivityIntent);
                    finish();
                }
            }
        });

        vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaRecorderWrapper.onRecord(false);
                    videoButton.setText(VideoButtonText.Merge.toString());
                    Boolean result = AudioVideoMuxer.CombineFilesUsingMp4Parser(getFullPath(videoFile), getFullPath(audioOutFile), getFullPath(videoOutfile));
                    if (result) {
                        videoButton.setText(VideoButtonText.Merge.toString());
                    } else {
                        videoButton.setText("Operation failed.");
                    }
                }
            }
        );
    }

    private String getFullPath(String fileName) {
        return externalDirectory + "/" + fileName;
    }

    private void DeleteFileIfExists(String mFileName) {
        if (mFileName != null) {
            File file = new File(mFileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

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
    public void onPause() {
        //mediaRecorderWrapper.onPause();

        // enable the following after mux integration is done.
        //DeleteFileIfExists(mFileName);
    }
}
