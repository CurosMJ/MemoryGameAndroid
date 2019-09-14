package `in`.curos.memorygame

import android.animation.ObjectAnimator
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.ViewAnimator
import com.squareup.picasso.Picasso
import java.util.*

class CardGridAdapter(val context: Context, images: List<String>, val matchesToFind: Int, val onUpdate: (CardGridAdapter) -> Unit ) : RecyclerView.Adapter<CardGridAdapter.VH>() {
    var images: List<String> = emptyList()
    var openCards: MutableList<Int> = ArrayList()
    var solvedCards: MutableList<Int> = ArrayList()
    var disable = false

    init {
        this.images = images
    }

    fun disableGame() {
        this.disable = true
    }

    override fun onCreateViewHolder(root: ViewGroup, viewType: Int): VH {
        return VH(Card(root.context))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(viewHolder: VH, index: Int) {
        Picasso.get().load(Uri.parse(images[index])).into(viewHolder.imageView)

        if (solvedCards.contains(index)) {
            viewHolder.card.completed()
            Handler().postDelayed({
                viewHolder.imageView?.animate()
                        ?.alpha(0f)
                        ?.setDuration(500)
                        ?.setInterpolator(AccelerateInterpolator())
                        ?.start()
            }, 1600)
        } else {
            // Sync card's state with data
            viewHolder.card.show(openCards.contains(index))
        }

        viewHolder.card.setOnClickListener(fun (view: View) {
            var card = view as Card

            if (openCards.size < matchesToFind && !solvedCards.contains(index) && !card.isShowing() && !disable) {
                openCards.add(index)
                notifyItemChanged(index)

                if (openCards.size == matchesToFind) {
                    // Find out if the open cards are the same
                    var distinct = getOpenCardUrls().distinct()
                    if (distinct.size == 1) {
                        solvedCards.addAll(openCards)
                        openCards.forEach {
                            notifyItemChanged(it)
                        }
                        openCards.clear()
                        onUpdate(this)
                    } else {
                        Handler().postDelayed({
                            openCards.forEach {
                                notifyItemChanged(it)
                            }
                            openCards.clear()
                        }, 800)
                    }
                }
            }
        })
    }

    fun getOpenCardUrls(): ArrayList<String> {
        var list = ArrayList<String>()
        for (i in openCards) {
            list.add(images[i])
        }
        return list
    }

    class VH(var card: Card): RecyclerView.ViewHolder(card) {
        var imageView: ImageView? = null
        init {
            imageView = card.findViewById(R.id.image)
        }
    }
}