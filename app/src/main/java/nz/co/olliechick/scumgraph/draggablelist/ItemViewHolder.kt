package nz.co.olliechick.scumgraph.draggablelist

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.R

class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    val handleView = itemView?.findViewById(R.id.handle) as ImageView?
    val textView = itemView?.findViewById(R.id.text) as TextView?
    val deleteButtonView = itemView?.findViewById(R.id.delete_button) as ImageButton?
}