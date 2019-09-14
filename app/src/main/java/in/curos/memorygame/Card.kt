package `in`.curos.memorygame

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import com.squareup.picasso.Picasso

class Card(context: Context) : FrameLayout(context) {
    var flipper: ViewFlipper? = null

    init {
        View.inflate(context, R.layout.card, this)
        flipper = findViewById(R.id.flipper)
        flipper?.setInAnimation(context, android.R.anim.fade_in)
        flipper?.setOutAnimation(context, android.R.anim.fade_out)
    }

    fun isShowing(): Boolean {
        return flipper?.displayedChild == 1
    }

    fun show(show: Boolean) {
        if (show) {
            flipper?.displayedChild = 1
        } else {
            flipper?.displayedChild = 0
        }
    }

    fun reset() {
        flipper?.displayedChild = 0
    }

    fun completed() {
        this.show(true)
        flipper?.findViewById<View>(R.id.image_holder)?.setBackgroundResource(R.drawable.card_background_done)
    }
}