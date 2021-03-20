package nz.co.olliechick.scumgraph

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
    var numberOfMiddlemen: Int,
    private val players: ArrayList<Player>,
    private val dragStartListener: OnStartDragListener,
    private val context: Context,
    private val notifyChartUpdated: () -> Unit
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

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n") //todo override performClick
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val player = players[position]

        holder.textView?.text =
            "${player.name}: ${generateTitle(position, numberOfMiddlemen, players.size, context)}"
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

        val handleDrawable =
            if (Colours.isWhiteText(player.colour)) R.drawable.ic_reorder_white_500_24dp
            else R.drawable.ic_reorder_black_500_24dp
        holder.handleView?.setImageDrawable(
            context.resources.getDrawable(
                handleDrawable,
                context.theme
            )
        )

        val deleteButtonDrawable =
            if (Colours.isWhiteText(player.colour)) R.drawable.baseline_delete_white_24
            else R.drawable.baseline_delete_black_24
        holder.deleteButtonView?.setImageDrawable(
            context.resources.getDrawable(
                deleteButtonDrawable,
                context.theme
            )
        )

        holder.deleteButtonView?.setOnClickListener {
            AlertDialog.Builder(context).run {
                setTitle(context.getString(R.string.are_you_sure_delete_player, player.name))
                setMessage(context.getString(R.string.you_cant_add_them_back))
                setPositiveButton(context.getString(R.string.remove)) { _, _ ->
                    onRemovePlayer(player.name)
                }
                setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
                show()
            }
        }

        holder.deleteButtonView?.visibility = if (players.size > 3) View.VISIBLE else View.GONE

    }

    override fun onRemovePlayer(name: String) {
        val position = players.indexOfFirst { it.name === name }
        players.removeAt(position)
        // we need to update the labels on all of them, so their status (e.g. President) updates
        notifyDataSetChanged()
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
        notifyChartUpdated()
    }
}