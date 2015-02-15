package com.klprjct.watchout;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class BluetoothSetup extends Activity {
	public static final int REQUEST_BT_ENABLE = 5000;
	public static final int REQUEST_BT_DISCOVER = 5001;
	public static final String TAG = "BluetoothSetup";
	
	SharedPreferences pref;
	
	Button setupBluetooth;
	Button startService;
	Button setWallpaper;
	Button setDiscoverable;
	BluetoothAdapter btAdapter;
	
	BluetoothDevice[] devices;
	
	boolean isStarted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_setup);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		setupBluetooth = (Button) this.findViewById(R.id.openForDiscovery);
		setupBluetooth.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Set<BluetoothDevice> devicesSet = btAdapter.getBondedDevices();
				if(devicesSet != null && devicesSet.size() > 0){
					devices = new BluetoothDevice[devicesSet.size()];
					devices = devicesSet.toArray(devices);
					createDeviceListener();
				} else {
					Toast.makeText(BluetoothSetup.this, "Please pair with a phone first", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		startService = (Button) this.findViewById(R.id.startService);
		startService.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent phoneLink = new Intent(BluetoothSetup.this, PhoneLink.class);
				if(!isStarted){
					BluetoothSetup.this.startService(phoneLink);
					startService.setText("Stop Service");
					isStarted = true;
				} else {
					BluetoothSetup.this.stopService(phoneLink);
					startService.setText("Start Service");
					isStarted = false;
				}
			}
		});
		
		setWallpaper = (Button) this.findViewById(R.id.setWallpaper);
		setWallpaper.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		        startActivity(Intent.createChooser(pickWallpaper, "Set Wallpaper"));
			}
		});
		
		setDiscoverable = (Button) this.findViewById(R.id.setDiscoverable);
		setDiscoverable.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				btAdapter.enable();
			}
		});
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter == null){
			//Does not support BT; disable button
			setupBluetooth.setText("No Bluetooth Support");
			setupBluetooth.setEnabled(false);
		} else {
			if(!btAdapter.isEnabled()){
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
			}
		}	
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_BT_ENABLE){
        	if(resultCode == Activity.RESULT_OK){
        		log("Bluetooth enabled");
        		//Good.
        	} else {
        		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        	}
        }
    }
	
	private void createDeviceListener(){
		String[] choices = new String[devices.length];
		for(int i = 0; i < choices.length; i++){
			choices[i] = devices[i].getName();
		}
		new AlertDialog.Builder(this)
			.setTitle("Choose your phone")
			.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					pref.edit().putString("pairedAddress", devices[which].getAddress()).commit();
					Toast.makeText(BluetoothSetup.this, "Device set", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
			}).create().show();
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
}
