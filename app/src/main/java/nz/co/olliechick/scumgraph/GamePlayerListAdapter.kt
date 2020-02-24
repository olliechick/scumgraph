package nz.co.olliechick.scumgraph

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.draggablelist.ItemTouchHelperAdapter
import nz.co.olliechick.scumgraph.draggablelist.ItemViewHolder
import nz.co.olliechick.scumgraph.draggablelist.OnStartDragListener
import nz.co.olliechick.scumgraph.util.Colours
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.generateTitle
import java.util.*

class GamePlayerListAdapter(
    private val numberOfMiddlemen: Int,
    private val players: ArrayList<Player>,
    private val dragStartListener: OnStartDragListener,
    private val context: Context
) :
    RecyclerView.Adapter<ItemViewHolder?>(),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.player_item,
                parent,
                false
            )
        )

    @SuppressLint("ClickableViewAccessibility") //todo override performClick
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val player = players[position]
        holder.textView?.text =
            "${generateTitle(position, numberOfMiddlemen, players.size, context)}: ${player.name}"
        holder.handleView?.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) dragStartListener.onStartDrag(holder)
            false
        }
        holder.itemView.setBackgroundColor(player.colour)
        holder.textView?.setTextColor(
            Colours.getTextColour(
                player.colour
            )
        )
        val drawable = if (Colours.isWhiteText(player.colour)) R.drawable.ic_reorder_white_500_24dp
        else R.drawable.ic_reorder_black_500_24dp
        holder.handleView?.setImageDrawable(context.resources.getDrawable(drawable, context.theme))
    }

    override fun getItemCount() = players.size

    override fun onItemDismiss(position: Int) {}

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(players, i, i + 1)
                notifyItemChanged(i, false)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(players, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        notifyItemChanged(fromPosition, false)
        notifyItemChanged(toPosition, false)
    }
}