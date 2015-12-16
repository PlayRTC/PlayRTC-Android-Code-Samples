package com.playrtc.sample;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.playrtc.sample.handler.PlayRTCDataChannelHandler;
import com.playrtc.sample.util.Utils;
import com.playrtc.sample.view.PlayRTCChannelView;
import com.playrtc.sample.view.PlayRTCLogView;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.PlayRTC.PlayRTCAudioType;
import com.sktelecom.playrtc.PlayRTC.PlayRTCCode;
import com.sktelecom.playrtc.PlayRTC.PlayRTCStatus;
import com.sktelecom.playrtc.config.PlayRTCSettings;

// sdk v2.2.0 StatsReport
import com.sktelecom.playrtc.config.PlayRTCConfig;
import com.sktelecom.playrtc.PlayRTCStatsReport;
import com.sktelecom.playrtc.PlayRTCStatsReport.RatingValue;

import com.sktelecom.playrtc.config.PlayRTCVideoConfig.CameraType;

import com.sktelecom.playrtc.config.ConstraintSetting.PlayRTCFrame;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;
import com.sktelecom.playrtc.stream.PlayRTCData;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.android.PlayRTCAudioManager;
import com.sktelecom.playrtc.util.android.PlayRTCAudioManager.AudioDevice;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;
//v2.2.1
import com.sktelecom.playrtc.observer.PlayRTCStatsReportObserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.annotation.SuppressLint;

/**
 * PlayRTC를 구현한 Activity <br>
 * PlayRTC객체의 이벤트 처리를 위해 PlayRTCEvent, 채널 종료등의 HTTP 요청 결과를 수신하기 위한 PlayRTCHttpClient를 <br>
 * interface상속 하여 구현 <br>
 * <br>
 * <b>주요 멤버 </b>
 * <pre>
 * - PlayRTCChannelView channelInfoView
 *     Sample에서 채널을 생성하거나 채널 목록을 조회하여 입장 할 채널을 선택하는 팝업 뷰
 *     생성/입장 버튼 이벤트를 받기 위해 PlayRTCChannelViewListener를 구현한다.
 * - PlayRTC playRTC
 *     PlayRTC 인스턴스
 *     PlayRTCObserver Listener Interface 구현 필요
 * - PlayRTCVideoView localView/remoteView
 *     영살 출력 뷰.
 *     PlayRTCMedia 수신 시 영상 출력을 이해 PlayRTCVideoView의 renderer 인터페이스 등록해야 한다.
 * - PlayRTCDataChannelHandler dataHandler
 *     PlayRTCData를 위한 PlayRTCDataObserver Interface를 구현한 Handler Class
 *     PlayRTCData를 이용해 데이터 송/수신 처리
 * - PlayRTCLogView logView
 *     PlayRTC 로그를 출력하기위해 TextView를 확장한 Class
 * - PlayRTCAudioManager pAudioManager
 *     Audio 출력 디바이스를 제어하는 PLayRTC Util Class
 * </pre>
 * <p/>
 * <p/>
 * <b>PlayRTC 구현</b>
 * <pre>
 * 1. PlayRTCSettings 생성
 *   v2.1.2
 *   PlayRTCActivity#onCreate {@link PlayRTCActivity#createPlayRTCSettings(int)}
 *   v2.2.0
 *   PlayRTCActivity#onCreate {@link PlayRTCActivity#createPlayRTCConfig(int)}
 * 2. PlayRTC 인스턴스를 생성
 *   PlayRTCSettings, PlayRTCObserver 구현체 전달
 *   PlayRTCActivity#onCreate : PlayRTCFactory.newInstance(settings, (PlayRTCObserver)new PlayRTCObserverImpl())
 *
 * 3. PlayRTCAudioManager 생성 및 구동
 *   PlayRTCActivity#onCreate
 *     pAudioManager = PlayRTCAudioManager.create(this, new Runnable({})}
 *     pAudioManager.init()
 *
 * 4. 영상 출력을 위한 PlayRTCVideoView 생성
 *   PlayRTCActivity#onWindowFocusChanged
 *     PlayRTCVideoView 동적 생성 시 화면 사이즈 계산을 위해 화면 사이즈를 획들할 수 있는 onWindowFocusChanged에서 구현
 *     createVideoView()
 *       -> createRemoteVideoView : 상대방 영상 화면
 *       -> createRemoteVideoView : 본인 영상 화면
 *
 * 5. 채널 서비스에 채널 생성/입장 요청 -> PlayRTCChannelView 팝업에서 채널 생성 또는 입장 버튼 리스너 PlayRTCChannelViewListener 구현
 *   PlayRTCChannelViewListener#onClickCreateChannel
 *     playRTC.createChannel(parameters)
 *   PlayRTCChannelViewListener#onClickConnectChannel
 *     playRTC.connectChannel(parameters)
 *
 * 6. 채널 서비스에 채널 생성/입장 성공 후 PlayRTC Connect
 *   PlayRTCObserverImpl#onConnectChannel
 *
 * 7. 로컬 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
 *   PlayRTCObserverImpl#onAddLocalStream
 *   PlayRTCMedia 수신 시 영상 출력을 이해 PlayRTCVideoView의 renderer 인터페이스 등록
 *     media.setVideoRenderer(localView.getVideoRenderer());
 *
 * 8. P2P 연결 시 상대방 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
 *   PlayRTCObserverImpl#onAddRemoteStream
 *   PlayRTCMedia 수신 시 영상 출력을 이해 PlayRTCVideoView의 renderer 인터페이스 등록
 *     media.setVideoRenderer(remoteView.getVideoRenderer());
 *
 * 9. Data 송수신을 위한  PlayRTCData 수신 이벤트 처리 -> 데이터 채널 사용 설정 시
 *   PlayRTCObserverImpl#onAddDataStream
 *   PlayRTCData 수신 시 수신 이벤트 처리를 이해 PlayRTCDataObserver를 구현한 PlayRTCDataChannelHandler 등록
 *     dataHandler.setDataChannel(data);
 *
 * 10. 상대방 채널 퇴장 이벤트 처리
 *   사용자가 playRTC.disconnectChannel을 호출하면 상대방에게 onOtherDisconnectChannel 호출됨.
 *   본인은 onDisconnectChannel 호출
 *   PlayRTCObserverImpl#onOtherDisconnectChannel
 *
 *
 * 11. 채널 종료 이벤트 처리
 *   playRTC.deleteChannel()을 호출하면 채널의 모든 사용자에게 채널 종료 이벤트가 전달
 *   PlayRTCObserverImpl#onDisconnectChannel
 *
 * 12. 종료 처리
 *   Back 키 또는 종료 버튼을 누르면 playRTC.deleteChannel()를 호출
 *   PlayRTCObserverImpl#onDisconnectChannel에서 화면 종료 처리
 * </pre>
 */

public class PlayRTCActivity extends Activity {
    private static final String LOG_TAG = "PlayRTCActivity";

    /**
     * PlayRTC SDK 콘솔 Log 레벨 정의. WARN
     */
    private int CONSOLE_LOG = PlayRTCSettings.WARN;
    /**
     * PlayRTC SDK 파일 Log 레벨 정의. WARN
     */
    private int FILE_LOG = PlayRTCSettings.WARN;

    /**
     * 채널 팝업 뷰
     *
     * @see com.playrtc.sample.view.PlayRTCChannelView
     */
    private PlayRTCChannelView channelInfoView = null;

    /**
     * PlayRTC 인스턴스 전역변수
     */
    private PlayRTC playRTC = null;

    /**
     * PlayRTCVideoView를 위한 부모 뷰 그룹
     */
    private RelativeLayout videoArea = null;

    /**
     * 로컬 영상 출력 뷰
     */
    private PlayRTCVideoView localView = null;

    /**
     * 상대방 영상 출력 뷰
     */
    private PlayRTCVideoView remoteView = null;

    /**
     * 로컬 PlayRTCMedia 전역 변수
     */
    private PlayRTCMedia localMedia = null;

    /**
     * 상대방 PlayRTCMedia 전역 변수
     */
    private PlayRTCMedia remoteMedia = null;

    /**
     * PlayRTCData를 위한 Handler Class
     *
     * @see com.playrtc.sample.handler.PlayRTCDataChannelHandler
     */
    private PlayRTCDataChannelHandler dataHandler = null;

    /**
     * Peer 채널 퇴장 버튼
     */
    private Button btnDisconnectChannel = null;

    /**
     * 로그 출력 TextBox
     *
     * @see com.playrtc.sample.view.PlayRTCLogView
     */
    private PlayRTCLogView logView = null;

    /**
     * 채널아이디, 서비스서버에서 전달 받음
     */
    private String channelId = "";

    /**
     * 서비스 사용자 상대방 아이디, 서비스에서 사용하는 아이디
     */
    private String peerId = null;

    /**
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리. 만약 채널에 입장한 상태이면 먼저 채널을 종료한다.
     */
    private boolean isCloesActivity = false;

    /**
     * Audio 출력 디바이스관련 Helper Class
     */
    private PlayRTCAudioManager pAudioManager = null;

    private String TDCProjectId = "60ba608a-e228-4530-8711-fa38004719c1"; // playrtc

    private String TDCLicense = "1";

    private Intent resultIntent = null;

    /**
     * PlayRTC Sample Type<br>
     * 1 : 영상 + 음성 + Data Sample<br>
     * 2 : 영상 + 음성 Sample<br>
     * 3 : 음성 Sample<br>
     * 4 : Data Sample<br>
     */
    private int playrtcType = 1;

    // use sdk v2.2.0
    private boolean USE_SDK2_2_0 = true;

    public static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);

        // Application permission 23
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            checkPermission(MANDATORY_PERMISSIONS);
        }

        resultIntent = new Intent();
        Intent intent = getIntent();
        /**
         * PlayRTC Sample Type
         */
        playrtcType = intent.getIntExtra("type", 1);

        // UI 인스턴스 변수 처리
        this.initUIControls();

        try {
            // 1. MainActivity에서 선택한 Sample Type에 맞게 PlayRTCSettings 개체 생성
            if(USE_SDK2_2_0 == false) {
                // sdk 2.1.2
                PlayRTCSettings settings = createPlayRTCSettings(playrtcType);
                /*
                 * 2. PlayRTC 인스턴스를 생성
                 * PlayRTC 인터페이스를 구현한 객체 인스턴스를 생성하고 PlayRTC를 반환한다. static  <br>
                 * settings PlayRTCSettings, PlayRTC 서비스 설정 정보 객체
                 * observer PlayRTCObserver, PlayRTC Event 리스너
                 */
                this.playRTC = PlayRTCFactory.newInstance(settings, (PlayRTCObserver) new PlayRTCObserverImpl());
            }
            else {
                // sdk 2.2.0
                PlayRTCConfig config = createPlayRTCConfig(playrtcType);
                /*
                 * 2. PlayRTC 인스턴스를 생성
                 * PlayRTC 인터페이스를 구현한 객체 인스턴스를 생성하고 PlayRTC를 반환한다. static  <br>
                 * settings PlayRTCSettings, PlayRTC 서비스 설정 정보 객체
                 * observer PlayRTCObserver, PlayRTC Event 리스너
                 */
                this.playRTC = PlayRTCFactory.createPlayRTC(config, (PlayRTCObserver) new PlayRTCObserverImpl());
            }


        } catch (UnsupportedPlatformVersionException e) {
            e.printStackTrace();
        } catch (RequiredParameterMissingException e) {
            e.printStackTrace();
        }

        // 채널 생성/입장 팝업 뷰 초기화 설정
        this.channelInfoView.init(this, this.playRTC, new PlayRTCChannelViewListener());

        this.dataHandler = new PlayRTCDataChannelHandler(this, logView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

		/*
		 * PlayRTCAudioManager를 사용하므로 주석 처리 
		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audioManager.setSpeakerphoneOn(true);
		*/

        /**
         * 3. PlayRTCAudioManager 생성 및 구동. sdk v2.1.2
         * The sdk 2.2.0 버전은  PlayRTCConfig에서 PlayRTCAudioManager사용 여부를 지정
         * PlayRTCAudioManager 인스턴스 생성. static
         * @param context Context
         * @param runnable Runnable, Audio 출력 디비이스가 변경될 때 호출 받을 Runnable 객체
         * @return PlayRTCAudioManager
         */
        if(USE_SDK2_2_0 == false) {
            //sdk v2.2.0 에서는  PlayRTCConfig 에서 PlayRTCAudioManager를 사용하도록 설정.
            pAudioManager = PlayRTCAudioManager.create(this, new Runnable() {
                @Override
                public void run() {

	        	/* Audio 출력 디비이스 조회 한후 PlayRTC에 전달한다.
	             * AudioDevice : Audio 출력 장치의 종류를 정의.
	             * - WIRED_HEADSET
	             * - SPEAKER_PHONE
	             * - EARPIECE
	             * - BLUETOOTH, 미지원
	             */
                    AudioDevice audioDivece = pAudioManager.getSelectedAudioDevice();
                    if (playRTC != null) {
                        if (audioDivece == AudioDevice.WIRED_HEADSET) {
                            // PlayRTC SDK에 Audio Path를 전달한다.
                            playRTC.notificationAudioType(PlayRTCAudioType.AudioReceiver);
                            Log.i(LOG_TAG, "AudioDevice audioDivece = AudioReceiver");
                        } else if (audioDivece == AudioDevice.SPEAKER_PHONE) {
                            // PlayRTC SDK에 Audio Path를 전달한다.
                            playRTC.notificationAudioType(PlayRTCAudioType.AudioSpeaker);
                            Log.i(LOG_TAG, "AudioDevice audioDivece = AudioSpeaker");
                        } else if (audioDivece == AudioDevice.EARPIECE) {
                            // PlayRTC SDK에 Audio Path를 전달한다.
                            playRTC.notificationAudioType(PlayRTCAudioType.AudioEarphone);
                            Log.i(LOG_TAG, "AudioDevice audioDivece = AudioEarphone");
                        } else if (audioDivece == AudioDevice.BLUETOOTH) {
                            // PlayRTC SDK에 Audio Path를 전달한다.
                            playRTC.notificationAudioType(PlayRTCAudioType.AudioBluetooth);
                            Log.i(LOG_TAG, "AudioDevice audioDivece = AudioBluetooth");
                        }
                    }
                }
            });

            // PlayRTCAudioManager 구동
            pAudioManager.init();
        }
        // 채널 팝업 출력
        if (TextUtils.isEmpty(channelId)) {
            this.channelInfoView.showChannelList();
            this.channelInfoView.show(600);
        }
    }
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;
    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {

        requestPermissions(permissions, MY_PERMISSION_REQUEST_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                int cnt = permissions.length;
                for(int i = 0; i < cnt; i++ ) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED ) {

                        Log.i(LOG_TAG, "Permission[" + permissions[i] + "] = PERMISSION_GRANTED");

                    } else {

                        Log.i(LOG_TAG, "permission[" + permissions[i] + "] always deny");
                    }
                }
                break;
        }
    }

    // Activty의 포커스 여부를 확인
    // 영상 스트림 출력을 위한 PlayRTCVideoView(GLSurfaceView를 상속) 동적 코드 생성
    // 생성 시 스크린 사이즈를 생성자에 넘김
    // hasFocus = true , 화면보여짐 , onCreate | onResume
    // hasFocus = false , 화면안보임 , onPause | onDestory
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (playrtcType == 3 || playrtcType == 4) {
            return;
        }
        if (hasFocus && (localView == null || remoteView == null)) {

            this.runOnUiThread(new Runnable() {
                public void run() {
                    // 4. 영상 스트림 출력을 위한 PlayRTCVideoView(GLSurfaceView를 상속) 동적 생성
                    createVideoView();
                }
            });
        }
    }

    /**
     * 영상 스트림 출력을 위한 PlayRTCVideoView(GLSurfaceView를 상속) 동적 코드 생성<br>
     * video 스트림 출력을 위한 PlayRTCVideoView의 부모 ViewGroup의 사이즈 재조정<br>
     * 가로-세로 비율 1(가로):0.75(세로), 높이 기준으로 폭 재지정
     */
    private void createVideoView() {

        // PlayRTCVideoView의 부모 ViewGroup의 사이즈 확인
        Point screenDimensions = new Point();
        int height = videoArea.getHeight();
        // PlayRTCVideoView의 부모 ViewGroup의 사이즈 재조정
        float width = (height * 1.0f) / 0.75f;
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) videoArea.getLayoutParams();
        param.width = (int) width;
        param.height = (int) height;
        videoArea.setLayoutParams(param);

        screenDimensions.x = param.width;
        screenDimensions.y = param.height;

        // big, remote
        if (remoteView == null) createRemoteVideoView(screenDimensions);
        // small. local
        if (localView == null) createLocalVideoView(screenDimensions);
    }

    private void createLocalVideoView(final Point screenDimensions) {
        // 자신의 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if (localView == null) {
            // 부모 View의 30% 비율로 크기를 지정한다
            Point displaySize = new Point();
            displaySize.x = (int) (screenDimensions.x * 0.3);
            displaySize.y = (int) (screenDimensions.y * 0.3);
			
			/*
			 *  PlayRTCVideoView 생성자
			 * @param context Context
			 * @param dimensions Point
			 * @param mirror boolean, 영상 출력을 거울 모드로 할지 여부를 지정한다.<br>
			 *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
			 *        주로 로컬 영상의 경우 거울 모드로 지정한다.
			 */
            localView = new PlayRTCVideoView(this.getBaseContext(), displaySize, true);

            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(displaySize.x, displaySize.y);
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            param.setMargins(30, 30, 30, 30);

            localView.setLayoutParams(param);
            // 부모뷰에 PlayRTCVideoView 추가
            videoArea.addView(localView);
            localView.setZOrderOnTop(true);
            localView.setVisibility(View.INVISIBLE);
            localView.setVideoFrameObserver(new PlayRTCVideoView.VideoRendererObserver() {
                @Override
                public void onFrameResolutionChanged(PlayRTCVideoView view, int videoWidth, int videoHeight, int rotationDegree) {
                    Log.e(LOG_TAG, "Local FrameResolution videoWidth[" + videoWidth + "] videoHeight[" + videoHeight + "] rotationDegree[" + rotationDegree + "]");
                }
            });
        }
    }

    private void createRemoteVideoView(final Point screenDimensions) {
        // 상대방 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if (remoteView == null) {
            Point displaySize = new Point();
            displaySize.x = screenDimensions.x;
            displaySize.y = screenDimensions.y;
			/*
			 *  PlayRTCVideoView 생성자
			 * @param context Context
			 * @param dimensions Point
			 * @param mirror boolean, 영상 출력을 거울 모드로 할지 여부를 지정한다.<br>
			 *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
			 *        주로 로컬 영상의 경우 거울 모드로 지정한다.
			 */
            remoteView = new PlayRTCVideoView(this.getBaseContext(), displaySize, false);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            remoteView.setLayoutParams(param);
            // 부모뷰에 PlayRTCVideoView 추가
            videoArea.addView(remoteView);
            remoteView.setVideoFrameObserver(new PlayRTCVideoView.VideoRendererObserver() {
                @Override
                public void onFrameResolutionChanged(PlayRTCVideoView view, int videoWidth, int videoHeight, int rotationDegree) {
                    Log.e(LOG_TAG, "Remote FrameResolution videoWidth[" + videoWidth + "] videoHeight[" + videoHeight + "] rotationDegree[" + rotationDegree + "]");
                }

            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playRTC != null) playRTC.pause();
        // 화면 출력 pause
        if (localView != null) localView.pause();
        if (remoteView != null) remoteView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playRTC != null) playRTC.resume();
        // 화면 출력 resume
        if (localView != null) localView.resume();
        if (remoteView != null) remoteView.resume();
    }

    @Override
    protected void onDestroy() {
        Log.e(LOG_TAG, "onDestroy===============================");
        // The sdk 2.2.0 버전은 PlayRTCConfig 설정에서 내부 동작으로 지정.
        // PlayRTCConfig에서 사용 설정한 경우(USE_SDK2_2_0=true) pAudioManager 는  null value
        if (pAudioManager != null) {
            pAudioManager.close();
            pAudioManager = null;
        }

        this.finish();
        super.onDestroy();
    }

    /**
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리. 만약 채널에 입장한 상태이면 먼저 채널을 종료한다.
     */
    @Override
    public void onBackPressed() {
        Log.e(LOG_TAG, "onBackPressed===============================");
        // Activity를 종료하도록 isCloesActivity가 true로 지정되어 있다면 종료 처리
        if (isCloesActivity) {
            // BackPress 처리 -> onDestroy 호출
            Log.e(LOG_TAG, "super.onBackPressed()===============================");
            setResult(RESULT_OK, resultIntent);
            super.onBackPressed();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("PlayRTC");
            alert.setMessage("PlayRTC를 종료하겠습니까?");

            alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // 채널에 입장한 상태라면 채널을 먼저 종료한다.
                    // 종료 이벤트에서 isCloesActivity를 true로 설정하고 onBackPressed()를 호출하여
                    // Activity를 종료 처리
                    String userPid = playRTC.getPeerId();
                    if (TextUtils.isEmpty(userPid) == false) {
                        isCloesActivity = false;
                        playRTC.disconnectChannel(userPid);

                    }
                    // 채널에 입장한 상태가 아니라면 바로 종료 처리
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

    /**
     * 가로/세로 회전시 처리를 위한 Sample 코드
     * Sample에서는 가로모드 고정 이므로 사용 안됨.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        switch (this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                //Point displaySize = new Point();
                //displaySize.x = 가로크기;
                //displaySize.y = 세로크기;
                //localView.updateDisplaySize(displaySize);
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                //Point displaySize = new Point();
                //displaySize.x = 가로크기;
                //displaySize.y = 세로크기;
                //localView.updateDisplaySize(displaySize);
            }
            break;

            default:

        }
        super.onConfigurationChanged(newConfig);
    }


    /**
     * PlayRTCObserver 구현 Class
     * <pre>
     * - onConnectChannel : 채널 생성 또는 채널 입장 요청에 대한 응답
     * - onRing : ring 설정 시. 나중에 입장한 사용자가 번저 입장한 사용자에게 연결 요청을 보낸다.
     * - onReject : 상대방으로 부터 연결 거부 의사를 수신 함.
     * - onAccept : 상대방으로 부터 연결 수락 의사를 수신 함.
     * - onUserCommand : 상대방으로부터 User Defined Command를 수신
     * - onAddLocalStream : 단말기의 미디어 개체 생성 시 전달 받는다.
     * - onAddRemoteStream : 상대방의 미디어 개체를 전달 받는다.
     * - onAddDataStream : PlayRTCData을 전달 받는다.
     * - onDisconnectChannel : 채널이 종료 되거나, 내가 채널에서 퇴장할 때 호출
     * - onOtherDisconnectChannel  : 상대방이 채냘에서 퇴장할 때.
     * - onStateChange  : PlayRTC의 상태 변경 이벤트 처리
     * - onError : PlayRTC의 오류 발생 이벤트
     * </pre>
     */
    private class PlayRTCObserverImpl extends PlayRTCObserver {

        /**
         * 6. 채널 서비스에 채널 생성/입장 성공 후 PlayRTC Connect
         * 채널을 새로 생성하면 채널 아이디를 전달한다.
         *
         * @param obj       PlayRTC
         * @param channelId String, 새로 생성한 채널 아이디
         * @param reason    String, createChannel은 "create", connectChannel은 "connect"
         */
        @Override
        public void onConnectChannel(final PlayRTC obj, final String channelId, final String reason) {
            if (reason.equals("create")) {
                // 채널 팝업 뷰에 채널 아이디를 전달하여 화면에 표시
                channelInfoView.setChannelId(channelId);
                PlayRTCActivity.this.channelId = channelId;
            }
            // 1초 후 숨김
            channelInfoView.hide(1000);
        }

        /**
         * PlayRTCSettings Channel.ring = true 설정 시 나중에 채널에 입장한 사용자 측에서<br>
         * 연결 수락 의사를 물어옴 <br>
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         */
        @Override
        public void onRing(final PlayRTC obj, final String peerId, final String peerUid) {
            PlayRTCActivity.this.peerId = peerId;
            logView.appendLog(">>[" + peerId + "] onRing....");
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayRTCActivity.this);
            alert.setTitle("PlayRTC");
            alert.setMessage(peerId + "이 연결을 요청했습니다.");

            alert.setPositiveButton("연결", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    Utils.showToast(PlayRTCActivity.this, "[" + peerId + "] accept....");
                    logView.appendLog(">>[" + peerId + "] accept....");

                    playRTC.accept(peerId);
                }
            });
            alert.setNegativeButton("거부", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    Utils.showToast(PlayRTCActivity.this, "[" + peerId + "] reject....");
                    logView.appendLog(">>[" + peerId + "] reject....");
                    playRTC.reject(peerId);
                }
            });
            alert.show();
        }

        /**
         * 상대방으로 부터 연결 수락 의사를 수신 함.
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         */
        @Override
        public void onReject(final PlayRTC obj, final String peerId, final String peerUid) {
            Utils.showToast(PlayRTCActivity.this, "[" + peerId + "] onReject....");
            logView.appendLog(">>[" + peerId + "] onReject....");
        }

        /**
         * 상대방으로 부터 연결 거부 의사를 수신 함.
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         */
        @Override
        public void onAccept(final PlayRTC obj, final String peerId, final String peerUid) {
            Utils.showToast(PlayRTCActivity.this, "[" + peerId + "] onAccept....");
            logView.appendLog(">>[" + peerId + "] onAccept....");
        }

        /**
         * 상대방으로부터 User Defined Command를 받은 처리는 각자 알아서
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         * @param data    String, 상대방이 전달한 문자열
         */
        @Override
        public void onUserCommand(final PlayRTC obj, final String peerId, final String peerUid, final String data) {
            Utils.showToast(PlayRTCActivity.this, "[" + peerId + "] onCommand....");
            logView.appendLog(">>[" + peerId + "] onCommand[" + data + "]");
        }


        /**
         * 7. 로컬 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
         * 로컬 미디어 PlayRTCMedia객체 전달 <br>
         * PlayRTCMedia객체를 전달 받으면 이벤트 처리를 위해 PlayRTCMediaEvent객체(dataObserver)를 등록<br>
         *
         * @param obj   PlayRTC
         * @param media PlayRTCMedia, PlayRTCVideoRenderer를 등록하여 화면 출력 처리
         */
        @Override
        public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia media) {
            Log.e(LOG_TAG, "onMedia onAddLocalStream==============");
            // 영상뷰가 있는지 검사.
            if (localView == null) {
                return;
            }
            Point screenDimensions = new Point();
            screenDimensions.x = videoArea.getWidth();
            screenDimensions.y = videoArea.getHeight();

            //PlayRTCVideoRenderer를 등록하여 화면 출력 처리
            logView.appendLog(">> onLocalStream...");
            localMedia = media;
            localMedia.setVideoRenderer(localView.getVideoRenderer());
            localView.show(200);
        }

        /**
         * 8. P2P 연결 시 상대방 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
         * 상대방 미디어 PlayRTCMedia객체 전달 <br>
         * PlayRTCMedia객체를 전달 받으면 이벤트 처리를 위해 PlayRTCMediaEvent객체(dataObserver)를 등록<br>
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         * @param media   PlayRTCMedia, PlayRTCVideoRenderer를 등록하여 화면 출력 처리
         */
        @Override
        public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUid, final PlayRTCMedia media) {
            Log.e(LOG_TAG, "onMedia onAddRemoteStream==============");
            // 영상뷰가 있는지 검사.
            if (remoteView == null) {
                return;
            }
            Point screenDimensions = new Point();
            screenDimensions.x = videoArea.getWidth();
            screenDimensions.y = videoArea.getHeight();

            // PlayRTCVideoRenderer를 등록하여 화면 출력 처리
            logView.appendLog(">> onRemoteStream[" + peerId + "]...");
            remoteMedia = media;
            remoteMedia.setVideoRenderer(remoteView.getVideoRenderer());
            remoteView.show(200);
        }

        /**
         * 9. Data 송수신을 위한  PlayRTCData 수신 이벤트 처리 -> 데이터 채널 사용 설정 시
         * Data-Channel이 생성되면 전달, PlayRTCData(DataChannel) 객체 전달
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         * @param data    PlayRTCData
         */
        @Override
        public void onAddDataStream(final PlayRTC obj, final String peerId, final String peerUid, final PlayRTCData data) {
            PlayRTCActivity.this.peerId = peerId;
            logView.appendLog(">> onDataStream[" + peerId + "]...");
            // PlayRTCDataObserver를 등록한다.
            dataHandler.setDataChannel(data);
        }

        /**
         * 채널이 종료 되거나, 내가 채널에서 퇴장할 때 호출<br>
         * deleteChannel을 호출하거나, 내가 disconnectChannel을 호출하면 발생한다.<br>
         *
         * @param obj    PlayRTC
         * @param reason String, deleteChannel인 경우 "delete", disconnectChannel인경우 "disconnect"
         */
        @Override
        public void onDisconnectChannel(final PlayRTC obj, final String reason) {
            if (reason.equals("disconnect")) {
                Utils.showToast(PlayRTCActivity.this, "채널에서 퇴장하였습니다....");
                logView.appendLog(">>PlayRTC 채널에서 퇴장하였습니다....");
            } else {
                Utils.showToast(PlayRTCActivity.this, "채널이 종료되었습니다....");
                logView.appendLog(">>PlayRTC 채널이 종료되었습니다....");
            }
            isCloesActivity = true;
            onBackPressed();
        }

        /**
         * 상대방이 채널에서 퇴장할 때.<br>
         * 상대가 disconnectChannel을 호출.
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         */
        @Override
        public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUid) {
            Utils.showToast(PlayRTCActivity.this, "[" + peerId + "]가 채널에서 퇴장하였습니다....");
            logView.appendLog("[" + peerId + "]가 채널에서 퇴장하였습니다....");
        }

        /**
         * PlayRTC의 상태 변경 이벤트 처리, PlayRTC의 enum 코드 참고
         *
         * @param obj     PlayRTC
         * @param peerId  String, 상대방 사용자의 peer 아이디
         * @param peerUid String, 상대방 사용자의 아이디
         * @param status  PlayRTCStatus PlayRTC의 상태 변경 코드 참고
         * @param desc    String, Description
         */
        @Override
        public void onStateChange(final PlayRTC obj, String peerId, final String peerUid, PlayRTCStatus status, String desc) {
            //Utils.showToast(PlayRTCActivity.this, peerId+"  Status["+ status+ "]...");
            logView.appendLog(">>" + peerId + "  onStatusChange[" + status + "]...");

            // v2.2.1
            if(status == PlayRTCStatus.PeerSuccess) {
                obj.startStatsReport(5000L, new PlayRTCStatsReportObserver(){
                    @Override
                    public void onStatsReport(PlayRTCStatsReport report) {
                        /**
                         * RatingValue
                         * int getLevel()
                         * float getValue()
                         */
                        RatingValue localVideoFl = report.getLocalVideoFractionLost();
                        RatingValue localAudioFl = report.getLocalAudioFractionLost();
                        RatingValue remoteVideoFl = report.getRemoteVideoFractionLost();
                        RatingValue remoteAudioFl = report.getRemoteAudioFractionLost();

                        String localReport = String.format("Local Report\n   ICE:%s\n   Frame:%sx%sx%s\n   Bandwidth[%sps]\n   RTT[%s]\n   RttRating[%d/%.4f]\n   VFractionLost[%d/%.4f]\n   AFractionLost[%d/%.4f]",
                                report.getLocalCandidate(),
                                report.getLocalFrameWidth(),
                                report.getLocalFrameHeight(),
                                report.getLocalFrameRate(),
                                android.text.format.Formatter.formatFileSize(getApplicationContext(), report.getAvailableSendBandwidth()) + "",
                                report.getRtt(),
                                report.getRttRating().getLevel(),
                                report.getRttRating().getValue(),
                                localVideoFl.getLevel(),
                                localVideoFl.getValue(),
                                localAudioFl.getLevel(),
                                localAudioFl.getValue());

                        String remoteReport = String.format("Remote Report\n   ICE:%s\n   Frame:%sx%sx%s\n   Bandwidth[%sps]\n   VFractionLost[%d/%.4f]\n   AFractionLost[%d/%.4f]\n",
                                report.getRemoteCandidate(),
                                report.getRemoteFrameWidth(),
                                report.getRemoteFrameHeight(),
                                report.getRemoteFrameRate(),
                                android.text.format.Formatter.formatFileSize(getApplicationContext(), report.getAvailableReceiveBandwidth()) + "",
                                remoteVideoFl.getLevel(),
                                remoteVideoFl.getValue(),
                                remoteAudioFl.getLevel(),
                                remoteAudioFl.getValue());


                        Log.d("StatsReport", "-----------------------------------------------------");
                        Log.d("StatsReport", localReport);
                        Log.d("StatsReport", remoteReport);
                        Log.d("StatsReport", "-----------------------------------------------------");
                    }
                });
            }
        }

        /**
         * PlayRTC의 오류 발생 이벤트 처리, PlayRTC의 enum 코드 참고
         *
         * @param obj     PlayRTC
         * @param status  PlayRTCStatus PlayRTC의 상태 변경 코드 참고
         * @param code    PlayRTCCode PlayRTC의 오류 코드 참고
         * @param desc    String, Description
         */
        @Override
        public void onError(final PlayRTC obj, PlayRTCStatus status, PlayRTCCode code, String desc) {
            Utils.showToast(PlayRTCActivity.this, "Error[" + code + "] Status[" + status + "] " + desc);
            logView.appendLog(">>" + peerId + "  onError[" + code + "] Status[" + status + "] " + desc);
        }
    }


    /**
     * MainActivity에서 선택한 Sample Type에 맞게 PlayRTCSettings 개체 생성
     * <pre>
     * 1. new PlayRTCSettings
     * 2. TDCProjectId/TDCLicense set
     * 3.
     * 4. Ring : false 연결 수립 여부를 상대방에게 묻ㅈ; 않음
     * 5. Audio/Video/Data Enable runType타입에 따라 지정
     * 6. setLevel
     * </pre>
     *
     * @param runType int
     * @return
     */
    private PlayRTCSettings createPlayRTCSettings(int runType) {
		/* PlayRTC 서비스 설정 */
        PlayRTCSettings settings = new PlayRTCSettings();
        settings.android.setContext(this.getApplicationContext());
        settings.setTDCProjectId(TDCProjectId);
        settings.setTDCLicense(TDCLicense);
		
		 /* ring, 연결 수립 여부를 상대방에게 묻는지 여부를 지정, true면 상대의 수락이 있어야 연결 수립 진행  */
        settings.channel.setRing(false);

        settings.constraints.setVideoFrame(PlayRTCFrame.P640x480Frame);

        // 양상 + 음성 + Data
        if (runType == 1) {
            settings.setAudioEnable(true);   /* 음성 전송 사용 */
            settings.setVideoEnable(true);   /* 영상 정송 사용 */
            settings.setDataEnable(true);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */

            settings.video.setFrontCameraEnable(true);
            settings.video.setBackCameraEnable(false);

        }
        // 영상 + 음성
        else if (runType == 2) {
            settings.setAudioEnable(true);   /* 음성 전송 사용 */
            settings.setVideoEnable(true);   /* 영상 정송 사용 */
            settings.setDataEnable(false);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */

            settings.video.setFrontCameraEnable(true);
            settings.video.setBackCameraEnable(false);

        }
        // 음성 only
        else if (runType == 3) {
            settings.setAudioEnable(true);   /* 음성 전송 사용 */
            settings.setVideoEnable(false);   /* 영상 정송 사용 */
            settings.setDataEnable(false);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */

            settings.video.setFrontCameraEnable(false);
            settings.video.setBackCameraEnable(false);
        }
        // Data Only
        else {
            settings.setAudioEnable(false);   /* 음성 전송 사용 */
            settings.setVideoEnable(false);   /* 영상 정송 사용 */
            settings.setDataEnable(true);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
        }

        // 여기서 지정한 사용자 turn을 사용한다.
        //settings.iceServers.add("turn:IP:PORT", "USERID", "PASSWD");

        /**
         * SDK Console 로그 레벨 지정
         */
        settings.log.console.setLevel(CONSOLE_LOG);
		
		/* SDK 파일 로그 레벨 지정 */
        settings.log.file.setLevel(FILE_LOG);
		/* 파일 로그를 남기려면 로그파일 폴더 지정 . [PATH]/yyyyMMdd.log , 10일간 보존 */
		/* SDK 파일 로깅을 위한 로그 파일 경로, 파일 로깅을 사용하지 않는다면 Pass */
        File logPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/log");
        File cachePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/cache");
        settings.log.file.setLogPath(logPath.getAbsolutePath());

		/* 서버 로그 전송 실패 시 재 전송 지연 시간, msec  */
        settings.log.setCachePath(cachePath.getAbsolutePath());
        settings.log.setRetryQueueDelays(1000);
		/* 서버 로그 재 전송 실패시 로그 DB 저장 후 재전송 시도 지연 시간, msec */
        settings.log.setRetryCacheDelays(10 * 1000);

        return settings;
    }

    private PlayRTCConfig createPlayRTCConfig(int runType) {
        /* PlayRTC 서비스 설정 */
        PlayRTCConfig config = PlayRTCFactory.createConfig();
        config.setAndroidContext(this.getApplicationContext());
        config.setProjectId(TDCProjectId);

        config.setRingEnable(false);

        // 양상 + 음성 + Data
        if(runType == 1) {
            config.video.setEnable(true);
            //전방 카메라 사용
            config.video.setCameraType(CameraType.Front);
            // video 해상도는 기본 640x480,
            // min - max 범위를 다르게 지정하면 내부적으로 max 해상도 사용
            config.video.setMaxFrameSize(640, 480);
            config.video.setMinFrameSize(640, 480);

            config.audio.setEnable(true);   /* 음성 전송 사용 */
            //SDK 2.2.0에서는 PlayRTCAudioManager를 사용하도록 설정할수 있음.
            // false로 지정하면 sdk 2.1.2에서 사용하는 방법으로 구현해야함.
            config.audio.setAudioManagerEnable(true);
            config.data.setEnable(true);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */

        }
        // 영상 + 음성
        else if(runType == 2){
            config.video.setEnable(true);
            //전방 카메라 사용
            config.video.setCameraType(CameraType.Front);
            // video 해상도는 기본 640x480,
            // min - max 범위를 다르게 지정하면 내부적으로 max 해상도 사용
            config.video.setMaxFrameSize(640, 480);
            config.video.setMinFrameSize(640, 480);

            config.audio.setEnable(true);   /* 음성 전송 사용 */

            //SDK 2.2.0에서는 PlayRTCAudioManager를 사용하도록 설정할수 있음.
            // false로 지정하면 sdk 2.1.2에서 사용하는 방법으로 구현해야함.
            config.audio.setAudioManagerEnable(true);

            config.data.setEnable(false);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */


        }
        // 음성 only
        else if(runType == 3){
            // video  전송 안함.
            config.video.setEnable(false);
            // audio 전송 사용.
            config.audio.setEnable(true);

            //SDK 2.2.0에서는 PlayRTCAudioManager를 사용하도록 설정할수 있음.
            // false로 지정하면 sdk 2.1.2에서 사용하는 방법으로 구현해야함.
            config.audio.setAudioManagerEnable(true);

            config.data.setEnable(false);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */


        }
        // Data Only
        else {
            config.video.setEnable(false);  /* 영상 전송 안함 */
            config.audio.setEnable(false);   /* 음성 전송 안함 */
            config.data.setEnable(true);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
        }

        /**
         * SDK Console 로그 레벨 지정
         */
        config.log.console.setLevel(PlayRTCConfig.WARN);

		/* SDK 파일 로그 레벨 지정 */
        config.log.file.setLevel(PlayRTCConfig.WARN);
		/* 파일 로그를 남기려면 로그파일 폴더 지정 . [PATH]/yyyyMMdd.log , 10일간 보존 */
		/* SDK 파일 로깅을 위한 로그 파일 경로, 파일 로깅을 사용하지 않는다면 Pass */
        File logPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/log");
        config.log.file.setLogPath(logPath.getAbsolutePath());
        config.log.file.setRolling(10); // 10일 유지


        return config;
    }
    /**
     * 5. 채널 서비스에 채널 생성/입장 요청
     * 채널 팝업 뷰의 PlayRTCChannelViewListener를 구현한 리스너 구현 Class<br>
     * <pre>
     * - onClickCreateChannel : 채널 생성 버튼을 늘렀을 때
     * - onClickConnectChannel : 채널 입장 보튼을 눌렀을때
     * </pre>
     *
     * @see com.playrtc.sample.view.PlayRTCChannelView.PlayRTCChannelViewListener
     */
    private class PlayRTCChannelViewListener implements PlayRTCChannelView.PlayRTCChannelViewListener {

        /**
         * 채널 생성 버튼을 늘렀을 때 <br>
         *
         * @param channelName String, 채널 이름
         * @param userId      String, 사용자 Application 사용자 아이디
         * @param userName    String, 사용자 이름
         */
        @Override
        public void onClickCreateChannel(String channelName, String userId, String userName) {
            Log.d(LOG_TAG, "onClickCreateChannel channelName[" + channelName + "] userId[" + userId + "] userName[" + userName + "]");

            // 채널방 정보 생성
            JSONObject parameters = new JSONObject();

            if (TextUtils.isEmpty(channelName) == false) {
                // 채널정보를 정의한다.
                JSONObject channel = new JSONObject();
                try {
                    // 채널에 대한 이름을 지정한다.
                    channel.put("channelName", channelName);
                    parameters.put("channel", channel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (TextUtils.isEmpty(userId) == false || TextUtils.isEmpty(userName) == false) {
                // 채널 사용자에 대한 정보를 정의한다.
                JSONObject peer = new JSONObject();
                try {
                    if (TextUtils.isEmpty(userId) == false) {
                        // application에서 사용하는 사용자 아이디를 지정
                        peer.put("uid", userId);
                    }
                    if (TextUtils.isEmpty(userName) == false) {
                        // 사용자에 대한 별칭을 지정한다.
                        peer.put("userName", userName);
                    }
                    parameters.put("peer", peer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.d(LOG_TAG, "playRTC.createChannel " + parameters.toString());
            try {
				/*
				 * PlayRTC 플랫폼 채널 서비스에 새로 채널을 생성한다.<br>
				 * 채널 생성 시 채널아이디와 서비스 설정 정보, 사용자 토큰을 전달 받아 채널에 입장한다.<br>
				 * 
				 *  @param parameters JSONObject, 생성하는 채널및 peer에 유지할 부가 데이터 항목을 전달, 데이터는 채널이 close 될때 까지 유지된다. 
				 * <pre>
				 * - channel JSONObject, 채널에 대한 부가 정보 
				 *   - channelName String, 채널에 대한 이름
				 * - peer JSONObject, peer(사용자)에 대한 부가 정보 
				 *   - userId String, User에 대한 ID로  User에 대한 ID로 application에서 사용하는 사용자 아이디
				 *   - userName String, User 이름
				 * </pre>
				 * @throws com.sktelecom.playrtc.exception.RequiredConfigMissingException
				 */
                playRTC.createChannel(parameters);
            } catch (RequiredConfigMissingException e) {
                e.printStackTrace();
            }
        }

        /**
         * 채널 입장 보튼을 눌렀을 때 <br>
         *
         * @param channelId String, 입장할 채널의 아이디
         * @param userId    String, 사용자 Application 사용자 아이디
         * @param userName  String, 사용자 이름
         */
        @Override
        public void onClickConnectChannel(String channelId, String userId, String userName) {
            Log.d("LOG_TAG", "onConnectChannel channelId[" + channelId + "] userId[" + userId + "] userName[" + userName + "]");
            JSONObject parameters = new JSONObject();
            PlayRTCActivity.this.channelId = channelId;
            if (TextUtils.isEmpty(userId) == false || TextUtils.isEmpty(userName) == false) {

                JSONObject peer = new JSONObject();
                try {
                    if (TextUtils.isEmpty(userId) == false) {
                        peer.put("uid", userId);
                    }
                    if (TextUtils.isEmpty(userName) == false) {
                        peer.put("userName", userName);
                    }
                    parameters.put("peer", peer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("LOG_TAG", "onConnectChannel call playRTC.createChannel(" + channelId + ", parameters)");
            try {
				/*
				 * PlayRTC 플랫폼 채널 서비스에 생성 되어 있는 채널에 입장한다.<br>
				 * 채널아이디와 서비스 설정 정보, 사용자 토큰을 전달 받아 채널에 입장한다.<br>
				 * @param channelId String, PlayRTC 플랫폼 채널 서비스의 채널 아이디 
				 * @param parameters JSONObject, 사용자 관련 부가 정보를 전달할 수 있다. 
				 * <pre>
				 * - peer JSONObject, peer(사용자)에 대한 부가 정보 
				 *   - userId String, User에 대한 ID로  User에 대한 ID로 application에서 사용하는 사용자 아이디
				 *   - userName String, User 이름 
				 * </pre>
				 * @throws com.sktelecom.playrtc.exception.RequiredConfigMissingException
				 */
                playRTC.connectChannel(channelId, parameters);
            } catch (RequiredConfigMissingException e) {

                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private void initUIControls() {

        this.channelInfoView = (PlayRTCChannelView) findViewById(R.id.channel_info);

		/*video 스트림 출력을 위한 PlayRTCVideoView의 부모 ViewGroup */
        this.videoArea = (RelativeLayout) findViewById(R.id.videoarea);
        this.videoArea.setBackgroundColor(Color.WHITE);
		
		/* 로그 출력 TextView */
        logView = (PlayRTCLogView) this.findViewById(R.id.logtext);
		
		/* 채널 정보 팝업 뷰 토글 버튼 이벤트 처리 */
        channelInfoView.setTargetButton((Button) this.findViewById(R.id.btn_channel));
        Button cameraBtn = (Button) this.findViewById(R.id.btn_switch_camera);
		
		/* 카메라 전환 */
        if (playrtcType == 1 || playrtcType == 2) {
            cameraBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playRTC.switchVideoCamera();
                }
            });
        } else {
            // 영상 전송을 사용하지 않으므로 화면에서 숨긴다.
            cameraBtn.setVisibility(View.INVISIBLE);
        }
		
		/* DataChannel Text 전송 버튼 */
        Button btnText = (Button) this.findViewById(R.id.btn_text);
		/* DataChannel Binary 전송 버튼 */
        Button btnBinary = (Button) this.findViewById(R.id.btn_binary);
		/* DataChannel 파일 전송 버튼 */
        Button btnFile = (Button) this.findViewById(R.id.btn_file);
        if (playrtcType == 1 || playrtcType == 4) {
            btnText.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataHandler.sendText();
                }
            });
            btnBinary.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataHandler.sendBinary();
                }
            });
            btnFile.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataHandler.sendFile();
                }
            });
        } else {
            // DataChannel을 사용하지 않으므로 화면에서 숨긴다.
            btnText.setVisibility(View.INVISIBLE);
            btnBinary.setVisibility(View.INVISIBLE);
            btnFile.setVisibility(View.INVISIBLE);
        }
		
		/* 로그뷰  토글 버튼 이벤트 처리 */
        Button btnLog = (Button) this.findViewById(R.id.btn_log);
        if (playrtcType == 1 || playrtcType == 2) {
            btnLog.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    if (logView.isShown() == false) {
                        logView.show();
                        ((Button) v).setText("로그닫기");
                    } else {
                        logView.hide();
                        ((Button) v).setText("로그보기");
                    }
                }
            });
        } else {
            btnLog.setVisibility(View.GONE);
            logView.show();
        }
		
		/* Peer 채널 퇴장 버튼 */
        btnDisconnectChannel = (Button) this.findViewById(R.id.btn_peerChClose);
        btnDisconnectChannel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playRTC != null) {
					/*
					 * PlayRTC 플랫폼 채널을 종료한다.
					 * 채널 종료를 호출하면 채널에 있는 모든 사용자에게 onDisconnectChannel이 호출된다.
					 */
                    playRTC.deleteChannel();
                }
            }
        });
		
	
		/* Local Video Mute 버튼 */
        Button btnMuteLVideo = (Button) this.findViewById(R.id.btn_local_vmute);
		/* Local Video Mute 처리시 로컬 영상 스트림은 화면에 출력이 안되며 상대방에게 전달이 되지 않는다. */
        btnMuteLVideo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (localMedia != null) {
                    String text = (String) b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    localMedia.setVideoMute(setMute);
                    b.setText((setMute == true) ? "VIDEO-ON" : "VIDEO-OFF");
                }
            }
        });
		
		/* Local Audio Mute 버튼 */
        Button btnMuteLAudio = (Button) this.findViewById(R.id.btn_local_amute);
		/* Local Audio Mute 처리시 로컬 음성 스트림은 상대방에게 전달이 되지 않는다. */
        btnMuteLAudio.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (localMedia != null) {
                    String text = (String) b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    localMedia.setAudioMute(setMute);
                    b.setText((setMute == true) ? "AUDIO-ON" : "AUDIO-OFF");
                }
            }
        });
		
		/* Remote Video Mute 버튼 */
        Button btnMuteRVideo = (Button) this.findViewById(R.id.btn_remote_vmute);
		/* Remote Video Mute 처리시 상대방 영상 스트림은 화면에만 출력이 안된다. */
        btnMuteRVideo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (remoteMedia != null) {
                    String text = (String) b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    remoteMedia.setVideoMute(setMute);
                    b.setText((setMute == true) ? "VIDEO-ON" : "VIDEO-OFF");
                }
            }
        });
		
		/* Remote Audio Mute 버튼 */
        Button btnMuteRAudio = (Button) this.findViewById(R.id.btn_remote_amute);
		/* Remote Video Mute 처리시 상대방 영상 스트림은 소리만 출력이 안된다. */
        btnMuteRAudio.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (remoteMedia != null) {
                    String text = (String) b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    remoteMedia.setAudioMute(setMute);
                    b.setText((setMute == true) ? "AUDIO-ON" : "AUDIO-OFF");
                }
            }
        });
    }
}
