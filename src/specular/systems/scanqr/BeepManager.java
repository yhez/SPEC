package specular.systems.scanqr;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;
import specular.systems.R;

import java.io.IOException;

/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
final class BeepManager {

	private static final String TAG = BeepManager.class.getSimpleName();

	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;

	private final Activity activity;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private boolean vibrate;

	BeepManager(Activity activity) {
		this.activity = activity;
		this.mediaPlayer = null;
		updatePrefs();
	}

	void updatePrefs() {
		mediaPlayer = buildMediaPlayer(activity);
	}

	void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) activity
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private static MediaPlayer buildMediaPlayer(Context activity) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// When the beep has finished playing, rewind to queue up another one.
		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer player) {
						player.seekTo(0);
					}
				});

		AssetFileDescriptor file = activity.getResources().openRawResourceFd(
				R.raw.beep);
		try {
			mediaPlayer.setDataSource(file.getFileDescriptor(),
					file.getStartOffset(), file.getLength());
			file.close();
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			mediaPlayer = null;
		}
		return mediaPlayer;
	}

}
