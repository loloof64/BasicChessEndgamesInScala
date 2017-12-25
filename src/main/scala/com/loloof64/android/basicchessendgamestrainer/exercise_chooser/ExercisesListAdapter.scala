package com.loloof64.android.basicchessendgamestrainer.exercise_chooser

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, ViewGroup, View}
import android.widget.LinearLayout
import android.widget.TextView
import com.loloof64.android.basicchessendgamestrainer.MyApplication
import com.loloof64.android.basicchessendgamestrainer.R

trait ItemClickListener {
    def onClick(position: Int)
}

object ExercisesListAdapter {
    class ViewHolder(val textView: TextView) extends RecyclerView.ViewHolder(textView)
}

import ExercisesListAdapter.ViewHolder

class ExercisesListAdapter(private val exercisesList: Array[ExerciseInfo],
                           private val itemClickListener: ItemClickListener) extends RecyclerView.Adapter[ViewHolder](){

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = {
        val layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercises_list_row, parent, false).asInstanceOf[LinearLayout]
        val textView = layout.findViewById(R.id.exercise_list_row_value).asInstanceOf[TextView]
        layout.removeView(textView)
        return new ViewHolder(textView)
    }

    override def onBindViewHolder(holder: ViewHolder, position: Int) {
        def getColor(colorId: Int) : Int = {
            val context = MyApplication.getApplicationContext()
            return ResourcesCompat.getColor(context.getResources(), colorId, null)
        }

        holder.textView.setText( MyApplication.getApplicationContext().getString(exercisesList(position).textId))
        holder.textView.setOnClickListener( new View.OnClickListener{ def onClick(v: View){ itemClickListener.onClick(position) }})
        holder.textView.setBackgroundColor(
                if (exercisesList(position).mustWin) getColor(R.color.exercise_chooser_winning_color)
                else getColor(R.color.exercise_chooser_nullifying_color)
        )
    }

    override def getItemCount(): Int = {
        return exercisesList.size
    }
}
