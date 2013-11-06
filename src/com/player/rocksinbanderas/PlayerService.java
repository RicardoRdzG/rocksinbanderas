package com.player.rocksinbanderas;

import java.io.IOException;
import java.lang.ref.WeakReference;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class PlayerService extends Service implements OnCompletionListener, OnPreparedListener,
OnClickListener {
	public static MediaPlayer mp;
	private WeakReference<ImageButton> btnPlay;

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnPreparedListener(this);
		mp.reset();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);//

		super.onCreate();
	}

	@Override
	public void onClick(View v) {
		if (mp != null) 			
			if (mp.isPlaying()) {
				mp.pause();
				// Changing button image to play button
				btnPlay.get()
				.setImageResource(R.drawable.ic_media_play);
				Log.d("Player Service", "Pause");			
			} else {
				// Resume play
				mp.start();
				// Changing button image to pause button
				btnPlay.get().setImageResource(
						R.drawable.ic_media_pause);
				Log.d("Player Service", "Play");
			}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.start();
		// Changing Button Image to pause image
		btnPlay.get().setImageResource(R.drawable.ic_media_pause);
		//initNotification();
	}

	// --------------onStartCommand-------------------------------------------------------------------------//
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		btnPlay = new WeakReference<ImageButton>(MainActivity.btnPlay);
		if (mp.isPlaying()){
			btnPlay.get().setImageResource(R.drawable.ic_media_play);
			//btnPlay.get().setImageResource(R.drawable.ic_media_pause);
			mp.pause();
			initNotification(false);
		}
		else{
			
			
			play();
		}
		Log.d("Player Service","OnStartCommand");
		super.onStart(intent, startId);

		return START_STICKY;
	}

	public void play() {
		// Play song
		try {
			mp.reset();
			mp.setDataSource("http://sintonizarte.net/rocksb");
			mp.prepareAsync();


		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	// ---------------------------------------------------------//
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Player Service", "Player Service Stopped");
		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
			}
			mp.release();
		}

	}

	// --------------------Push Notification
	// Set up the notification ID
	public static final int NOTIFICATION_ID = 1;
	public static final String PLAYING = "playing";
	private NotificationManager mNotificationManager;

	// Create Notification
	private void initNotification(boolean playing) {
		int icon_id;
		String text;
		if(playing){
			icon_id=R.drawable.ic_media_play;
			text="rockeando!";
		}
		else{
			icon_id=R.drawable.ic_media_pause;
			text=" press to play";
		}
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_media_play)
		.setContentTitle("Rock sin banderas")
		.setContentText(text);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new 
				Intent(this, MainActivity.class);
		resultIntent.putExtra(PLAYING, playing);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
						);
		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		// Changing Button Image to pause image
		btnPlay.get().setImageResource(R.drawable.ic_media_pause);
		initNotification(true);
	}
}
