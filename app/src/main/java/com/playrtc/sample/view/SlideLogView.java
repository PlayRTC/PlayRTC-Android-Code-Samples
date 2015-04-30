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
 * 로그뷰 출력을 위한 TextView 확장 클래스 
 *
 */
public class SlideLogView extends TextView{

	public SlideLogView(Context context) {
		super(context);
		this.setMovementMethod(new ScrollingMovementMethod());
	}

	public SlideLogView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setMovementMethod(new ScrollingMovementMethod());
	}
	
	public SlideLogView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.setMovementMethod(new ScrollingMovementMethod());
	}
	
	/**
	 * 로그 뷰를 화면에 보여준다. 
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
				SlideLogView.this.setVisibility(View.VISIBLE);
			}
			
		});
		this.startAnimation(animation);
		SlideLogView.this.bringToFront();
		
	}
	/**
	 * 로그 뷰를 화면에서 숨긴다. 
	 */
	public void hide() {
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.log_hide);
		animation.setAnimationListener(new Animation.AnimationListener(){

			@Override
			public void onAnimationEnd(Animation anim) {
				SlideLogView.this.setVisibility(View.INVISIBLE);
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
	 * 로그 출력 내용을 전부 지운다.
	 */
	public void clear() {
		this.post(new Runnable(){
		   public void run()
		   {
			   SlideLogView.this.setText("");
		   }
	   });
	}
	
	/**
	 * 로그 내용을 맨끝에 추가한다.
	 * 
	 * @param msg String, 로그 메세지 
	 */
	public void appendLog(final String msg) {
	   this.post(new Runnable(){
		   public void run()
		   {
			   SlideLogView.this.append(msg+"\n");
			   // 스트롤 자동이동
			   final Layout layout = SlideLogView.this.getLayout();
		        if(layout != null){
		            int scrollDelta = layout.getLineBottom(SlideLogView.this.getLineCount() - 1) 
		                - SlideLogView.this.getScrollY() - SlideLogView.this.getHeight();
		            if(scrollDelta > 0)
		            	SlideLogView.this.scrollBy(0, scrollDelta);
		        }
		   }
	   });
		    
	}
}