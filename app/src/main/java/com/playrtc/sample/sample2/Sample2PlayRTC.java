package com.playrtc.sample.sample2;

import java.io.File;

import android.os.Environment;

import com.playrtc.sample.BaseActivity;
import com.playrtc.sample.playrtc.BasePlayRTC;
import com.sktelecom.playrtc.config.PlayRTCSettings;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;


/**
 * PlayRTC 인터페이스를 구현한 Class.<br>
 * <b>Method</b>
  * <pre>
 * 1. public PlayRTC getPlayRTC()
 *    PlayRTC 인터페이스 개체를 반환한다.
 * 2. public void setConfiguration()
 *    PlayRTC Communication 서비스를 위한 PlayRTC config 설정 
 * 3. public void createChannel(String uid)
 *    P2P 연결을 수립하기 위해 PlayRTC 채널 서비스에 채널을 생성하고 입장힌다.
 * 4. public void connectChannel(String channelId, String uid)
 *    P2P 연결을 수립하기 위해 생성되어 있는 채널에 입장한다. 
 * 5. public void disconnectChannel(String peerId)
 *    입장한 채널에서 퇴장한다.
 * 6. public void deleteChannel()
 *    입장한 채널을 종료시킨다.
 * 7. public void resume()
 *    Activity onResume
 * 8. public void pause()
 *    Activity onPause
 * 9. public String getChannelId()
 *    입장해 있는 채널의 아이디를 반환한다.
 * 10. public String getPeerId()
 *    입장해 있는 채널에서 부여 받은 사용자의 아이디를 반환한다. 
 * 11 public String getPeerUid()
 *    입장해 있는 채널에 등록한 Application 사용자의 아이디를 반환한다. 
 * 12. public void getChannelList(PlayRTCServiceHelperListener listener)
 *    채널 서비스에 생성되어 있는 채널의 목록을 조회하여 반환한다. 
 * </pre>
 * 
 * @see com.playrtc.sample.playrtc.BasePlayRTC
 */
public class Sample2PlayRTC extends BasePlayRTC{

	
	/**
	 * 생성자
	 * @param activity Activity
	 * @param oberver PlayRTCObserver, PlayRTC의 이벤트를 처리하기 위한 리스너 구현 객체 
	 * @throws RequiredParameterMissingException  SERVICE_URL과 PlayRTCObserver 구현개체를 생성자에 전달해야 한다.
	 * @throws UnsupportedPlatformVersionException Android SDK 11 이상만 지원한다.
	 */
	public Sample2PlayRTC(BaseActivity activity, PlayRTCObserver oberver) 
			throws UnsupportedPlatformVersionException, RequiredParameterMissingException {
		
		super(activity, oberver);
		
	}
	
	
	/**
	 * PlayRTC Communication 서비스를 위한 PlayRTC config 설정 <br>
	 * 음성을 사용하도록 지정한다. 
	 */
	public void setConfiguration() {
		PlayRTCSettings settings = playRTC.getSettings();		
		
		/**
		 * Android Application Context를 필요로 한다. 
		 * 지정하지 않으면 RequiredConfigMissingException 발생 
		 */
		settings.android.setContext(activity.getApplicationContext());
		
		/**
		 * SKT T-DEV PlayRTC 프로젝트 아이디, 필수 항목 
		 */
		settings.setTDCProjectId(TDCProjectId);
		
		/**
		 *  영상 스트림 전송 여부를 지정, 기본적으로 영상 스트림 수신은 기본 적용 됨 <br>
		 *  false 설정 시 상대방은 read-only이므로 remote-media객체가 생성 되니<br>
		 *  주의를 요함. 
		 */
		settings.setVideoEnable(false); /* 영상 전송 사용 */
		/* 사용할 카메라를 지정  "front", "back" 카메라 지정 */
		settings.video.setFrontCameraEnable(false); // default true
		settings.video.setBackCameraEnable(false);  // default false
		
		
		/**
		 *  음성 스트림 전송 여부를 지정, 기본적으로 음성 스트림 수신은 기본 적용 됨 <br>
		 *  false 설정 시 상대방은 read-only이므로 remote-media객체가 생성 되니<br>
		 *  주의를 요함. 
		 */  
		settings.setAudioEnable(true);   /* 음성 전송 사용 */
		
		// 데이터 스트림 통신을 사용할 지 여부, false 시 데이터 통신은 불가  
		settings.setDataEnable(false);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
		
		/*  채널에 입장하여 상대방과의 연결 승인 과정을 지정, true면 먼저 입장한 상대방의 수락이 있어야 연결 수립 진행  */ 
		settings.channel.setRing(false); 
		
		/* SDK Console 로그 레벨 지정 */
		settings.log.console.setLevel(LOG_LEVEL);
		
		/* SDK 파일 로깅을 위한 로그 파일 경로, 파일 로깅을 사용하지 않는다면 Pass */ 
        File logPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + activity.getPackageName() + "/files/log/sample2/");  
        
        if(logPath.exists() == false) {
        	logPath.mkdirs();
        }
        
		/* 파일 로그를 남기려면 로그파일 폴더 지정 . [PATH]/yyyyMMdd.log , 10일간 보존 */
		settings.log.file.setLogPath(logPath.getAbsolutePath());
		/* SDK 파일 로그 레벨 지정 */
		settings.log.file.setLevel(LOG_LEVEL);
		

        /* SDK 서버 로깅을 위한 로그 임시 캐시 경로, 전송 실패 시 이 경로에 파일 저장을 한다.  */
        File cachePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + activity.getPackageName() + "/files/cache/sample2/");  
        if(cachePath.exists() == false) {
        	cachePath.mkdirs();
        }
        
		/* 서버 로그 전송 실패 시 임시 로그 저장 DB 파일 폴더   */
		settings.log.setCachePath(cachePath.getAbsolutePath());
		
		/* 서버 로그 전송 실패 시 재전송 시도 지연 시간, msec */
		settings.log.setRetryQueueDelays(5 * 1000);
		/* 서버 로그 재 전송 실패시 로그 DB 저장 후 재전송 시도 지연 시간, msec */
		settings.log.setRetryCacheDelays(20 * 1000);
	}
}
