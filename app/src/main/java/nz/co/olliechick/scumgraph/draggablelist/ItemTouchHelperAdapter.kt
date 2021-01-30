package nz.co.olliechick.scumgraph.draggablelist

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
    fun onRemovePlayer(name: String)
}