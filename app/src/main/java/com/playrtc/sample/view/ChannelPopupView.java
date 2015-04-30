package com.playrtc.sample.view;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playrtc.sample.BaseActivity;
import com.playrtc.sample.R;
import com.playrtc.sample.channellist.ChannelData;
import com.playrtc.sample.channellist.ChannelListAdapter;
import com.playrtc.sample.channellist.ChannelListAdapter.ChannelListAdapterListener;
import com.playrtc.sample.playrtc.BasePlayRTC;
import com.playrtc.sample.util.AppUtil;
import com.sktelecom.playrtc.connector.servicehelper.PlayRTCServiceHelperListener;

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
 * 채널 생성, 채널 목록 조회, 채널 입장을 구현하기 위한 Popup Layer View 로 RelativeLayout를 확장하여 구현 <br>>
 * 채널 목록 리스트의 입장하기 버튼 이벤트를 받기 위해 ChannelListAdapterListener 리스터 구현 <br>
 * Channel 생성, 입장과 관련해서 Sample App은 channel-id를 전달하는 방법을 구현하지 않아<br>
 * Channel 목록을 조회하고 리스트에서 사용자가 Channel을 직접 선택하여 Channel에 입장하도록 구성했음.<br>
 * @see com.playrtc.sample.channellist.ChannelListAdapter.ChannelListAdapterListener
 *
 */
public class ChannelPopupView extends RelativeLayout implements ChannelListAdapterListener{

	private static final String LOG_TAG = "CHANNEL_INFO";
	
	/**
	 * PlayRTC 인터페이스를 구현한 Class 인스턴스. 
	 * Channel 샹성/입장/목록조회 
	 */
	private BasePlayRTC playrtc = null;
	/**
	 * PlayRTC 서비스에서 조회하는 채널 목록 정보를 ChannelPopupView의 리스트에서 출력하기 위한 BaseAdapter를 확장한 List Adapter 클래스
	 */
	private ChannelListAdapter listAdapter = null;
	/**
	 * ChannelPopupViewListener 인터페이스 개체<br>
	 * 채널 목록 리스트의 채널입장 버튼 클릭 이벤트를 전달 받기 위한 리스너 인터페리스 
	 */
	private ChannelPopupViewListener listener = null;
	
	/**
	 * 채널 생성 탭 Layout 
	 */
	private LinearLayout tabCreate = null;
	/**
	 * 채널 입장 탭 Layout 
	 */
	private LinearLayout tabConnect = null;

	/**
	 * 채널 생성 탭 Contents 
	 */
	private LinearLayout createContents = null;
	private EditText txtCrChannelName = null;
	private EditText txtCrUserId = null;
	
	private TextView labelChannelId = null;
	
	/**
	 * 채널 입장 탭 Contents 
	 */
	private LinearLayout connectContents = null;
	private ListView chList = null;
	private EditText txtCnUserId = null;
	
	
	
	/**
	 * ChannelPopupView의 채널 생성 또는 채널 입장 버튼 클릭 시 채널 정보를 전달하기 위한 리스너 <br>
	 * <b>Interface</b>
	 * <pre>
	 * - public void onClickCreateChannel(String channelName, String userId)
	 * - public void onClickConnectChannel(String channelId, String userId)
	 * </pre>
	 */
	public interface ChannelPopupViewListener {
		
		/**
		 * 채널 생성 버튼을 클릭 
		 * @param channelName String, 목록에 나타나는 채널의 이름 
		 * @param userId String, option 이지만 사용자 식별을 위해 전달해주눈 것이 좋습니다.
		 */
		public void onClickCreateChannel(String channelName, String userId);
		
		/**
		 * 생성되어 있는 채널에 입장 버튼 클릭 
		 * @param channelId String, 입장할 채널의 아이디, 예제에서는 리스트에서 채널을 선택하지만 <br>
		 *        실제 App에서는 Push 메세지 등을 이용해서 channel 아이디를 전달.
		 * @param userId String, option 이지만 사용자 식별을 위해 전달해주눈 것이 좋습니다.
		 */
		public void onClickConnectChannel(String channelId, String userId);
	}
	
	
	public ChannelPopupView(Context context) {
		super(context);
	
	}

	public ChannelPopupView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	
	public ChannelPopupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		
	}
	
	/**
	 * ChannelPopupView 초기화
	 * @param activity BaseActivity
	 * @param playrtc PlayRTCHandler
	 * @param l ChannelPopupViewListener
	 * @see com.playrtc.sample.playrtc.BasePlayRTC
	 * @see com.playrtc.sample.view.ChannelPopupView.ChannelPopupViewListener
	 */
	public void init(BaseActivity activity, BasePlayRTC playrtc, ChannelPopupViewListener l) {
		this.playrtc = playrtc;
		this.listener = l;
		this.listAdapter = new ChannelListAdapter(activity, this);
		initLayout();
	}

	/**
	 * ChannelListAdapterListener의 onSelectListItem 구현체 <br>
	 * 채널 입장 탭화면의 채널 목록에서 채널 입장 버튼을 클릭 
	 * @param data ChannelData 채널 정보 
	 * @see com.playrtc.sample.channellist.ChannelData
	 * @see com.playrtc.sample.view.ChannelPopupView.ChannelPopupViewListener
	 */
	@Override
	public void onSelectListItem(ChannelData data) {
		Log.d("LIST", "onSelectListItem channelId="+data.getChannelId());
		if(TextUtils.isEmpty(data.getChannelId()) == false) {
			String userId = txtCnUserId.getText().toString();
			this.listener.onClickConnectChannel(data.getChannelId(), userId);
		}
		
	}
	
	/**
	 * show 
	 * @param delayed long, show 지연 시간 , 1/1000 초 
	 */
	public void show(final long delayed) {
		
		String crUserId = txtCrUserId.getText().toString();
		if(TextUtils.isEmpty(crUserId)) {
			txtCrUserId.setText(AppUtil.getRandomServiceMailId());
		}
		String cnUserId = txtCnUserId.getText().toString();
		if(TextUtils.isEmpty(cnUserId)) {
			txtCnUserId.setText(AppUtil.getRandomServiceMailId());
		}
		final Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.channel_show);
		animation.setAnimationListener(new Animation.AnimationListener(){
			
			@Override
			public void onAnimationEnd(Animation anim) {
				
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
		
			}

			@Override
			public void onAnimationStart(Animation anim) {
				setVisibility(View.VISIBLE);
				bringToFront();
			}
			
		});
		if(delayed == 0) {
			this.startAnimation(animation);
		}
		else {
			this.postDelayed(new Runnable(){
				public void run() {
					startAnimation(animation);
				}
			}, delayed);
		}
		
	}
	
	/**
	 * hide 
	 */
	public void hide() {
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.channel_hide);
		animation.setAnimationListener(new Animation.AnimationListener(){

			@Override
			public void onAnimationEnd(Animation anim) {
				ChannelPopupView.this.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation anim) {
				
			}

			@Override
			public void onAnimationStart(Animation anim) {
				
			}
			
		});
		startAnimation(animation);
	}
	

	/**
	 * 채널 아이디를 전달 받아 채널 생성 탭에 표시한다. 
	 * @param channelId String
	 * @see com.playrtc.sample.playrtc.PlayRTCObserverImpl#onConnectChannel(com.sktelecom.playrtc.PlayRTC, String, String)
	 */
	public void setChannelId(final String channelId) {

		labelChannelId.post(new Runnable(){
			public void run() {
				labelChannelId.setText(channelId);
			}
		});
	}
	
	/**
	 * 채널 생성 목록울 조회하고 채널 입장탭의 리스트에 출력한다.  
	 */
	public void showChannelList() {
		playrtc.getChannelList(new PlayRTCServiceHelperListener(){

			// 채널 목록 데이터 수신 
			@Override
			public void onServiceHelperResponse(int code, String statusMsg, Object returnParam, JSONObject oData) {
				try {
					// 오류 여부 검사 
					if(oData.has("error")) {
						JSONObject error = oData.getJSONObject("error");
						String errCode = error.getString("code");
						String errMsg = error.getString("message");
						Log.d(LOG_TAG, "getChannelList httpCode["+code+"] err["+errCode+"] "+ errMsg);
						return;				
					}
					// 오류 여부 검사 
					else if(code != 200) {
						Log.d(LOG_TAG, "getChannelList error httpCode["+code+"] err["+statusMsg+"]");
					}
					// 데이터 조회 성공 
					else {
						JSONArray channels = oData.getJSONArray("channels");
						int cnt = channels.length();
						List<ChannelData> list = new ArrayList<ChannelData>();
						for(int i = 0 ; i < cnt; i++) {
							JSONObject channel = channels.getJSONObject(i);
							String channelId = channel.getString("channelId");
							String channelName = (channel.has("channelName"))?channel.getString("channelName") : "";
							ChannelData item = new ChannelData();
							item.setChannelId(channelId);
							item.setChannelName(channelName);
							
							list.add(item);
						}
						// 리스트에 출력한다, 
						listAdapter.setListItems(list);
						listAdapter.notifyDataSetChanged(); 		
					}
				} catch (JSONException e) {
					Log.d(LOG_TAG, "getChannelList httpCode["+code+"] err["+statusMsg+"] "+e.getLocalizedMessage());
				
				}
			}

			// 데이터 조회 오류 발생 
			@Override
			public void onServiceHelperFail(int code, String statusMsg, Object returnParam) {
				Log.d(LOG_TAG, "getChannelList httpCode["+code+"] err["+statusMsg+"]");
			}
			
		});
				
	}
	
	

	private void initLayout() {
		this.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		this.tabCreate = (LinearLayout)this.findViewById(R.id.tab_btn_creator);
		this.tabCreate.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
		        if (MotionEvent.ACTION_UP == event.getAction())
		        {
		        	setActivePanel(0);
		            return true;
		        }
		        return true;
		    }
		});
		tabConnect = (LinearLayout)findViewById(R.id.tab_btn_connetor);
		tabConnect.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
		        if (MotionEvent.ACTION_UP == event.getAction())
		        {
		        	setActivePanel(1);
		            return true;
		        }
		        return true;
		    }
		});
		Button closeBtn = (Button)this.findViewById(R.id.btn_popup_close);
		closeBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelPopupView.this.hide();
			}
		});
		createContents = (LinearLayout)findViewById(R.id.tab_creator_contents);
		txtCrChannelName = (EditText)findViewById(R.id.txt_channel_name);
		txtCrUserId = (EditText)findViewById(R.id.txt_cruser_id);
		
		Button btnCrClear = (Button)findViewById(R.id.btn_creator_clear);
		btnCrClear.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				txtCrChannelName.setText("");
				txtCrUserId.setText("");
				labelChannelId.setText("CHANNEL-ID");
			}
		});
		
		Button btnCrCreate = (Button)findViewById(R.id.btn_create_channel);
		btnCrCreate.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				String channelName = txtCrChannelName.getText().toString();
				String userId = txtCrUserId.getText().toString();
				if(listener != null) {
					listener.onClickCreateChannel(channelName, userId);
				}
			}
		});
		
		connectContents = (LinearLayout)findViewById(R.id.tab_connector_contents);
		txtCnUserId = (EditText)findViewById(R.id.txt_cnuser_id);	
		Button btnCnClear = (Button)findViewById(R.id.btn_connect_clear);
		btnCnClear.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				txtCnUserId.setText("");
			}
		});
		Button btnCnList = (Button)findViewById(R.id.btn_connect_channel_list);
		btnCnList.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				showChannelList();
				
			}
		});
		
		this.chList = (ListView)findViewById(R.id.channel_list);
		this.chList.setAdapter(listAdapter);
		
		this.labelChannelId = (TextView)findViewById(R.id.txt_create_channel_id);
	}
	


	
	
	private void setActivePanel(int index) {
		if(index == 0) {
			tabCreate.setBackgroundResource(R.drawable.tab_btn_active); 
			tabConnect.setBackgroundResource(R.drawable.tab_btn_normal);
				
			createContents.setVisibility(View.VISIBLE);
			connectContents.setVisibility(View.INVISIBLE);
		}
		else {
			tabConnect.setBackgroundResource(R.drawable.tab_btn_active);
			tabCreate.setBackgroundResource(R.drawable.tab_btn_normal); 
			
			connectContents.setVisibility(View.VISIBLE);
			createContents.setVisibility(View.INVISIBLE);
			
			showChannelList();
		}
	}
}
