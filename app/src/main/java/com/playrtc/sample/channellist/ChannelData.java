package com.playrtc.sample.channellist;

/**
 * PlayRTC 서비스에서 조회하는 채널 목록 정보를 담기 위한 데이터 클래스<br>
 * ChannelPopupView의 리스트 출력 시 ChannelListAdapter에서 사용한다. 
 */
public class ChannelData {
	
	/**
	 * PlayRTC 채널 서비스에서 발급 받은 아이디 
	 */
	private String channelId = null;
	/**
	 * PlayRTC 채널 서비스에 생성한 채널의 Alias 
	 */
	private String channelName = null;
	/**
	 * PlayRTC 채널 서비스에서 사용하는 Application User의 ID, Application마다 고유한 사용자 아이디를 시용<BR>
	 * 예를 들면 메일 아이디 
	 */
	private String userId = null;
	
	
	public ChannelData() {
		
	}

	/**
	 * PlayRTC 채널 서비스에서 발급 받은 아이디를 반환
	 * @return String
	 */
	public String getChannelId() {
		return channelId;
	}
	/**
	 * PlayRTC 채널 서비스에서 발급 받은 아이디를 저장한다.
	 * @param channelId String
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/**
	 *  PlayRTC 채널 서비스에 생성한 채널의 Alias를 반환 
	 * @return String
	 */
	public String getChannelName() {
		return channelName;
	}

	/**
	 * PlayRTC 채널 서비스에 생성한 채널의 Alias를 저장한다.
	 * @param channelName String
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	/**
	 * PlayRTC 채널 서비스에서 사용하는 Application User의 ID를 반환
	 * @return String
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * PlayRTC 채널 서비스에서 사용하는 Application User의 ID를 저장
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
