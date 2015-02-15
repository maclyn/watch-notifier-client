package com.klprjct.watchout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Boot Receiver", "Boot completed");
		//Start ScreenOffService
		context.startService(new Intent(context, ScreenOffService.class));
		context.startActivity(new Intent(context, MainActivity.class));
	}
}
