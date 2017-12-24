package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.R

trait ItemClickListener {
    def onClick(position: Int)
}

object ExercisesListAdapter {
    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}

class ExercisesListAdapter(private val exercisesList: List[ExerciseInfo],
                           private val itemClickListener: ItemClickListener) extends RecyclerView.Adapter[ViewHolder](){

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.exercises_list_row, parent, false).asInstanceOf[LinearLayout]
        val textView = layout.findViewById(R.id.exercise_list_row_value).asInstanceOf[TextView]
        layout.removeView(textView)
        return ViewHolder(textView)
    }

    override def onBindViewHolder(holder: ViewHolder, position: Int) {
        def getColor(colorId: Int) : Int {
            val context = MyApplication.getApplicationContext()
            return ResourcesCompat.getColor(context.resources, colorId, null)
        }

        holder.textView.text = MyApplication.getApplicationContext().getString(exercisesList(position).textId)
        holder.textView.setOnClickListener{ itemClickListener.onClick(position) }
        holder.textView.setBackgroundColor(
                if (exercisesList(position).mustWin) getColor(R.color.exercise_chooser_winning_color)
                else getColor(R.color.exercise_chooser_nullifying_color)
        )
    }

    override def getItemCount(): Int {
        return exercisesList.size
    }
}
