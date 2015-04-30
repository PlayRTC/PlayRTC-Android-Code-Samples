package com.playrtc.sample.sample1;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.playrtc.sample.view.IVideoGroupView;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;


/**
 * 영상 출력 뷰 의 부모 컨테이너 뷰로 RelativeLayout를 확장 <br>
 * IVideoGroupView implements
 * @see  com.playrtc.sample.view.IVideoGroupView
 */
public class Sample1ViewGroup extends RelativeLayout implements IVideoGroupView {

	// 로컬 영상 출력 뷰 
	private PlayRTCVideoView localView 		= null;
	// 상대방 영상 출력 뷰
	private PlayRTCVideoView remoteView 	= null;
	
	/**
	 * 4:3 비율로 4에 해당하는 크기, 화면 회전 시 4:3비율을 반대로 적용해서 사이즈 재조정할 때 사용 
	 */
	private int video4rate 					= 0;
	/**
	 * 4:3 비율로 3에 해당하는 크기, 화면 회전 시 4:3비율을 반대로 적용해서 사이즈 재조정할 때 사용 
	 */
	private int video3rate 					= 0;
	
	/**
	 * 생성자 
	 * @param context Context
	 */
	public Sample1ViewGroup(Context context) {
		super(context);

	}

	/**
	 * 생성자
	 * @param context Context
	 * @param attrs AttributeSet
	 */
	public Sample1ViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	
	/**
	 * 생성자
	 * @param context Context
	 * @param attrs AttributeSet
	 * @param defStyleAttr defStyleAttr
	 */
	public Sample1ViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	
	}
	
	/**
	 * 영상을 출력하기 위한 PlayRTCVideoView를 생성한다.
	 */
	public void createVideoView() {
		
		if(localView != null) {
			return;
		}
		/* video 스트림 출력을 위한 PlayRTCVideoView의 부모 ViewGroup의 사이즈 재조정 
		 * 가로-세로 비율 1(가로):0.75(세로), 높리 기준으로 폭 재지정 
		 */
		Point screenDimensions = new Point();
		int height = getHeight();
		float width = height / 0.75f;
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)getLayoutParams();
		param.width = (int)width;
		param.height = (int)height;
		setLayoutParams(param);

		screenDimensions.x = param.width;
		screenDimensions.y = param.height;
		
		// big
		if(remoteView == null)createRemoteVideoView (screenDimensions);
		// small
		if(localView == null)createLocalVideoView (screenDimensions);
	}
	
	/**
	 * IVideoGroupView 인터페이스 
	 */
	@Override
	public void resume()
	{
		if(localView != null){
			localView.resume();
			
		}
		if(remoteView != null) {
			remoteView.resume();
		}
	}
	
	/**
	 * IVideoGroupView 인터페이스 
	 */
	@Override
	public void pause()
	{
		if(localView != null){
			localView.pause();
		}
		if(remoteView != null) {
			remoteView.pause();
		}
	}
	
	/**
	 * IVideoGroupView 인터페이스 <br>
	 * Video View가 생성되어 있는지 여부 
	 * @return boolean
	 */
	@Override
	public boolean hasVideoView() {
		return localView == null ? false : true;
	}
	
	/**
	 * IVideoGroupView 인터페이스 <br>
	 * Local VideoView 반환. 영상 출력을 위해 PlayRTCVideoView를 사용하지만 View로 반환한다.
	 * @return View
	 */
	@Override
	public View getLocalVideoView() {
		return localView;
	}
	
	
	/**
	 * IVideoGroupView 인터페이스 <br>
	 * Remote VideoView 반환. 영상 출력을 위해 PlayRTCVideoView를 사용하지만 View로 반환한다.
	 * @return View
	 */
	@Override
	public View getRemoteVideoView() {
		return remoteView;
	}
	
	@Override
	public void onOrienrationChanged (int orientation) {
		
		switch (orientation)
		{
			case Configuration.ORIENTATION_PORTRAIT:
			{
				Point displaySize = new Point();
				displaySize.x = video3rate;
				displaySize.y = video4rate;
				localView.updateDisplaySize(displaySize);
			}
			  break;
			case Configuration.ORIENTATION_LANDSCAPE:
			{
				Point displaySize = new Point();
				displaySize.x = video4rate;
				displaySize.y = video3rate;
				localView.updateDisplaySize(displaySize);
			}
			  break;
			
			default:
		  
		}
	}
	

	/**
	 * Local 영상을 출력하기 위한 PlayRTCVideoView를 생성하여 부모뷰에 추가한다.<br>
	 * Local 영상을 화면 우상단에 Remote영상의 30% 크기로 줄여서 생성한다. 
	 * @param screenDimensions Point, 부모 뷰의 가로 세로 크기  
	 */
	private void createLocalVideoView (final Point screenDimensions) {
		// 자신의 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성 
		if(localView == null) {
			// 부모 View의 30% 비율로 크기를 지정한다
			Point displaySize = new Point();
			displaySize.x = (int)(screenDimensions.x*0.3);
			displaySize.y = (int)(screenDimensions.y*0.3);
			video4rate = displaySize.x;
			video3rate = displaySize.y;
			localView = new PlayRTCVideoView(this.getContext(), displaySize);
			
			// 화면 우상단에 위치 
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(displaySize.x, displaySize.y);
			param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			param.setMargins(30,30,30,30);
			
			localView.setLayoutParams(param);
			this.addView(localView);
			
			// remote 뷰 위에 위치하므로 ZOrder 지정 
			localView.setZOrderOnTop(true);
			localView.setVisibility(View.INVISIBLE);
			
			// 영상 크기가 변할 때 이벤트를 받기 위해서 
			localView.setVideoFrameObserver(new PlayRTCVideoView.VideoFrameObserver(){
				@Override
				public void onFrameSize(Point size) {
					Log.e("Local", "on Remote FrameSize "+size);
				}
			});
		}
	}
	
	/**
	 * Remote 영상을 출력하기 위한 PlayRTCVideoView를 생성하여 부모뷰에 추가한다.<br>
	 * Remote 영상을 뷰모뷰와 크기를 맞춘다. 
	 * @param screenDimensions Point, 부모 뷰의 가로 세로 크기  
	 */
	private void createRemoteVideoView (final Point screenDimensions) {
		// 자신의 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성 
		if(remoteView == null) {
			// 부모 View의 크기를 지정한다
			Point displaySize = new Point();
			displaySize.x = (int)(screenDimensions.x);
			displaySize.y = (int)(screenDimensions.y);
			remoteView = new PlayRTCVideoView(this.getContext(), displaySize);
			
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			remoteView.setLayoutParams(param);
			this.addView(remoteView);
			
			remoteView.setVisibility(View.VISIBLE);
			// 영상 크기가 변할 때 이벤트를 받기 위해서 
			remoteView.setVideoFrameObserver(new PlayRTCVideoView.VideoFrameObserver(){
				@Override
				public void onFrameSize(Point size) {
					Log.e("Remote", "on Remote FrameSize "+size);
				}
			});
		}
	}
}
