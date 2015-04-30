package com.playrtc.sample.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Sample4 ScreenCast 에제에서 로컬 카메라 영상 대신 Capture 이미지를 얻는 대상 View<br>
 * 내부적으로 Touch 이베트를 받아 선을 Drawing 하도록 구성 
 *
 */
public class DrawingView  extends View implements View.OnTouchListener{
	private static final String LOG_TAG = "DrawingView"; 
	private Paint paint = null;


	ArrayList<Vertex> arVertex;
	public DrawingView(Context context) {
		super(context);
		initView();
	}
	
	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	private void initView() {
		Log.d(LOG_TAG, "initView");
		this.setClickable(true);
		this.setFocusable(true);
		this.setOnTouchListener(this);
		arVertex = new ArrayList<Vertex>();
		paint = new Paint();
		paint.setStrokeWidth(3);
		paint.setAntiAlias(true);
	}
	
	public class Vertex{
        float x;
        float y;
        boolean draw;   // 그리기 여부
         
        public Vertex(float x, float y, boolean draw){
            this.x = x;
            this.y = y;
            this.draw = draw;
        }
    };
    
	public void clearCanvas()
	{
		arVertex = new ArrayList<Vertex>();
		this.invalidate();
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int act = event.getAction();
		float posX = event.getX();
		float posY = event.getY();
		if(act == MotionEvent.ACTION_DOWN)
		{
			
			arVertex.add(new Vertex(posX, posY, false));
		}
		else if(act == MotionEvent.ACTION_MOVE)
		{
			arVertex.add(new Vertex(posX, posY, true));
		}
		else if(act == MotionEvent.ACTION_UP)
		{
			arVertex.add(new Vertex(posX, posY, true));
		}
		this.invalidate();
		return true;
	}
	
	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 그리기
        for(int i=0; i<arVertex.size(); i++){
            if(arVertex.get(i).draw){       // 이어서 그리고 있는 중이라면      
                canvas.drawLine(arVertex.get(i-1).x, arVertex.get(i-1).y, 
                        arVertex.get(i).x, arVertex.get(i).y, paint);
                        // 이전 좌표에서 다음좌표까지 그린다.
            }else{      
                canvas.drawPoint(arVertex.get(i).x, arVertex.get(i).y, paint);
                // 점만 찍는다.
            }
        }
	}
}
