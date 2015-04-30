package com.playrtc.sample.sample1;

import com.playrtc.sample.BaseActivity;
import com.playrtc.sample.R;
import com.playrtc.sample.playrtc.PlayRTCObserverImpl;
import com.playrtc.sample.util.AppUtil;
import com.playrtc.sample.view.ChannelPopupView;
import com.playrtc.sample.view.SlideLogView;
import com.playrtc.sample.view.ChannelPopupView.ChannelPopupViewListener;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.stream.PlayRTCMedia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;


/**
 * PlayRTC Sample Activity<br> 
 * BaseActivity 상속 하여 구현 <br>  
 * Video, Audio 통신 테스트  <br>
 * PlayRTCObserverImpl.onDisconnectChannel에서 Activity 종료 요청을 위해 <br>
 * BaseActivity.CloseActivityForResult구현 
 */
public class Sample1Activity extends BaseActivity implements BaseActivity.CloseActivityForResult {
	private static final String LOG_TAG = "Sample1Activity";

	/**
	 * PlayRTC 인터페이스를 구현한 Class
	 */
	private Sample1PlayRTC playrtc = null;
	
	/**
	 * PlayRTC 객체의 이벤트를 전달 받기 위한 PlayRTCObserver의 구현 개체 클래스 
	 */
	private PlayRTCObserverImpl playrtcObserver = null;
	
	/**
	 * 채널 생성, 채널 목록 조회, 채널 입장을 구현하기 위한 Popup Layer View<br>
	 * 채널 목록 리스트의 입장하기 버튼 이벤트를 받기 위해 ChannelListAdapterListener 리스너 필요 
	 */
	private ChannelPopupView popupView = null;
	
	/**
	 * 로그뷰 출력을 위한 TextView 확장 클래스
	 */
	private SlideLogView logView = null;
	
	/**
	 * 영상 출력 뷰 의 부모 컨테이너 뷰로 RelativeLayout를 확장
	 */
	private Sample1ViewGroup videoGroup = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample1);
		
		// 화면 Layout 인스턴스 및 이벤트 설정 
		initUILayoutControls();
		
		int currVol = this.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		AppUtil.showToast(this, "Master Volume["+currVol+"]");
		
		// PlatRTCObserver 구현 개체 
		playrtcObserver = new PlayRTCObserverImpl(this, videoGroup, popupView,  logView);

		//PlayRTCHandler 구현 개체 
		try {
			playrtc = new Sample1PlayRTC(this, playrtcObserver);
		} catch (UnsupportedPlatformVersionException e) {
			// 현재 SDK는 Android SDK 11 이상만 지원한다.
			e.printStackTrace();
			return;
		} catch (RequiredParameterMissingException e) {
			// PlayRTCObserver구현개체를 생성자에 전달해야 한다.
			e.printStackTrace();
			return;
		}
		
		// PlayRTC 설정 
		playrtc.setConfiguration();
		
		// Data CHannel은 사용하지 않으므로 DataChannelHandler dataHandler = null 
		playrtcObserver.setHandlers(playrtc.getPlayRTC(), (BaseActivity.CloseActivityForResult)this, null);
		
		// 채널 생성 및 생성된 채널 입장을 위한 팝업 레이어 
		popupView.init(this, playrtc, channelPopupListenet); 
		// 채널 생성 리스트를 조회하여 출력한다.
		popupView.showChannelList();
		// 팝업을 0.6초 후에 화면에 출력한다.
		popupView.show(600);
		
	}
	
	// Activty의 포커스 여부를 확인 
	// View의 크기를 구할수 있다 
	// hasFocus = true , 화면보여짐 , onCreate | onResume
	// hasFocus = false , 화면안보임 , onPause | onDestory
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 영상 스트림 출력을 위한 PlayRTCVideoView(GLSurfaceView를 상속) 동적 코드 생성   
		if(hasFocus && videoGroup.hasVideoView() == false) {
			videoGroup.createVideoView();
		}
	}
			
	@Override
	public void onPause() {
		super.onPause();
		if(playrtc != null)playrtc.pause();
		if(videoGroup != null)videoGroup.pause();
		
	}

	@Override
	public void onResume() {
		super.onResume(); 
		if(playrtc != null)playrtc.resume();
		if(videoGroup != null)videoGroup.resume();

	}
		
	@Override
	protected void onDestroy() {
		Log.e(LOG_TAG, "onDestroy===============================");
		playrtc = null;
		playrtcObserver = null;
		popupView = null;
		logView = null;
		videoGroup = null;
		this.finish();
		super.onDestroy();
	}
	
	/**
	 * BaseActivity.CloseActivityForResult
	 * @see com.playrtc.sample.BaseActivity.CloseActivityForResult#setCloseActivityForResult()
	 */
	@Override
	public void setCloseActivityForResult() {
		this.onBackPressed();
	}
	
	/**
	 * isCloesActivity가 false이면 앱 종료 의사를 묻는 다이얼로그를 출력하고
	 * true이면 super.onBackPressed()를 호출하여 앱을 종료하도록 한다.
	 */
	@Override
	public void onBackPressed()
    {
		Log.e(LOG_TAG, "onBackPressed===============================");
    	if(isCloesActivity)
    	{
    		// BackPress 처리 -> onDestroy 호출 
    		Log.e(LOG_TAG, "super.onBackPressed()===============================");
    		super.onBackPressed();
    	}
    	else
    	{
	    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    	alert.setTitle("Sample1");
	    	alert.setMessage("Sample을 종료하겠습니까?");
			
			alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					// 채널에 입장힌 상태이면 userPid값이 존재하므로 이값을 기준으로 채널 입장한 상태를
					// 판별하여 채널 퇴장을 호출한다.
					// 채널에서 퇴장을 하면 PlayRTCObserverImpl의 onDisconnectChannel에서
					// isCloesActivity를 true로 설정하고 onBackPressed()를 호출하여 종료 처리를 한다.
					String userPid = playrtc.getPeerId();
					if(TextUtils.isEmpty(userPid) == false) {
						isCloesActivity = false;
						playrtc.deleteChannel();
						
					}
					// 채널에 입장한 상태가 아니라면 바로 종료 처리를 한다.
					else {
						isCloesActivity = true;
						onBackPressed();
					}
					
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
		
	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		
		switch (this.getResources().getConfiguration().orientation)
		{
			case Configuration.ORIENTATION_PORTRAIT:
			{
				videoGroup.onOrienrationChanged(Configuration.ORIENTATION_PORTRAIT);
			}
			break;
			case Configuration.ORIENTATION_LANDSCAPE:
			{
				videoGroup.onOrienrationChanged(Configuration.ORIENTATION_LANDSCAPE);
			}
			break;
			
			default:
		  
		}
	    super.onConfigurationChanged(newConfig);
	}
	
	
		
		
	////////////////////////////////////////////////////////////////////////////////////////////
	// ChannelPopupViewListener

	private ChannelPopupViewListener channelPopupListenet = new ChannelPopupViewListener() {
	
		/**
		 * 채널 생성 버튼을 클릭 
		 * @param channelName String, 목록에 나타나는 채널의 이름 
		 * @param userId String, option 이지만 사용자 식별을 위해 전달해주눈 것이 좋습니다.
		 * @see com.playrtc.sample.view.ChannelPopupView.ChannelPopupViewListener#onClickCreateChannel
		 */
		@Override
		public void onClickCreateChannel(String channelName, String userId) {
			Log.d("LOG_TAG", "onCreateChannel channelName["+channelName+"] userId["+userId+"]");
		
			try {
				playrtc.createChannel(userId);
			} catch (RequiredConfigMissingException e) {
				e.printStackTrace();
			}
		
		}
		
		/**
		 * 생성되어 있는 채널에 입장 버튼 클릭 
		 * @param channelId String, 입장할 채널의 아이디, 예제에서는 리스트에서 채널을 선택하지만 <br>
		 *        실제 App에서는 Push 메세지 등을 이용해서 channel 아이디를 전달.
		 * @param userId String, option 이지만 사용자 식별을 위해 전달해주눈 것이 좋습니다.
		 * @see com.playrtc.sample.view.ChannelPopupView.ChannelPopupViewListener#onClickConnectChannel
		 */
		@Override
		public void onClickConnectChannel(String channelId, String userId) {
			Log.d("LOG_TAG", "onConnectChannel channelId["+channelId+"] userId["+userId+"]");
			try {
				playrtc.connectChannel(channelId, userId);
			} catch (RequiredConfigMissingException e) {
				e.printStackTrace();
			}
		}		
	
	};
	
	private void initUILayoutControls() {

		
		popupView = (ChannelPopupView)findViewById(R.id.sample1_channel_info);
		logView = (SlideLogView)findViewById(R.id.sample1_logview);
		videoGroup = (Sample1ViewGroup)findViewById(R.id.sample1_videogroup);
		
		// local audio mute
		((Button)this.findViewById(R.id.btn_sample1_mute_alocal)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button)v;
				String text = (String)b.getText();
				boolean setMute = text.endsWith("-OFF");
				PlayRTCMedia localMedia = playrtcObserver.getLocalMedia();
				if(localMedia != null) {
					localMedia.setAudioMute(setMute);
					b.setText((setMute == true)?"A-ON" : "A-OFF");
				}
			}
		});
		// local video mute
		((Button)this.findViewById(R.id.btn_sample1_mute_vlocal)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button)v;
				String text = (String)b.getText();
				boolean setMute = text.endsWith("-OFF");
				PlayRTCMedia localMedia = playrtcObserver.getLocalMedia();
				if(localMedia != null) {
					localMedia.setVideoMute(setMute);
					b.setText((setMute == true)?"A-ON" : "A-OFF");
				}
			}
		});
		// local audio mute
		((Button)this.findViewById(R.id.btn_sample1_mute_aremote)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button)v;
				String text = (String)b.getText();
				boolean setMute = text.endsWith("-OFF");
				PlayRTCMedia remoteMedia = playrtcObserver.getRemoteMedia();
				if(remoteMedia != null) {
					remoteMedia.setAudioMute(setMute);
					b.setText((setMute == true)?"A-ON" : "A-OFF");
				}
			}
		});
		// local video mute
		((Button)this.findViewById(R.id.btn_sample1_mute_vremote)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button)v;
				String text = (String)b.getText();
				boolean setMute = text.endsWith("-OFF");
				PlayRTCMedia remoteMedia = playrtcObserver.getRemoteMedia();
				if(remoteMedia != null) {
					remoteMedia.setVideoMute(setMute);
					b.setText((setMute == true)?"A-ON" : "A-OFF");
				}
			}
		});
		
		/* UserCommand 파일 전송 버튼 */
		((Button)this.findViewById(R.id.btn_sample1_command)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				playrtc.userCommand(playrtcObserver.getOtherPeerId(), "{\"command\":\"alert\", \"data\":\"usercommand입니다.\"}");
			}
		});
		
		
		/* 로그뷰  토글 버튼 이벤트 처리 */
		((Button)this.findViewById(R.id.btn_sample1_log)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if(logView.isShown() == false) {
					logView.show();
					((Button)v).setText("로그닫기");
				}
				else {
					logView.hide();
					((Button)v).setText("로그보기");
				}
			}
		});
		
		/* Channel Popup 출력 버튼 */
		((Button)this.findViewById(R.id.btn_sample1_channel)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				if( popupView.isShown()) {
					popupView.hide();
				}
				else {
					popupView.showChannelList();
					popupView.show(0);
				}
				
			}
		});
		/* Channel 나가기 버튼 */
		((Button)this.findViewById(R.id.btn_sample1_disconnect)).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				playrtc.disconnectChannel(playrtc.getPeerId());
			}
		});
	}

	
}
