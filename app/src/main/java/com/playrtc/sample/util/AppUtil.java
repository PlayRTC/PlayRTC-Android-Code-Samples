package com.playrtc.sample.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class AppUtil {
	private static Toast logToast = null;
	private static ProgressDialog waitDlg = null;
	public static void showToast(final Activity activity, final String msg) {
		activity.runOnUiThread(new Runnable(){
		   public void run()
		   {
			  if (logToast != null) {
				   logToast.cancel();
				   logToast = null;
			  }
			   logToast = Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT);
			   logToast.show();
		   }
		   
	   }); 
	}
	
	public static void showWaitDialog(final Activity activity, final String title, final String message) {
		
		activity.runOnUiThread(new Runnable(){
		   public void run()
		   {
			  if (waitDlg != null) {
				  waitDlg.dismiss();;
				  waitDlg = null;
			  }
			  waitDlg = ProgressDialog.show(activity, title, message, true, false);
		   }
		 }); 
	}
	public static boolean isShowingWaitDlg() {
		if (waitDlg != null) {
			return waitDlg.isShowing();
		}
		return false;

	}
	public static void hideWaitDialog(final Activity activity) {
		if (waitDlg == null) {
			return;
		}
		activity.runOnUiThread(new Runnable(){
		   public void run()
		   {
			   if(waitDlg.isShowing()) {
				   waitDlg.dismiss();
			   }
			   waitDlg = null;
		   }
		 });
	}
	public static String getRandomServiceMailId()
	{
		String userId = "";
		String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for( int i=0; i < 5; i++ )
		{
			double randomVal = Math.random();
			int idx = (int)Math.floor(randomVal * possible.length());
			userId += possible.charAt(idx);
		}
		
		return userId + "@playrtc.sample.com";
	}
	
	public static boolean isWifiConnected(Context context)
	{
		ConnectivityManager cm = getConnectivityManager(context);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (ni==null)?false:ni.isConnected();
	}
	
	public static boolean isMobileConnected(Context context)
	{
		ConnectivityManager cm = getConnectivityManager(context);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isConnected = (ni == null)?false:ni.isConnected();
		return isConnected;
	}
	
	public static boolean isNetworkConnected(Context context)
	{
		boolean isConnected = (isWifiConnected(context) || isMobileConnected(context));
		return isConnected;
	}
	
	public static String getWifiSSID(Context context) {
		WifiManager wifimanager;
		wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifimanager.getConnectionInfo();

		String ssid = info.getSSID();
		if(ssid != null)ssid = ssid.replaceAll("\"", "");
		return ssid;
	}
	private static ConnectivityManager getConnectivityManager(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm;
	}
}
