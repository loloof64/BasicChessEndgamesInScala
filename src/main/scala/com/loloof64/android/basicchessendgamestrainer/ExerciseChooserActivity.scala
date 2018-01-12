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
import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentPagerAdapter}
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view._
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.{ExercisesListAdapter, ItemClickListener}
import com.loloof64.android.basicchessendgamestrainer.exercise_chooser.Exercises.availableGenerators
import com.loloof64.android.basicchessendgamestrainer.playing_activity.EngineInteraction

object ExerciseChooserPagerAdapter {
    val pagesCount = 2
}

class ExerciseChooserPagerAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

  override def getItem(item: Int): Fragment = {
    item match {
      case 0 => ExerciseChooserPredefinedExercisesFragment.newInstance()
      case 1 => ExerciseChooserCustomExercisesFragment.newInstance()
    }
  }

  override def getCount: Int = ExerciseChooserPagerAdapter.pagesCount

  override def getPageTitle(position: Int): CharSequence = {
    MyApplication.getApplicationContext.getResources.getString(position match {
      case 0 => R.string.predefined_exercises
      case 1 => R.string.custom_exercises
    })
  }
}

class ExerciseChooserPredefinedExercisesFragment extends Fragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_exercise_chooser_predefined, container, false)

    val exercisesListView = rootView.findViewById[RecyclerView](R.id.exercisesListView)
    exercisesListView.setLayoutManager(new LinearLayoutManager(getActivity))
    exercisesListView.setAdapter(new ExercisesListAdapter(availableGenerators, new ItemClickListener {
      override def onClick(position: Int): Unit = {
        val intent = new Intent(getActivity, classOf[PlayingActivity])
        val bundle = new Bundle()
        bundle.putInt(PlayingActivity.generatorIndexKey, position)
        intent.putExtras(bundle)
        startActivity(intent)
      }
    }))

    rootView
  }
}

object ExerciseChooserPredefinedExercisesFragment {
  def newInstance() = new ExerciseChooserPredefinedExercisesFragment()
}

class ExerciseChooserCustomExercisesFragment extends Fragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_exercise_chooser_custom, container, false)
    rootView
  }
}

object ExerciseChooserCustomExercisesFragment {
  def newInstance() = new ExerciseChooserCustomExercisesFragment()
}

class ExerciseChooserActivity extends FragmentActivity {

    implicit val context = this
    lazy val vh: TypedViewHolder.activity_exercise_chooser = TypedViewHolder.setContentView(this, TR.layout.activity_exercise_chooser)

    override def onCreate(savedInstanceState: Bundle): Unit = {
        super.onCreate(savedInstanceState)
        EngineInteraction.copyStockfishIntoInternalMemoryIfNecessary()
        vh.exercise_chooser_pager.setAdapter(new ExerciseChooserPagerAdapter(getSupportFragmentManager))
        vh.exercise_chooser_tabs.setupWithViewPager(vh.exercise_chooser_pager)
    }

  override def onCreateOptionsMenu(menu: Menu): Boolean =  {
    getMenuInflater.inflate(R.menu.menu_exercise_chooser, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_help =>
        val intent = new Intent(this, classOf[HelpActivity])
        startActivity(intent)
        true

      case _ => super.onOptionsItemSelected(item)
    }
  }
}