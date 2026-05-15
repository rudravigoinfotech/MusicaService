package com.app.musicapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer()

        try {
            // Malang Sajna Direct MP3 Link
            val url = "https://pagalnew.com/128-download/1741"
            // Note: Agar upar wala link expire ho jaye toh ye use karna:
            // val url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                it.isLooping = true
                it.start()
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                android.util.Log.e("MusicService", "Error playing: $what")
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Notification setup
        startForegroundServiceWithNotification()

        // Note: mediaPlayer.start() yahan se hata diya hai
        // kyunki online music load hone mein time leta hai,
        // wo onPreparedListener ke andar handle ho raha hai.

        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundServiceWithNotification() {
        val channelId = "music_channel"
        val channelName = "Music Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Playing: Malang Sajna")
            .setContentText("Enjoy your music")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}