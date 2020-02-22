package nz.co.olliechick.scumgraph

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val players = arrayListOf("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = PlayerListAdapter(players, viewManager as LinearLayoutManager)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    fun startGame(view: View) {
        toast("Not yet supported.")
    }
}

class PlayerListAdapter(
    private val players: ArrayList<String>,
    private val manager: LinearLayoutManager
) :
    RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class PlayerViewHolder(linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout) {
        val playerName: EditText? = linearLayout.findViewById(R.id.playerName)
        val button: Button = linearLayout.findViewById(R.id.button)
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

        return PlayerViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        if (holder.adapterPosition == players.size) {
            // The "Add player" button
            holder.button.setOnClickListener {
                players.add("")
                notifyItemInserted(players.size - 1)
                notifyItemRangeChanged(players.size - 1, players.size)
                manager.scrollToPosition(players.size)
            }
        } else {
            // Player entry - textbox and remove button
            holder.playerName?.setText(players[holder.adapterPosition])
            holder.playerName?.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    players[holder.adapterPosition] = s.toString()
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
            holder.button.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != -1) {
                    players.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, players.size)
                }
            }
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = players.size + 1
}