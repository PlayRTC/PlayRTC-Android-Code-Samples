package com.playrtc.sample;


import com.playrtc.sample.sample1.Sample1Activity;
import com.playrtc.sample.sample2.Sample2Activity;
import com.playrtc.sample.sample3.Sample3Activity;
import com.playrtc.sample.sample4.Sample4Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * PlayRTC Sample을 구동하기 위한 Main Menu Activity 
 * BaseActivity 상속 하여 구현  
 */
public class MainActivity extends BaseActivity {
	private static final String LOG_TAG = "MainActivity";
	private static final int LAUNCHED_PLAYRTC_SAMPLE1 = 100;
	private static final int LAUNCHED_PLAYRTC_SAMPLE2 = 200;
	private static final int LAUNCHED_PLAYRTC_SAMPLE3 = 300;
	private static final int LAUNCHED_PLAYRTC_SAMPLE4 = 400;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// 화면 Layout 인스턴스 및 이벤트 설정 
		initUILayoutControls();
		
	}

	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
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
	protected void onDestroy() {
		Log.e(LOG_TAG, "onDestroy....");

		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}
	
	/*
	 * isCloesActivity가 false이면 앱 종료 의사를 묻는 다이얼로그를 출력하고
	 * true이면 super.onBackPressed()를 호출하여 앱을 종료하도록 한다.
	 */
	@Override
	public void onBackPressed()
    {
    	if(isCloesActivity)
    	{
    		// BackPress 처리 -> onDestroy 호출 
    		super.onBackPressed();
    	}
    	else
    	{
	    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    	alert.setTitle("PlayRTCSample");
	    	alert.setMessage("앱을 종료하겠습니까?");
			
			alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					isCloesActivity = true;
					onBackPressed();
				}
			});
			alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	dialog.dismiss();
	            	isCloesActivity = false;
	            }
	        });
			alert.show();
    	}
    }

	private void initUILayoutControls() {
		// Sample1 Activity  실행 버튼 이벤트 
		((Button)this.findViewById(R.id.btn_go_sample1)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				goPlayRTCSample1();
			}
		});

		// Sample2 Activity  실행 버튼 이벤트 
		((Button)this.findViewById(R.id.btn_go_sample2)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				goPlayRTCSample2();
			}
		});
		
		// Sample3 Activity  실행 버튼 이벤트 
		((Button)this.findViewById(R.id.btn_go_sample3)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				goPlayRTCSample3();
			}
		});
		
		// Sample4 Activity  실행 버튼 이벤트 
		((Button)this.findViewById(R.id.btn_go_sample4)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				goPlayRTCSample4();
			}
		});
	}
	
	// Sample1 Activity  실행  
	private void goPlayRTCSample1() {
		Intent intent = new Intent(MainActivity.this, Sample1Activity.class);
		MainActivity.this.startActivityForResult(intent, LAUNCHED_PLAYRTC_SAMPLE1);
	}
	// Sample2 Activity  실행  
	private void goPlayRTCSample2() {
		Intent intent = new Intent(MainActivity.this, Sample2Activity.class);
		MainActivity.this.startActivityForResult(intent, LAUNCHED_PLAYRTC_SAMPLE2);
	}
	// Sample3 Activity  실행  
	private void goPlayRTCSample3() {
		Intent intent = new Intent(MainActivity.this, Sample3Activity.class);
		MainActivity.this.startActivityForResult(intent, LAUNCHED_PLAYRTC_SAMPLE3);
	}
	// Sample4 Activity  실행  
	private void goPlayRTCSample4() {
		Intent intent = new Intent(MainActivity.this, Sample4Activity.class);
		MainActivity.this.startActivityForResult(intent, LAUNCHED_PLAYRTC_SAMPLE4);
	}
}

