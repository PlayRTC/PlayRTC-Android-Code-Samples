package com.playrtc.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.playrtc.sample.handler.PlayRTCHandler;
import com.playrtc.sample.handler.PlayRTCDataChannelHandler;
import com.playrtc.sample.handler.PlayRTCChannelViewListener;
import com.playrtc.sample.view.PlayRTCChannelView;
import com.playrtc.sample.view.PlayRTCLogView;
import com.playrtc.sample.view.PlayRTCVideoViewGroup;
import com.playrtc.sample.view.PlayRTCSnapshotView;
import com.playrtc.sample.view.PlayRTCSnapshotView.SnapshotLayerObserver;

import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView.SnapshotObserver;

/*
 * PlayRTC를 구현한 Activity Class
 *
 * 주요 멤버
 * - PlayRTCHandler playrtcHandler
 *     PlayRTC 인스턴스 및 관련 기능을 제공
 *     PlayRTCObserver Listener Interface 구현
 *
 * - PlayRTCDataChannelHandler dataHandler
 *     PlayRTCData를 위한 PlayRTCDataObserver Interface를 구현한 Handler Class
 *     PlayRTCData를 이용해 데이터 송/수신 처리
 *
 * - PlayRTCVideoViewGroup videoLayer
 *     영상 출력 뷰(PlayRTCVideoView)의 ViewGroup .
 *     Local/Remote 뷰 생성 및 인터페이스 제공
 *
 * - PlayRTCChannelView channelInfoView
 *     Sample에서 채널을 생성하거나 채널 목록을 조회하여 입장 할 채널을 선택하는 팝업 뷰
 *     생성/입장 버튼 이벤트를 받기 위해 PlayRTCChannelViewListener를 구현한다.
 *
 * - PlayRTCLogView logView
 *     PlayRTC 로그를 출력하기위해 TextView를 확장한 Class
 */
public class PlayRTCActivity extends Activity {
    private static final String LOG_TAG = "PlayRTCActivity";

    /*
     * 채널 팝업 뷰
     * 채널 서비스에 채널을 생성하거나 입장할 채널을 선택하는 UI
     *
     * @see com.playrtc.sample.view.PlayRTCChannelView
     */
    private PlayRTCChannelView channelInfoView = null;


    /*
     * PlayRTC-Handler Class
     * PlayRTC 메소드 , PlayRTC객체의 이벤트 처리
     */
    private PlayRTCHandler playrtcHandler = null;

    /*
     * PlayRTCVideoView를 위한 부모 뷰 그룹
     */
    private PlayRTCVideoViewGroup videoLayer = null;

    /*
     * PlayRTCData를 위한 Handler Class
     *
     * @see com.playrtc.sample.handler.PlayRTCDataChannelHandler
     */
    private PlayRTCDataChannelHandler dataHandler = null;


    /*
     * 로그 출력 TextView
     *
     * @see com.playrtc.sample.view.PlayRTCLogView
     */
    private PlayRTCLogView logView = null;

    /*
     * PlayRTC P2P Status report 출력 TextView
     */
    private TextView txtStatReport	= null;


    /*
     * 영상 뷰 Snapshot 이미지 요청 및 이미지 출력을 위한 뷰 그룹
     */
    private PlayRTCSnapshotView snapshotLayer = null;

    /*
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리. 만약 채널에 입장한 상태이면 먼저 채널을 종료한다.
     */
    private boolean isCloesActivity = false;

    /*
     * 영상 뷰를 사용하지 않는 경우 로그 뷰를 화면 중앙에 1회 위치 시키기 위한 변수
     * onWindowFocusChanged에서 로그뷰 Layout을 조정 하므로 필요함.
     */
    private boolean isResetLogViewArea = false;

    /*
     * PlayRTC Sample Type
     * - 1 : 영상 + 음성 + Data Sample<br>
     * - 2 : 영상 + 음성 Sample<br>
     * - 3 : 음성 Sample<br>
     * - 4 : Data Sample<br>
     */
    private int playrtcType = 1;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        Intent intent = getIntent();

        /*
         * PlayRTC Sample Type
         * - 1. 영상, 음성, p2p data
         * - 2. 영상, 음성
         * - 3. 음성, data
         * - 4. p2p data only
         */
        playrtcType = intent.getIntExtra("type", 1);

        // UI 인스턴스 변수 처리
        initUIControls();

        if(playrtcType < 3) {
            initSnapshotControlls();
        }

        playrtcHandler = new PlayRTCHandler(this);
        try {
            //  PlayRTC 인스턴스를 생성.
            playrtcHandler.createPlayRTC(playrtcType);
        } catch (UnsupportedPlatformVersionException e) {
            // Android SDK 버전 체크 Exception
            e.printStackTrace();
        } catch (RequiredParameterMissingException e) {
            // 필수 Parameter 체크 Exception
            e.printStackTrace();
        }

        // P2P 데이터 통신을 위한 객체 생성
        this.dataHandler = new PlayRTCDataChannelHandler(this);

        // 채널 생성/입장 팝업 뷰 초기화 설정
        this.channelInfoView.init(this, playrtcHandler.getPlayRTC(), new PlayRTCChannelViewListener(this));

        // PlayRTC 채널 서비스에서 채멀 목록을 조회하여 리스트에 출력한다.
        this.channelInfoView.showChannelList();
        // 채널 생성 또는 채널 입장하기 위한 팝업 레이어 출력
        this.channelInfoView.show(600);

    }

    // Activty의 포커스 여부를 확인
    // 영상 스트림 출력을 위한 PlayRTCVideoView(GLSurfaceView를 상속) 동적 코드 생성
    // 생성 시 스크린 사이즈를 생성자에 넘김
    // hasFocus = true , 화면보여짐 , onCreate | onResume
    // hasFocus = false , 화면안보임 , onPause | onDestory
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (playrtcType == 3 || playrtcType == 4) {
            if(hasFocus && isResetLogViewArea == false) {
                resetLogViewArea();
            }
            return;
        }
        /*
        // Layout XML을 사용하지 않고 소스 코드에서 직접 샹성하는 경우
        if (hasFocus && videoLayer.isCreatedVideoView() == false) {

            // 4. 영상 스트림 출력을 위한 PlayRTCVideoView 동적 생성
            videoLayer.createVideoView();

        }
        */
        // Layout XML에 VideoView를 기술한 경우. v2.2.6
        if (hasFocus && videoLayer.isInitVideoView() == false) {

            // 4. 영상 스트림 출력을 위한 PlayRTCVideoView 초기화
            videoLayer.initVideoView();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 미디어 스트리밍 처리 pause
        if(playrtcHandler != null)playrtcHandler.onActivityPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 미디어 스트리밍 처리 resume
        if (playrtcHandler != null) playrtcHandler.onActivityResume();
    }

    @Override
    protected void onDestroy() {
        Log.e(LOG_TAG, "onDestroy===============================");

        // PlayRTC 인스턴스 해제
        if(playrtcHandler != null) {
            playrtcHandler.close();
            playrtcHandler = null;
        }
        // v2.2.6
        if(videoLayer != null) {
            videoLayer.releaseView();
        }
        this.finish();
        super.onDestroy();
    }

    /*
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리. 만약 채널에 입장한 상태이면 먼저 채널을 종료한다.
     */
    @Override
    public void onBackPressed() {
        Log.e(LOG_TAG, "onBackPressed===============================");

        // 채널 팝업이 보여지는 상태에서 onBackPressed()가 호출 되면
        // 팝업 창을 닫기 만 한다.
        if(channelInfoView.isShown()) {
            channelInfoView.hide(0);
            return;
        }

        // Activity를 종료하도록 isCloesActivity가 true로 지정되어 있다면 종료 처리
        if (isCloesActivity) {
            // BackPress 처리 -> onDestroy 호출
            Log.e(LOG_TAG, "super.onBackPressed()===============================");
            setResult(RESULT_OK, new Intent());
            super.onBackPressed();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("PlayRTC");
            alert.setMessage("PlayRTC를 종료하겠습니까?");

            alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // 채널에 입장한 상태라면 채널을 먼저 종료한다.
                    // 종료 이벤트에서 isCloesActivity를 true로 설정하고 onBackPressed()를 호출하여
                    // Activity를 종료 처리
                    if(playrtcHandler.isChannelConnected() == true) {
                        isCloesActivity = false;
                        // PlayRTC 플랫폼 채널을 종료한다.
                        playrtcHandler.disconnectChannel();

                    }
                    // 채널에 입장한 상태가 아니라면 바로 종료 처리
                    else {
                        isCloesActivity = true;
                        onBackPressed();
                    }
                }
            });
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                    isCloesActivity = false;
                }
            });
            alert.show();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        switch (this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {

            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {

            }
            break;

            default:

        }
        super.onConfigurationChanged(newConfig);
    }

    /*
     * PlayRTCHandler 인스턴스를 반환한다.
     * @return PlayRTCHandler
     */
    public PlayRTCHandler getPlayRTCHandler() {
        return playrtcHandler;
    }

    public PlayRTCVideoViewGroup getVideoLayer() {
        return videoLayer;
    }
    /*
     * 로컬 영상 PlayRTCVideoView 인스턴스를 반환한다.
     * @return PlayRTCVideoView
     */
    public PlayRTCVideoView getLocalVideoView() {
        return videoLayer.getLocalView();
    }

    /*
     * 상대방 영상 PlayRTCVideoView 인스턴스를 반환한다.
     * @return PlayRTCVideoView
     */
    public PlayRTCVideoView getRemoteVideoView() {
        return videoLayer.getRemoteView();
    }

    /*
     * PlayRTCActivity를 종료한다.
     * PlayRTCHandler에서 채널이 종료 할 때 호출한다.
     * @param isClose boolean, 종료 처리 시 사용자의 종료 으의사를 묻는 여부
     */
    public void setOnBackPressed(boolean isClose) {
        if(channelInfoView.isShown()) {
            channelInfoView.hide(0);
        }
        isCloesActivity = isClose;
        this.onBackPressed();
    }

    /*
     * PlayRTCDataChannelHandler 인스턴스를 반환한다.
     * @return PlayRTCDataChannelHandler
     */
    public PlayRTCDataChannelHandler getRtcDataHandler() {
        return dataHandler;
    }

    /*
     * PlayRTCChannelView 인스턴스를 반환한다.
     * @return PlayRTCChannelView
     */
    public PlayRTCChannelView getChannelInfoPopup() {
        return channelInfoView;
    }

    /*
     * PlayRTCLogView의  하단에 로그 문자열을 추가 한다.
     * @param message String
     */
    public void appnedLogMessage(String message) {
        if (logView != null) {
            logView.appnedLogMessage(message);
        }
    }

    /*
     * PlayRTCLogView의 최 하단에 로그 문자열을 추가 한다. <br>
     * 주로 진행 상태 메세지를 표시 하기 위해 최 하단의 진행 상태 메세지만 갱신한다.
     * @param message String
     */
    public void progressLogMessage(String message) {
        if(logView != null) {
            logView.progressLogMessage(message);
        }
    }

    /*
     * PlayRTC P2P 상태 문자열을 출력한다.
     * @param resport
     */
    public void printRtcStatReport(final String resport) {
        txtStatReport.post(new Runnable() {
            public void run() {
                txtStatReport.setText(resport);

            }
        });
    }

    /*
     * Layout 관련 인스턴스 설정 및 이벤트 정의
     */
    private void initUIControls() {

        /* 채널 팝업 뷰 */
        channelInfoView = (PlayRTCChannelView) findViewById(R.id.channel_info);

		/*video 스트림 출력을 위한 PlayRTCVideoView의 부모 ViewGroup */
        videoLayer = (PlayRTCVideoViewGroup) findViewById(R.id.videoarea);

        /*video 스트림 출력을 위한 PlayRTCVideoView의 부모 ViewGroup */
        videoLayer = (PlayRTCVideoViewGroup) findViewById(R.id.videoarea);

		/* 로그 출력 TextView */
        logView = (PlayRTCLogView) this.findViewById(R.id.logtext);

        snapshotLayer = (PlayRTCSnapshotView)this.findViewById(R.id.snapshot_area);

        /* PlayRTC P2P Status report 출력 TextView */
        txtStatReport = (TextView)this.findViewById(R.id.txt_stat_report);
        String text = "Local\n ICE:none\n Frame:0x0x0\n Bandwidth[0bps]\n RTT[0]\n eModel[-]\n VFLost[0]\n AFLost[0]\n\nRemote\n ICE:none\n Frame:0x0x0\n Bandwidth[0bps]\n VFLost[0]\n AFLost[0]";
        txtStatReport.setText(text);


        /* 채널 팝업 버튼 */
        Button channelPopup = (Button) this.findViewById(R.id.btn_channel);
        channelPopup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (channelInfoView.isShown()) {
                    channelInfoView.hide(0);
                } else {
                    channelInfoView.showChannelList();
                    channelInfoView.show(0);
                }
            }
        });
        Button cameraBtn = (Button) this.findViewById(R.id.btn_switch_camera);

		/* 카메라 전환 */
        if (playrtcType == 1 || playrtcType == 2) {
            cameraBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playrtcHandler.switchVideoCamera();
                }
            });
        } else {
            // 영상 전송을 사용하지 않으므로 화면에서 숨긴다.
            cameraBtn.setVisibility(View.INVISIBLE);
        }
        Button flashBtn = (Button) this.findViewById(R.id.btn_switch_flash);

		/* 후방 카메라 플래쉬 On/Off, 후방 카메라 사용 시 작동  */
        if (playrtcType == 1 || playrtcType == 2) {
            flashBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playrtcHandler == null) {
                        return;
                    }
                    playrtcHandler.switchBackCameraFlash();
                }
            });

        } else {
            // 영상 전송을 사용하지 않으므로 화면에서 숨긴다.
            flashBtn.setVisibility(View.INVISIBLE);
        }

		/* DataChannel Text 전송 버튼 */
        Button btnText = (Button) this.findViewById(R.id.btn_text);
		/* DataChannel 파일 전송 버튼 */
        Button btnFile = (Button) this.findViewById(R.id.btn_file);

        if (playrtcType == 1 || playrtcType == 4) {
            btnText.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataHandler.sendText();
                }
            });
            btnFile.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataHandler.sendFile();
                }
            });
        } else {
            // DataChannel을 사용하지 않으므로 화면에서 숨긴다.
            btnText.setVisibility(View.INVISIBLE);
            btnFile.setVisibility(View.INVISIBLE);
        }

		/* 로그뷰  토글 버튼 이벤트 처리 */
        Button btnLog = (Button) this.findViewById(R.id.btn_log);
        if (playrtcType == 1 || playrtcType == 2) {
            btnLog.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    if (logView.isShown() == false) {
                        logView.show();
                        ((Button) v).setText("로그닫기");
                    } else {
                        logView.hide();
                        ((Button) v).setText("로그보기");
                    }
                }
            });
        } else {
            btnLog.setVisibility(View.GONE);
            logView.setVisibility(View.VISIBLE);
        }

		/* Peer 채널 퇴장 버튼 */
        Button btnDisconnectChannel = (Button)this.findViewById(R.id.btn_peerChClose);
        btnDisconnectChannel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playrtcHandler != null && playrtcHandler.isChannelConnected()){

                    playrtcHandler.disconnectChannel();
                }
            }
        });

		/*  채널 종료 버튼 */
        Button btnCloseChannel = (Button)this.findViewById(R.id.btn_chClose);
        btnCloseChannel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playrtcHandler != null && playrtcHandler.isChannelConnected()){
                    playrtcHandler.delateChannel();
                }
            }
        });


		/* Local Video Mute 버튼 */
        Button btnMuteLVideo = (Button) this.findViewById(R.id.btn_local_vmute);
		/* Local Video Mute 처리시 로컬 영상 스트림은 화면에 출력이 안되며 상대방에게 전달이 되지 않는다. */
        btnMuteLVideo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if(playrtcHandler != null) {
                    String text = (String)b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    playrtcHandler.setLocalVideoPause(setMute);
                    b.setText((setMute == true)?"VIDEO-ON" : "VIDEO-OFF");
                }
            }
        });

		/* Local Audio Mute 버튼 */
        Button btnMuteLAudio = (Button) this.findViewById(R.id.btn_local_amute);
		/* Local Audio Mute 처리시 로컬 음성 스트림은 상대방에게 전달이 되지 않는다. */
        btnMuteLAudio.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if(playrtcHandler != null) {
                    String text = (String)b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    playrtcHandler.setLocalAudioMute(setMute);
                    b.setText((setMute == true)?"AUDIO-ON" : "AUDIO-OFF");
                }
            }
        });

		/* Remote Video Mute 버튼 */
        Button btnMuteRVideo = (Button) this.findViewById(R.id.btn_remote_vmute);
		/* Remote Video Mute 처리시 상대방 영상 스트림은 화면에만 출력이 안된다. */
        btnMuteRVideo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if(playrtcHandler != null) {
                    String text = (String)b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    playrtcHandler.setRemoteVideoPause(setMute);
                    b.setText((setMute == true)?"VIDEO-ON" : "VIDEO-OFF");
                }
            }
        });

		/* Remote Audio Mute 버튼 */
        Button btnMuteRAudio = (Button) this.findViewById(R.id.btn_remote_amute);
		/* Remote Video Mute 처리시 상대방 영상 스트림은 소리만 출력이 안된다. */
        btnMuteRAudio.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if(playrtcHandler != null) {
                    String text = (String)b.getText();
                    boolean setMute = text.endsWith("-OFF");
                    playrtcHandler.setRemoteAudioMute(setMute);
                    b.setText((setMute == true)?"AUDIO-ON" : "AUDIO-OFF");
                }
            }
        });

         /* snapshot 레이어 보기 버튼 */
        Button btnShowSnapshot = (Button)this.findViewById(R.id.btn_show_snapshot);
        if (playrtcType == 1 || playrtcType == 2) {


            btnShowSnapshot.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(snapshotLayer.isShown() == false){

                        snapshotLayer.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void initSnapshotControlls() {
        if(snapshotLayer != null && videoLayer != null) {

            // Snapshot 버튼과 이미지 배치등의 자식 요소를 동적으로 생성하여 Layout 구성
            snapshotLayer.createControls(new SnapshotLayerObserver(){

                @Override
                public void onClickSnapshotImage(boolean local) {
                    if(local && videoLayer.getLocalView() != null) {

                        /*
                         * Snapshot 이미지 요청
                         */
                        videoLayer.getLocalView().snapshot(new PlayRTCVideoView.SnapshotObserver(){

                            @Override
                            public void onSnapshotImage(Bitmap image) {
                                int w = image.getWidth();
                                int h = image.getHeight();
                                Log.e("SNAP-SHOT", "snapshot Bitmap["+w+"x"+h+"].....");

                                /*
                                 * Snapshot 이미지 출력
                                 */
                                snapshotLayer.setSnapshotImage(image);

                            }

                        });
                    }
                    else if(local == false && videoLayer.getRemoteView() != null) {

                        /*
                         * Snapshot 이미지 요청
                         */
                        videoLayer.getRemoteView().snapshot(new PlayRTCVideoView.SnapshotObserver(){

                            @Override
                            public void onSnapshotImage(Bitmap image) {
                                int w = image.getWidth();
                                int h = image.getHeight();
                                Log.e("SNAP-SHOT", "snapshot Bitmap["+w+"x"+h+"].....");

                                /*
                                 * Snapshot 이미지 출력
                                 */
                                snapshotLayer.setSnapshotImage(image);

                            }

                        });
                    }

                }

            });
        }
    }
    private void resetLogViewArea() {
        if(isResetLogViewArea == true) {
            return;
        }

        Point screenDimensions = new Point();
        int height = videoLayer.getHeight();

        // ViewGroup의 사이즈 재조정, 높이 기준으로 4(폭):3(높이)으로 재 조정
        // 4:3 = width:height ,  width = ( 4 * height) / 3
        float width = (4.0f * height) / 3.0f;


        RelativeLayout.LayoutParams logLayoutparam = new RelativeLayout.LayoutParams((int)width, (int)height);
        logLayoutparam.addRule(RelativeLayout.CENTER_VERTICAL);
        logLayoutparam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        logView.setLayoutParams(logLayoutparam);

        RelativeLayout.LayoutParams videoLayoutparam = new RelativeLayout.LayoutParams((int)width, (int)height);
        videoLayoutparam.addRule(RelativeLayout.CENTER_VERTICAL);
        videoLayoutparam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        videoLayer.setLayoutParams(videoLayoutparam);

        isResetLogViewArea = true;

    }
}
