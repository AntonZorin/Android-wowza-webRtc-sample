package com.az.webrtcwowzaexample.common;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;

import org.webrtc.ThreadUtils;

/**
 * Created by zorin.a on 15.09.2017.
 */

public class RTCAudioManager {
    private static final String TAG = "RTCAudioManager";
    private final Context context;
    private AudioManagerState state;
    private AudioManager audioManager;
    private OnAudioFocusChangeListener audioFocusChangeListener;
    private int savedAudioMode;


    /**
     * AudioManager state.
     */
    public enum AudioManagerState {
        UNINITIALIZED,
        PREINITIALIZED,
        RUNNING,
    }


    private RTCAudioManager(Context context) {
        ThreadUtils.checkIsOnMainThread();
        this.context = context;
        audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        state = AudioManagerState.UNINITIALIZED;
    }

    public static RTCAudioManager create(Context context) {
        return new RTCAudioManager(context);
    }

    public void start() {
        Log.d(TAG, "start");
        ThreadUtils.checkIsOnMainThread();
        if (state == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active");
            return;
        }

        state = AudioManagerState.RUNNING;
        savedAudioMode = audioManager.getMode();
        Log.d(TAG, "AudioManager starts...");

        audioFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                String typeOfChange = "AUDIOFOCUS_NOT_DEFINED";
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        typeOfChange = "AUDIOFOCUS_GAIN";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        typeOfChange = "AUDIOFOCUS_LOSS";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                        break;
                    default:
                        typeOfChange = "AUDIOFOCUS_INVALID";
                        break;
                }
                Log.d(TAG, "onAudioFocusChange: " + typeOfChange);
            }
        };

        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(TAG, "Audio focus request failed");
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode.
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);
        Log.e(TAG, "AudioManager.MODE_IN_COMMUNICATION");

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false);
    }

    public void stop() {
        Log.d(TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (state != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: " + state);
            return;
        }
        state = AudioManagerState.UNINITIALIZED;
        audioManager.setMode(savedAudioMode);
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * Sets the microphone mute state.
     */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }

        audioManager.setMicrophoneMute(on);
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }
}
