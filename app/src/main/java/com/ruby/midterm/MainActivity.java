package com.ruby.midterm;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
    //    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/taeyeon.mp4";
    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/protraitVideo.mp4";
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private TextView mCurrentTime;
    private int mVideoLength;
    private int mCurrentPosition;
    private AudioManager mAudioManager;
    private boolean mIsPlaying;
    private int mStreamVolume;
    private View mPlaceHolder;
    private View mControllerHolder;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
//        mMediaController.setMediaPlayer(mVideoView);
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
                                    mSeekBar.setSecondaryProgress(mVideoView.getBufferPercentage() * (mVideoLength / 100));
                                    Log.d("TRY ", "run: " + mVideoView.getBufferPercentage());
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
                Log.d("HEIGHT!!!!!  ", "onCreate: " + mVideoView.getHeight());
                Log.d("WIDTH!!!!!  ", "onCreate: " + mVideoView.getWidth());
                if (mVideoView.getHeight() >= mVideoView.getWidth()) {
                    float factor = getResources().getDisplayMetrics().density;

                    mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(Math.round(200 * factor), ViewGroup.LayoutParams.WRAP_CONTENT));
                } else {
                    mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        });
        mVideoView.start();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mPlaceHolder.setOnTouchListener(touch);
            setControllerVisibility(false);
            mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mSeekBar.setOnSeekBarChangeListener(change);
//        mVideoView.setOnInfoListener(playStateListener);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mTimer != null) {
                mTimer.cancel();
            }
            if (mVideoView.getHeight() >= mVideoView.getWidth()) {
                float factor = getResources().getDisplayMetrics().density;

                mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(Math.round(200 * factor), ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {

                mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mPlaceHolder.setOnTouchListener(null);
            setControllerVisibility(true);

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mVideoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
                findViewById(R.id.playImageView).setVisibility(View.INVISIBLE);
                findViewById(R.id.pauseImageView).setVisibility(View.VISIBLE);
                break;
            case R.id.pauseImageView:
                mVideoView.pause();
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
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                findViewById(R.id.exitFullscreenImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.fullscreenImageView).setVisibility(View.INVISIBLE);
                break;
            case R.id.exitFullscreenImageView:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                findViewById(R.id.fullscreenImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.exitFullscreenImageView).setVisibility(View.INVISIBLE);
                if (mTimer != null) {
                    mTimer.cancel();
                }
                break;
            default:
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
            if (mTimer != null) {
                mTimer.cancel();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVideoView.seekTo(seekBar.getProgress());
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setTimer();
            }
        }
    };


    private String formatTime(int miliSecond) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(miliSecond),
                TimeUnit.MILLISECONDS.toSeconds(miliSecond)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(miliSecond)));
    }

    private View.OnTouchListener touch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mTimer != null) {
                mTimer.cancel();
            }

            setControllerVisibility(true);
            setTimer();

            return true;
        }
    };

    private void setTimer() {
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
    }

    private void setControllerVisibility(boolean visibility) {
        if (visibility) {
            mControllerHolder.setVisibility(View.VISIBLE);
        } else {
            mControllerHolder.setVisibility(View.GONE);
        }
    }

}
