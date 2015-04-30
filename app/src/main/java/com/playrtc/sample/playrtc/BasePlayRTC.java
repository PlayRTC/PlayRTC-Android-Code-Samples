package com.playrtc.sample.playrtc;

import org.json.JSONException;
import org.json.JSONObject;

import com.playrtc.sample.BaseActivity;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.config.PlayRTCSettings;
import com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
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
 */
public class BasePlayRTC {
	
	protected static final int LOG_LEVEL = PlayRTCSettings.WARN;
	
	protected BaseActivity activity = null;
	/**
	 * SKT T-DEV에 등록한 PlayRTC 프로젝트 아이디
	 */
	protected String TDCProjectId = "60ba608a-e228-4530-8711-fa38004719c1";
	/**
	 * PlayRTC 인터페이스 개체 
	 */
	protected PlayRTC playRTC = null;
	/**
	 * 생성자 BasePlayRTC
	 * @param activity BaseActivity
	 * @param oberver PlayRTCObserver, PlayRTC의 이벤트를 처리하기 위한 리스너 구현 객체 
	 * @throws RequiredParameterMissingException PlayRTCObserver 구현개체를 생성자에 전달해야 한다.
	 * @throws UnsupportedPlatformVersionException Android SDK 11 이상만 지원한다.
	 * @see com.playrtc.sample.BaseActivity
	 */
	public BasePlayRTC(BaseActivity activity, PlayRTCObserver oberver) 
					throws UnsupportedPlatformVersionException, RequiredParameterMissingException {

		this.activity = activity;
		// PlayRTC 구현 개체를 생성하고 PlayRTC를 전달 받는다.
		this.playRTC = PlayRTCFactory.newInstance(oberver);
	}
	
	/**
	 * PlayRTC 인터페이스 개체를 반환한다.
	 * @return PlayRTC
	 */
	public PlayRTC getPlayRTC() {
		return playRTC;
	}
	
	
	/**
	 * P2P 연결을 수립하기 위해 PlayRTC 채널 서비스에 채널을 생성하고 입장힌다.<br>
	 * 채널에 입장하면 채널 서비스는 PlayRTC 서비스 관련 Configuration 정보와 채널 아이디를 반환하고,<br>
	 * SDK가 정보를 수신하여 내부적으로 서비스 설정을 한다.<br>
	 * 이 과정에서 획득한 채널 아이디를 PlayRTCObserver 인터페이스(onConnectChannel : reson -> "create")를 통해 전달한다.<br>
	 * <br>
	 * 
	 * @param uid String, Application 사용자 아이디. mail id 사용 
	 * @throws RequiredConfigMissingException  Android Application Context를 필요로 한다. <br>
	 * PlayRTCSettings.android.setContext 메소드로 Context를 반듯이 전달해야 한다. 
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onConnectChannel(PlayRTC, String, String)
	 */
	public void createChannel(String uid) throws RequiredConfigMissingException{
		JSONObject parameters = new JSONObject();
		// 채널정보를 정의한다.
		JSONObject channel = new JSONObject();
		try {
			// 채널에 대한 Alias 이름을 지정한다.
			channel.put("channelName", uid+"님의 채널방입니다.");
			parameters.put("channel", channel);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject peer = new JSONObject();
		try {
			// Application의 사용자 아이디를 전달한다. 
			peer.put("uid", uid);
			parameters.put("peer", peer);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// 채널을 신규 생성하고 입장한다.
		// 채널 입장 시 PlayRTCObserver의 onConnectChannel가 호출된다.
		playRTC.createChannel(parameters);

	}
	
	/**
	 * P2P 연결을 수립하기 위해 생성되어 있는 채널에 입장한다. 
	 * 
	 * @param channelId String, 생성되어 있는 채널의 아이디 
	 * @param uid String, Application 사용자 아이디. mail id 사용 
	 * @throws RequiredConfigMissingException  Android Application Context를 필요로 한다. <br>
	 * PlayRTCSettings.android.setContext 메소드로 Context를 반듯이 전달해야 한다. 
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onConnectChannel(PlayRTC, String, String)
	 */
	public void connectChannel(String channelId, String uid) throws RequiredConfigMissingException {
		JSONObject parameters = new JSONObject();

		JSONObject peer = new JSONObject();
		try {
			// Application의 사용자 아이디를 전달한다.  
			peer.put("uid", uid);
			parameters.put("peer", peer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// 채널에 입장한다.
		// 채널 입장 시 PlayRTCObserver의 onConnectChannel가 호출된다.
		playRTC.connectChannel(channelId, parameters);
	}
	
	/**
	 * 입장한 채널에서 퇴장한다.<br>
	 * PlayRTCObserver 인터페이스의 onDisconnectChannel : reson-> "disconnect"가 호출되고,<br> 
	 * 상대방은 onOtherDisconnectChannel가 호출된다. -> disconnectChannel, deleteChannel를 이용하여 상대방도 채널에서 나가도록 구현필요 
	 * @param peerId String, 채널에서 부여 받은 채널 내부의 사용자 식별아이디 
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onDisconnectChannel(PlayRTC, String)
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onOtherDisconnectChannel(PlayRTC, String, String)
	 */
	public void disconnectChannel(String peerId) {
		if(playRTC != null){
			playRTC.disconnectChannel(peerId);
		}

	}
	/**
	 * 입장한 채널을 종료시킨다.<br>
	 * 모든 시용자에게 PlayRTCObserver 인터페이스의 onDeleteChannel : reson-> "close"가 호출된다.
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onDisconnectChannel(PlayRTC, String)
	 */
	public void deleteChannel() {
		if(playRTC != null){
			playRTC.deleteChannel();
		}
	}
	
	
	/* Activity onResume */
	public void resume()
	{
		if(playRTC != null){
			playRTC.resume();
		}
	}
	
	
	/* Activity onPause */
	public void pause()
	{
		if(playRTC != null){
			playRTC.pause();
		}
		
	}
	
	/**
	 * 입장해 있는 채널의 아이디를 반환한다.
	 * @return String
	 */
	public String getChannelId()
	{
		if(playRTC != null){
			return playRTC.getChannelId();
		}
		return null;
	}
	
	/**
	 * 입장해 있는 채널에서 부여 받은 사용자의 Peer 아이디를 반환한다. 
	 * @return String
	 */
	public String getPeerId()
	{
		if(playRTC != null){
			return playRTC.getPeerId();
		}
		return null;
	}
	
	/**
	 * 상대방에게 User-Defined Command를  데이터 가공없이 그대로 전달한다.<br>
	 * @param peerId String, PlayRTC 플랫폼 채널 서비스의 User 아이디
	 * @param data String, 데이터 문자열 데이터 형식은 개별 application에서 정의 한 형태 
	 * @see com.sktelecom.playrtc.connector.PlayRTCConnector#command
	 */ 
	public void userCommand(final String peerId, final String data) {
		if(playRTC != null){
			playRTC.userCommand(peerId, data);
		}
	}
	/**
	 * 채널 서비스에 생성되어 있는 채널의 목록을 조회하여 반환한다. 
	 * @param listener PlayRTCServiceHelperListener
	 */
	public void getChannelList(PlayRTCServiceHelperListener listener) {
		playRTC.getChannelList(listener);
	}
}
