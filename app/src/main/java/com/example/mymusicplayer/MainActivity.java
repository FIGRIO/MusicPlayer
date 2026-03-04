package com.example.mymusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MusicService musicService;
    private boolean isBound = false;
    private MaterialButton btnPlayPause;
    private SeekBar seekBar;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            musicService = binder.getService();
            isBound = true;
            updatePlayPauseIcon();
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

        btnPrevious.setOnClickListener(v -> {
            // Logic cho bài trước đó
        });

        btnNext.setOnClickListener(v -> {
            // Logic cho bài kế tiếp
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
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}