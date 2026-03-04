package com.example.mymusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MusicService musicService;
    private boolean isBound = false;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar;
    private Handler handler = new Handler();

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (isBound && musicService.isPlaying()) {
                int currentPosition = musicService.getCurrentPosition();
                seekBar.setProgress(currentPosition);
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

            seekBar.setMax(musicService.getDuration());
            updatePlayPauseIcon();
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
        MaterialButton btnPrevious = findViewById(R.id.btnPrevious);
        MaterialButton btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);

        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        btnPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                } else {
                    musicService.resumeMusic();
                }
                updatePlayPauseIcon();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updatePlayPauseIcon() {
        if (isBound && musicService.isPlaying()) {
            btnPlayPause.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause));
        } else {
            btnPlayPause.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_media_play));
        }
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