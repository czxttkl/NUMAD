/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
 ***/
package edu.neu.zhengxingchen.madcourse.dabble;

import android.content.Context;
import android.media.MediaPlayer;

public class Music {
	private static MediaPlayer mp = null;
	public static boolean musicShouldPause = true;
	public static boolean musicPaused = false;

	/** Stop old song and start new one */

	public static void play(Context context, int resource) {
		stop(context);

		// Start music only if not disabled in preferences
		if (Prefs.getMusic(context)) {
			mp = MediaPlayer.create(context, resource);
			mp.setLooping(true);
			mp.start();
		}
	}

	/** Stop the music */
	public static void stop(Context context) {
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
	}

	public static void pause(Context context) {
		if (mp != null) {
			mp.pause();
		}
	}

	public static void start(Context context) {
		if (mp != null && Prefs.getMusic(context)) {
			mp.start();
		}
	}

	public static void start(Context context, int resource) {
		if (Prefs.getMusic(context)) {
			if (mp == null) {
				mp = MediaPlayer.create(context, resource);
				mp.setLooping(true);
				mp.start();
			} else
				mp.start();
		}
	}

}
