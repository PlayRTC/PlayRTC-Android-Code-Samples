package com.playrtc.sample;

import com.playrtc.sample.util.AppUtil;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * Activity에서 사용하는 공통 기능을 구현한 기본 Activity Class<BR> 
 * Application에서 사용하는 모든 Activity는 BaseActivity를 상속 하여 구현한다.  
 */
public class BaseActivity extends Activity {
	
	/**
	 * AudioManager 관련 설정을 위한 인스턴스 
	 */
	protected AudioManager audioManager = null;
	
	/**
	 * Activity 종료 시 사용자에게 종료의사를 묻고 그 결과를 임시로 저장하여 처리하기 위한 변수<BR> 
	 * 각각의 Activity의 onBackPressed()에서 사용   
	 */
	protected boolean isCloesActivity = false;
	
	/**
	 * PlayRTC Activity가 구동전에 설정되어 있는 AudioManager 모드를 기억하고 종료 시 원래대로 설정하기 위해 사용.
	 */
	protected int regAmgrMode = AudioManager.MODE_IN_CALL;
	/**
	 * PlayRTC Activity가 구동전에 설정되어 있는 Speakerphone 설정을 기억하고 종료 시 원래대로 설정하기 위해 사용.
	 */
	protected boolean regAmgrSpeakerphoneOn 		= false; 
	
	
	/**
	 * startActivityForResult로 생성한 Activity를 종료 시키기 위해 사용하는 인터페이스.<BR>
	 * PlayRTC Activity를 구성할 때 CloseActivityForResult 인터페이스를 구현
	 * 
	 * <b>interface</b><br>
	 * - void setCloseActivityForResult()<br>
	 */
	public interface CloseActivityForResult {
		
		/**
		 * startActivityForResult로 생성한 Activity를 종료 시키기 위해 사용<BR>
		 * PlayRTC Activity를 구성할 때 CloseActivityForResult 인터페이스를 구현
		 */
		public void setCloseActivityForResult();
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// WiredHeadset/블루투스 를 사용하는지 에 따라 적절한 설정을 해주세요 
		audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		
		//AudioManager 모드를 기억하고 종료 시 원래대로 설정하기 위해 사용.
		regAmgrMode = audioManager.getMode();
		//Speakerphone 설정을 기억하고 종료 시 원래대로 설정하기 위해 사용
		regAmgrSpeakerphoneOn = audioManager.isSpeakerphoneOn();
		
	}
	
	
	@Override
	protected void onDestroy() {
		//AudioManager 모드를 원래대로 설정하기 위해 사용.
		audioManager.setMode(regAmgrMode);
		//Speakerphone 설정을 원래대로 설정하기 위해 사용
		audioManager.setSpeakerphoneOn(regAmgrSpeakerphoneOn);	
		super.onDestroy();
	}
	
	/**
	 * 단말기의 볼륨키 이벤트를 받아 Master Volume을 조절한다.
	 * @param keyCode int 
	 * @param event KeyEvent
	 * @return boolean
	 * @see android.view.KeyEvent
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			incrementMasterVolume(true);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			incrementMasterVolume(false);
			return true;
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 채널에서 퇴장을 하면 PlayRTCObserverImpl의 onDisconnectChannel에서 isCloesActivity를 true로 설정하기 위해 호출한다.
	 * @param isCloesActivity boolean, onBackPressed()에서 사용 
	 * 
	 */
	public void setCloesActivity(boolean isCloesActivity) {
		this.isCloesActivity = isCloesActivity;
	}
		
	/**
	 *  Master Volume 조정 
	 * @param isUp boolean, 증감 여부 
	 */
	private void incrementMasterVolume(boolean isUp) {
		int playVol = getMasterVolume();
		if(isUp)playVol+=1;
		else playVol -= 1;
		setMasterVolume(playVol);
	}

	/**
	 *  현재 단말기의 Master Volume 값을 반환한다.
	 * @return int, 0 ~ 15 
	 */
	public int getMasterVolume() {
    	int currVol = this.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 
    	return currVol;
    }
	/**
	 *  현재 단말기의 Master Volume 값을 조정한다.
	 * param playVol int, 0 ~ 15 
	 */ 
	 public int setMasterVolume(int playVol) {
    	int currVol = this.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    	int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    	if(playVol < 0 || playVol > maxVol) return currVol;
    	this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, playVol, AudioManager.FLAG_PLAY_SOUND);
    	AppUtil.showToast(this, "Master Volume["+currVol+"]");
    	return currVol;
    }
	 
}
