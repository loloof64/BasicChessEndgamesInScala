package com.loloof64.android.basicchessendgamestrainer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class HelpActivity extends AppCompatActivity() {

    implicit val context = this
    lazy val vh: TypedViewHolder.main = TypedViewHolder.setContentView(this, TR.layout.activity_help)

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        vh.help_text_view.text = resources.getString(R.string.help)
                .replace("[CR]", System.getProperty("line.separator"))
                .replace("[TAB]", "    ")
    }
}
