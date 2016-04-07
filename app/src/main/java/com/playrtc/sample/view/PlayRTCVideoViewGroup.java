package com.playrtc.sample.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

public class PlayRTCVideoViewGroup extends RelativeLayout {

    private static final String LOG_TAG = "VIDEO-VIEW";
    /**
     /**
     * 로컬 영상 출력 뷰
     */
    private PlayRTCVideoView localView = null;

    /**
     * 상대방 영상 출력 뷰
     */
    private PlayRTCVideoView remoteView = null;


    public PlayRTCVideoViewGroup(Context context) {
        super(context);
    }

    public PlayRTCVideoViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayRTCVideoViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 영상 출력을 위한 PlayRTCVideoView를 사이즈를 계산하여 생성한다. <br>
     * PlayRTCVideoViewGroup 크기를 높이 기준으로 4(폭):3(높이)으로 재 조정하고, <br>
     * Remote 뷰는 PlayRTCVideoViewGroup 크기에 맞게 생성하고 <br>
     * Local 뷰는 Remote 뷰 크기의 30%로 좌상단에 생성한다. <br>
     * createVideoView는 Activity의 onWindowFocusChanged에서 화면에 보여 질 때(사이즈 확인 가능 시점) 호출(최초 1번) 한다.
     */
    public void createVideoView() {
        // 이미 뷰를 생성 했는지 체크
        if(isCreatedVideoView() == true)
        {
            return;
        }
        // PlayRTCVideoView의 부모 ViewGroup의 사이즈 확인
        Point screenDimensions = new Point();
        int height = this.getHeight();

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        float width = (4.0f * height) / 3.0f;

        LayoutParams param = (LayoutParams)this.getLayoutParams();
        param.width = (int)width;
        param.height = (int)height;
        this.setLayoutParams(param);

        screenDimensions.x = param.width;
        screenDimensions.y = param.height;

        // big, remote, 4(폭):3(높이)
        createRemoteVideoView (screenDimensions);
        // small. local , 4(폭):3(높이) 30%
        createLocalVideoView (screenDimensions);
    }

    public void onActivityPause() {
        if(localView != null) {
            localView.pause();
        }
        if(remoteView != null) {
            remoteView.pause();
        }
    }

    public void onActivityResume() {
        if(localView != null) {
            localView.resume();
        }
        if(remoteView != null) {
            remoteView.resume();
        }
    }

    /**
     * 영상 뷰를 생성했는지 여부를 반환한다. <br>
     * 영상 뷰 샹성 여부를 체크하는 이유는 Sample App에서 영상뷰 생성 시 사이즈 지정을 위해  <br>
     * Activity의 onWindowFocusChanged에서 화면에 보여 질 때 최초 1번 createVideoView()를 호출하기 위해서 임.
     * @return boolean
     *
     */
    public boolean isCreatedVideoView() {
        if(localView == null && remoteView == null)
        {
            return false;
        }

        return true;
    }

    /**
     * Local 영상 뷰를 반환한다.
     * @return PlayRTCVideoView
     */
    public PlayRTCVideoView getLocalView() {
        return localView;
    }

    /**
     * Remote 영상 뷰를 반환한다.
     * @return PlayRTCVideoView
     */
    public PlayRTCVideoView getRemoteView() {
        return remoteView;
    }

    /**
     * Local 뷰는 PlayRTCVideoViewGroup 크기의 30%로 생성하여 좌상단에 배치한다.<br>
     * @param screenDimensions Point, PlayRTCVideoViewGroup 크기
     */
    private void createLocalVideoView (final Point screenDimensions) {
        // 자신의 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if(localView == null) {
            // 부모 View의 30% 비율로 크기를 지정한다
            Point displaySize = new Point();
            displaySize.x = (int)(screenDimensions.x*0.3);
            displaySize.y = (int)(screenDimensions.y*0.3);

			/*
			 * PlayRTCVideoView 생성자
			 * @param context Context
			 * @param dimensions Point
			 * @param mirror boolean, 영상 출력을 거울 모드로 할지 여부를 지정한다.<br>
			 *        true로 지정하면 거울로 보는것처럼 오른쪽이 화면의 오른쪽으로 출력된다.<br>
			 *        주로 로컬 영상의 경우 거울 모드로 지정한다.
			 */
            localView = new PlayRTCVideoView(this.getContext(), displaySize, true);
			/*
			 * 화면 배경색을 지정한다. R,G,B,A 0 ~ 255 정수
			 * 영상 스트림이 출력 되기 전, bgClearColor() 호출 시 지정한 색으로 배경을 칠한다.
			 * v2.2.5 추가
			 */
            localView.setBgClearColor(225, 225, 225, 255);
            localView.hide(0);
            LayoutParams param = new LayoutParams(displaySize.x, displaySize.y);
            param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            param.setMargins(30,30,30,30);

            localView.setLayoutParams(param);
            // 부모뷰에 PlayRTCVideoView 추가
            this.addView(localView);
            localView.setZOrderOnTop(true);
            //localView.setVisibility(View.INVISIBLE);
            localView.setVideoFrameObserver(new PlayRTCVideoView.VideoRendererObserver(){
                @Override
                public void onFrameResolutionChanged(PlayRTCVideoView view, int videoWidth, int videoHeight, int rotationDegree) {
                    Log.e(LOG_TAG, "Local FrameResolution videoWidth[" + videoWidth + "] videoHeight["+videoHeight+"] rotationDegree["+rotationDegree+"]");
                }
            });
        }
    }

    /**
     * Remote 뷰는 PlayRTCVideoViewGroup 크기에 맞게 생성하여 배치한다.
     * @param screenDimensions Point, PlayRTCVideoViewGroup 크기
     */
    private void createRemoteVideoView (final Point screenDimensions) {
        // 상대방 스트림 출력을 위한 PlayRTCVideoView 객체 동적 생성
        if(remoteView == null) {
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
            remoteView = new PlayRTCVideoView(this.getContext(), displaySize, false);
			/*
			 * 화면 배경색을 지정한다. R,G,B,A 0 ~ 255 정수
			 * 영상 스트림이 출력 되기 전, bgClearColor() 호출 시 지정한 색으로 배경을 칠한다.
			 * v2.2.5 추가
			 */
            remoteView.setBgClearColor(200, 200, 200, 255);
            LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            remoteView.setLayoutParams(param);
            // 부모뷰에 PlayRTCVideoView 추가
            this.addView(remoteView);
            remoteView.setVideoFrameObserver(new PlayRTCVideoView.VideoRendererObserver(){
                @Override
                public void onFrameResolutionChanged(PlayRTCVideoView view, int videoWidth, int videoHeight, int rotationDegree) {
                    Log.i(LOG_TAG, "Remote FrameResolution videoWidth[" + videoWidth + "] videoHeight["+videoHeight+"] rotationDegree["+rotationDegree+"]");
                }

            });
        }
    }
}
