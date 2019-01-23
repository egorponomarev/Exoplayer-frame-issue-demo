package com.example.frame.main;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;

import com.google.android.exoplayer2.PlaybackPreparer;

public final class MainActivity extends Activity implements PlaybackPreparer, TextureView.SurfaceTextureListener {

	private TextureView playerView;
	private PlayerManager player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		playerView = findViewById(R.id.player_view);
		playerView.setOnClickListener(v -> player.togglePlay());
		player = new PlayerManager(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		playerView.setSurfaceTextureListener(this);
		if (playerView.isAvailable()) {
			player.init(this, playerView);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		playerView.setSurfaceTextureListener(null);
		player.pause();
	}

	@Override
	public void onDestroy() {
		player.release();
		super.onDestroy();
	}

	@Override
	public void preparePlayback() {
		player.prepare();
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		player.init(this, playerView);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}
}
