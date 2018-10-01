package br.ufpe.cin.if710.rss

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.content.SharedPreferences
import android.view.MenuInflater
import android.view.Menu
import android.view.MenuItem

class MainActivity : Activity() {
    //carrega a partir do strings.xml
    var RSS_FEED =""
    //inicia o adapter com lista vazia, para que os dados possam ser mudados posteriormente
    private var adapter = RssListAdapter(emptyList(),this)

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        conteudoRSS.layoutManager =LinearLayoutManager(this,LinearLayout.VERTICAL,false)
        conteudoRSS.adapter = adapter
        preferences = this.getSharedPreferences(RSS_FEED, 0)
        RSS_FEED = preferences.getString("rssfeed",getString(R.string.rssfeed))

    }

    override fun onStart() {
        super.onStart()
        try {
            getRssFeedAux(RSS_FEED)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    //criada funcao auxiliar para fazer o uso do async
    @Throws(IOException::class)
    fun getRssFeedAux(feed:String){
        //cria uma thread para pode fazer operações de rede
        doAsync {
            val feedXml =  getRssFeed(feed)
            val feedList = ParserRSS.parse(feedXml)
            //permite que conteudos da thread principal posssam ser alteradas para uma thread alternativa
            uiThread {
                //modifica a lista do adapter
                adapter.rss = feedList
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.actions_opt -> {
                startActivity(Intent(applicationContext, PreferencesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    @Throws(IOException::class)
    private fun getRssFeed(feed:String):String {
        var iN: InputStream? = null
        var rssFeed = ""
        try{
            val url= URL(feed)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            iN = conn.getInputStream()
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count:Int = iN.read(buffer)
            while (count != -1) {
                out.write(buffer, 0, count)
                count = iN.read(buffer)
            }
            val response = out.toByteArray()
            rssFeed = String(response, charset("UTF-8"))
        }finally {
            if(iN != null) {
                iN.close()
            }
        }
        return rssFeed
    }

}




