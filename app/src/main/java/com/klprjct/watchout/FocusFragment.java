package com.klprjct.watchout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FocusFragment extends Fragment {
	public static final String TAG = "FocusFragment";
	
	LayoutInflater li;

	View rootView;
	DisplayMetrics dm;
	
	RelativeLayout noNotifications;
	
	LinearLayout actionList;
	ImageView icon;
	
	TextView appName;
	TextView line1;
	TextView line2;

	String currPackage = "";
	String currTag = "";
	String currId = ""; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup mainView = (ViewGroup) inflater.inflate(
				R.layout.fragment_focus, container, false);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		dm = this.getResources().getDisplayMetrics();
		li = this.getLayoutInflater(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();

		log("On pause");
	}

	@Override
	public void onResume() {
		super.onResume();

		log("on resume");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		actionList = (LinearLayout) getView().findViewById(R.id.actionContainer);
		appName = (TextView) getView().findViewById(R.id.appName);
		icon = (ImageView) getView().findViewById(R.id.appIco);
		line1 = (TextView) getView().findViewById(R.id.appMsgLine1);
		line2 = (TextView) getView().findViewById(R.id.appMsgLine2);
		noNotifications = (RelativeLayout) getView().findViewById(R.id.noNotfications);
		
		reset();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void reset(){
		noNotifications.setVisibility(View.VISIBLE);
		
		appName.setText("");
		line1.setText("");
		line2.setText("");
		icon.setVisibility(View.GONE);
		actionList.removeAllViews();
	}
	
	public void newReceive(Intent intent) {
		log("Recieved new intent");
		if(this.getView() != null){ //Check to make sure we don't do something stupid
			if (intent.getAction().equals(PhoneLink.NOTIFICATION_INTENT)) {
				log("Notification intent received");
				String data = intent.getStringExtra("data");
				log("Data: " + data);
				
				String[] allData = data.split("\\|");
				
				noNotifications.setVisibility(View.GONE);
				
				//Set on main lines
				icon.setVisibility(View.VISIBLE);
				line1.setText(allData[0]);
				line2.setText(allData[1]);
				appName.setText(allData[6]);
				
				final String packageName = allData[2];
				final String tagName = allData[3];
				final String id = allData[4];
				
				currPackage = allData[2];
				currTag = allData[3];
				currId = allData[4];
				
				Bitmap bitmapToSet = null;
				try {
					byte[] imageBytes = Base64.decode(allData[5], Base64.DEFAULT);
					bitmapToSet = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					icon.setImageBitmap(bitmapToSet);
					icon.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					icon.setVisibility(View.GONE);
				}
				
				//Set action views
				actionList.removeAllViews();
				int numActions = Integer.parseInt(allData[7]);
				if(numActions > 0){
					for(int i = 0; i < numActions; i++){
						final int which = i;
						
						LinearLayout actionButton = (LinearLayout) li.inflate(R.layout.action_button, null);
						((TextView)actionButton.findViewById(R.id.actionText)).setText(allData[8 + 2*i]);
						ImageView actionIco = ((ImageView)actionButton.findViewById(R.id.actionIcon));
						
						Bitmap actionToSet = null;
						try {
							byte[] actionBytes = Base64.decode(allData[8 + 1 + 2*i], Base64.DEFAULT);
							actionToSet = BitmapFactory.decodeByteArray(actionBytes, 0, actionBytes.length);
							actionIco.setImageBitmap(actionToSet);
							actionIco.setVisibility(View.VISIBLE);
						} catch (Exception e) {
							actionIco.setVisibility(View.GONE);
						}
						
						actionButton.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								//Send a dismiss intent
								Intent i = new Intent();
								i.setAction(PhoneLink.ACTION_INTENT);
								i.putExtra("data", packageName + "|" + id + "|" + which);
								LocalBroadcastManager.getInstance(FocusFragment.this.getActivity()).sendBroadcast(i);
							}
						});
						 
						actionList.addView(actionButton);
					}
				}
				
				//Add a dismiss button
				LinearLayout dismissButton = (LinearLayout) li.inflate(R.layout.action_button, null);
				((TextView)dismissButton.findViewById(R.id.actionText)).setText("Dismiss");
				((ImageView)dismissButton.findViewById(R.id.actionIcon)).setImageResource(R.drawable.dismiss_button);
				
				dismissButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//Send a dismiss intent
						Intent i = new Intent();
						i.setAction(PhoneLink.DISMISS_INTENT);
						i.putExtra("data", packageName + "|" + tagName + "|" + id);
						LocalBroadcastManager.getInstance(FocusFragment.this.getActivity()).sendBroadcast(i);
						reset();
					}
				});
				
				actionList.addView(dismissButton);
			} else if (intent.getAction().equals(PhoneLink.NOTIFICATION_R_INTENT)) {
				//Removal intent
				log("Notification removal intent received");
				String data = intent.getStringExtra("data");
				log("Data: " + data);
				
				String[] allData = data.split("\\|");
				String packageName = allData[0];
				String tagName = allData[1];
				String idName = allData[2];
				
				if(packageName.equals(currPackage) && tagName.equals(currTag) && idName.equals(currId)){
					reset();
				}
			}
		}
	}
	
	private int toPixel(int pixels) {
		return (int) Math.ceil(pixels * dm.density);
	}

	private void log(String text) {
		Log.d(TAG, text);
	}
}
