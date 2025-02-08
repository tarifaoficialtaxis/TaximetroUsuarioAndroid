package com.mitarifamitaxi.taximetrousuario.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.mitarifamitaxi.taximetrousuario.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContent {
            SplashScreen {
                validateNextScreen()
            }
        }

        //validateNextScreen()


    }

    private fun validateNextScreen() {
        val sharedPref = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userJson = sharedPref.getString("USER_OBJECT", null)

        if (userJson != null) {

            startActivity(
                Intent(this, HomeActivity::class.java)
            )
            finish()

        } else {
            if (hasUserAcceptedTerms()) {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
                finish()
            } else {
                startActivity(
                    Intent(this, TermsConditionsActivity::class.java)
                )
                finish()
            }
        }
    }


    private fun hasUserAcceptedTerms(): Boolean {
        val sharedPref = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("accepted_terms", false)
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun SplashScreen(onVideoFinished: () -> Unit) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black)),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // Hides playback controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FILL // Crops video to fullscreen

                    val player = ExoPlayer.Builder(ctx).build()
                    this.player = player

                    val mediaItem =
                        MediaItem.fromUri(Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash}"))
                    player.setMediaItem(mediaItem)

                    player.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                onVideoFinished()
                            }
                        }
                    })

                    player.prepare()
                    player.playWhenReady = true
                }
            }
        )
    }


}