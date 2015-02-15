package com.klprjct.watchout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class MainActivity extends FragmentActivity {
	public static final String TAG = "MainActivity";
	
	List<String> appPackages;
	
	SharedPreferences prefs;
	
	int SLIDE_PAGES = 3;
	
	ViewPager pager;
	PagerAdapter adapter;
	
	BroadcastReceiver br;
	
	int taps = 0;
	
	Timer t;
	TimerTask manageMedia;
	boolean tRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_viewpager);
		
		pager = (ViewPager) this.findViewById(R.id.mainPager);
		adapter = new SlideScreenPagerAdapter(this.getSupportFragmentManager());
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				log("Page selected: " + arg0);
			}
		});
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		appPackages = new ArrayList<String>();
		
		br = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				//Send intent to the watch activity and switch back to that
				pager.setCurrentItem(adapter.getCount() - 2, true);
				((SlideScreenPagerAdapter)adapter).sendIntentToHome(intent);
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(PhoneLink.NOTIFICATION_INTENT);
		intf.addAction(PhoneLink.NOTIFICATION_R_INTENT);
		intf.addAction(PhoneLink.ACTION_INTENT);
		intf.addAction(PhoneLink.MEDIA_CLEAR);
		intf.addAction(PhoneLink.MEDIA_UPDATE);
		intf.addAction(PhoneLink.DISCONNECT_INTENT);
		intf.addAction(PhoneLink.CONNECT_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);
		
		pager.setCurrentItem(adapter.getCount() - 2);
		
		t = new Timer();
	}

	@Override
	public void onBackPressed(){
		if(pager.getCurrentItem() == adapter.getCount() - 2){
		} else {
			pager.setCurrentItem(1);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		log("On resume");
	}
	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
	        	//Menu pressed -- pause music, but wait -- if there are two taps, go forward
	        	log("Menu: " + pager.getCurrentItem());
	        	if(pager.getCurrentItem() == 1){
		        	taps += 1;
		        	if(manageMedia != null){
		        		manageMedia.cancel();
		        	} 
	        		manageMedia = new TimerTask(){
	        			@Override
	        			public void run() {
	        				//Respond to timer tasks by sending intents to the phones
	        				Intent i = new Intent();
	        				i.setAction(PhoneLink.PLAYBACK_CONTROL);
	        				switch(taps){
	        				case 1:
	        					i.putExtra("data", "0");
	        					break;
	        				case 2:
	        					i.putExtra("data", "1");
	        					break;
	        				default:
	        					i.putExtra("data", "2");
	        					break;
	        				}
	        				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(i);
	        				taps = 0;
	        				manageMedia = null;
	        			}
	        		};
	        		t.schedule(manageMedia, 2000);
	        	} else if (pager.getCurrentItem() == 2){
	        		this.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	        	}
	            return true;
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	        	Intent ivd = new Intent();
				ivd.setAction(PhoneLink.PLAYBACK_CONTROL);
				ivd.putExtra("data", "vdn");
				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(ivd);
	        	return true;
	        case KeyEvent.KEYCODE_VOLUME_UP:
	        	Intent ivu = new Intent();
				ivu.setAction(PhoneLink.PLAYBACK_CONTROL);
				ivu.putExtra("data", "vup");
				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(ivu);
	        	return true;
	        case KeyEvent.KEYCODE_BACK:
	        	return true;
	    }

	    return super.onKeyDown(keycode, e);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		log("on pause");
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
	
	public void goHome(){
		pager.setCurrentItem(adapter.getCount() - 2);
	}
	
	private class SlideScreenPagerAdapter extends FragmentPagerAdapter {
		WatchScreenFragment watchScreen;
		AppLauncherFragment appLauncher;
		FocusFragment focusFragment;
		
		public SlideScreenPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if(position == getCount() - 1){ //last position --> app launcher
				log("Returning applauncher");
				if(appLauncher == null){
					appLauncher = new AppLauncherFragment();
				}
				return appLauncher;
			} else if (position == getCount() - 2){ //second to last position --> watch screen
				log("Returning watchscreen");
				if(watchScreen == null){
					watchScreen = new WatchScreenFragment();
				} 
				return watchScreen;
			} else if (position == getCount() - 3){ //third to last position --> focus screen
				log("Returing focus fragment");
				if(focusFragment == null){
					focusFragment = new FocusFragment();
				}
				return focusFragment;
			} else {
				return new WatchScreenFragment();
			}
		}
		
		@Override
		public int getCount() {
			return SLIDE_PAGES;
		}
		
		public void sendIntentToHome(Intent i){
			if(watchScreen == null){
				watchScreen = new WatchScreenFragment();
			}
			watchScreen.newReceive(i);
			
			if(focusFragment == null){
				focusFragment = new FocusFragment();
			}
			focusFragment.newReceive(i);
		}
	}
}
