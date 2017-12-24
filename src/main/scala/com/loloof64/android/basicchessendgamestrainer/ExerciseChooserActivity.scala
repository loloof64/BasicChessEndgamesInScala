/**
BasicChessEndgamesInScala : very basic chess endgames training for android devices.
    Copyright (C) 2017  Laurent Bernabe

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

package com.loloof64.android.basicchessendgamestrainer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.{Menu, MenuItem}
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.{ExercisesListAdapter, ItemClickListener}
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.Exercises.availableGenerators
import com.loloof64.android.basicchessendgamestrainer.playing_activity.EngineInteraction
import kotlinx.android.synthetic.main.activity_exercise_chooser._

class ExerciseChooserActivity extends AppCompatActivity {

    implicit val context = this
    lazy val vh: TypedViewHolder.main = TypedViewHolder.setContentView(this, TR.layout.activity_exercise_chooser)

    override def onCreate(savedInstanceState: Bundle): Unit = {
        super.onCreate(savedInstanceState)

        EngineInteraction.copyStockfishIntoInternalMemoryIfNecessary()

        vh.exercisesListView.layoutManager = new LinearLayoutManager(this)
        vh.exercisesListView.adapter = new ExercisesListAdapter(availableGenerators, new ItemClickListener {
            override def onClick(position: Int) {
                val intent = new Intent(context, PlayingActivity::getClass)
                val bundle = new Bundle()
                bundle.putInt(PlayingActivity.generatorIndexKey, position)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        })
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean =  {
        menuInflater.inflate(R.menu.menu_exercise_chooser, menu)
        return true
    }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        return item.itemId match {
            case R.id.action_help => {
                val intent = Intent(this, HelpActivity.getClass)
                startActivity(intent)
                return true
            }
            case _ => super.onOptionsItemSelected(item)
        }
    }
}