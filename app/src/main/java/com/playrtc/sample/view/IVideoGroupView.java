package com.playrtc.sample.view;

import android.view.View;


/**
 * 영상 출력 뷰 의 부모 컨테이너 뷰가 기본적으로 제공하는 기능을 정의한 인터페이스 Class
 * <b>Method</b>
 * <pre>
 * - public void resume() Activity onResume
 * - public void pause() Activity onPause
 * - public boolean hasVideoView() Video View가 생성되어 있는지 여부
 * - public View getLocalVideoView() Local VideoView 반환
 * - public View getRemoteVideoView() Remote VideoView 반환
 * - public void onOrienrationChanged (int orientation) 단말 회전시 처리
 * </pre>
 */
public interface IVideoGroupView{

	/* Activity onResume */
	public void resume();
	/* Activity onPause */
	public void pause();
	
	/**
	 * Video View가 생성되어 있는지 여부 
	 * @return boolean
	 */
	public boolean hasVideoView();
	/**
	 * Local VideoView 반환. 영상 출력을 위해 PlayRTCVideoView를 사용하지만, 
	 * Sample4에서는 일반 뷰를 사용하기 때문에 View로 반환한다.  
	 * @return View
	 */
	public View getLocalVideoView();
	/**
	 * Remote VideoView 반환. 영상 출력을 위해 PlayRTCVideoView를 사용하지만, 
	 * View로 반환한다.  
	 * @return View
	 */
	public View getRemoteVideoView();
	/**
	 * 단말 회전시 처리하기 위한 인터페이스 
	 * @param orientation
	 */
	public void onOrienrationChanged (int orientation);
}
