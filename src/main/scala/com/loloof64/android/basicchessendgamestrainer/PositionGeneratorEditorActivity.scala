package com.loloof64.android.basicchessendgamestrainer

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v7.app.AppCompatActivity
import android.view._
import android.widget.TextView

class PositionGeneratorEditorActivity extends AppCompatActivity() {

    implicit val context = this
    lazy val vh: TypedViewHolder.activity_position_generator_editor = TypedViewHolder.setContentView(this, TR.layout.activity_position_generator_editor)

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter = null

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_position_generator_editor)

        setSupportActionBar(vh.toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager())

        // Set up the ViewPager with the sections adapter.
        vh.container.setAdapter(mSectionsPagerAdapter)

        vh.container.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(vh.tabs))
        vh.tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vh.container))

        vh.fab.setOnClickListener(new View.OnClickListener { 
            override def onClick(view: View) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        })

    }


    override def onCreateOptionsMenu(menu: Menu): Boolean =  {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_position_generator_editor, menu)
        return true
    }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.getItemId()

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

        override def getItem(position: Int): Fragment = {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1)
        }

        override def getCount(): Int = {
            // Show 3 total pages.
            return 3
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment extends Fragment() {

        override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                                  savedInstanceState: Bundle): View = {
            val rootView = inflater.inflate(R.layout.fragment_position_generator_editor, container, false)
            val section_label = rootView.findViewById(R.id.section_label).asInstanceOf[TextView]
            section_label.setText(getString(R.string.section_format, getArguments().getInt(PlaceholderFragment.ARG_SECTION_NUMBER).asInstanceOf[AnyRef]))
            return rootView
        }

    }

    object PlaceholderFragment {
        /**
            * The fragment argument representing the section number for this
            * fragment.
            */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
            * Returns a new instance of this fragment for the given section
            * number.
            */
        def newInstance(sectionNumber: Int): PlaceholderFragment = {
            val fragment = new PlaceholderFragment()
            val args = new Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.setArguments(args)
            return fragment
        }
    }

}
