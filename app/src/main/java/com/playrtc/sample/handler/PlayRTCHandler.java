package com.playrtc.sample.handler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import com.playrtc.sample.PlayRTCActivity;
import com.playrtc.sample.util.Utils;
import com.playrtc.sample.view.PlayRTCVideoViewGroup;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTC.PlayRTCCode;
import com.sktelecom.playrtc.PlayRTC.PlayRTCStatus;
import com.sktelecom.playrtc.PlayRTC.PlayRTCWhiteBalance;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.config.PlayRTCAudioConfig;
import com.sktelecom.playrtc.config.PlayRTCAudioConfig.AudioCodec;
import com.sktelecom.playrtc.config.PlayRTCConfig;
import com.sktelecom.playrtc.config.PlayRTCVideoConfig;
import com.sktelecom.playrtc.config.PlayRTCVideoConfig.VideoCodec;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;
import com.sktelecom.playrtc.stream.PlayRTCData;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.PlayRTCRange;

import org.json.JSONObject;

import java.io.File;


/*
 * PlayRTC 인스턴스를 생성하고, PlayRTC 객체의 이벤트 처리를 위해 PlayRTCObserver 인터페이스를
 * 구현한 PlayRTC-Handler Class
 *
 *
 * PlayRTC 구현 방법
 * 1. PlayRTCConfig 생성
 *    PlayRTCFactory.createConfig()
 *
 * 2. PlayRTC 인스턴스를 생성
 *   PlayRTCConfig, PlayRTCObserver 구현체 전달
 *   PlayRTCFactory#newInstance(PlayRTCConfig config, PlayRTCObserver observer)
 *
 * 3. 영상 출력을 위한 PlayRTCVideoView  초기화
 *   - xml layout을 사용하지 않는경우 소스 코드에서 직접 생성
 *     PlayRTCVideoView 화면 사이즈 계산을 위해 화면 사이즈를 획득할 수 있는 PlayRTCActivity#onWindowFocusChanged 에서 생성
 *     PlayRTCVideoViewGroup#createVideoView()
 *   - xml layout을 사용하는 경우
 *     PlayRTCVideoView 화면 사이즈 계산을 위해 화면 사이즈를 획득할 수 있는 PlayRTCActivity#onWindowFocusChanged 에서 생성
 *     PlayRTCVideoViewGroup#initVideoView()
 *
 *
 * 4. 채널 서비스에 채널 생성/입장 요청 -> PlayRTCChannelView 팝업에서 채널 생성 또는 입장 버튼 리스너 PlayRTCChannelViewListener 구현
 *   PlayRTCChannelViewListener#onClickCreateChannel
 *     playRTC.createChannel(parameters)
 *   PlayRTCChannelViewListener#onClickConnectChannel
 *     playRTC.connectChannel(parameters)
 *
 * 5. 채널 서비스에 채널 생성/입장 성공 후 PlayRTC Connect 직전 이벤트 전달
 *   PlayRTCObserver#onConnectChannel(PlayRTC obj, final String channelId, final String reason)
 *
 * 6. 로컬 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
 *   PlayRTCObserver#onAddLocalStream
 *   PlayRTCMedia 수신 시 영상 출력을 이해 PlayRTCVideoView의 renderer 인터페이스 등록
 *     PlayRTCMedia#setVideoRenderer(PlayRTCVideoView의#getVideoRenderer());
 *
 * 7. P2P 연결 시 상대방 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
 *   PlayRTCObserver#onAddRemoteStream
 *   PlayRTCMedia 수신 시 영상 출력을 이해 PlayRTCVideoView의 renderer 인터페이스 등록
 *     PlayRTCMedia#setVideoRenderer(PlayRTCVideoView의#getVideoRenderer());
 *
 * 8. Data 송수신을 위한  PlayRTCData 수신 이벤트 처리 -> 데이터 채널 사용 설정 시
 *   PlayRTCObserver#onAddDataStream
 *   PlayRTCData 수신 시 수신 이벤트 처리를 이해 PlayRTCDataObserver를 구현한 PlayRTCDataChannelHandler 등록
 *     PlayRTCDataChannelHandler#setDataChannel(data);
 *
 * 9. 상대방 채널 퇴장 이벤트 처리
 *   사용자가  PlayRTC#disconnectChannel을 호출하면 상대방에게 onOtherDisconnectChannel 호출됨.
 *   본인은 onDisconnectChannel 호출
 *   PlayRTCObserver#onOtherDisconnectChannel
 *
 *
 * 10. 채널 종료 이벤트 처리
 *   PlayRTC#deleteChannel을 호출하면 채널의 모든 사용자에게 채널 종료 이벤트가 전달
 *   PlayRTCObserver#onDisconnectChannel
 *
 * 11. 종료 처리
 *   Back 키 또는 종료 버튼을 누르면 PlayRTC#deleteChannel 또는 PlayRTC#disconnectChannel을 호출
 *   PlayRTCObserver#onDisconnectChannel에서 화면 종료 처리
 *
 *
 * 주요 멤버
 *
 * - PlayRTC playrtc
 *     PlayRTC 인스턴스
 *
 * - PlayRTCMedia localMedia
 *     로컬(단말기) 카메라 영상 스트림 객체
 *
 * - PlayRTCMedia remoteMedia
 *     상대방(Remote) 카메라 영상 스트림 객체
 *
 * - PlayRTCStatsReportHandler statReportHandler
 *     P2P연결 수립 후 PlayRTC의 P2P 상태 정보를 제공하기 위한 Report 객체
 *     PlayRTCObserver#onStateChange에서 상태 이벤트 PlayRTCStatus.PeerSuccess가 발생하면 구동 시킨다.
 *
 * - PlayRTCLogView logView
 *     PlayRTC 로그를 출력하기위해 TextView를 확장한 Class
 *
 * 주요 Method
 * - public void createPlayRTC();
 *   SDK 설정 객체인 PlayRTCConfig를 생성한 후  PlayRTC 인스턴스를 생성.
 *
 * - void createChannel(JSONObject parameters);
 *   PlayRTC 인스턴스 후 PlayRTC 채널 서비스에 P2P 채널 생성을 요청하고 생성된 채널에 입장한다.
 *   P2P 상대방은 생성된 채널에 connectChannel메소드를 이용하여 입장한다.
 *   채널이 생성되면 채널서비스의 채널 아이디를 전달한다.
 *
 * - void connectChannel(String channelId, JSONObject parameters);
 *   채널 서비스에 샹성된 P2P 채널에 입장하여 P2P 연결 수립을 시작한다.
 *
 * - PlayRTC getPlayRTC();
 *   PlayRTC 인스턴스를 반환한다.
 *
 * - void onActivityPause();
 *   Activity가 onPause 또는 onStop 시 호출하여 PlayRTC의 스트리밍 처리를 Pause 시킨다.
 *
 * - void onActivityResume();
 *   Activity가 onResume 또는 onStart 시 호출하여 PlayRTC의 스트리밍 처리를 Resume 시킨다.
 *
 * - void close();
 *   P2P가 종료 되어 객체를 해제할 경우(P2P 종료, Activity Destroy) 호출한다.
 *
 * - void disconnectChannel();
 *   PlayRTC 플랫폼 채널을 종료한다.
 *   호출 성공 시 채널을 퇴장하며, 채널에 있는 다른 사용자는 onOtherDisconnectChannel이 호출된다.
 *
 * - void delateChannel();
 *   PlayRTC 플랫폼 채널을 종료한다.
 *   채널 종료를 호출하면 채널에 있는 모든 사용자는 onDisconnectChannel이 호출된다.
 *
 */
public class PlayRTCHandler extends PlayRTCObserver {

    private static final String LOG_TAG = "PLAYRTC";

    /**
     * PlayRTC SDK 콘솔 Log 레벨 정의. WARN
     */
    private static final int CONSOLE_LOG = PlayRTCConfig.DEBUG;

    /**
     * PlayRTC SDK 파일 Log 레벨 정의. WARN
     */
    private static final int FILE_LOG = PlayRTCConfig.WARN;

    /**
     * PlayRTC 서비스에 생성한 프로젝트의 고유 아이디.
     * Sample 테스트용으로 생성한 프로젝트 아이디를 사용. 실제 앱 서비스 개발 시 프로젝트 아이디를 사용해야 함.
     */
    private static final String TDCProjectId = "60ba608a-e228-4530-8711-fa38004719c1"; // playrtc

    /**
     * Activity 인스턴스. <br>
     * PlayRTCActivity 인스턴스를 이용하여 이용해 객체 간 인터페이스 사용
     */
    private PlayRTCActivity activity = null;

    /**
     * PlayRTC 인스턴스
     */
    private PlayRTC playrtc = null;
    /**
     *  로컬 PlayRTCMedia 전역 변수
     */
    private PlayRTCMedia localMedia = null;
    /**
     *  상대방 PlayRTCMedia 전역 변수
     */
    private PlayRTCMedia remoteMedia = null;

    /**
     * P2P연결 수립 후 PlayRTC의 P2P 상태 정보를 제공하기 위한 Report 객체
     */
    private PlayRTCStatsReportHandler statReportHandler = null;

    /**
     * 채널 서비스에 생성된 P2P 채널의 아이디
     */
    private String channelId = null;

    /**
     * 채널 서비스에서 발급한 P2P 채널 임시 사용자 고유 아이디.
     */
    private String userPid = null;

    /**
     * 채널 서비스에서 발급한 상대방의 P2P 채널 임시 사용자 고유 아이디.
     */
    private String peerId = null;

    /**
     * 채널 서비스 연결 여부.
     * 채널 연결 여부를 체크하는 이유는 Acticity 종료 시 채널 서비스 연결을 확인하고 연결을 해제하기 위해서 임.
     * 채널 서비스 연결을 해제하면 PlayRTC는 내부 인스턴스를 해제한다.
     */
    private boolean isChannelConnected = false;

    public PlayRTCHandler(PlayRTCActivity activity) {

        this.activity = activity;

        // P2P연결 수립 후 PlayRTC의 P2P 상태 정보를 제공하기 위한 Report 객체 생성
        statReportHandler = new PlayRTCStatsReportHandler(activity);
    }

    /**
     * SDK 설정 객체인 PlayRTCConfig를 생성한 후 PlayRTC 인스턴스를 생성.
     * @param runType int
     *  - 1. 영상, 음성, p2p data
     *  - 2. 영상, 음성
     *  - 3. 음성, data
     *  - 4. p2p data only
     * @throws UnsupportedPlatformVersionException Android SDK 버전 체크 Exception
     * @throws RequiredParameterMissingException 필수 Parameter 체크 Exception
     */
    public void createPlayRTC(int runType, String channelRing, String videoCodec, String audioCodec) throws UnsupportedPlatformVersionException, RequiredParameterMissingException {

        // PlayRTC 서비스 설정 객체를 생성하여 반환
        PlayRTCConfig config = createPlayRTCConfig(runType, channelRing, videoCodec, audioCodec);

		/*
		 * PlayRTC 인터페이스를 구현한 객체 인스턴스를 생성하고 PlayRTC를 반환한다. static
		 *
		 * @param settings PlayRTCSettings, PlayRTC 서비스 설정 정보 객체
		 * @param observer PlayRTCObserver, PlayRTC Event 리스너
		 * @return PlayRTC
		 */
        playrtc =  PlayRTCFactory.createPlayRTC(config, (PlayRTCObserver) this);

    }

    /**
     * PlayRTC 인스턴스 후 PlayRTC 채널 서비스에 P2P 채널 생성을 요청하고 생성된 채널에 입장한다.<br>
     * P2P 상대방은 생성된 채널에 connectChannel메소드를 이용하여 입장한다. <br>
     * 채널이 생성되고 사용자가 등록 되면 채널서비스의 채널 아이디를 PlayRTCObserver#onConnectChannel을 호출하여 전달한다.
     * @param parameters JSONObject, 생성하는 채널 및 peer 데이터 항목을 전달, 데이터는 채널이 close 될때 까지 유지된다.
     *  - channel JSONObject, 채널에 대한 부가 정보
     *    - channelName String, 채널에 대한 이름
     *  - peer JSONObject, peer(사용자)에 대한 부가 정보
     *    - userId String, User에 대한 ID로 application에서 사용하는 사용자 아이디
     *    - userName String, User 이름
     * @throws RequiredConfigMissingException 필수 Parameter 체크 Exception
     */
    public void createChannel(JSONObject parameters) throws RequiredConfigMissingException {

        playrtc.createChannel(parameters);
    }

    /**
     * 채널 서비스에 생성된 P2P 채널에 입장하여 P2P 연결 수립을 시작한다.<br>
     * 채널에 사용자가 등록 되면 PlayRTCObserver#onConnectChannel을 호출한다.<br>
     * @param channelId String, 채널 서비스에 생성된 채널의 아이디
     * @param parameters JSONObject, 사용자 관련 부가 정보를 전달
     *  - peer JSONObject, peer(사용자)에 대한 부가 정보
     *    - userId String, User에 대한 ID로  application에서 사용하는 사용자 아이디
     *    - userName String, User 이름 (Option)
     * @throws RequiredConfigMissingException  필수 Parameter 체크 Exception
     */
    public void connectChannel(String channelId, JSONObject parameters) throws RequiredConfigMissingException {

        playrtc.connectChannel(channelId, parameters);
    }

    /**
     * PlayRTC 인스턴스를 반환한다.
     * @return PlayRTC
     */
    public PlayRTC getPlayRTC() {

        return playrtc;
    }

    /**
     * Activity가 onPause 또는 onStop 시 호출하여 PlayRTC의 스트리밍 처리를 Pause 시킨다.
     */
    public void onActivityPause() {
        if(playrtc != null) {
            playrtc.pause();
        }
    }

    /**
     * Activity가 onResume 또는 onStart 시 호출하여 PlayRTC의 스트리밍 처리를 Resume 시킨다.
     */
    public void onActivityResume() {
        if(playrtc != null) {
            playrtc.resume();
        }
    }

    /**
     * P2P가 종료 되어 객체를 해제할 경우(P2P 종료, Activity Destroy) 호출한다.
     */
    public void close() {
        if(playrtc != null) {
            playrtc.close();
            statReportHandler.stop();
        }
        playrtc = null;
        localMedia = null;
        remoteMedia = null;
        statReportHandler = null;
        activity = null;
    }

    /**
     * 입장해 있는 채널 서비스의 채널 아이디를 반환한다.<br>
     * @return String
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * PlayRTC 채널 서비스에 입장하여 발급 받은 사용자 아이디.<br>
     * 채널 생성/입장 시 전달한 사용자 아이디는 Application 서비스에서 사용하는 아이디로<br>
     * Peer-Id와는 다르다. Peer-Id는 PlayRTC 서비스에서 사용하는 임시로 발급하는 고유 아이디이다.
     * @return String
     */
    public String getUserPid() {
        return userPid;
    }

    /**
     * 채널 서비스 연결 여부를 반환한다.<br>
     * 채널 연결 여부를 체크하는 이유는 Acticity 종료 시 채널 서비스 연결을 확인하고 연결을 해제하기 위해서 임.<br>
     * 채널 서비스 연결을 해제하면 PlayRTC는 내부 인스턴스를 해제한다.<br>
     *
     * @return boolean
     */
    public boolean isChannelConnected() {
        return isChannelConnected;
    }

    /**
     * 전/후방 카메라를 전환하는 기능을 제공한다.<br>
     * 채널 입장 전에는 동작하지 않는다.
     */
    public void switchVideoCamera() {
        if(playrtc != null) {
            playrtc.switchVideoCamera();
        }
    }

    /**
     * 후방 카메라 사용 시 후방 카메라 플래쉬를 On/Off 하는 기능을 제공한다.<br>
     * 채널 입장 전, 전방 카메라 사용 시 동작하지 않는다.
     */
    public void switchBackCameraFlash() {
        if(playrtc != null) {
            boolean on = !playrtc.isBackCameraFlashOn();
            playrtc.setBackCameraFlash(on);
        }
    }

    /**
     * PlayRTC 플랫폼 채널을 종료한다.<br>
     * 채널을 퇴장하며, 채널에 있는 다른 사용자는 onOtherDisconnectChannel이 호출된다.<br>
     * onOtherDisconnectChannel이 호출 되는 사용자는 채널 서비스에 입장해 있는 상태이므로,<br>
     * 새로운 사용자가 채널에 입장하면 P2P 연결을 할 수 있다.
     */
    public void disconnectChannel() {
        if(playrtc != null) {
			/*
			 * 전달하는 사용자 아이디의 채널 퇴장이 일어난다.
			 */
            playrtc.disconnectChannel(playrtc.getPeerId());
        }
    }

    /**
     * PlayRTC 플랫폼 채널을 종료한다.<br>
     * 채널 종료를 호출하면 채널에 있는 모든 사용자는 onDisconnectChannel이 호출된다.<br>
     * onDisconnectChannel이 호출되면 PlayRTC 인스턴스는 더이상 사용할 수 없다.<br>
     * 세로 P2P를 연결하려면 PlayRTC 인스턴스를 다시 생성하여 채널 서비스에 입장해야 한다.
     */
    public void delateChannel() {
        if(playrtc != null){
            //채널에 있는 모든 사용자는 onDisconnectChannel이 호출
            // PlayRTC 인스턴스는 더이상 사용할 수 없다
            playrtc.deleteChannel();
        }
    }

    /**
     * 로컬 전송 음성 스트림을 Mute. - 전송 스트림
     * 상대방은 나의 소리가 들리지 않는다. <br>
     * @param on boolean
     */
    public void setLocalAudioMute(boolean on) {
        if(localMedia != null) {
            localMedia.setAudioMute(on);
        }
    }
    /**
     * 상대방의 수신 음성 스트림 출력을 Mute. - <br>
     * 상대방의 소리가 들리지 않는다. <br>
     * @param on boolean
     */
    public void setRemoteAudioMute(boolean on) {
        if(remoteMedia != null) {
            remoteMedia.setAudioMute(on);
        }
    }

    /**
     * 로컬 전송 영상 스트림을 Pause. - 전송 스트림 <br>
     * 상대방은 나의 영상이  출력되지 않는다. <br>
     * @param on boolean
     */
    public void setLocalVideoPause(boolean on) {
        if(localMedia != null) {
            localMedia.setVideoMute(on);
        }
    }

    /**
     * 상대방 수신 영상 스트림 출력을 Pause.<br>
     * 상대방의 영상이 출력되지 않는다. <br>
     * @param on boolean
     */
    public void setRemoteVideoPause(boolean on) {
        if(remoteMedia != null) {
            remoteMedia.setVideoMute(on);
        }
    }

    /**
     * 카메라 영상 회전 기능. v2.2.9 <br>
     * 전면 카메라 : 시계 반대방향 회전 <br>
     * 후면카메라 : 시계방향 회잔 <br>
     * @param rotation int 0, 90, 180, 270
     */
    public void setCameraRotation(int rotation) {
        if(playrtc != null) {
            playrtc.setCameraRotation(rotation);
        }
    }

    /**
     * 현재 카메라의 Zoom 설정 범위를 반환한다. <br>
     * v2.2.10
     * @return PlayRTCRange<Integer>
     */
    public PlayRTCRange<Integer> getCameraZoomRange() {
        if(playrtc != null) {
            return playrtc.getCameraZoomRange();
        }

        return PlayRTCRange.create(0,  0);
    }

    /**
     * 현재 카메라의 Zoom 설정 level을 반환한다.<br>
     * v2.2.10
     * @return int
     */
    public int getCurrentCameraZoom() {
        if(playrtc != null) {
            return playrtc.getCurrentCameraZoom();
        }

        return 0;
    }
    /**
     * 현재 카메라의 Zoom level을 지정한다.<br>
     * 지정 가능한 값은 범위는 getCameraZoomRange()의 min ~ max<br>
     * v2.2.10
     * @return boolean
     */
    public boolean setCameraZoom(int value) {
        if(playrtc != null) {
            PlayRTCRange<Integer> zoomRange = playrtc.getCameraZoomRange();
            if(value >= zoomRange.getMinValue() && value <= zoomRange.getMaxValue()) {
                return playrtc.setCameraZoom(value);
            }
        }

        return false;
    }

    /**
     * 현재 사용중인 카메라의 WhiteBalance를 반환한다.<br>
     * v2.3.0
     * @return PlayRTCWhiteBalance
     * <pre>
     * - Auto
     * - Incandescent : 백열등빛
     * - FluoreScent : 형광등빛
     * - DayLight :  햇빛, 일광
     * - CloudyDayLight : 흐린빛
     * - TwiLight : 저녁빛
     * - Shade : 그늘, 그림자
     * </pre>
     */
    public PlayRTCWhiteBalance getCameraWhiteBalance() {
        if(playrtc != null) {
            return playrtc.getCameraWhiteBalance();
        }

        return PlayRTCWhiteBalance.Auto;
    }

    /**
     * 현재 사용중인 카메라가 전달받은 WhiteBalance를 지원 하는지 여부를 검사한다.<br>
     * 단말기마다 지원 여부가 다름  <br>
     * v2.2.10
     * @param whiteBalance PlayRTCWhiteBalance
     * <pre>
     * - Auto
     * - Incandescent : 백열등빛
     * - FluoreScent : 형광등빛
     * - DayLight :  햇빛, 일광
     * - CloudyDayLight : 흐린빛
     * - TwiLight : 저녁빛
     * - Shade : 그늘, 그림자
     * </pre>
     * @return boolean, 지원 여부
     * @see com.sktelecom.playrtc.PlayRTC.PlayRTCWhiteBalance
     */
    public boolean isSupportedCameraWhiteBalance(PlayRTCWhiteBalance whiteBalance) {
        if(playrtc != null) {
            return playrtc.isSupportedCameraWhiteBalance(whiteBalance);
        }

        return false;
    }

    /**
     * 현재 사용중인 카메라의 WhiteBalance를 지정한다.<br>
     * 단말기에 따라 WhiteBalance를 지원하지 안ㄹ을 수 있음<br>
     * v2.2.10
     * @param whiteBalance PlayRTCWhiteBalance
     * <pre>
     * - Auto
     * - Incandescent : 백열등빛
     * - FluoreScent : 형광등빛
     * - DayLight :  햇빛, 일광
     * - CloudyDayLight : 흐린빛
     * - TwiLight : 저녁빛
     * - Shade : 그늘, 그림자
     * </pre>
     * @return boolean, 실행 여부
     */
    public boolean setCameraWhiteBalance(PlayRTCWhiteBalance whiteBalance) {
        if(playrtc != null) {
            return playrtc.setCameraWhiteBalance(whiteBalance);
        }

        return false;
    }

    /**
     * 현재 사용중인 카메라의 노출 보정값 설정 범위를 반환한다.<br>
     * v2.2.10
     * maximim 값이 0 이면 지원 않함.
     * @return PlayRTCRange<Integer>
     */
    public PlayRTCRange<Integer> getCameraExposureCompensationRange() {
        if(playrtc != null) {
            return playrtc.getCameraExposureCompensationRange();
        }

        return PlayRTCRange.create(0, 0);
    }

    /**
     * 현재 사용중인 카메라의 노출 보정값 설정 범위를 반환한다. <br>
     * 0 이면 노출 보정이 지원안됨<br>
     * v2.2.10
     * @return int
     */
    public int getCameraExposureCompensation() {
        if(playrtc != null) {
            return playrtc.getCameraExposureCompensation();
        }

        return 0;
    }

    /**
     * 현재 사용중인 카메라의 노출 보정값을 지정한다.<br>
     * maximim값이 0이면  노출이 조정되지 않는다.<br>
     * 노출 보정이 지원되는지 알아보기 위해 유효한 값 범위(getCameraExposureCompensationRange() 호출하여)를 확인해야한다<br>
     * v2.2.10
     * @param value int
     * @return boolean, 실행 여부
     */
    public boolean setCameraExposureCompensation (int value) {
        if(playrtc != null) {
            return playrtc.setCameraExposureCompensation(value);
        }

        return false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // PlayRTCObserver Implements
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * 채널 서비스에 채널 생성/입장 성공 시 채널 정보를 전달 받는다.
     * @param obj PlayRTC
     * @param channelId String, 새로 생성한 채널 아이디
     * @param reason String, createChannel은 "create", connectChannel은 "connect"
     */
    @Override
    public void onConnectChannel(final PlayRTC obj, final String channelId, final String reason, final String channelType) {
        if(reason.equals("create")) {
            // 채널 팝업 뷰에 채널 아이디를 전달하여 화면에 표시
            activity.getChannelInfoPopup().setChannelId(channelId);
            this.channelId = channelId;
        }
        isChannelConnected = true;
        // 채널 아이디를 확인하기 위해서 0.8초 지연 시간 후 숨김
        activity.getChannelInfoPopup().hide(800);
    }


    /*
     * PlayRTCConfig Channel의 ring = true 설정 시 나중에 채널에 입장한 사용자 측에서
     * 연결 수락 의사를 물어옴. ring 설정은 상호간에 동일해야한다.
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     */
    @Override
    public void onRing(final PlayRTC obj, final String peerId, final String peerUid) {

        activity.appnedLogMessage(">>["+peerId+"] onRing....");
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("PlayRTC");
        alert.setMessage(peerId + "이 연결을 요청했습니다.");

        alert.setPositiveButton("연결", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Utils.showToast(activity, "[" + peerId + "] accept....");
                activity.appnedLogMessage(">>["+peerId+"] accept....");

                obj.accept(peerId);
            }
        });
        alert.setNegativeButton("거부", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                Utils.showToast(activity, "[" + peerId + "] reject....");
                activity.appnedLogMessage(">>["+peerId+"] reject....");

                obj.reject(peerId);
            }
        });
        alert.show();

    }

    /*
     * 상대방으로 부터 연결 수락 의사를 수신 함.
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     */
    @Override
    public void onReject(final PlayRTC obj, final String peerId, final String peerUid) {
        Utils.showToast(activity, "[" + peerId + "] onReject....");
        activity.appnedLogMessage(">>["+peerId+"] onReject....");
    }

    /*
     * 상대방으로 부터 연결 거부 의사를 수신 함.
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     */
    @Override
    public void onAccept(final PlayRTC obj, final String peerId, final String peerUid) {
        Utils.showToast(activity, "[" + peerId + "] onAccept....");
        activity.appnedLogMessage(">>[" + peerId + "] onAccept....");
    }

    /*
     * 상대방으로부터 User Defined Command(문자열 형식)를 수신.
     * Application에서 정의한 JSON String 또는 Command 문자열을 주고 받아 원하는 용도로 사용할 수 있다.
     * 예를 들어 상대방 단말제어.
     *
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param data String, 상대방이 전달한 문자열
     */
    @Override
    public void onUserCommand(final PlayRTC obj, final String peerId, final String peerUid, final String data) {
        Utils.showToast(activity, "[" + peerId + "] onCommand....");
        activity.appnedLogMessage(">>[" + peerId + "] onCommand[" + data + "]");
    }


    /*
     * 로컬 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
     * 로컬 미디어 PlayRTCMedia객체 전달
     *
     * @param obj PlayRTC
     * @param media PlayRTCMedia, PlayRTCVideo의 PlayRTCVideoRenderer를 등록하여 화면 출력 처리
     */
    @Override
    public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia media) {

        Log.e(LOG_TAG, "onMedia onAddLocalStream==============");

        // 영상 미디어 스트림이  있는지 검사.
        if(media.hasVideoStream() == false) {
            return;
        }
        localMedia = media;
        localMedia.setAudioMute(false);
        // 영상뷰가 있는지 검사.
        if(activity.getLocalVideoView() == null) {
            return;
        }

        activity.appnedLogMessage(">> onLocalStream...");

        //PlayRTCVideoRenderer를 등록하여 화면 출력 처리
        localMedia.setVideoRenderer(activity.getLocalVideoView().getVideoRenderer());
        activity.getLocalVideoView().show(400);

    }
    /*
     * P2P 연결 시 상대방 미디어 처리를 위한 PlayRTCMedia 수신 이벤트 처리
     * 상대방 미디어 PlayRTCMedia객체 전달 <br>
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param media PlayRTCMedia, PlayRTCVideo의 PlayRTCVideoRenderer를 등록하여 화면 출력 처리
     */
    @Override
    public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUid, final PlayRTCMedia media) {

        Log.e(LOG_TAG, "onMedia onAddRemoteStream==============");

        // 영상 미디어 스트림이  있는지 검사.
        if(media.hasVideoStream() == false) {
            return;
        }
        remoteMedia = media;
        remoteMedia.setAudioMute(false);
        // 영상뷰가 있는지 검사.
        if(activity.getRemoteVideoView() == null) {
            return;
        }

        activity.appnedLogMessage(">> onRemoteStream[" + peerId + "]...");

        // PlayRTCVideoRenderer를 등록하여 화면 출력 처리
        remoteMedia.setVideoRenderer(activity.getRemoteVideoView().getVideoRenderer());


    }
    /*
     * Data 송수신을 위한 PlayRTCData 수신 이벤트 처리 -> 데이터 채널 사용 설정 시
     * Data-Channel이 생성되면 전달, PlayRTCData(DataChannel) 객체 전달
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param data PlayRTCData
     */
    @Override
    public void onAddDataStream(final PlayRTC obj, final String peerId, final String peerUid, final PlayRTCData data) {
        this.peerId =  peerId;
        activity.appnedLogMessage(">> onDataStream[" + peerId + "]...");
        // PlayRTCDataObserver를 등록한다.
        activity.getRtcDataHandler().setDataChannel(data);

    }

    /*
     * 채널이 종료 되거나, 내가 채널에서 퇴장할 때 호출
     * deleteChannel을 호출하거나, 내가 disconnectChannel을 호출하면 발생한다.
     * PlayRTC 인스턴스는 재사용 할수 없다.(내부 P2P 객체 헤제됨)
     * @param obj PlayRTC
     * @param reason String, deleteChannel인 경우 "delete", disconnectChannel인 경우 "disconnect"
     */
    @Override
    public void onDisconnectChannel(final PlayRTC obj, final String reason) {
        if(reason.equals("disconnect")){
            Utils.showToast(activity, "채널에서 퇴장하였습니다....");
            activity.appnedLogMessage(">>PlayRTC 채널에서 퇴장하였습니다....");
        }
        else {
            Utils.showToast(activity, "채널이 종료되었습니다....");
            activity.appnedLogMessage(">>PlayRTC 채널이 종료되었습니다....");
        }
        statReportHandler.stop();

        // 채널이 종료 되었으므로 false 지정
        isChannelConnected = false;

        /**
         * 내가 채널에서 퇴장 했으므로 PlayRTCActivity를 종료한다.
         */
        activity.setOnBackPressed(true);
    }

    /**
     * 상대방이 채널에서 퇴장할 때.<br>
     * 상대가 disconnectChannel을 호출. <br>
     * 자신은 아직 채널 서비스에 입장해 있는 상태이므로, 채널 서비스에 추가로 입장한 사용자와 P2P연결을 수립 할 수 있다. <br>
     * v2.2.11
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param reason String, "disconnect", "timeout"
     */
    @Override
    public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUid, final String reason) {

        String notiMsg = "";
        if(reason != null && reason.equals("timeout")) {
            notiMsg = "[" + peerUid + "]님의 네트워크 연결이 해제되었습니다....";
        }
        else {
            notiMsg = "[" + peerUid + "]님이 채널에서 퇴장하였습니다....";
        }

        Utils.showToast(activity, notiMsg);
        activity.appnedLogMessage(notiMsg);
        statReportHandler.stop();

        // 상대방 스트림이 멈추면 View에 마지막 화면 잔상이 남는다.
        // 뷰 생성 시 지정한 배경 색으로 화면을 초기화 한다.
        if(activity.getRemoteVideoView() != null) {
            activity.getRemoteVideoView().bgClearColor();

            // local videoView 크기를 크게 변경
            activity.getVideoLayer().resizeLocalVideoView(PlayRTCVideoViewGroup.RTCViewSizeType.Full);
        }
    }

    /*
     * PlayRTC의 상태 변경 이벤트 처리, PlayRTC의 enum 코드 참고
     * @param obj PlayRTC
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param status PlayRTCStatus PlayRTC의 상태 변경 코드 참고
     * @param desc String, Description
    */
    @Override
    public void onStateChange(final PlayRTC obj, String peerId, final String peerUid, PlayRTCStatus status, String desc) {

        activity.appnedLogMessage(">>"+peerId+"  onStatusChange["+ status+ "]...");

        // 최초 P2P연결 수립된 상태. 1번 호출
        // 연결 수립 이후 네트워크 상태에 따라 연결 상태가 PeerDisconnected <-> PeerConnected 상태를 반복할 수 있다.
        if(status == PlayRTCStatus.PeerSuccess) {
            //P2P 상태 리포트 구동 시작
            statReportHandler.start(obj, peerId);
        }
    }

    /*
     * PlayRTC의 오류 발생 이벤트 처리, PlayRTC의 enum 코드 참고
     * @param obj PlayRTC
     * @param status PlayRTCStatus PlayRTC의 상태 변경 코드 참고
     * @param code PlayRTCCode PlayRTC의 오류 코드 참고
     * @param desc String, Description
     */
    @Override
    public void onError(final PlayRTC obj, PlayRTCStatus status, PlayRTCCode code, String desc) {
        String msg = "Error["+ code + "] Status["+ status+ "] "+desc ;

        Utils.showToast(activity, msg);
        activity.appnedLogMessage(">>"+peerId + "  " + msg);

        //P2P 상태 리포트 구동 중지
        statReportHandler.stop();

    }


    /*
     * PlayRTC 서비스 설정 객체를 생성하여 반환한다.
     *
     * 서비스 설정
     * - Android Contect, PlayRTC project-ID
     * - 영상 설정
     *   - 영상 전송 : 사용
     *   - 영상 해상도 : 640 x 480
     * - 음성 설정
     *   - 음성 전송 : 사용
     * - P2P 데이터 통신 : 사용
     * - 로그레벨 지정
     *
     * @param runType int
     *  - 1. 영상, 음성, p2p data
     *  - 2. 영상, 음성
     *  - 3. 음성, data
     *  - 4. p2p data only
     *
     * @return PlayRTCConfig
     */
    private PlayRTCConfig createPlayRTCConfig(int runType, String channelRing, String videoCodec, String audioCodec) {
		/* PlayRTC 서비스 설정 */
        // 1. create PlayRTCConfig
        PlayRTCConfig config = PlayRTCFactory.createConfig();
        config.setAndroidContext(activity.getApplicationContext());

        // 2. TDCProjectId/TDCLicense set
        config.setProjectId(TDCProjectId);

        boolean useChannelRing = (channelRing.equals("true"));
        // 3. Ring : false 연결 수립 여부를 상대방에게 묻지 않음
        config.setRingEnable(useChannelRing);

        // UserMedia 인스턴스 생성 시점을 지정. default true
        // true : 채널 입장 후 바로 생성, 화면에 나의 영상을 바로 출력할 수 있다.
        // false : 채널 입장 후 상대방과의 연결 과정을 시작할 때 생성. blank 화면이 표시됨
        // new v2.2.8
        config.setPrevUserMediaEnable(true);

        /*
		 * 영상 해상도 지정 , 기본 640x480
		 * min - max 범위를 다르게 지정하면 내부적으로 max 해상도 우선 사용
		 * - 320x240 해상도
		 * - 640x480 해상도 : 기본 해상도
		 * - 1280x720 해상도 : 단말기 성능에 따라 영상 품질이 매우 않좋아질 수 있음.
		 */
        int frameWidth = 640;
        int frameHeight = 480;

        int minVideoFrameRate = 15;
        int maxVideoFrameRate = 30;
        int videoBitrateKbps = 1500;

        // 음성 데이터 평균 bitrate 지정,kbps
        // ISAC 32
        // OPUS 32 ~ 64
        int audioBitrateKbps = 32;

        /*
         * 전방카메라 사용
         * enum PlayRTCVideoConfig.CameraType
		 * - Front,
		 * - Back
         */
        PlayRTCVideoConfig.CameraType cameraType = PlayRTCVideoConfig.CameraType.Front;
        /* enum PlayRTCVideoConfig.VideoCodec
         *  - VP8
         *  - VP9
         *  - H264, Open H.264
         */
        PlayRTCVideoConfig.VideoCodec videPreferCodec = VideoCodec.VP8;
        if(videoCodec.equals("vp9")) {
            videPreferCodec = VideoCodec.VP9;
        }
        else if(videoCodec.equals("h264")) {
            videPreferCodec = VideoCodec.H264;
        }

        /*
         * enum PlayRTCVideoConfig.AudioCodec
         *  - ISAC,
         *  - OPUS
         */
        PlayRTCAudioConfig.AudioCodec audioPreferCodec = AudioCodec.OPUS;
        if(audioCodec.equals("isac")) {
            audioPreferCodec = AudioCodec.ISAC;
        }

        // 4. Audio/Video/Data Enable runType 타입에 따라 지정
        // 양상 + 음성 + Data
        if(runType == 1) {
            /*
		     * 영상 스트림 전송 사용.
		     * false 설정 시 SDK는 read-only 모드로 동작하며, 상대방이 영상 스트림을 전송하면 수신이 된다.
		     */
            config.video.setEnable(true);

            /*
             * 전방카메라 사용
             * @param  enum CameraType
		     * - Front,
		     * - Back
             */
            config.video.setCameraType(cameraType);

            /*
             * Video 영상의 선호 코덱을 지정, default VP8
             * 상호 SDK 교환과정에서 선호코덱을 사용할 수 있으면 사용됨. 코덱 미지원 시 다른 코덱 사용
             * v2.2.6
             * @param enum VideoCodec,
             *  - VP8
             *  - VP9
             *  - H264, Open H.264
            */
            config.video.setPreferCodec(videPreferCodec);
            /*
		     * 영상 해상도 지정 , 기본 640x480
		     * min - max 범위를 다르게 지정하면 내부적으로 max 해상도 우선 사용
		     * - 320x240 해상도
		     * - 640x480 해상도 : 기본 해상도
		     * - 1280x720 해상도 : 단말기 성능에 따라 영상 품질이 매우 않좋아질 수 있음.
		     */
            config.video.setMaxFrameSize(frameWidth, frameHeight);
            config.video.setMinFrameSize(frameWidth, frameHeight);

            config.video.setMinFrameRate(minVideoFrameRate);
            config.video.setMaxFrameRate(maxVideoFrameRate);

            /*
             * PlayRTC Video-Stream BandWidth를 지정한다.
             * 600 ~ 2500
             * default 1500 (640x480)
             */

            config.bandwidth.setVideoBitrateKbps(videoBitrateKbps);

            /*
             * 음성 스트림 전송 사용.
             * false 설정 시 SDK는 read-only 모드로 동작하며, 상대방이 음성 스트림을 전송하면 수신이 된다.
             */
            config.audio.setEnable(true);

            /*
             * Audio의 선호 코덱을 지정, default ISAC
             * 상호 SDK 교환과정에서 선호코덱을 사용할 수 있으면 사용됨. 코덱 미지원 시 다른 코덱 사용
             * v2.2.6
             * @param codec AudioCodec
             *  - ISAC,
             *  - OPUS
             */
            config.audio.setPreferCodec(audioPreferCodec);

            // 음성 데이터 평균 bitrate 지정,kbps
            // ISAC 32
            // OPUS 32 ~ 64
            config.bandwidth.setAudioBitrateKbps(audioBitrateKbps);

            /*
             * SDK 내부에 구현되어 있는 단말기 Sound 출력 장치 제어 기능을 시용하도록 설정
             * 사용자가 직접 기능(AudioManager)을 구현하는 경우 false로 지정
             * default true, loud speaker 모드
             *
             * -------------------------------------------------
             * ear-speaker |                | wired-earphone
             *     ^       |                |       ^
             *     |       |                |       |
             * 근접 센서 감지  | <-----------> |     장치 유형
             *     |       |  장치 연결 감지   |       |
             *     v       |                |       v
             * loud speaker|                | bluetooth-headset
             * -------------------------------------------------
             */
            config.audio.setAudioManagerEnable(true);

            /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
            config.data.setEnable(true);

        }
        // 영상 + 음성
        else if(runType == 2){
            /*
		     * 영상 스트림 전송 사용.
		     * false 설정 시 SDK는 read-only 모드로 동작하며, 상대방이 영상 스트림을 전송하면 수신이 된다.
		     */
            config.video.setEnable(true);

            /*
             * 전방카메라 사용
             * @param  enum CameraType
		     * - Front,
		     * - Back
             */
            config.video.setCameraType(cameraType);

            /*
             * Video 영상의 선호 코덱을 지정, default VP8
             * 상호 SDK 교환과정에서 선호코덱을 사용할 수 있으면 사용됨. 코덱 미지원 시 다른 코덱 사용
             * v2.2.6
             * @param enum VideoCodec,
             *  - VP8
             *  - VP9
             *  - H264, Open H.264
            */
            config.video.setPreferCodec(videPreferCodec);
            /*
		     * 영상 해상도 지정 , 기본 640x480
		     * min - max 범위를 다르게 지정하면 내부적으로 max 해상도 우선 사용
		     * - 320x240 해상도
		     * - 640x480 해상도 : 기본 해상도
		     * - 1280x720 해상도 : 단말기 성능에 따라 영상 품질이 매우 않좋아질 수 있음. 대부분의 단말기 성능 저하 발생
		     */
            config.video.setMaxFrameSize(frameWidth, frameHeight);
            config.video.setMinFrameSize(frameWidth, frameHeight);

            config.video.setMinFrameRate(minVideoFrameRate);
            config.video.setMaxFrameRate(maxVideoFrameRate);

            /*
             * PlayRTC Video-Stream BandWidth를 지정한다.
             * 600 ~ 2500
             * default 1500 (640x480)
             */

            config.bandwidth.setVideoBitrateKbps(videoBitrateKbps);

            /*
             * 음성 스트림 전송 사용.
             * false 설정 시 SDK는 read-only 모드로 동작하며, 상대방이 음성 스트림을 전송하면 수신이 된다.
             */
            config.audio.setEnable(true);

            /*
             * Audio의 선호 코덱을 지정, default ISAC
             * 상호 SDK 교환과정에서 선호코덱을 사용할 수 있으면 사용됨. 코덱 미지원 시 다른 코덱 사용
             * v2.2.6
             * @param codec AudioCodec
             *  - ISAC,
             *  - OPUS
             */
            config.audio.setPreferCodec(audioPreferCodec);

            // 음성 데이터 평균 bitrate 지정,kbps
            // ISAC 32
            // OPUS 32 ~ 64
            config.bandwidth.setAudioBitrateKbps(audioBitrateKbps);

            /*
             * SDK 내부에 구현되어 있는 단말기 Sound 출력 장치 제어 기능을 시용하도록 설정
             * 사용자가 직접 기능(AudioManager)을 구현하는 경우 false로 지정
             * default true, speaker 모드
             */
            config.audio.setAudioManagerEnable(true);

            /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
            config.data.setEnable(false);


        }
        // 음성 only
        else if(runType == 3){
            // video  전송 안함.
            config.video.setEnable(false);
            /*
             * 음성 스트림 전송 사용.
             * false 설정 시 SDK는 read-only 모드로 동작하며, 상대방이 음성 스트림을 전송하면 수신이 된다.
             */
            config.audio.setEnable(true);

            /*
             * Audio의 선호 코덱을 지정, default ISAC
             * 상호 SDK 교환과정에서 선호코덱을 사용할 수 있으면 사용됨. 코덱 미지원 시 다른 코덱 사용
             * v2.2.6
             * @param codec AudioCodec
             *  - ISAC,
             *  - OPUS
             */
            config.audio.setPreferCodec(AudioCodec.OPUS);

            // 음성 데이터 평균 bitrate 지정,kbps
            // ISAC 32
            // OPUS 32 ~ 64
            config.bandwidth.setAudioBitrateKbps(audioBitrateKbps);

            /*
             * SDK 내부에 구현되어 있는 단말기 Sound 출력 장치 제어 기능을 시용하도록 설정
             * 사용자가 직접 기능(AudioManager)을 구현하는 경우 false로 지정
             * default true, speaker 모드
             */
            config.audio.setAudioManagerEnable(true);

            /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
            config.data.setEnable(false);


        }
        // Data Only
        else {
            config.video.setEnable(false);  /* 영상 전송 안함 */
            config.audio.setEnable(false);   /* 음성 전송 안함 */
            config.data.setEnable(true);    /* P2P 데이터 교환을 위한 DataChannel 사용 여부 */
        }

        /*
         * SDK Console 로그 레벨 지정
         */
        config.log.console.setLevel(CONSOLE_LOG);

		/* SDK 파일 로그 레벨 지정 */
        config.log.file.setLevel(FILE_LOG);

		/* 파일 로그를 남기려면 로그파일 폴더 지정 . [PATH]/yyyyMMdd.log , default 10일간 보존 */
		/* SDK 파일 로깅을 위한 로그 파일 경로, 파일 로깅을 사용하지 않는다면 Pass */
        File logPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + activity.getPackageName() + "/files/log");

        //  파일 로그 10일간 보존
        config.log.file.setRolling(10);
        config.log.file.setLogPath(logPath.getAbsolutePath());


        return config;
    }
}