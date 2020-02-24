package nz.co.olliechick.scumgraph.draggablelist

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.R

class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    val textView = itemView?.findViewById(R.id.text) as TextView?
    val handleView = itemView?.findViewById(R.id.handle) as ImageView?
}