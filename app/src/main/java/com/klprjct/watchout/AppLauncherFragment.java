package com.klprjct.watchout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppLauncherFragment extends Fragment {
	private class AppButton {
		Intent launchIntent;
		String label;
		String packageName;
		Drawable icon;
		
		public AppButton(Intent launchIntent, String label, String packageName, Drawable icon){
			this.launchIntent = launchIntent;
			this.label = label;
			this.packageName = packageName;
			this.icon = icon;
		}

		public Intent getLaunchIntent() {
			return launchIntent;
		}

		public String getLabel() {
			return label;
		}

		public Drawable getIcon() {
			return icon;
		}
	}
	
	public static final String TAG = "AppLauncherFragment";
	
	ListView lv;
	
	PackageManager pm;
	LayoutInflater li;
	ActivityManager am;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		lv = new ListView(inflater.getContext());
		return lv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pm = this.getActivity().getPackageManager();
		am = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		
		li = this.getLayoutInflater(savedInstanceState);
		
		AppListGetter getter = new AppListGetter();
		getter.execute();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	private void updateUI(List<AppButton> list){
		if(lv != null){
			AppAdapter aa = new AppAdapter(list);
			lv.setAdapter(aa);
			lv.setDivider(null);
			lv.setDividerHeight(0);
		}
	}
	
	private class AppListGetter extends AsyncTask<Void, Void, List<AppButton>>{
		@Override
		protected void onPostExecute(List<AppButton> result) {
			//Update UI
			updateUI(result);
		}

		@Override
		protected List<AppButton> doInBackground(Void... arg0) {
			List<AppButton> appList = new ArrayList<AppButton>();
			
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			
			List<ResolveInfo> all = pm.queryIntentActivities(mainIntent, 0);
			for(ResolveInfo ri : all){
				String label = "";
				Drawable ico = null;
				Intent li = null;
				String pn = null;
				
				try {
					Resources res = pm.getResourcesForApplication(ri.activityInfo.applicationInfo);
					
					//Label
					if (ri.activityInfo.labelRes != 0) {
	                    label = res.getString(ri.activityInfo.labelRes);
	                } else {
	                    label = ri.activityInfo.loadLabel(pm).toString();
	                }
					
					//Icon
					if (ri.activityInfo.icon != 0) {
						ico = res.getDrawable(ri.activityInfo.icon); 
					} else {
						ico =  pm.getApplicationIcon(ri.activityInfo.packageName);
					}
					
					//Launch intent
					ActivityInfo activity = ri.activityInfo;
					ComponentName com = new ComponentName(activity.applicationInfo.packageName, activity.name);
					li = new Intent(Intent.ACTION_MAIN);
					li.addCategory(Intent.CATEGORY_LAUNCHER);
					li.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					li.setComponent(com);
					
					//Package name
					pn = activity.applicationInfo.packageName;
					
					appList.add(new AppButton(li, label, pn, ico));
				} catch (Exception e) {
				}
			}
			
			Collections.sort(appList, new Comparator<AppButton>(){
				@Override
				public int compare(AppButton arg0, AppButton arg1) {
					return arg0.label.compareToIgnoreCase(arg1.label);
				}
			});
			return appList;
		}	
	}
	
	private class AppAdapter implements ListAdapter {
		List<AppButton> appList;
		
		public AppAdapter(List<AppButton> list){
			this.appList = list;
		}
		
		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return appList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public int getItemViewType(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup parentView) {
			View newView = convertView;
			if(convertView == null){
				newView = li.inflate(R.layout.app_button, null);
			} 
			((TextView)newView.findViewById(R.id.appText)).setText(appList.get(pos).getLabel());
			((ImageView)newView.findViewById(R.id.appIcon)).setImageDrawable(appList.get(pos).getIcon());
			newView.findViewById(R.id.appButton).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					AppLauncherFragment.this.startActivity(appList.get(pos).getLaunchIntent());
					((MainActivity)AppLauncherFragment.this.getActivity()).finish();
				}
			});
			return newView;
		}

		@Override
		public int getViewTypeCount() {
			return appList.size();
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return appList.isEmpty();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
}
