package com.playrtc.sample.view;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener;
import com.playrtc.sample.R;
import com.playrtc.sample.PlayRTCActivity;
import com.playrtc.sample.util.ChannelData;
import com.playrtc.sample.util.ChannelListAdapter;
import com.playrtc.sample.util.Utils;
import com.playrtc.sample.util.ChannelListAdapter.IChannelListAdapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 채널을 생성하거나 만들어진 채널 목록을 조회하여 채널에 입장하는 UI를 제공하는 RelativeLayout 확장 Class <br>
 * 내부적으로 채널 목록 리스트의 채널 입장 버튼을 눌렀을 때 해당 채널 정보를 전달 받기 위한 IChannelListAdapter구현.<br>
 * 채널 목록을 조회하기 위해 PlayRTC의 getChannelList메소드를 사용하며, 응답 결과를 받기 위해 <br>
 * PlayRTCServiceHelperListener 구현체가 필요하다. <br>
 * 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위해 PlayRTCChannelViewListener Interface를 정의. <br>
 * <pre>
 * 채널 생성 버튼 선택 시
 * - void onClickCreateChannel(String channelName, String userId, String userName)
 * 채널 입장 버튼 선택 시
 * - void onClickConnectChannel(String channelId, String userId, String userName)
 * </pre>
 *
 * @see com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener
 * @see com.playrtc.sample.util.ChannelListAdapter.IChannelListAdapter
 */
public class PlayRTCChannelView extends RelativeLayout implements IChannelListAdapter {
    private static final String LOG_TAG = "CHANNEL_INFO";

    /**
     * 채널 생성 탭 Layout
     */
    private LinearLayout tabCreate = null;

    /**
     * 채널 입장 탭 Layout
     */
    private LinearLayout tabConnect = null;

    /**
     * 창 닫기 버튼
     */
    private Button tabClose = null;

    /**
     * 채널 생성 화면 영역
     */
    private LinearLayout createContents = null;

    /**
     * 채널 생성 화면 - 채널 이름 입력
     */
    private EditText txtCrChannelName = null;

    /**
     * 채널 생성 화면 - 사용자 아이디(Application 사용자) 입력
     */
    private EditText txtCrUserId = null;

    /**
     * 채널 생성 화면 - 사용자 이름 입력
     */
    private EditText txtCrUserName = null;

    /**
     * 채널 생성 화면 입력 컨트롤 초기화
     */
    private Button btnCrClear = null;

    /**
     * 채널 생성 버튼
     */
    private Button btnCrCreate = null;

    /**
     * 채널 생성 후 발급 빋은 채널 아이디 출력
     */
    private TextView labelChannelId = null;

    /**
     * 채널 입장 화면 영역
     */
    private LinearLayout connectContents = null;

    /**
     * 채널 입장 화면 - 채널 목록 출력 리스트
     */
    private ListView chList = null;

    /**
     * 채널 입장 화면 - 사용자 아이디(Application 사용자) 입력
     */
    private EditText txtCnUserId = null;

    /**
     * 채널 입장 화면 - 사용자 이름 입력
     */
    private EditText txtCnUserName = null;

    /**
     * 채널 입장 화면 입력 컨트롤 초기화
     */
    private Button btnCnClear = null;

    /**
     * 채널 목록 리스트 조회 버튼
     */
    private Button btnCnList = null;

    private PlayRTC playRTC = null;

    private ChannelListAdapter listAdapter = null;

    // 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위한 PlayRTCChannelViewListener Interface 구현 개체.
    private PlayRTCChannelViewListener listener = null;

    private String channelId = "";

    /**
     * 채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위해 PlayRTCChannelViewListener Interface를 정의.
     * <pre>
     * 채널 생성 버튼 선택 시
     * - void onClickCreateChannel(String channelName, String userId, String userName)
     * 채널 입장 버튼 선택 시
     * - void onClickConnectChannel(String channelId, String userId, String userName)
     * </pre>
     */
    public interface PlayRTCChannelViewListener {
        /**
         * 채널 생성 버튼 선택 시 채널 생성 관련 정보를 전달
         *
         * @param channelName String, 생성할 채널의 별칭을 지정
         * @param userId      String, 채널을 생성하는 사용자의 Application에서 사용하는 아이디 지정
         * @param userName    userName, 채널을 생성하는 사용자의 이름을 지정
         */
        public abstract void onClickCreateChannel(String channelName, String userId, String userName);

        /**
         * 채널 입장 버튼 선택 시 채널 생성 관련 정보를 전달
         *
         * @param channelId String, 입장 할 채널의 아이디를 지정
         * @param userId    String, 채널에 입장하는 사용자의 Application에서 사용하는 아이디 지정
         * @param userName, 채널에 입장하는 사용자의 이름을 지정
         */
        public abstract void onClickConnectChannel(String channelId, String userId, String userName);
    }

    /**
     * 생성자
     *
     * @param context Context
     */
    public PlayRTCChannelView(Context context) {
        super(context);
    }

    /**
     * 생성자
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public PlayRTCChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 생성자
     *
     * @param context  Context
     * @param attrs    AttributeSet
     * @param defStyle int
     */
    public PlayRTCChannelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * PlayRTCChannelView를 초기화한다.
     *
     * @param activity PlayRTCActivity
     * @param playRTC  PlayRTC
     * @param l        PlayRTCChannelViewListener,  채널 생성/입장 버튼 선택 시 선택 정보를 전달하기 위한 PlayRTCChannelViewListener Interface 구현 개체.
     * @see com.playrtc.sample.PlayRTCActivity
     * @see com.playrtc.sample.view.PlayRTCChannelView.PlayRTCChannelViewListener
     * @see com.playrtc.sample.util.ChannelListAdapter
     */
    public void init(PlayRTCActivity activity, PlayRTC playRTC, PlayRTCChannelViewListener l) {
        this.playRTC = playRTC;
        this.listener = l;
        this.listAdapter = new ChannelListAdapter(activity, this);
        initLayout();
    }

    /**
     * IChannelListAdapter Interface<br>
     * 채널 목록 리스트의 채널 입장 버튼을 눌렀을 때 해당 채널 정보를 전달 받기 위한 IChannelListAdapter 구현
     *
     * @param data ChannelData, 채널 정보
     * @see com.playrtc.sample.util.ChannelData
     */
    @Override
    public void onSelectListItem(ChannelData data) {
        Log.d("LIST", "onSelectListItem channelId=" + data.channelId);
        if (TextUtils.isEmpty(data.channelId) == false) {
            String userId = this.txtCnUserId.getText().toString();
            String userName = this.txtCnUserName.getText().toString();
            // 채널 입장 버튼 선택 시 채널 생성 관련 정보를 전달
            this.listener.onClickConnectChannel(data.channelId, userId, userName);
        }
    }

    /**
     * PlayRTCChannelView를 화면에 보여준다.
     *
     * @param delayed long, 화면에 보여주는 Fade-in 시간을 지정. 0이면 바로 보여준다. msec 기준
     */
    public void show(final long delayed) {

        String crUserId = txtCrUserId.getText().toString();
        if (TextUtils.isEmpty(crUserId)) {
            String userId = Utils.getRandomServiceMailId();
            txtCrUserId.setText(userId);
            txtCrChannelName.setText("Android::" + userId + "의 채널입니다.");
        }
        String cnUserId = txtCnUserId.getText().toString();
        if (TextUtils.isEmpty(cnUserId)) {
            txtCnUserId.setText(Utils.getRandomServiceMailId());
        }
        final Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.channel_show);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation anim) {
            }

            @Override
            public void onAnimationRepeat(Animation anim) {
            }

            @Override
            public void onAnimationStart(Animation anim) {
                PlayRTCChannelView.this.setVisibility(View.VISIBLE);
                PlayRTCChannelView.this.bringToFront();
            }
        });
        if (delayed == 0) {
            this.startAnimation(animation);
        } else {
            this.postDelayed(new Runnable() {
                public void run() {
                    startAnimation(animation);
                }
            }, delayed);
        }
    }

    /**
     * PlayRTCChannelView를 화면에서 숨긴다.
     *
     * @param delayed long, 화면을 순기는 Fade-out 시간을 지정. 0이면 바로 숨긴다. msec 기준
     */
    public void hide(final long delayed) {
        final Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.channel_hide);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation anim) {
                PlayRTCChannelView.this.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation anim) {
            }

            @Override
            public void onAnimationStart(Animation anim) {
            }

        });
        if (delayed == 0) {
            this.startAnimation(animation);
        } else {
            this.postDelayed(new Runnable() {
                public void run() {
                    startAnimation(animation);
                }
            }, delayed);
        }
    }

    /**
     * 채널아이디를 반환한다. <br>
     * 채널 생성 또는 채널입장 시 획득한 채널의 아이디
     *
     * @return String
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * 외부에서 채널아이디를 전달 받아 채널 생성 탭의 출력영역에 표시한다.<br>
     * PlayRTC의 createChannel의 결과로 채널아이디를 발급받아 아이디가 전달 된다.
     *
     * @param channelId String, 채널아이디
     */
    public void setChannelId(final String channelId) {
        this.channelId = channelId;
        this.labelChannelId.post(new Runnable() {
            public void run() {
                labelChannelId.setText(channelId);
            }
        });
    }

    /**
     * 외부에서 전달받은 버튼에 PlayRTCChannelView에 show/hide 이벤트를 지정한다.
     *
     * @param btn Button
     */
    public void setTargetButton(Button btn) {
        final PlayRTCChannelView refThis = this;
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (refThis.isShown()) {
                    refThis.hide(0);
                } else {
                    refThis.showChannelList();
                    refThis.show(0);
                }
            }
        });
    }

    /**
     * PlayRTC의 getChannelList메소드를 호출하여 채널 목록을 조회하고 리스트에 출력한다.<br>
     * 채널 목록을 전달 받기 위해 PlayRTCServiceHelperListener 구현 개체가 필요
     *
     * @see com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener
     */
    public void showChannelList() {
        //채널 목록을 전달 받기 위해 PlayRTCServiceHelperListener 구현
        playRTC.getChannelList(new PlayRTCServiceHelperListener() {

            // 서비스 조회 결과를 전달 받는다.
            // 서비스 응답 시 오류 여부를 검사해야 한다.
            @Override
            public void onServiceHelperResponse(int code, String statusMsg, Object returnParam, JSONObject oData) {
                try {
                    // 서비스 오류 여부 검사
                    if (oData.has("error")) {
                        JSONObject error = oData.getJSONObject("error");
                        String errCode = error.getString("code");
                        String errMsg = error.getString("message");
                        Log.d(LOG_TAG, "getChannelList httpCode[" + code + "] err[" + errCode + "] " + errMsg);
                        return;
                    } else if (code != 200) {
                        Log.d(LOG_TAG, "getChannelList error httpCode[" + code + "] err[" + statusMsg + "]");
                    } else {
                        // 채널 데이터 리스트를 생성하여 ChannelListAdapter에 전달한다.
                        JSONArray channels = oData.getJSONArray("channels");
                        int cnt = channels.length();
                        List<ChannelData> list = new ArrayList<ChannelData>();
                        for (int i = 0; i < cnt; i++) {
                            JSONObject channel = channels.getJSONObject(i);
                            String channelId = channel.getString("channelId");
                            String channelName = (channel.has("channelName")) ? channel.getString("channelName") : "";
                            ChannelData item = new ChannelData();
                            item.channelId = channelId;
                            item.channelName = channelName;
                            list.add(item);
                        }
                        // 리스트 데이터 전달
                        listAdapter.setListItems(list);
                        // 리스트 갱신
                        listAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "getChannelList httpCode[" + code + "] err[" + statusMsg + "] " + e.getLocalizedMessage());
                }
            }

            // 통신 오류 발생
            @Override
            public void onServiceHelperFail(int code, String statusMsg, Object returnParam) {
                Log.d(LOG_TAG, "getChannelList httpCode[" + code + "] err[" + statusMsg + "]");
            }

        });

    }


    private void initLayout() {
        // PlayRTCChannelView 자체의 클릭 이벤트를 걸고 onClick를 구현하지 않는다. 클릭 동작 방지를 위해
        this.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // 채널 생성 탭 버튼
        this.tabCreate = (LinearLayout) this.findViewById(R.id.tab_btn_creator);
        this.tabCreate.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    // 탭 전환
                    // 0 : 채널 생성 탭
                    setActivePanel(0);
                    return true;
                }
                return true;
            }
        });
        // 채널 입장 탭 버튼
        this.tabConnect = (LinearLayout) this.findViewById(R.id.tab_btn_connetor);
        this.tabConnect.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    // 탭 전환
                    // 0 : 채널 입장 탭
                    setActivePanel(1);
                    return true;
                }
                return true;
            }
        });

        // PlayRTCChannelView 닫기 버튼
        this.tabClose = (Button) this.findViewById(R.id.btn_popup_close);
        this.tabClose.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayRTCChannelView.this.hide(0);
            }
        });
        // 채널 생성 탭 화면 영역
        this.createContents = (LinearLayout) this.findViewById(R.id.tab_creator_contents);
        // 채널 이름을 입력받는다.
        this.txtCrChannelName = (EditText) this.findViewById(R.id.txt_channel_name);
        // 사용자 아이디를 입력받는다.
        this.txtCrUserId = (EditText) this.findViewById(R.id.txt_cruser_id);
        // 사용자 이름을 입력 받는다.
        this.txtCrUserName = (EditText) this.findViewById(R.id.txt_cruser_name);
        // 입력 컨트롤 초기화 버튼
        this.btnCrClear = (Button) this.findViewById(R.id.btn_creator_clear);
        this.btnCrClear.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayRTCChannelView.this.txtCrChannelName.setText("");
                PlayRTCChannelView.this.txtCrUserId.setText("");
                PlayRTCChannelView.this.txtCrUserName.setText("");
                PlayRTCChannelView.this.labelChannelId.setText("CHANNEL-ID");
                PlayRTCChannelView.this.channelId = "";
            }
        });

        // 채널 생성 버튼
        this.btnCrCreate = (Button) this.findViewById(R.id.btn_create_channel);
        this.btnCrCreate.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channelName = txtCrChannelName.getText().toString();
                String userId = txtCrUserId.getText().toString();
                String userName = txtCrUserName.getText().toString();
                if (PlayRTCChannelView.this.listener != null) {
                    PlayRTCChannelView.this.listener.onClickCreateChannel(channelName, userId, userName);
                }
            }
        });

        // 채널 입장 탭 화면 영역
        this.connectContents = (LinearLayout) this.findViewById(R.id.tab_connector_contents);
        // 사용자 아이디를 입력받는다.
        this.txtCnUserId = (EditText) this.findViewById(R.id.txt_cnuser_id);
        // 사용자 이름을 입력받는다.
        this.txtCnUserName = (EditText) this.findViewById(R.id.txt_cnuser_name);
        // 입력 컨트롤 초기화 버튼
        this.btnCnClear = (Button) this.findViewById(R.id.btn_connect_clear);
        this.btnCnClear.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayRTCChannelView.this.txtCnUserId.setText("");
                PlayRTCChannelView.this.txtCnUserName.setText("");
            }
        });
        // 채널 목록 조회 버튼
        this.btnCnList = (Button) this.findViewById(R.id.btn_connect_channel_list);
        this.btnCnList.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 채널 목록을 조회한다.
                PlayRTCChannelView.this.showChannelList();

            }
        });

        // 채널 목록 출력을 위한 ListView
        this.chList = (ListView) this.findViewById(R.id.channel_list);
        this.chList.setAdapter(this.listAdapter);

        // 채널 아이디를 출력
        this.labelChannelId = (TextView) this.findViewById(R.id.txt_create_channel_id);
    }

    /**
     * 탭 전환
     *
     * @param index int, 0: 채널 생성 탭  화성화, 1: 채널 입장 탭 활성화
     */
    private void setActivePanel(int index) {
        if (index == 0) {
            this.tabCreate.setBackgroundResource(R.drawable.tab_btn_active);
            this.tabConnect.setBackgroundResource(R.drawable.tab_btn_normal);

            this.createContents.setVisibility(View.VISIBLE);
            this.connectContents.setVisibility(View.INVISIBLE);
        } else {
            this.tabConnect.setBackgroundResource(R.drawable.tab_btn_active);
            this.tabCreate.setBackgroundResource(R.drawable.tab_btn_normal);

            connectContents.setVisibility(View.VISIBLE);
            createContents.setVisibility(View.INVISIBLE);
            // 채널 입장 탭 활성화 시 채널 목록 갱신
            showChannelList();
        }
    }
}
