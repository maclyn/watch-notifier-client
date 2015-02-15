package com.klprjct.watchout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WatchScreenFragment extends Fragment {
	public static final float NOT_TRIGGERED = -2000f;
	public static final String TAG = "WatchScreenFragment";

	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	
	LayoutInflater li;

	View rootView;

	TextView time;
	TextView date;
	RelativeLayout mainLayout;
	
	LinearLayout settingsLayout;
	ImageView dismissButton;
	
	RelativeLayout notificationLayout;
	TextView notificationText;
	TextView notificationText2;
	ImageView notificationIcon;
	
	ImageView coverImage;
	LinearLayout mediaControls;
	TextView track;
	TextView artistAlbum;
	
	ImageView hideNL;
	ImageView dismissAll;
	LinearLayout nContainer;
	LinearLayout nView;
	
	ToggleButton wifi;
	ToggleButton bt;
	ToggleButton brightness;
	TextView batteryLevel;
	
	ImageView next;
	ImageView play;
	ImageView last;
	TextView appSettings;
	TextView watchSettings;
	
	RelativeLayout messageView;
	ImageView messageImg;
	TextView messageText;
	
	BluetoothAdapter bm;
	WifiManager wm;
	
	boolean expired = true;
	int taps = 0;
	
	BroadcastReceiver batteryValues;
	
	DisplayMetrics dm;

	List<NotificationContainer> allNotifications;

	Timer t;
	Timer t2;

	float startX = NOT_TRIGGERED;
	float startY = NOT_TRIGGERED;

	private class NotificationContainer {
		String mainText;
		String infoText;
		String packageName;
		String tag;
		String id;
		Bitmap bmp;

		public NotificationContainer(String mainText, String infoText,
				String packageName, String tag, String id, Bitmap bitmapToSet) {
			super();
			this.mainText = mainText;
			this.infoText = infoText;
			this.packageName = packageName;
			this.tag = tag;
			this.id = id;
			bmp = bitmapToSet;
		}

		public String getMainText() {
			return mainText;
		}

		public String getInfoText() {
			return infoText;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getTag() {
			return tag;
		}

		public String getId() {
			return id;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup mainView = (ViewGroup) inflater.inflate(
				R.layout.activity_main, container, false);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		dm = this.getResources().getDisplayMetrics();
		li = this.getLayoutInflater(savedInstanceState);
		
		bm = BluetoothAdapter.getDefaultAdapter();
		wm = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		batteryValues = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
					int val = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 50);
					batteryLevel.setText(val + "%");
				} else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)){
					showMessage("Battery is running low");
				}
			}
		};
		WatchScreenFragment.this.getActivity().registerReceiver(batteryValues, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		log("On pause");

		if (t != null) {
			t.cancel();
			t.purge();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		t = new Timer();
		t2 = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				WatchScreenFragment.this.updateTime();
			}
		}, 0, 500);

		log("on resume");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
		time = (TextView) view.findViewById(R.id.time);
		date = (TextView) view.findViewById(R.id.date);
		dismissButton = (ImageView) view.findViewById(R.id.dismissButton);
		settingsLayout = (LinearLayout) view.findViewById(R.id.settingsLayout);
		notificationLayout = (RelativeLayout) view.findViewById(R.id.notificationLayout);
		notificationText = (TextView) view.findViewById(R.id.text);
		notificationText2 = (TextView) view.findViewById(R.id.text2);
		hideNL = (ImageView) view.findViewById(R.id.hideNL);
		dismissAll = (ImageView) view.findViewById(R.id.dismissAll);
		nContainer = (LinearLayout) view.findViewById(R.id.nContainer);
		nView = (LinearLayout) view.findViewById(R.id.allNotifications);
		notificationIcon = (ImageView) view.findViewById(R.id.nIcon);
		coverImage = (ImageView) view.findViewById(R.id.mediaCover);
		mediaControls = (LinearLayout) view.findViewById(R.id.playbackLayout);
		track = (TextView) view.findViewById(R.id.trackName);
		artistAlbum = (TextView) view.findViewById(R.id.artistAlbum);
		wifi = (ToggleButton) view.findViewById(R.id.wifiButton);
		bt = (ToggleButton) view.findViewById(R.id.btButton);
		batteryLevel = (TextView) view.findViewById(R.id.batteryText);
		brightness = (ToggleButton) view.findViewById(R.id.brightButton);
		messageView = (RelativeLayout) view.findViewById(R.id.messageLayout);
		messageImg = (ImageView) view.findViewById(R.id.messageImage);
		messageText = (TextView) view.findViewById(R.id.messageText);
		next = (ImageView) view.findViewById(R.id.next);
		last = (ImageView) view.findViewById(R.id.last);
		play = (ImageView) view.findViewById(R.id.play);
		appSettings = (TextView) view.findViewById(R.id.appSettings);
		watchSettings = (TextView) view.findViewById(R.id.watchSettings);
		
		coverImage.setColorFilter(new LightingColorFilter( Color.rgb(170, 170, 170), 0));

		mainLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				float deltaY = 0;
				if (startX != NOT_TRIGGERED) {
					deltaY = startY - event.getRawY();
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					log("Action up");
					if (-deltaY > toPixel(50)) {
						settingsLayout.setVisibility(View.VISIBLE);				
						
						//Set WiFi functionality
						if(wm.isWifiEnabled()){
							wifi.setChecked(true);
						} else {
							wifi.setChecked(false);
						}
					
						//Set BT functionality
						if(bm.isEnabled()){
							bt.setChecked(true);
						} else {
							bt.setChecked(false);
						}
					} else if (deltaY > toPixel(50)){
						notificationLayout.setVisibility(View.GONE);
						nView.setVisibility(View.VISIBLE);
					} 
					break;
				case MotionEvent.ACTION_DOWN:
					startX = event.getRawX();
					startY = event.getRawY();
					log("Action down");
					break;
				case MotionEvent.ACTION_MOVE:
					log("Action move");
					break;
				}
				return true;
			}
		});
		
		mainLayout.setLongClickable(true);

		dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				settingsLayout.setVisibility(View.GONE);
			}
		});
		
		dismissAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeAllViews();
				allNotifications = new ArrayList<NotificationContainer>();
				
				checkStateAndHide();
			}
		});
		
		hideNL.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nView.setVisibility(View.GONE);
			}
		});
		
		play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(PhoneLink.PLAYBACK_CONTROL);
				i.putExtra("data", "1");
				LocalBroadcastManager.getInstance(WatchScreenFragment.this.getActivity()).sendBroadcast(i);
			}
		});
		
		next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(PhoneLink.PLAYBACK_CONTROL);
				i.putExtra("data", "2");
				LocalBroadcastManager.getInstance(WatchScreenFragment.this.getActivity()).sendBroadcast(i);
			}
		});
		
		last.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(PhoneLink.PLAYBACK_CONTROL);
				i.putExtra("data", "3");
				LocalBroadcastManager.getInstance(WatchScreenFragment.this.getActivity()).sendBroadcast(i);
			}
		});
		
		appSettings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				WatchScreenFragment.this.getActivity().startActivity(new Intent(WatchScreenFragment.this.getActivity(),
						BluetoothSetup.class));
				settingsLayout.setVisibility(View.GONE);
			}
		});
		
		watchSettings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				WatchScreenFragment.this.getActivity().startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
				settingsLayout.setVisibility(View.GONE);
			}
		});
		
		//Set battery level functionality
		batteryLevel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				WatchScreenFragment.this.getActivity().startActivity(new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS));
				settingsLayout.setVisibility(View.GONE);
			}
		});
		
		//Set brightness button functionality
		brightness.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(
					CompoundButton buttonView, boolean isChecked) {
				WindowManager.LayoutParams lp = WatchScreenFragment.this.getActivity().getWindow().getAttributes();
				if(isChecked){
					lp.screenBrightness = ((float)90) / 100F;
				} else {
					lp.screenBrightness = ((float)10) / 100F;
				}
				WatchScreenFragment.this.getActivity().getWindow().setAttributes(lp);
			}
		});
		
		wifi.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(
					CompoundButton buttonView, boolean isChecked) {
				if(wm != null)
				{
					if(isChecked)
					{
						wm.setWifiEnabled(true);
					}
					else
					{
						wm.setWifiEnabled(false);
					}
				}
			}		
		});
		
		bt.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(
					CompoundButton buttonView, boolean isChecked) {
				if(bm != null)
				{
					if(isChecked)
					{
						bm.enable();
					}
					else
					{
						bm.disable();
					}
				}
			}
		});
		
		timeFormat = new SimpleDateFormat("h:mm", Locale.US);
		dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		allNotifications = new ArrayList<NotificationContainer>();
	}
	
	public void checkStateAndHide(){
		if(nContainer.getChildCount() == 0){
			nView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(batteryValues != null){
			this.getActivity().unregisterReceiver(batteryValues);
		}
	}

	public void newReceive(Intent intent) {
		log("Recieved new intent");
		if(this.getView() != null){ //Check to make sure we don't do something stupid
			if (intent.getAction().equals(PhoneLink.NOTIFICATION_INTENT)) {
				log("Notification intent received");
				String data = intent.getStringExtra("data");
				log("Data: " + data);
				
				String[] allData = data.split("\\|");
				
				//See if it matches another existing notification, and if so remove it
				for(int i = 0; i < allNotifications.size(); i++){
					NotificationContainer nc = allNotifications.get(i);
					
					if(nc.getPackageName().equals(allData[2]) && nc.getId().equals(allData[4])){
						//Remove from both layout, homescreen, and db
						nContainer.removeViewAt(i);
						allNotifications.remove(nc);
						if(i == 0) notificationLayout.setVisibility(View.GONE);
					}
				}
				
				//Set on main watchface
				notificationLayout.setVisibility(View.VISIBLE);
				notificationText.setText(allData[0]);
				notificationText2.setText(allData[1]);
				
				Bitmap bitmapToSet = null;
				try {
					String imageText = allData[5];
					log("Image text: " + imageText);
					byte[] imageBytes = Base64.decode(allData[5], Base64.DEFAULT);
					bitmapToSet = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					log("Bitmap width: " + bitmapToSet.getWidth());
					log("Bitmap height: " + bitmapToSet.getHeight());
					notificationIcon.setImageBitmap(bitmapToSet);
					notificationIcon.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					notificationIcon.setVisibility(View.GONE);
				}
				
				//Set in notification container below
				NotificationContainer nc = new NotificationContainer(allData[0], allData[1], allData[2], allData[3], allData[4], bitmapToSet);
				allNotifications.add(nc);
				addNotification(nc);
			} else if (intent.getAction().equals(PhoneLink.NOTIFICATION_R_INTENT)) {
				//Removal intent
				log("Notification removal intent received");
				String data = intent.getStringExtra("data");
				log("Data: " + data);
				
				String[] allData = data.split("\\|");
				String packageName = allData[0];
				String tagName = allData[1];
				String idName = allData[2];
				
				for(int i = 0; i < allNotifications.size(); i++){
					NotificationContainer nc = allNotifications.get(i);
					
					if(nc.getPackageName().equals(packageName) && nc.getId().equals(idName)){
						//Remove from both layout, homescreen, and db
						nContainer.removeViewAt(i);
						allNotifications.remove(nc);
						if(i == 0) notificationLayout.setVisibility(View.GONE);
					}
				}
			} else if (intent.getAction().equals(PhoneLink.MEDIA_UPDATE)){
				log("Media update intent received");
				
				String data = intent.getStringExtra("data");
				mediaControls.setVisibility(View.VISIBLE);
				coverImage.setVisibility(View.VISIBLE);
				
				String[] allData = data.split("\\|");
				track.setText(allData[0]);
				artistAlbum.setText(allData[1] + " - " + allData[2]);
				track.setVisibility(View.VISIBLE);
				artistAlbum.setVisibility(View.VISIBLE);
				
				Bitmap bitmapToSet = null;
				try {
					byte[] imageBytes = Base64.decode(allData[3], Base64.DEFAULT);
					bitmapToSet = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					log("Bitmap width: " + bitmapToSet.getWidth());
					log("Bitmap height: " + bitmapToSet.getHeight());
					coverImage.setImageBitmap(bitmapToSet);
					coverImage.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					coverImage.setVisibility(View.GONE);
				}
			} else if (intent.getAction().equals(PhoneLink.MEDIA_CLEAR)){
				log("Media clear intent received");
				
				mediaControls.setVisibility(View.GONE);
				coverImage.setImageBitmap(null);
				coverImage.setVisibility(View.GONE);
			} else if (intent.getAction().equals(PhoneLink.DISCONNECT_INTENT)){
				showMessage("Disconnected from phone");
			} else if (intent.getAction().equals(PhoneLink.CONNECT_INTENT)){
				showMessage("Connected to phone");
			}
		}
	}
	
	void addNotification(final NotificationContainer nc){
		View nnc = li.inflate(R.layout.notification_layout, null);
		((TextView)nnc.findViewById(R.id.mainText)).setText(nc.getMainText());
		if(nc.getInfoText() != null && !nc.getInfoText().equals("null")){
			((TextView)nnc.findViewById(R.id.infoText)).setText(nc.getInfoText());
		} else {
			((TextView)nnc.findViewById(R.id.infoText)).setVisibility(View.GONE);
		}
		((TextView)nnc.findViewById(R.id.timeText)).setText(timeFormat.format(GregorianCalendar.getInstance().getTime()));
		
		nnc.findViewById(R.id.dismissNotification).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeView((View)((View) v.getParent()).getParent());
				allNotifications.remove(nc);
				
				//Send a dismiss intent
				Intent i = new Intent();
				i.setAction(PhoneLink.DISMISS_INTENT);
				i.putExtra("data", nc.getPackageName() + "|" + nc.getTag() + "|" + nc.getId());
				LocalBroadcastManager.getInstance(
						WatchScreenFragment.this.getActivity())
						.sendBroadcast(i);
				
				checkStateAndHide();
			}
		});
		
		nnc.findViewById(R.id.hideNotification).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeView((View)((View) v.getParent()).getParent());
				allNotifications.remove(nc);
				
				checkStateAndHide();
			}
		});
		
		if(nc.bmp != null){
			((ImageView)nnc.findViewById(R.id.bigNIcon)).setImageBitmap(nc.bmp);
		}
		
		nContainer.addView(nnc);
	}

	void updateTime() {
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Calendar c = GregorianCalendar.getInstance();
				time.setText(timeFormat.format(c.getTime()));
				date.setText(dateFormat.format(c.getTime()));
			}
		});
	}
	
	void showMessage(String text){
		messageText.setText(text);
		messageView.setVisibility(View.VISIBLE);
		
		messageView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				messageView.setVisibility(View.GONE);
			}
		});
	}

	private int toPixel(int pixels) {
		return (int) Math.ceil(pixels * dm.density);
	}

	private void log(String text) {
		Log.d(TAG, text);
	}
}
