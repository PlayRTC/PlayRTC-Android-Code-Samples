package com.playrtc.sample.util;


import android.app.Activity;
import android.widget.Toast;

/**
 * PlayRTC Sample App Util Class
 * @author ds3grk
 *
 */
public class Utils {
	private static Toast logToast = null;
	

	/**
	 * 화면에 Toast를 짧게 출력 
	 * @param activity Activity
	 * @param msg String, 출력 메세지 
	 */
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
	
	/**
	 * 사용자 아이디를 랜덤하게 생성하여 반환 <br>
	 * XXXXX@@playrtc.com
	 * @return String
	 */
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
		
		return userId + "@playrtc.com";
	}
}
