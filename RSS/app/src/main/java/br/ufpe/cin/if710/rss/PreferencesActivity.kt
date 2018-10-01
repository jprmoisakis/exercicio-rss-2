package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceFragment

class PreferencesActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frag_prefs)
    }
    class RssFeedPreferenceFragment: PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferencias)
        }
    }
}