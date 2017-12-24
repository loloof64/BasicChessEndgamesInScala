package com.loloof64.android.basicchessendgamestrainer.playing_activity

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, ViewGroup, LinearLayout, TextView}
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.R
import java.lang.ref.WeakReference

case class RowInput(val san:String, val relatedFen: String, val moveToHighlight: MoveToHighlight)
case class MoveToHighlight(val startFile: Int, val startRank : Int,
                           val endFile: Int, val endRank : Int)

abstract class ItemClickListener {
    def onClick(weakRefContext: WeakReference[Context], position: Int,
                         positionFen: String, moveToHighlight: MoveToHighlight)
}

object MovesListAdapter {
    class ViewHolder(val textView: TextView) extends RecyclerView.ViewHolder(textView)
}

class MovesListAdapter(private val weakRefContext: WeakReference[Context], private val itemClickListener: ItemClickListener) extends RecyclerView.Adapter[ViewHolder]() {
    @SuppressWarnings("DEPRECATION")
    private def getColor(colorResId: Int): Int = MyApplication.getApplicationContext().resources.getColor(colorResId)

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = {
        val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.playing_activity_moves_list_single_item, parent, false).asInstanceOf[LinearLayout]
        val txtView = layout.findViewById(R.id.moves_list_view_item).asInstanceOf[TextView]
        val font = Typeface.createFromAsset(MyApplication.appContext.assets, "FreeSerif.ttf")
        txtView.typeface = font

        layout.removeView(txtView)
        return ViewHolder(txtView)
    }

    override def onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        holder.textView.text = inputsList[currentPosition].san
        holder.textView.setBackgroundColor(getColor(
                if (position == _selectedNavigationItem && _switchingPositionFeatureActive) R.color.moves_history_cell_selected_color
                else R.color.moves_history_cell_standard_color
        ))
        if (position%3 > 0) {
            holder.textView.setOnClickListener {
                _selectedNavigationItem = currentPosition
                updateHostView()
                update()
            }
        }
    }

    override def getItemCount(): Int = {
        return inputsList.size
    }

    def addPosition(san: String, fen: String, moveToHighlight: MoveToHighlight){
        inputsList.add(RowInput(san, fen, moveToHighlight))
        update()
    }

    def clear() {
        inputsList.clear()
        update()
    }

    def goBackInHistory(){
        if (_switchingPositionFeatureActive){
            if (_selectedNavigationItem > 1) {
                _selectedNavigationItem -= 1
                val pointingToAMoveNumber = _selectedNavigationItem % 3 == 0
                val pointingToAMoveAnnotation = items[_selectedNavigationItem].san == ".."
                if (pointingToAMoveNumber){
                    if (_selectedNavigationItem > 1) _selectedNavigationItem -= 1 // going further back
                    else _selectedNavigationItem += 1 //cancelling
                }
                if (pointingToAMoveAnnotation){
                    _selectedNavigationItem += 1 // cancelling one step
                }
                updateHostView()
                update()
            }
        }
    }

    def goForwardInHistory(){
        if (_switchingPositionFeatureActive){
            if (_selectedNavigationItem < (inputsList.size - 1)) {
                _selectedNavigationItem += 1
                val pointingToAMoveAnnotation = _selectedNavigationItem % 3 == 0
                if (pointingToAMoveAnnotation){
                    if (_selectedNavigationItem < (inputsList.size - 1)) _selectedNavigationItem += 1 // going further
                    else _selectedNavigationItem -= 1 // cancelling
                }
                updateHostView()
                update()
            }
        }
    }

    def switchingPosition: Boolean = _switchingPositionFeatureActive
    def switchingPosition_=(value: Boolean) {
        if (inputsList.size > 0) {
            _switchingPositionFeatureActive = value
            _selectedNavigationItem = inputsList.size - 1
            updateHostView()
            update()
        }
    }

    def items: Array[RowInput] = inputsList.toTypedArray()
    def items_=(value: Array[RowInput]){
        inputsList = value.toArrayBuffer
        update()
    }

    def selectedNavigationItem: Int = selectedNavigationItem
    def selectedNavigationItem_=(value: Int){
        if (_switchingPositionFeatureActive){
            _selectedNavigationItem = value
            updateHostView()
            update()
        }
    }

    private def updateHostView(){ // switch the current position in host view (Playing activity)
        val relatedFen = inputsList(_selectedNavigationItem).relatedFen
        val moveToHighlight = inputsList(_selectedNavigationItem).moveToHighlight
        if (relatedFen.isNotEmpty() && _switchingPositionFeatureActive) {
            itemClickListener.onClick(weakRefContext, _selectedNavigationItem, relatedFen, moveToHighlight)
        }
    }

    private def update(){ // switch the position highlighter in host view (Playing activity)
        notifyDataSetChanged()
    }

    private var inputsList = new ArrayBuffer[RowInput]()
    private var _switchingPositionFeatureActive = false
    private var _selectedNavigationItem = -1
}