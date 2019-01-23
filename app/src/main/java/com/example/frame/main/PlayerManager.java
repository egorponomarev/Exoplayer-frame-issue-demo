package com.example.frame.main;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.DefaultMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;

public final class PlayerManager implements AdsMediaSource.MediaSourceFactory {

	private final DataSource.Factory dataSourceFactory;
	private ExoPlayer player;
	private MediaSource mediaSource;
	private Renderer videoRenderer;

	PlayerManager(Context context) {
		DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
		dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayertest"), bandwidthMeterA);
	}

	void init(Context context, TextureView view) {
		if (player != null) {
			setTextureView(view);
			return;
		}

		// Create a default track selector.
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
		TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		// Create a player instance.
		player = ExoPlayerFactory.newInstance(
				new Renderer[] {
						videoRenderer = new MediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT),
						new MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT)
				},
				trackSelector,
				new DefaultLoadControl()
		);

		// Bind the player to the view.
		setTextureView(view);

		String url1 = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";
		mediaSource = buildMediaSource(Uri.parse(url1));
		mediaSource.addEventListener(new Handler(Looper.getMainLooper()), new DefaultMediaSourceEventListener() {
			@Override
			public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
				super.onLoadError(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData, error, wasCanceled);
				Log.e("Player", "error", error);
			}
		});

		player.prepare(mediaSource);
		player.setPlayWhenReady(true);
	}

	private void setTextureView(TextureView view) {
		Surface surface = new Surface(view.getSurfaceTexture());
		player.createMessage(videoRenderer)
				.setType(C.MSG_SET_SURFACE)
				.setPayload(surface)
				.send();
	}

	void pause() {
		if (player != null) {
			player.setPlayWhenReady(false);
		}
	}

	void release() {
		if (player != null) {
			player.release();
			player = null;
		}
	}

	@Override
	public MediaSource createMediaSource(Uri uri) {
		return buildMediaSource(uri);
	}

	@Override
	public int[] getSupportedTypes() {
		// IMA does not support Smooth Streaming ads.
		return new int[] { C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER };
	}

	// Internal methods.

	private MediaSource buildMediaSource(Uri uri) {
		@ContentType int type = Util.inferContentType(uri);
		switch (type) {
			case C.TYPE_DASH:
				return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_SS:
				return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_HLS:
				return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			default:
				throw new IllegalStateException("Unsupported type: " + type);
		}
	}

	void prepare() {
		if (player != null) {
			player.prepare(mediaSource, false, false);
		}
	}

	void togglePlay() {
		if (player != null) {
			player.setPlayWhenReady(!player.getPlayWhenReady());
		}
	}
}
