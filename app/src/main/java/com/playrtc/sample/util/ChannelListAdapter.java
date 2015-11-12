package com.playrtc.sample.util;


import java.util.ArrayList;
import java.util.List;

import com.playrtc.sample.R;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Button;

/**
 * 채널 팝업의 리스트뷰에서 사용하는 Adapter class<br>
 * BaseAdapter를 확장 구현 <br>
 * <pre>
 * List&lt;ChannelData&gt; channelList, 채널 데이터 리스트
 * </pre>
 * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위해 IChannelListAdapter를 정의 <br>
 * <pre>
 * IChannelListAdapter interface
 * - void onSelectListItem(ChannelData data)
 * </pre>
 *
 * @see android.widget.BaseAdapter
 * @see com.playrtc.sample.util.ChannelData
 */
public class ChannelListAdapter extends BaseAdapter {
    private Activity activity = null;
    /**
     * List&lt;ChannelData&gt; channelList, 채널 데이터 리스트
     */
    private List<ChannelData> channelList = new ArrayList<ChannelData>();
    private LayoutInflater inflater;
    private IChannelListAdapter listener = null;

    /**
     * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위한 Interface class
     *
     * @author ds3grk
     *         <pre>
     *         - void onSelectListItem(ChannelData data)
     *         </pre>
     */
    public interface IChannelListAdapter {
        /**
         * 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달
         *
         * @param data ChannelData
         * @see com.playrtc.sample.util.ChannelData
         */
        public abstract void onSelectListItem(ChannelData data);
    }

    /**
     * 생성자
     *
     * @param activity Activity
     * @param l        IChannelListAdapter, 리스트의 채널 입장 버튼 틀릭 시 채널 데이터를 전달하기 위한 Interface 구현 개체
     * @see com.playrtc.sample.util.ChannelListAdapter.IChannelListAdapter
     */
    public ChannelListAdapter(Activity activity, IChannelListAdapter l) {
        this.activity = activity;
        this.channelList = new ArrayList<ChannelData>();
        this.listener = l;
    }

    /**
     * 채널 목록 리스트를 지정한다.
     *
     * @param list List&lt;ChannelData&gt;
     * @see com.playrtc.sample.util.ChannelData
     */
    public void setListItems(List<ChannelData> list) {
        Log.e("LIST_VIEW", "setListItems list");
        synchronized (this.channelList) {
            channelList.clear();
            channelList.addAll(list);
        }
    }


    /**
     * 채널 목록 전체 갯수 반환 <br>
     * BaseAdapter 인터페이스
     *
     * @return int
     */
    @Override
    public int getCount() {
        return this.channelList.size();
    }

    /**
     * 특정 채널 데이터 반환 <br>
     * BaseAdapter 인터페이스
     *
     * @return Object, ChannelData
     * @see com.playrtc.sample.util.ChannelData
     */
    @Override
    public Object getItem(int location) {
        return this.channelList.get(location);
    }

    /**
     * 특정 채널 데이터 위치값 반환 <br>
     * BaseAdapter 인터페이스
     *
     * @param position int
     * @return long
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 리스트 뷰에 특정 위치의 데이터를 지정한 Row UI 객체를 반환 <br>
     * BaseAdapter 인터페이스
     *
     * @param position    int
     * @param convertView View
     * @param parent      ViewGroup
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // layout/list_row.xml
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = convertView;
        if (v == null) {
            // ROW 초기 객체 생성 시 버튼 이벤트 설정
            v = inflater.inflate(R.layout.list_row, null);
            ((Button) v.findViewById(R.id.row_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View bv) {
                    // 채널 목록의 입장 버튼을 누르면 채널 정보를 전달한다.
                    ChannelData data = (ChannelData) bv.getTag();
                    Log.d("LIST", "setOnClickListener channelId=" + data.channelId);
                    if (ChannelListAdapter.this.listener != null) {
                        ChannelListAdapter.this.listener.onSelectListItem(data);
                    }
                }

            });
        }

        // 채널 입장 버튼
        Button btn = (Button) v.findViewById(R.id.row_btn);
        TextView txtChannelId = (TextView) v.findViewById(R.id.row_channel_id);

        // 채널이름 TextView
        TextView txtChannelName = (TextView) v.findViewById(R.id.row_channel_name);

        // 데이터 리스트에서 특정 위치의 데이터를 조회
        ChannelData item = this.channelList.get(position);

        //버튼에 채널 데이터 등록 
        btn.setTag(item);

        // 채널 아이디 표시 
        txtChannelId.setText(item.channelId);

        // 채널이름 표시 
        if (TextUtils.isEmpty(item.channelName) == false) {
            txtChannelName.setText(item.channelName);
        } else {
            txtChannelName.setText("");
        }

        return v;
    }
}
