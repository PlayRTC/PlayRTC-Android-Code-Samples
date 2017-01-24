package com.playrtc.sample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

/*
 * PlayRTC Sample App Main Activity Class
 * Sample 구현
 *
 * 1. 영상, 음성, p2p data
 * 2. 영상, 음성
 * 3. 음성, data
 * 4. p2p data only
 *
 */
public class MainActivity extends Activity {

    private static final String LOG_TAG = "MainActivity";

    private static final int LAUNCHED_PLAYRTC = 100;
    /**
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리.
     */
    private boolean isCloesActivity = false;

    private final int MY_PERMISSION_REQUEST_STORAGE = 100;
    /**
     * Application permission 목록, android build target 23
     */
    public static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };


    private String channelRing = "false";
    private String videoCodec = "vp8";
    private String audioCodec = "isac";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 버튼 이벤트 등록
        initUIControls();

        // Application permission 23
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            checkPermission(MANDATORY_PERMISSIONS);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LAUNCHED_PLAYRTC) {
            if (resultCode == RESULT_OK) {
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    /**
     * isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인하고<br>
     * Activity를 종료 처리.
     */
    @Override
    public void onBackPressed() {
        // isCloesActivity가 true이면 Activity를 종료 처리.
        if (isCloesActivity) {
            super.onBackPressed();
        }
        // isCloesActivity가 false이면 Dialog를 통해 사용자의 종료 의사를 확인
        else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("PlayRTC Sample");
            alert.setMessage("PlatRTC Sample App을 종료하겠습니까?");

            alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    isCloesActivity = true;
                    onBackPressed();
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

    /**
     * Sample Type 별 버튼 이벤트 등록
     */
    private void initUIControls() {



        // 영상 + 음성 + Data Sample
        this.findViewById(R.id.btn_go_sample1).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                excutePlayRTCSample(1);
            }
        });
        // 영상 + 음성 Sample
        this.findViewById(R.id.btn_go_sample2).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                excutePlayRTCSample(2);
            }
        });
        // 음성 only Sample
        this.findViewById(R.id.btn_go_sample3).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                excutePlayRTCSample(3);
            }
        });
        //  Data  only Sample
        this.findViewById(R.id.btn_go_sample4).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                excutePlayRTCSample(4);
            }
        });

        ((RadioGroup)findViewById(R.id.radio_ring_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_ring_false) {
                    channelRing = "false";
                }
                else {
                    channelRing = "true";
                }

            }
        });
        ((RadioGroup)findViewById(R.id.radio_video_codec_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_video_codec_vp8) {
                    videoCodec = "vp8";
                }
                else if(checkedId == R.id.radio_video_codec_vp9) {
                    videoCodec = "vp9";
                }
                else {
                    videoCodec = "h264";
                }

            }
        });
        ((RadioGroup)findViewById(R.id.radio_audio_codec_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radio_audio_codec_isac) {
                    audioCodec = "isac";
                }
                else {
                    audioCodec = "opus";
                }

            }
        });
    }

    @TargetApi(23)
    private void checkPermission(String[] permissions) {

        requestPermissions(permissions, MY_PERMISSION_REQUEST_STORAGE);
    }

    /**
     * Application permission, android build target 23
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                int cnt = permissions.length;
                for(int i = 0; i < cnt; i++ ) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED ) {

                        Log.i(LOG_TAG, "Permission[" + permissions[i] + "] = PERMISSION_GRANTED");

                    } else {

                        Log.i(LOG_TAG, "permission[" + permissions[i] + "] always deny");
                    }
                }
                break;
        }
    }

    /**
     * PlayRTCActivity 이동
     *
     * @param type int
     * <pre>
     * 1. 영상, 음성, p2p data
     * 2. 영상, 음성
     * 3. 음성, data
     * 4. p2p data only
     * </pre>
     */
    private void excutePlayRTCSample(int type) {
        Intent intent = new Intent(MainActivity.this, PlayRTCActivity.class);
        // PlayRTC Sample 유형 전달
        intent.putExtra("type", type);
        intent.putExtra("channelRing", channelRing);
        intent.putExtra("videoCodec", videoCodec);
        intent.putExtra("audioCodec", audioCodec);
        MainActivity.this.startActivityForResult(intent, LAUNCHED_PLAYRTC);
    }

}