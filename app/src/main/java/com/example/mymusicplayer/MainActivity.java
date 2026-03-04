package com.example.mymusicplayer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MusicService musicService;
    private boolean isBound = false;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private ObjectAnimator rotateAnimation;
    private Handler handler = new Handler();

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (isBound && musicService.isPlaying()) {
                int currentPos = musicService.getCurrentPosition();
                seekBar.setProgress(currentPos);
                tvCurrentTime.setText(formatTime(currentPos));
            }
            handler.postDelayed(this, 1000);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            musicService = binder.getService();
            isBound = true;
            int duration = musicService.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
            updatePlayPauseStatus();
            handler.postDelayed(updateSeekBar, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        CardView cardAlbumArt = findViewById(R.id.cardAlbumArt);

        setupRotationAnimation(cardAlbumArt);

        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        btnPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (musicService.isPlaying()) musicService.pauseMusic();
                else musicService.resumeMusic();
                updatePlayPauseStatus();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupRotationAnimation(CardView view) {
        rotateAnimation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void updatePlayPauseStatus() {
        if (isBound && musicService.isPlaying()) {
            btnPlayPause.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_pause_bold));
            if (rotateAnimation.isPaused()) rotateAnimation.resume();
            else if (!rotateAnimation.isRunning()) rotateAnimation.start();
        } else {
            btnPlayPause.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow));
            rotateAnimation.pause();
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}