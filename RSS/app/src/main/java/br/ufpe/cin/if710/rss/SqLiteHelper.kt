package br.ufpe.cin.if710.rss

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.ufpe.cin.if710.rss.ItemRSS


class SQLiteRSSHelper private constructor(//alternativa
        internal var c: Context) : SQLiteOpenHelper(c, DATABASE_NAME, null, DB_VERSION) {
    val items: Cursor?
        @Throws(SQLException::class)
        get() = null

    override fun onCreate(db: SQLiteDatabase) {
        //Executa o comando de criação de tabela
        db.execSQL(CREATE_DB_COMMAND)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //estamos ignorando esta possibilidade no momento
        throw RuntimeException("nao se aplica")
    }

    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    fun insertItem(item: ItemRSS): Long {
        return insertItem(item.title, item.pubDate, item.description, item.link)
    }

    fun insertItem(title: String, pubDate: String, description: String, link: String): Long {
        //insere item no banco
        val database = this.writableDatabase
        val values = ContentValues()
        values.put(ITEM_TITLE, title)
        values.put(ITEM_DATE, pubDate)
        values.put(ITEM_DESC, description)
        values.put(ITEM_LINK, link)
        values.put(ITEM_UNREAD, false)
        return database.insert(DATABASE_TABLE, null, values)
    }

    @Throws(SQLException::class)
    fun getItemRSS(link: String): ItemRSS {
        val query = "SELECT * FROM " + RssProviderContract.ITEMS_TABLE + " WHERE " + RssProviderContract.LINK + " =  \"$link\""
        val database = this.writableDatabase
        //faz a query
        val cursor = database.rawQuery(query, null)
        lateinit var item: ItemRSS
        cursor.moveToFirst()
        //se existir retornara resultado
        if (cursor.count > 0) {
            val title = cursor.getString(cursor.getColumnIndex(RssProviderContract.TITLE))
            val pubDate = cursor.getString(cursor.getColumnIndex(RssProviderContract.DATE))
            val description = cursor.getString(cursor.getColumnIndex(RssProviderContract.DESCRIPTION))
            val link = cursor.getString(cursor.getColumnIndex(RssProviderContract.LINK))

            item = ItemRSS(title, link, pubDate, description)

        }
        cursor.close()
        database.close()
        return item
    }

    fun markAsUnread(link: String): Boolean {
        return markAs("unread",link)
    }

    fun markAsRead(link: String): Boolean {
        return markAs("read",link)
    }

    fun markAs(read:String,link:String ) :Boolean {
        val database = this.writableDatabase
        val values = ContentValues()
        var ret = false
        if(read =="read"){
            values.put(RssProviderContract.UNREAD,0)
            ret = true
        }else{
            values.put(RssProviderContract.UNREAD, 1)
        }
        database.update(RssProviderContract.ITEMS_TABLE, values, (RssProviderContract.LINK + "=?"), arrayOf(link))
        database.close()

        return ret
    }

    fun getItems():List<ItemRSS> {
        val query = "SELECT * FROM " + RssProviderContract.ITEMS_TABLE + " WHERE " + RssProviderContract.UNREAD + " = 1"
        val database = this.writableDatabase
        var cursor: Cursor = database.rawQuery(query, null)
        var item:ItemRSS
        val items = ArrayList<ItemRSS>()
        var title:String
        var pubDate:String
        var description:String
        var link:String
        if (cursor.count > 0) {
            while (cursor.isAfterLast == false) {
                title = cursor.getString(cursor.getColumnIndex(RssProviderContract.TITLE))
                pubDate = cursor.getString(cursor.getColumnIndex(RssProviderContract.DATE))
                description = cursor.getString(cursor.getColumnIndex(RssProviderContract.DESCRIPTION))
                link = cursor.getString(cursor.getColumnIndex(RssProviderContract.LINK))
                item = ItemRSS(title, link, pubDate, description)
                items.add(item)
                cursor.moveToNext()
              }
        }
        cursor.close()
        return items
    }




    companion object {
        //Nome do Banco de Dados
        private val DATABASE_NAME = "rss"
        //Nome da tabela do Banco a ser usada
        val DATABASE_TABLE = "items"
        //Versão atual do banco
        private val DB_VERSION = 1

        private var db: SQLiteRSSHelper? = null

        //Definindo Singleton
        fun getInstance(c: Context): SQLiteRSSHelper {
            if (db == null) {
                db = SQLiteRSSHelper(c.applicationContext)
            }
            return db!!
        }

        //Definindo constantes que representam os campos do banco de dados
        val ITEM_ROWID = RssProviderContract._ID
        val ITEM_TITLE = RssProviderContract.TITLE
        val ITEM_DATE = RssProviderContract.DATE
        val ITEM_DESC = RssProviderContract.DESCRIPTION
        val ITEM_LINK = RssProviderContract.LINK
        val ITEM_UNREAD = RssProviderContract.UNREAD

        //Definindo constante que representa um array com todos os campos
        val columns = arrayOf<String>(ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD)

        //Definindo constante que representa o comando de criação da tabela no banco de dados
        private val CREATE_DB_COMMAND = "CREATE TABLE " + DATABASE_TABLE + " (" +
                ITEM_ROWID + " integer primary key autoincrement, " +
                ITEM_TITLE + " text not null, " +
                ITEM_DATE + " text not null, " +
                ITEM_DESC + " text not null, " +
                ITEM_LINK + " text not null, " +
                ITEM_UNREAD + " boolean not null);"
    }

}