package com.ruby.midterm;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private VideoView mVideoView;
    private MediaController mMediaController;
    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/taeyeon.mp4";
    //    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/protraitVideo.mp4";
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private TextView mCurrentTime;
    private int mVideoLength;
    private int mCurrentPosition;
    private AudioManager mAudioManager;
    private boolean mIsPlaying;
    private int mStreamVolume;
    private View mPlaceHolder, mControllerHolder;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mStreamVolume = 30;
        mSeekBar = findViewById(R.id.customSeekBar);
        mTotalTime = findViewById(R.id.totalTimeTextView);
        mCurrentTime = findViewById(R.id.currentTimeTextView);
        mPlaceHolder = findViewById(R.id.allPlaceHolder);
        mControllerHolder = findViewById(R.id.controllerHolder);

        findViewById(R.id.playImageView).setOnClickListener(this);
        findViewById(R.id.pauseImageView).setOnClickListener(this);
        findViewById(R.id.forwardImageView).setOnClickListener(this);
        findViewById(R.id.rewindImageView).setOnClickListener(this);
        findViewById(R.id.muteImageView).setOnClickListener(this);
        findViewById(R.id.unmuteImageView).setOnClickListener(this);
        findViewById(R.id.fullscreenImageView).setOnClickListener(this);
        findViewById(R.id.exitFullscreenImageView).setOnClickListener(this);


        mVideoView = findViewById(R.id.videoViewPortrait);
        Uri videoUri = Uri.parse(mVideoUrl);
        mVideoView.setVideoURI(videoUri);

//        mMediaController = new CustomMediaController(this);
//        mVideoView.setMediaController(mMediaController);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoLength = mVideoView.getDuration();
                mSeekBar.setMax(mVideoLength);
                mTotalTime.setText(formatTime(mVideoLength));
                Log.d("DURATION!!!!!  ", "onCreate: " + mVideoLength);
                Log.d("RUN!!!!!  ", "onCreate: " + mVideoView.getCurrentPosition());

                new Thread() {
                    @Override
                    public void run() {
                        mIsPlaying = true;
                        while (mIsPlaying) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCurrentPosition = mVideoView.getCurrentPosition();
                                    mCurrentTime.setText(formatTime(mCurrentPosition));
                                    mSeekBar.setProgress(mCurrentPosition);
                                }
                            });

                            try {
                                sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }.start();
            }
        });
        mVideoView.start();

        mSeekBar.setOnSeekBarChangeListener(change);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mPlaceHolder.setOnTouchListener(null);
            setControllerVisibility(true);

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mPlaceHolder.setOnTouchListener(touch);
            setControllerVisibility(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playImageView:
                mVideoView.start();
                mIsPlaying = true;
                findViewById(R.id.playImageView).setVisibility(View.INVISIBLE);
                findViewById(R.id.pauseImageView).setVisibility(View.VISIBLE);
                break;
            case R.id.pauseImageView:
                mVideoView.pause();
                mIsPlaying = false;
                findViewById(R.id.playImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.pauseImageView).setVisibility(View.INVISIBLE);
                break;
            case R.id.forwardImageView:
                mVideoView.seekTo(mVideoView.getCurrentPosition() + 15000);
                break;
            case R.id.rewindImageView:
                mVideoView.seekTo(mVideoView.getCurrentPosition() - 15000);
                break;
            case R.id.muteImageView:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
                findViewById(R.id.unmuteImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.muteImageView).setVisibility(View.INVISIBLE);
                break;
            case R.id.unmuteImageView:
                mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                findViewById(R.id.muteImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.unmuteImageView).setVisibility(View.INVISIBLE);
                break;
            case R.id.fullscreenImageView:
                break;
            case R.id.exitFullscreenImageView:
                break;
        }

    }

    private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mCurrentTime.setText(formatTime(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVideoView.seekTo(seekBar.getProgress());
        }
    };


    private String formatTime(int mSec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mSec),
                TimeUnit.MILLISECONDS.toSeconds(mSec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mSec)));
    }

    private View.OnTouchListener touch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mTimer != null) {
                mTimer.cancel();
            }

            setControllerVisibility(true);
            boolean isNotTouch = true;
            mTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setControllerVisibility(false);
                        }
                    });
                }
            };

            mTimer.schedule(task, 3000);
            return true;
        }
    };

    private void setControllerVisibility(boolean visibility) {
        if (visibility) {
            mControllerHolder.setVisibility(View.VISIBLE);
        } else {
            mControllerHolder.setVisibility(View.GONE);
        }
    }

}
