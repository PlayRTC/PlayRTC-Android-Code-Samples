package com.playrtc.sample.view;

import com.playrtc.sample.R;

import android.content.Context;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * PlayRTC 로그를 출력하기위해 TextView를 확장한 Class<br>
 * 
 * <pre>
 * - public void appendLog(final String msg)
 *    로그창 하단에 로그 문자열울 추가하고 스크롤를 하단으로 이동한다.
 * - public void progressLog(final String msg)
 *   로그창 하단의 마지막 라인을 갱신하고 스크롤를 하단으로 이동한다.
 *   데이터 채널 데이터 전송/수신 등의 진척도를 표시하기 위해 사용 
 * </pre>
 */
public class PlayRTCLogView extends TextView{

	private String prevText = null;
	private boolean hasPrevText = false;
	
	/**
	 * 생성자 
	 * @param context Context
	 */
	public PlayRTCLogView(Context context) {
		super(context);
		// 스크롤 갱신을 위해 
		this.setMovementMethod(new ScrollingMovementMethod());
	}
	/**
	 * 생성자 
	 * @param context Context
	 * @param attrs AttributeSet
	 */
	public PlayRTCLogView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 스크롤 갱신을 위해
		this.setMovementMethod(new ScrollingMovementMethod());
	}
	
	/**
	 * 생성자
	 * @param context Context
	 * @param attrs AttributeSet
	 * @param defStyleAttr int
	 */
	public PlayRTCLogView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// 스크롤 갱신을 위해
		this.setMovementMethod(new ScrollingMovementMethod());
	}
	
	/**
	 * 로그뷰를 화면에 출력 
	 */
	public void show() {
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.log_show);
		animation.setAnimationListener(new Animation.AnimationListener(){
			
			@Override
			public void onAnimationEnd(Animation anim) {
				
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
		
			}

			@Override
			public void onAnimationStart(Animation anim) {
				PlayRTCLogView.this.setVisibility(View.VISIBLE);
			}
			
		});
		this.startAnimation(animation);
		PlayRTCLogView.this.bringToFront();
		
	}
	
	/**
	 * 로그뷰를 화면에서 숨긴다. 
	 */
	public void hide() {
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.log_hide);
		animation.setAnimationListener(new Animation.AnimationListener(){

			@Override
			public void onAnimationEnd(Animation anim) {
				PlayRTCLogView.this.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
				
			}

			@Override
			public void onAnimationStart(Animation anim) {
				
			}
			
		});
		this.startAnimation(animation);
	}
	
	/**
	 * 로그 창 초기화 
	 */
	public void clear() {
		this.post(new Runnable(){
		   public void run()
		   {
			   PlayRTCLogView.this.setText("");
		   }
	   });
	}
	/**
	 * 로그창 하단에 로그 문자열울 추가하고 스크롤를 하단으로 이동한다.
	 * @param msg String, 로그 출력 메세지 
	 */
	public void appendLog(final String msg) {
		
		if(hasPrevText == true) {
			hasPrevText = false;
			prevText = null;
		}
	   this.post(new Runnable(){
		   public void run()
		   {
			   PlayRTCLogView.this.append(msg+"\n");
			   final Layout layout = PlayRTCLogView.this.getLayout();
		        if(layout != null){
		            int scrollDelta = layout.getLineBottom(PlayRTCLogView.this.getLineCount() - 1) 
		                - PlayRTCLogView.this.getScrollY() - PlayRTCLogView.this.getHeight();
		            if(scrollDelta > 0)
		            	PlayRTCLogView.this.scrollBy(0, scrollDelta);
		        }
		   }
	   });
		    
	}
	/**
	 * 로그창 하단의 마지막 라인을 갱신하고 스크롤를 하단으로 이동한다.<br>
	 * 데이터 채널 데이터 전송/수신 등의 진척도를 표시하기 위해 사용 
	 * @param msg String, 로그 출력 메세지 
	 */
	public void progressLog(final String msg) {
		if(hasPrevText == false) {
			hasPrevText = true;
			prevText = PlayRTCLogView.this.getText().toString();
		}
		this.post(new Runnable(){
		   public void run()
		   {
			   PlayRTCLogView.this.setText(prevText + msg+"\n");
			   
			   final Layout layout = PlayRTCLogView.this.getLayout();
		        if(layout != null){
		            int scrollDelta = layout.getLineBottom(PlayRTCLogView.this.getLineCount() - 1) 
		                - PlayRTCLogView.this.getScrollY() - PlayRTCLogView.this.getHeight();
		            if(scrollDelta > 0)
		            	PlayRTCLogView.this.scrollBy(0, scrollDelta);
		        }
		   }
		});
		    
	}
}
