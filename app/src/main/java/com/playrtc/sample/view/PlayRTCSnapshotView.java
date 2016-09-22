package com.playrtc.sample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/*
 * PlayRTCVideoView의 Snapshot 기능과 이미지 배치 Layout을 구성하기 위한 부모뷰 그룹
 * RelativeLayout를 확장하여 Local뷰와 Remote뷰의 Snapshot 버튼과 이미지 출력뷰등을 생성하여 화면을 구성한다.
 */
public class PlayRTCSnapshotView extends RelativeLayout {

    /**
     * Snapshot 이미지 출력 뷰
     */
    private ImageView displayView = null;

    /**
     * SnapshotLayerObserver 인스턴스 객체
     */
    private SnapshotLayerObserver snapshotObserver = null;

    /**
     * Local뷰와 Remote뷰의 Snapshot 버튼 이벤트를 전달하기 위한 인터페이스 Class
     */
    public interface  SnapshotLayerObserver {
        /**
         * Local뷰와 Remote뷰의 Snapshot 버튼 이벤트를 전달
         * @param local boolean, Snapshot 대상이 Local뷰 인지  Remote뷰 인지 구분
         */
        public abstract void onClickSnapshotImage(boolean local);
    };

    public PlayRTCSnapshotView(Context context) {
        super(context);
    }

    public PlayRTCSnapshotView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayRTCSnapshotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     *  Snapshot 뷰의 Layout 크기를 PlayRTCVideoViewGroup과 같은 크기로 지정한다.
     */
    public void resetViewSize() {

        Point screenDimensions = new Point();
        int height = this.getHeight();

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        float width = (4.0f * height) / 3.0f;

        LayoutParams param = (LayoutParams)this.getLayoutParams();
        param.width = (int)width;
        param.height = (int)height;
        this.setLayoutParams(param);
    }

    /**
     * Snapshot 버튼과 이미지 배치등의 자식 요소를 동적으로 생성하여 Lauout 구성
     * @param observer SnapshotLayerObserver
     */
    public void createControls(SnapshotLayerObserver observer) {
        this.snapshotObserver = observer;

        /**
         * Snapshot 이미지 출력 뷰 생성, 화면의 중간에 위치
         */
        displayView = new ImageView(this.getContext());
        RelativeLayout.LayoutParams image_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        image_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        image_params.addRule(RelativeLayout.CENTER_VERTICAL);
        displayView.setLayoutParams(image_params);
        displayView.setBackgroundColor(Color.argb(100, 255, 255, 255));
        this.addView(displayView);

        /**
         * Local Snapshot 버튼 생성
         */
        Button btnLocal = new Button(this.getContext());
        btnLocal.setText("Local");
        btnLocal.setId(1);
        RelativeLayout.LayoutParams local_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        local_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        local_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        local_params.setMargins(20,20,10,20);
        btnLocal.setLayoutParams(local_params);
        this.addView(btnLocal);
        btnLocal.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(snapshotObserver != null) {
                    snapshotObserver.onClickSnapshotImage(true);
                }
            }
        });

        /**
         * Remote Snapshot 버튼 생성
         */
        Button btnRemote = new Button(this.getContext());
        btnRemote.setText("Remote");
        btnRemote.setId(2);
        RelativeLayout.LayoutParams remote_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        remote_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        remote_params.addRule(RelativeLayout.RIGHT_OF, 1);
        remote_params.setMargins(10,20,10,20);
        btnRemote.setLayoutParams(remote_params);
        this.addView(btnRemote);
        btnRemote.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(snapshotObserver != null) {
                    snapshotObserver.onClickSnapshotImage(false);
                }
            }
        });

        /**
         * Snapshot 이미지 Clear 버튼 생성
         */
        Button btnClear = new Button(this.getContext());
        btnClear.setText("Clear");
        btnClear.setId(3);
        RelativeLayout.LayoutParams clear_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        clear_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        clear_params.addRule(RelativeLayout.RIGHT_OF, 2);
        clear_params.setMargins(10,20,10,20);
        btnClear.setLayoutParams(clear_params);
        this.addView(btnClear);
        btnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                clear();
            }
        });

        /**
         * Snapshot 창 닫기 버튼 생성
         */
        Button btnClose = new Button(this.getContext());
        btnClose.setText("Close");
        btnClose.bringToFront();
        RelativeLayout.LayoutParams close_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        close_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        close_params.addRule(RelativeLayout.RIGHT_OF, 3);
        close_params.setMargins(10,20,10,20);
        btnClose.setLayoutParams(close_params);
        this.addView(btnClose);
        btnClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PlayRTCSnapshotView.this.setVisibility(View.INVISIBLE);
                clear();
            }
        });

    }

    /**
     * Snapshot 이미지를 뷰에 출력한다.
     */
    public void setSnapshotImage(Bitmap image) {
        displayView.setImageBitmap(image);
    }

    /**
     * Snapshot 이미지 Clear
     */
    private void clear() {
        displayView.setImageResource(android.R.color.transparent);
    }
}
