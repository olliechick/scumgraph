package nz.co.olliechick.scumgraph

import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.util.ColourOption
import nz.co.olliechick.scumgraph.util.Colours.Companion.getAllColourOptions
import nz.co.olliechick.scumgraph.util.Colours.Companion.getNextFreeColour
import nz.co.olliechick.scumgraph.util.Colours.Companion.getTextColour
import petrov.kristiyan.colorpicker.ColorPicker


class PlayerListAdapter(
    private val players: ArrayList<Player>,
    private val manager: LinearLayoutManager,
    private val activity: Activity
) :
    RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class PlayerViewHolder(linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout) {
        val playerName: EditText? = linearLayout.findViewById(R.id.playerName)
        val removeButton: Button? = linearLayout.findViewById(R.id.removeButton)
        val pickColourButton: Button? = linearLayout.findViewById(R.id.pickColourButton)

        val button: Button? = linearLayout.findViewById(R.id.button)
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == players.size) R.layout.add_player_button else R.layout.player_item
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        // create a new view
        val linearLayout =
            LayoutInflater.from(parent.context).inflate(viewType, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters

        val holder = PlayerViewHolder(linearLayout)
//        holder.pickColourButton = players
        return holder
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (holder.adapterPosition == players.size) {
            // The "Add player" button
            holder.button?.setOnClickListener {
                players.add(Player("", getNextFreeColour(players)))
                notifyItemInserted(players.size - 1)
                notifyItemRangeChanged(players.size - 1, players.size)
                manager.scrollToPosition(players.size)
            }
        } else {
            // Player entry - textbox and remove button
            holder.playerName?.setText(players[holder.adapterPosition].name)
            holder.playerName?.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    players[holder.adapterPosition].name = s.toString()
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                }
            })
            holder.removeButton?.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != -1) {
                    players.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, players.size)
                }
            }
            val colour = players[holder.adapterPosition].colour
            holder.pickColourButton?.setBackgroundColor(colour)
            holder.pickColourButton?.setTextColor(getTextColour(colour))
            Log.i("scum", ColourOption.valueOf(ColourOption.values()[10].toString()).colour)
//            val x = ColourOption. .values().toList()
//            val y =ArrayList(x)
//            y
            holder.pickColourButton?.setOnClickListener {
                val colorPicker = ColorPicker(activity)
                colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                    override fun onChooseColor(position: Int, colour: Int) {
                        players[holder.adapterPosition].colour = colour
                        holder.pickColourButton.setBackgroundColor(colour)
                        holder.pickColourButton.setTextColor(getTextColour(colour))
                    }

                    override fun onCancel() {}
                })
                    .setColors(getAllColourOptions())
                    .setDefaultColorButton(players[holder.adapterPosition].colour)
                    .setRoundColorButton(true)
                    .show()
            }
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = players.size + 1
}