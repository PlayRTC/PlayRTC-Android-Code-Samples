package com.playrtc.sample.handler;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.playrtc.sample.PlayRTCActivity;
import com.playrtc.sample.util.Utils;
import com.sktelecom.playrtc.observer.PlayRTCDataObserver;
import com.sktelecom.playrtc.observer.PlayRTCSendDataObserver;
import com.sktelecom.playrtc.stream.PlayRTCData;
import com.sktelecom.playrtc.stream.PlayRTCData.PlayRTCDataCode;
import com.sktelecom.playrtc.stream.PlayRTCData.PlayRTCDataStatus;
import com.sktelecom.playrtc.stream.PlayRTCData.PlayRTCFileReveType;
import com.sktelecom.playrtc.stream.PlayRTCDataHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/*
 * PlayRTCData를 위한 Handler Class
 * PlayRTCDataObserver 인터페이스를 구성
 *
 * Method. PlayRTCSendDataObserver 구현체 필요
 * - sendText : 텍스트 데이터를 상대방에게 전송
 * - sendBinary : 이미지 등의 Binary 데이터를 상대방에게 전송
 * - sendFile : 파일을 상대방에게 전송
 *
 * PlayRTCDataObserver 인터페이스
 * - onProgress : 데이터 수신 진행 정보
 * - onMessage : 데이터 수신 완료
 * - onError : 오류 발생
 * - onStateChange : 상태 변경 발생
 *
 * @see com.sktelecom.playrtc.observer.PlayRTCDataObserver
 */
public class PlayRTCDataChannelHandler implements PlayRTCDataObserver {
    private static final String LOG_TAG = "DATA-HANDLER";

    private PlayRTCActivity activity = null;

    /*
     * P2P 데이터 통신을 위한 PlayRTCData객체
     */
    private PlayRTCData dataChannel = null;



    /*
     * 파일 전송 완료 이벤트에서 InputStream Close하기 위해 전역 변수 처리
     */
    private InputStream dataIs = null;

    private long elaspedStart = 0L;


    /*
     * 생성자
     *
     * @param activity PlayRTCActivity
     * @see com.playrtc.sample.view.PlayRTCLogView
     */
    public PlayRTCDataChannelHandler(PlayRTCActivity activity) {
        this.activity = activity;
    }

    /*
     * PlayRTCData를 PlayRTC에서 전달받아 지정함.
     *
     * @param dc PlayRTCData, PlayRTCData 인스턴스
     */
    public void setDataChannel(PlayRTCData dc) {
        this.dataChannel = dc;
        this.dataChannel.setEventObserver((PlayRTCDataObserver) this);
        /*
         * - PlayRTCFileReveType.File : 파일 수신 데이터를 파일로 저장히며 수신 완료 시 저장 파일 정보를 전달한다.
         * - PlayRTCFileReveType.Byte : 파일 수신 데이터를 메모리에 쌓았다가 수신 완료시 한번에 전달. 큰파일의경우 Out of Memory 문제 발생할 수 있음.
         */
        this.dataChannel.setFileReveMode(PlayRTCFileReveType.File);
    }

    /*
     * 텍스트 데이터를 상대방에게 전송 하며 글자 처리는 <br>
     * Javascript와의 통신을 위해 Unicode code point (2 byte)로 처리한 후 7KByte 기준으로 분할 하여 전송
     */
    public void sendText() {
        if (dataChannel != null && dataChannel.getStatus() == PlayRTCDataStatus.Open) {
            String sendData = "DataChannel Hello 안녕하세요 こんにちは 你好...";
            dataChannel.sendText(sendData, new PlayRTCSendDataObserver() {

                /*
                 * 데이터 전송 진척 정보를 알려준다.
                 * PlayRTCData은 데이터 전송 시 데이터를 특정 사이즈 크기로 분할하여 전체 스트림에 대해 고유아이디를
                 * 부여하고 분할 전송을 한다.
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid String, Application에서 사용하는 사용자 아이디.
                 * @param id long, 전성 스트림에 대해 고유아이디
                 * @param size long, 데이터 전체 크기
                 * @param send long, 전송 한 데이터의 누적 크기
                 * @param index index, 전성하는 분할 데이터 인덱스
                 * @param count long, 전체 분할 데이터 수
                 */
                @SuppressLint("DefaultLocale")
                @Override
                public void onSending(final PlayRTCData obj, final String peerId, final String peerUid, final long id, final long size, final long send, final long index, final long count) {
                    float per = ((float) send / (float) size) * 100.0f;
                    String sMsg = String.format("Data onSending [%d/%d] [%d/%d]  %.2f%%", index + 1, count, send, size, per);
                    Log.d(LOG_TAG, sMsg);
                    activity.progressLogMessage(sMsg);
                }

                /*
                 * 데이터 전송 완료를 알려준다.
                 *
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param id  long, 전성 스트림에 대해 고유아이디
                 * @param size long, 데이터 전체 크기
                 */
                @Override
                public void onSuccess(PlayRTCData obj, String peerId, String peerUid, long id, long size) {
                    Log.d(LOG_TAG, "sendText onSuccess " + peerUid + " " + id + "[" + size + "]");
                    activity.appnedLogMessage(">>Data-Channel sendText onSuccess[" + id + "] " + size + " bytes");
                }

                /*
                 * 데이터 전송 실패를 알려준다.
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param id  long, 전성 스트림에 대해 고유아이디
                 * @param code PlayRTCDataCode, PlayRTCDataCode = 오류 코드 정의
                 * <pre>
                 *  - None,
                 *  - NotOpen,
                 *  - SendBusy,
                 *  - SendFail,
                 *  - FileIO,
                 *  - ParseFail
                 * </pre>
                 * @param desc String, description
                 */
                @Override
                public void onError(PlayRTCData obj, String peerId, String peerUid, long id, PlayRTCDataCode code, String desc) {
                    Log.d(LOG_TAG, "sendText onError " + peerUid + " " + id + "[" + code + "] " + desc);
                    activity.appnedLogMessage(">>Data-Channel sendText onError[" + id + "] [" + code + "] " + desc);
                }
            });
        } else {
            Log.d(LOG_TAG, "데이터 채널이 연결 상태가 아닙니다. ");
            activity.appnedLogMessage(">>Data-Channel이 연결 상태가 아닙니다.");
        }
    }

    /*
     * 이미지 등의 Binary 데이터를 상대방에게 전송합니다. Binary 전송 시 데이터에 대한 MimeType을 같이 전달
     * 아래 예에서는 텍스트에서 Byte 배열을 추출하여 전송하고 있으며, MimeType으로 null을 전달하며
     * 문자에 대한 특별한 처리는 하지 않고 있어 수신 측이 native인 경우에 올바르게 수신
     */
    public void sendBinary() {
        if (dataChannel != null && dataChannel.getStatus() == PlayRTCDataStatus.Open) {
            String sendData = "DataChannel Hello 안녕하세요 こんにちは 你好...";
            dataChannel.sendByte(sendData.getBytes(), null, new PlayRTCSendDataObserver() {

                /*
                 * 데이터 전송 진척 정보를 알려준다.
                 * PlayRTCData은 데이터 전송 시 데이터를 특정 사이즈 크기로 분할하여 전체 스트림에 대해 고유아이디를
                 * 부여하고 분할 전송을 한다.
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid String, Application에서 사용하는 사용자 아이디.
                 * @param id long, 전송 스트림에 대해 고유아이디
                 * @param size long, 데이터 전체 크기
                 * @param send long, 전송 한 데이터의 누적 크기
                 * @param index index, 전성하는 분할 데이터 인덱스
                 * @param count long, 전체 분할 데이터 수
                 */
                @SuppressLint("DefaultLocale")
                @Override
                public void onSending(final PlayRTCData obj, final String peerId, final String peerUid, final long id, final long size, final long send, final long index, final long count) {
                    float per = ((float) send / (float) size) * 100.0f;
                    String sMsg = String.format("Data onSending [%d/%d] [%d/%d]  %.2f%%", index + 1, count, send, size, per);
                    Log.d(LOG_TAG, sMsg);
                    activity.progressLogMessage(sMsg);
                }

                /*
                 * 데이터 전송 완료를 알려준다.
                 *
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param id  long, 전송 스트림에 대해 고유아이디
                 * @param size long, 데이터 전체 크기
                 */
                @Override
                public void onSuccess(PlayRTCData obj, String peerId, String peerUid, long id, long size) {
                    Log.d(LOG_TAG, "sendBinary onSuccess " + peerUid + " " + id + "[" + size + "]");
                    activity.appnedLogMessage(">>Data-Channel sendBinary onSuccess[" + id + "] " + size + " bytes");

                }

                /*
                 * 데이터 전송 실패를 알려준다.
                 * @param obj PlayRTCData
                 * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                 * @param id  long, 전송 스트림에 대해 고유아이디
                 * @param code PlayRTCDataCode, PlayRTCDataCode = 오류 코드 정의
                 *  - None,
                 *  - NotOpen,
                 *  - SendBusy,
                 *  - SendFail,
                 *  - FileIO,
                 *  - ParseFail
                 * @param desc String, description
                 */
                @Override
                public void onError(PlayRTCData obj, String peerId, String peerUid, long id, PlayRTCDataCode code, String desc) {
                    Log.d(LOG_TAG, "sendBinary onError " + peerUid + " " + id + "[" + code + "] " + desc);
                    activity.appnedLogMessage(">>Data-Channel sendBinary onError[" + id + "] [" + code + "] " + desc);
                }
            });
        } else {
            Log.d(LOG_TAG, "데이터 채널이 연결 상태가 아닙니다. ");
            activity.appnedLogMessage(">>Data-Channel이 연결 상태가 아닙니다.");
        }
    }


    /*
     * 파일을 전송하기 위해서 File 객체를 생성하여 전달 하거나 파일에서 InputStream을 샹성하여 전달
     * 아래 예에서는 Android Application의 asset폴더에 있는 파일경로를  전달
     */
    public void sendFile() {
        // Data Channel이 Open 상태인지 검사
        if (dataChannel != null && dataChannel.getStatus() == PlayRTCDataStatus.Open) {
            elaspedStart = System.currentTimeMillis();
            final String fileName = "librtc_xmllite.a";
            dataIs = null;

            try {
                dataIs = this.activity.getAssets().open(fileName);
                Log.d(LOG_TAG, "sendFile [" + fileName + "]");
                activity.appnedLogMessage(">>Data-Channel sendFile[" + fileName + "]");
                /*
                 * byte 데이터를 전송한다.
                 * @param istream InputStream
                 * @param fileName String, 파일 명
                 * @param observer PlayRTCSendDataObserver
                 * @return long, 전송 데이터 스트림 고유 아이디
                 * @see com.sktelecom.playrtc.observer.PlayRTCSendDataObserver
                 */
                dataChannel.sendFile(dataIs, fileName, new PlayRTCSendDataObserver() {

                    /*
                     * 데이터 전송 진척 정보를 알려준다.
                     * PlayRTCData은 데이터 전송 시 데이터를 특정 사이즈 크기로 분할하여 전체 스트림에 대해 고유아이디를
                     * 부여하고 분할 전송을 한다.
                     * @param obj PlayRTCData
                     * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                     * @param peerUid String, Application에서 사용하는 사용자 아이디.
                     * @param id long, 전송 스트림에 대해 고유아이디
                     * @param size long, 데이터 전체 크기
                     * @param send long, 전송 한 데이터의 누적 크기
                     * @param index index, 전성하는 분할 데이터 인덱스
                     * @param count long, 전체 분할 데이터 수
                     */
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSending(final PlayRTCData obj, final String peerId, final String peerUid, final long id, final long size, final long send, final long index, final long count) {
                        float per = ((float) send / (float) size) * 100.0f;
                        String sMsg = String.format("Data onSending [%d/%d] [%d/%d]  %.2f%%", index + 1, count, send, size, per);
                        Log.d(LOG_TAG, sMsg);
                        activity.progressLogMessage(sMsg);
                    }

                    /*
                     * 데이터 전송 완료를 알려준다.
                     *
                     * @param obj PlayRTCData
                     * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                     * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                     * @param id  long, 전송 스트림에 대해 고유아이디
                     * @param size long, 데이터 전체 크기
                     */
                    @Override
                    public void onSuccess(PlayRTCData obj, String peerId, String peerUid, long id, long size) {
                        final long elasedTime = System.currentTimeMillis() - elaspedStart;
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                closeInputStream();
                                Toast logToast = Toast.makeText(activity.getApplicationContext(), "elasedTime = " + elasedTime, Toast.LENGTH_LONG);
                                logToast.show();

                            }
                        });
                        Log.d(LOG_TAG, "sendFile onSuccess " + peerUid + " " + id + "[" + size + "]");
                        activity.appnedLogMessage(">>Data-Channel[" + peerId + "] sendFile[" + fileName + "] onSuccess[" + id + "] " + size + " bytes");
                    }

                    /*
                     * 데이터 전송 실패를 알려준다.
                     * @param obj PlayRTCData
                     * @param peerId String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                     * @param peerUid  String, PlayRTC 채널 서비스에서 발급받은 사용자 아이디
                     * @param id  long, 전송 스트림에 대해 고유아이디
                     * @param code PlayRTCDataCode, PlayRTCDataCode = 오류 코드 정의
                     *  - None,
                     *  - NotOpen,
                     *  - SendBusy,
                     *  - SendFail,
                     *  - FileIO,
                     *  - ParseFail
                     * @param desc String, description
                     */
                    @Override
                    public void onError(PlayRTCData obj, String peerId, String peerUid, long id, PlayRTCDataCode code, String desc) {
                        Log.d(LOG_TAG, "sendFile onError " + peerUid + " " + id + "[" + code + "] " + desc);
                        activity.appnedLogMessage(">>Data-Channel sendFile[" + fileName + "] onError[" + id + "] [" + code + "] " + desc);
                        closeInputStream();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                closeInputStream();
            }
        } else {
            Log.d(LOG_TAG, "데이터 채널이 연결 상태가 아닙니다. ");
            activity.appnedLogMessage(">>Data-Channel이 연결 상태가 아닙니다.");

        }
    }

    /*
     * 데이터 수신 진행 정보
     *
     * @param obj PlayRTCData
     * @param peerId String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param recvIndex int, 수신 패킷 Index
     * @param recvSize long, 수신 패킷 데이터 원형 사이즈
     * @param header PlayRTCDataHeader, 패킷 정보
     *  - index : int, 데이터 패킷 분할 전송 index
     *  - size : long, 데이터 전체 크기
     *  - count : long, 데이터 분할 패킷 수
     *  - id : long, 전송하는 데이터의 고유 아이디
     *  - dataType : int, text 0, binary 1
     *  - fileName : String, 파일 전송일 경우 파일 명
     *  - mimeType : String, 파일 전송일 경우 파일의 Mime Type
     */
    private long recvStartTime = 0L;
    private long recvDataSize = 0L;

    @SuppressLint("DefaultLocale")
    @Override
    public void onProgress(final PlayRTCData obj, final String peerId, final String peerUid, final int recvIndex, final long recvSize, final PlayRTCDataHeader header) {

        if (recvDataSize == 0L) {
            recvStartTime = (new Date()).getTime();
        }
        recvDataSize += recvSize;
        long total = header.getSize();
        float per = ((float) recvSize / (float) total) * 100.0f;
        String sMsg = String.format("Data onProgress [%d/%d]  %.2f%%", recvSize, total, per);
        Log.d(LOG_TAG, sMsg);
        activity.progressLogMessage(sMsg);
    }

    /*
     * 데이터 수신 완료
     * 헤더 정보를 확인하고 데이터 타입에 맞게 데이터를 처리
     * 만약 수신 데이터가 파일 저장이 필요한 경우에 수신 데이터를 파일을 생성 하고 파일에 추가하는 로직이 필요
     *
     * @param obj     PlayRTCData
     * @param peerId  String, 상대방 사용자의 peer 아이디
     * @param peerUid String, 상대방 사용자의 아이디
     * @param header  PlayRTCDataHeader, 패킷 정보
     *                <pre>
     *                 - index : int, 데이터 패킷 분할 전송 index
     *                 - size : long, 데이터 전체 크기
     *                 - count : long, 데이터 분할 패킷 수
     *                 - id : long, 전송하는 데이터의 고유 아이디
     *                 - dataType : int, text 0, binary 1
     *                 - fileName : String, 파일 전송일 경우 파일 명
     *                 - mimeType : String, 파일 전송일 경우 파일의 Mime Type
     *                </pre>
     * @param data    byte[], 데이터
     */
    @Override
    public void onMessage(final PlayRTCData obj, final String peerId, final String peerUid, final PlayRTCDataHeader header, final byte[] data) {
        Log.d(LOG_TAG, "PlayRTCDataEvent onMessage peerId[" + peerId + "] peerUid[" + peerUid + "]");
        long recvDataElapsed = new Date().getTime() - recvStartTime;
        recvStartTime = 0;
        recvDataSize = 0L;
        Utils.showToast(activity, "Data Recv Elapsed-Time=" + recvDataElapsed);
        if (header.getType() == PlayRTCDataHeader.DATA_TYPE_TEXT) {
            String recvText = new String(data);
            Log.d(LOG_TAG, "Text[" + recvText + "]");
            activity.appnedLogMessage(">>Data-Channel onMessage[" + recvText + "]");
        } else {
            String filaNmae = header.getFileName();
            if (TextUtils.isEmpty(filaNmae)) {
                Log.d(LOG_TAG, "Binary[" + header.getSize() + "]");
                activity.appnedLogMessage(">>Data-Channel onMessage Binary[" + header.getSize() + "]");
            } else {
                Log.d(LOG_TAG, "File[" + filaNmae + "]");
                if (obj.getFileReveMode() == PlayRTCFileReveType.Byte) {
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Android/data/" + activity.getPackageName() + "/files/" + filaNmae);
                    Log.d(LOG_TAG, "FilePath[" + f.getAbsolutePath() + "]");
                    activity.appnedLogMessage(">>Data-Channel onMessage File[" + f.getAbsolutePath() + "]");
                    try {
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        FileOutputStream dataWs = new FileOutputStream(f.getAbsolutePath(), false);
                        dataWs.write(data);
                        dataWs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String recvFile = new String(data);
                    Log.d(LOG_TAG, "FilePath[" + recvFile + "]");
                    activity.appnedLogMessage(">>Data-Channel onMessage File[" + recvFile + "]");
                }
            }
        }
    }

    /*
     * 오류 발생
     *
     * @param obj     PlayRTCData
     * @param peerId  String, 상대방 사용자의 아이디
     * @param peerUid String, 상대방 사용자의 Application 아이디
     * @param code    PlayRTCDataCode, PlayRTCDataCode = 오류 코드 정의
     *                 - None,
     *                 - NotOpen,
     *                 - SendBusy,
     *                 - SendFail,
     *                 - FileIO,
     *                 - ParseFail
     * @param desc    String, description
     */
    @Override
    public void onError(final PlayRTCData obj, final String peerId, final String peerUid, final long id, final PlayRTCDataCode code, final String desc) {
        Utils.showToast(activity, "Data-Channel[" + peerId + "] onError[" + code + "] " + desc);
        activity.appnedLogMessage(">>Data-Channel onError[" + code + "] " + desc);
    }

    /*
     * 상태 변경 발생
     *
     * @param obj     PlayRTCData
     * @param peerId  String, 상대방 사용자의 아이디
     * @param peerUid String, 상대방 사용자의 Application 아이디
     * @param state   PlayRTCDataStatus, PlayRTCData 상태 코드 정의
     *                - None,
     *                - Connecting,
     *                - Open,
     *                - Closing,
     *                - Closed
     */
    @Override
    public void onStateChange(final PlayRTCData obj, final String peerId, final String peerUid, final PlayRTCDataStatus state) {
        Utils.showToast(activity, "Data-Channel[" + peerId + "] " + state + "...");
        activity.appnedLogMessage(">>Data-Channel " + state + "...");
    }

    private void closeInputStream() {
        if (dataIs != null) {
            try {
                dataIs.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataIs = null;
    }

}
