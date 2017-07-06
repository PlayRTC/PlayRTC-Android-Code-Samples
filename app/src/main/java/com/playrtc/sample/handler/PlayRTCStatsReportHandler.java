package com.playrtc.sample.handler;

import android.text.format.Formatter;

import com.playrtc.sample.PlayRTCActivity;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCStatsReport;
import com.sktelecom.playrtc.PlayRTCStatsReport.RatingValue;
import com.sktelecom.playrtc.observer.PlayRTCStatsReportObserver;

public class PlayRTCStatsReportHandler  implements PlayRTCStatsReportObserver {

    //StatsReport 조회 주기 msec
	private static final long TIMER_INTERVAL = 5000;//5 sec

	private PlayRTCActivity activity = null;
	private PlayRTC playrtc = null;
	
	public PlayRTCStatsReportHandler(PlayRTCActivity activity) {
		this.activity = activity;
	}
	
	public void start(PlayRTC playrtc, String peerId) {

		if(playrtc != null) {
			playrtc.startStatsReport(TIMER_INTERVAL, (PlayRTCStatsReportObserver)this, peerId);
		}
        this.playrtc = playrtc;
	}
	public void stop() {
		if(playrtc != null) {
			playrtc.stopStatsReport();
		}
	}


	/*
	 * PlayRTCStatsReportObserver Interface 구현
	 * @param report PlayRTCStatsReport
	 *
	 * PlayRTCStatsReport Interface
	 * - String getLocalCandidate();
	 *   자신의 ICE 서버 연결상태를 반환한다.
	 * - String getRemoteCandidate();
     *   상대방의 ICE 서버 연결상태를 반환한다.
	 * - String getLocalVideoCodec();
	 *   자신의 VideoCodec을 반환한다.
	 * - String getLocalAudioCodec();
	 *   자신의 AudioCodec을 반환한다.
	 * - String getRemoteVideoCodec();
	 *   상대방의 VideoCodec을 반환한다.
	 * - String getRemoteAudioCodec();
	 *   상대방의 AudioCodec을 반환한다.
     * - int getLocalFrameWidth();
     *   상대방에게 전송하는 영상의 해상도 가로 크기를 반환한다.
     * - int getLocalFrameWidth();
     *   상대방에게 전송하는 영상의 해상도 가로 크기를 반환한다.
     * - int getLocalFrameHeight();
     *   상대방에게 전송하는 영상의 해상도 세로 크기를 반환한다.
     * - int getRemoteFrameWidth();
     *   상대방 수신  영상의 해상도 가로 크기를 반환한다.
     * - int getRemoteFrameHeight();
     *   상대방 수신  영상의 해상도 세로 크기를 반환한다.
     * - int getLocalFrameRate();
     *   상대방에게 전송하는 영상의 Bit-Rate를 반환한다.
     * - int getRemoteFrameRate();
     *   상대방 수신  영상의 Bit-Rate를 반환한다.
     * - int getAvailableSendBandWidth();
     *   상대방에게 전송할 수 있는 네트워크 대역폭을 반환한다.
     * - int getAvailableReceiveBandWidth();
     *   상대방으로부터 수신할 수 있는 네트워크 대역폭을 반환한다.
     * - int getRtt();
     *   자신의 Rount Trip Time을 반환한다
     * - RatingValue getRttRating();
     *   RTT값을 기반으로 네트워크 상태를 5등급으로 분류하여 RttRating 를 반환한다.
     * - RatingValue getFractionRating();
     *   Packet Loss 값을 기반으로 상대방의 영상 전송 상태를 5등급으로 분류하여 RatingValue 를 반환한다.
     * - RatingValue getLocalAudioFractionLost();
     *   Packet Loss 값을 기반으로 자신의 음성 전송 상태를 5등급으로 분류하여RatingValue 를 반환한다.
     * - RatingValue getLocalVideoFractionLost();
     *   Packet Loss 값을 기반으로 자신의 영상 전송 상태를 5등급으로 분류하여RatingValue 를 반환한다.
     * - RatingValue getRemoteAudioFractionLost();
     *   Packet Loss 값을 기반으로 상대방의 음성 전송 상태를 5등급으로 분류하여RatingValue 를 반환한다.
     * - RatingValue getRemoteVideoFractionLost();
     *   Packet Loss 값을 기반으로 상대방의 영상 전송 상태를 5등급으로 분류하여RatingValue 를 반환한다.
	 */
	@Override
	public void onStatsReport(PlayRTCStatsReport report) {
		
		RatingValue localVideoFl = report.getLocalVideoFractionLost();
		RatingValue localAudioFl = report.getLocalAudioFractionLost();
		RatingValue remoteVideoFl = report.getRemoteVideoFractionLost();
		RatingValue remoteAudioFl = report.getRemoteAudioFractionLost();


		final String text = String.format("Local\n ICE:%s\n Frame:%sx%sx%s\n 코덱:%s,%s\n Bandwidth[%sps]\n RTT[%s]\n RttRating[%d/%.4f]\n VFLost[%d/%.4f]\n AFLost[%d/%.4f]\n\nRemote\n ICE:%s\n Frame:%sx%sx%s\n 코덱:%s,%s\n Bandwidth[%sps]\n VFLost[%d/%.4f]\n AFLost[%d/%.4f]\n",
											report.getLocalCandidate(),
											report.getLocalFrameWidth(),
											report.getLocalFrameHeight(),
											report.getLocalFrameRate(),
											report.getLocalVideoCodec(),
											report.getLocalAudioCodec(),
											Formatter.formatFileSize(activity.getApplicationContext(), report.getAvailableSendBandwidth())+"",
											report.getRtt(),
											report.getRttRating().getLevel(),
											report.getRttRating().getValue(),
											localVideoFl.getLevel(),
											localVideoFl.getValue(),
											localAudioFl.getLevel(),
											localAudioFl.getValue(),
											report.getRemoteCandidate(),
											report.getRemoteFrameWidth(),
											report.getRemoteFrameHeight(),
											report.getRemoteFrameRate(),
											report.getRemoteVideoCodec(),
											report.getRemoteAudioCodec(),
											Formatter.formatFileSize(activity.getApplicationContext(), report.getAvailableReceiveBandwidth())+"",
											remoteVideoFl.getLevel(),
											remoteVideoFl.getValue(),
											remoteAudioFl.getLevel(),
											remoteAudioFl.getValue());
				

		activity.printRtcStatReport(text);
	}

}
