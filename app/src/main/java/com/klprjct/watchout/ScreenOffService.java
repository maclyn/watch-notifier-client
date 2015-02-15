package com.klprjct.watchout;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class ScreenOffService extends Service {
	SharedPreferences reader;
	
	public ScreenOffService() {
	}
	
	@Override
	public void onCreate() {
		BroadcastReceiver br = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("ScreenOffService", "Go home on screen off enabled; going home");
				Intent i = new Intent(context, MainActivity.class);
			    context.startActivity(i);
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		this.registerReceiver(br, intentFilter);
		
		this.startForeground(500, new Notification());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int one, int two)
	{
		return Service.START_STICKY;
	}
}
