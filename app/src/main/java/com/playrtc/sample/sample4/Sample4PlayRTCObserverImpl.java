package com.playrtc.sample.sample4;

import android.util.Log;

import com.playrtc.sample.BaseActivity;
import com.playrtc.sample.playrtc.PlayRTCObserverImpl;
import com.playrtc.sample.view.ChannelPopupView;
import com.playrtc.sample.view.IVideoGroupView;
import com.playrtc.sample.view.SlideLogView;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

/**
 * PlayRTC 객체의 이벤트를 전달 받기 위한 PlayRTCObserver의 구현 개체 클래스 PlayRTCObserverImp를 확장  <br>
 * 로컬 카메라 영상과 로컬 영상 출력 뷰룰 사용 하지 않아   PlayRTCObserverImp를 확장하여 다르게 처리 
 * 
 * <b>PlayRTCObserver Interface</b><br>
 * - void onConnectChannel(PlayRTC obj, String channelId)<br>
 * - void onRing(PlayRTC obj, String peerId)<br>
 * - void onReject(PlayRTC obj, String peerId)<br>
 * - void onAccept(PlayRTC obj, String peerId)<br>
 * - void onCommand(PlayRTC obj, String peerId, String data)<br>
 * - void onAddLocalStream(PlayRTC obj, PlayRTCMedia media)<br>
 * - void onAddRemoteStream(PlayRTC obj, String peerId, PlayRTCMedia media)<br>
 * - void onAddDataStream(PlayRTC obj, String peerId, PlayRTCData data)<br>
 * - void onDisconnect(PlayRTC obj, )<br>
 * - void onOtherDisconnect(PlayRTC obj, String peerId)<br>
 * - void onDispose()<br>
 * - void onStateChange(PlayRTC obj, String peerId, PlayRTCStatus status, String desc)<br>
 * - void onError(PlayRTC obj, String peerUid, PlayRTCStatus status, PlayRTCCode code, String desc)<br>
 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl
 */
public class Sample4PlayRTCObserverImpl extends PlayRTCObserverImpl{

	/**
	 * 생성자 
	 * @param activity BaseActivity
	 * @param videoGroup IVideoGroupView
	 * @param channelPopup ChannelPopupView
	 * @param logView SlideLogView
	 * @see com.playrtc.sample.BaseActivity
	 * @see com.playrtc.sample.view.IVideoGroupView
	 * @see com.playrtc.sample.view.ChannelPopupView
	 * @see com.playrtc.sample.view.SlideLogView
	 */
	public Sample4PlayRTCObserverImpl(BaseActivity activity, IVideoGroupView videoGroup, ChannelPopupView channelPopup,  SlideLogView logView) {
		super(activity, videoGroup, channelPopup, logView );
	}
	
	
	/**
	 * Captur Target 뷰룰 사용하므로 PlayRTCMedia 출력 하지 않음  <br>
	 * @param obj PlayRTC
	 * @param media PlayRTCMedia, 미디어 처리를 위한 인터페이스 개체 
	 */
	@Override
	public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia media) {
		
		Log.d(LOG_TAG, "onMedia onAddLocalStream==============");
		logView.appendLog(">> onLocalStream...");
		localMedia = media;
	}
	
	/**
	 * P2P가 수립되어 상대방의 PlayRTCMedia 전달 <br>
	 * PlayRTCMedia객체를 전달 받으면 영상 출력을 위해 PlayRTCVideView의 렌더러 인터페이스를 전달해야 한다. <br>
	 * Sample 특성상 상대방 영상은 작게 출력한다. 
	 * @param obj PlayRTC
	 * @param peerId String, 채널에서 부여받은 사용자의 Peer 아이디 
	 * @param peerUid String, 사용자의 application에서 사용하는 아이디로 채널 입장 시 전달한 값, 없으면 ""
	 * @param media PlayRTCMedia, 미디어 처리를 위한 인터페이스 개체 
	 */
	@Override
	public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUid, final PlayRTCMedia media) {
		
		otherPeerId = peerId;
		Log.d(LOG_TAG, "onMedia onAddRemoteStream==============");
		logView.appendLog(">> onRemoteStream["+peerId+"]...");
		remoteMedia = media;
		
		if(videoGroup == null) {
			return;
		}
		
		if(media.hasVideoStream() == false) {
			return;
		}
		// PlayRTCVideView 개체를 조회한다.
		PlayRTCVideoView remoteView = (PlayRTCVideoView)videoGroup.getRemoteVideoView();
		//영상 출력을 위해 PlayRTCVideView의 렌더러 인터페이스를 전달해야 한다. 
		media.setVideoRenderer(remoteView.getVideoRenderer());
		
		remoteView.show(200);
	}
}
