package com.playrtc.sample.channellist;

import java.util.ArrayList;
import java.util.List;

import com.playrtc.sample.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * PlayRTC 서비스에서 조회하는 채널 목록 정보를<br>
 * ChannelPopupView의 리스트에서 출력하기 위한 BaseAdapter를 확장한 List Adapter 클래스 
 */
public class ChannelListAdapter extends BaseAdapter {

	private Activity activity = null;
	/**
	 * 채널 정보를 저장하기 위한 데이터 리스트 
	 * @see com.playrtc.sample.channellist.ChannelData
	 */
	private List<ChannelData> channelList = new ArrayList<ChannelData>();
	private LayoutInflater inflater = null;
	private ChannelListAdapterListener listener = null;
	
	/**
	 * 채널 목록 리스트의 채널입장 버튼 클릭 이벤트를 전달 받기 위한 리스너 인터페리스 <br> 
	 * <b>interface</b><br>
	 * - public abstract void onSelectListItem(ChannelData data)
	 */
	public interface ChannelListAdapterListener {
		/**
		 * 채널 리스트에서 선택한 ChannelData를 전달 받기 위한 인터페이스 
		 * @param data ChannelData, 채널 정보를 저장하기 위한 데이터
		 * @see com.playrtc.sample.channellist.ChannelData
		 */
		public abstract void onSelectListItem(ChannelData data);
	}
	
	/**
	 * 생성자 
	 * @param activity Activity
	 * @param l ChannelListAdapterListener
	 * @see com.playrtc.sample.channellist.ChannelListAdapter.ChannelListAdapterListener
	 */
	public ChannelListAdapter(Activity activity, ChannelListAdapterListener l) {
		this.activity = activity;
		this.channelList = new ArrayList<ChannelData>();
		this.listener = l;
	}
	
	/**
	 * 데이터 리스트를 전달 받는다.
	 * @param list ChannelData List 
	 */
	public void setListItems(List<ChannelData> list) {
		synchronized(this.channelList) {
			channelList.clear();
			channelList.addAll(list);
		}
	}
		
	/**
	 *  데이터 리스트 전체 갯수 반환 
	 *  @return int
	 */
	@Override
	public int getCount() {
		return this.channelList.size();
	}

	/**
	 *  특정 데이터 객체 반환 
	 *  @param location int
	 */
	@Override
	public Object getItem(int location) {
		return this.channelList.get(location);
	}

	/**
	 * 특정 데이터 위치값 반환 
	 * @param position int
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * List Row View 설정
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		View v = convertView;
		if (v == null) {
			// ROW 초기 객체에 버튼 클릭  이벤트 설정 
            v = inflater.inflate(R.layout.list_row, null);
            ((Button)v.findViewById(R.id.row_btn)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View bv) {
					ChannelData data = (ChannelData)bv.getTag();
					Log.d("LIST", "setOnClickListener channelId="+data.getChannelId());
					if(ChannelListAdapter.this.listener != null) {
						ChannelListAdapter.this.listener.onSelectListItem(data);
					}
				}
            	
            });
        }
		// 입장하기 버튼 
		Button btn = (Button) v.findViewById(R.id.row_btn);
		// 채널 아이디 
        TextView txtChannelId = (TextView) v.findViewById(R.id.row_channel_id);
        // 채널 이름 
        TextView txtChannelName = (TextView) v.findViewById(R.id.row_channel_name);
        
        // 데이터 리스트에서 특정 위치의 데이터를 조회
        ChannelData item = this.channelList.get(position);
        if(item != null) {
	        btn.setTag(item);
	        
	        txtChannelId.setText(item.getChannelId());
	        if(TextUtils.isEmpty(item.getChannelName()) == false) {
	        	txtChannelName.setText(item.getChannelName());
	        }
	        else {
	        	txtChannelName.setText("");
	        }
        }
		return v;
	}

}
