package nz.co.olliechick.scumgraph

import android.app.Activity
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.util.ColourOption
import nz.co.olliechick.scumgraph.util.Colours.Companion.getAllColourOptions
import nz.co.olliechick.scumgraph.util.Colours.Companion.getNextFreeColour
import nz.co.olliechick.scumgraph.util.Colours.Companion.getTextColour
import nz.co.olliechick.scumgraph.util.Player
import petrov.kristiyan.colorpicker.ColorPicker


class CreatePlayerListAdapter(
    private val players: ArrayList<Player>,
    private val manager: LinearLayoutManager,
    private val activity: Activity,
    private val notifyPlayerListUpdated: () -> Unit
) :
    RecyclerView.Adapter<CreatePlayerListAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout) {
        val playerName: EditText? = linearLayout.findViewById(R.id.playerName)
        val removeButton: Button? = linearLayout.findViewById(R.id.removeButton)
        val pickColourButton: Button? = linearLayout.findViewById(R.id.pickColourButton)

        val button: Button? = linearLayout.findViewById(R.id.button)
    }

    override fun getItemViewType(position: Int) =
        if (position == players.size) R.layout.add_player_button else R.layout.create_player_item

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        val linearLayout =
            LayoutInflater.from(parent.context).inflate(viewType, parent, false) as LinearLayout
        return PlayerViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (holder.adapterPosition == players.size) {
            // The "Add player" button
            holder.button?.setOnClickListener {
                players.add(Player("", getNextFreeColour(players)))
                notifyPlayerListUpdated()
                if (players.size == 4) {
                    notifyDataSetChanged()
                } else {
                    notifyItemInserted(players.size - 1)
                    notifyItemRangeChanged(players.size - 1, players.size)
                }
                manager.scrollToPosition(players.size)
            }
            holder.button?.isEnabled = players.size < ColourOption.values().size

        } else {
            // Player entry - textbox and remove button
            holder.playerName?.setText(players[holder.adapterPosition].name)
            holder.playerName?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    players[holder.adapterPosition].name = s.toString()
                    notifyPlayerListUpdated()
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) = Unit
            })

            holder.removeButton?.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != -1) {
                    players.removeAt(pos)
                    if (players.size == 3) {
                        notifyDataSetChanged()
                    } else {
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, players.size)
                    }

                    notifyPlayerListUpdated()
                }
            }

            holder.removeButton?.isEnabled = players.size > 3

            val colour = players[holder.adapterPosition].colour
            holder.pickColourButton?.let {
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(colour))
            }
            holder.pickColourButton?.setTextColor(getTextColour(colour))
            holder.pickColourButton?.setOnClickListener {
                val colorPicker = ColorPicker(activity)
                colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                    override fun onChooseColor(position: Int, colour: Int) {
                        players[holder.adapterPosition].colour = colour
                        ViewCompat.setBackgroundTintList(
                            holder.pickColourButton,
                            ColorStateList.valueOf(colour)
                        )
                        holder.pickColourButton.setTextColor(getTextColour(colour))
                        notifyPlayerListUpdated()
                        notifyItemChanged(holder.adapterPosition)
                    }

                    override fun onCancel() {}
                })
                    .setColors(getAllColourOptions(players, colour))
                    .setDefaultColorButton(colour)
                    .setRoundColorButton(true)
                    .show()
            }
        }
    }

    override fun getItemCount() = players.size + 1
}