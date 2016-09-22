package com.playrtc.sample.view;

import android.content.Context;
import android.util.AttributeSet;

import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

/*
 * 리모트 영상 출력 뷰
 * PlayRTCVideoView(SurfaceView)를 상속
 */
public class RemoteVideoView extends PlayRTCVideoView {

    public RemoteVideoView(Context context) {
        super(context);
        // 레이어 중첩 시 로컬 영상 뷰 밑에 출력 되도록 렌더링 우선순위를 낮게 지정
        super.setZOrderMediaOverlay(false);
    }

    public RemoteVideoView(Context context, AttributeSet attrs) {
        super(context, attrs );
        // 레이어 중첩 시 로컬 영상 뷰 밑에 출력 되도록 렌더링 우선순위를 낮게 지정
        super.setZOrderMediaOverlay(false);
    }
}
