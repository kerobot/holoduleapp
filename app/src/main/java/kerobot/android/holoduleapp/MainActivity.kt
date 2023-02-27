package kerobot.android.holoduleapp

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kerobot.android.holoduleapp.api.IApiService
import kerobot.android.holoduleapp.api.create
import kerobot.android.holoduleapp.model.Auth
import kerobot.android.holoduleapp.model.Holodule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var selectedDate: LocalDate
    private lateinit var searchString: String
    private lateinit var service: IApiService
    private lateinit var auth: Auth
    private lateinit var holoduleList: ArrayList<Holodule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate state:" + lifecycle.currentState)
        // 初期化
        selectedDate = LocalDate.now()
        searchString = ""
        holoduleList = ArrayList()
        // 設定読込
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val baseUrl = appInfo.metaData.getString("API_ENDPOINT")
        val userName = appInfo.metaData.getString("API_USERNAME")
        val password = appInfo.metaData.getString("API_PASSWORD")
        service = create(IApiService::class.java, baseUrl!!)
        auth = Auth(userName!!, password!!)
        // 日付ボタン
        val btDate = findViewById<Button>(R.id.btDate)
        btDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val dateListener = DateListener()
        btDate.setOnClickListener(dateListener)
        // 検索ボタン
        val btSearch = findViewById<Button>(R.id.btSearch)
        val searchListener = SearchListener()
        btSearch.setOnClickListener(searchListener)
        // リスト
        val lvList = findViewById<ListView>(R.id.lvList)
        val listItemClickListener = ListItemClickListener()
        lvList.onItemClickListener = listItemClickListener
        val listAdapter = ListAdapter(applicationContext, holoduleList)
        lvList.adapter = listAdapter
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart state:" + lifecycle.currentState)
        // ホロジュールの取得
        searchHolodule()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("MainActivity", "onSaveInstanceState state:" + lifecycle.currentState)
        // 日付
        outState.putString("date", selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
        // 検索文字列
        outState.putString("search", searchString)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("MainActivity", "onRestoreInstanceState state:" + lifecycle.currentState)
        // 日付
        val dateString = savedInstanceState.getString("date", selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
        selectedDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val btDate = findViewById<Button>(R.id.btDate)
        btDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        // 検索文字列
        searchString = savedInstanceState.getString("search", "")
        val btSearch = findViewById<Button>(R.id.btSearch)
        btSearch.text = searchString
        // ホロジュールの取得
        searchHolodule()
    }

    private inner class DateListener : View.OnClickListener {
        // 日付選択
        override fun onClick(view: View) {
            val datePickerDialog = DatePickerDialog(
                view.context,
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    val btDate = findViewById<Button>(R.id.btDate)
                    btDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    // ホロジュールの取得
                    searchHolodule()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private inner class ListItemClickListener: AdapterView.OnItemClickListener{
        // リストクリック
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val holodule = parent.getItemAtPosition(position) as Holodule
            try {
                // youtubeアプリで表示
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.youtube_vnd) + holodule.video_id)
                )
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                // ブラウザで表示
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.youtube_url) + holodule.video_id)
                )
                startActivity(intent)
            }
        }
    }

    private inner class SearchListener : View.OnClickListener {
        override fun onClick(view: View) {
            // ホロジュールの取得
            searchHolodule()
        }
    }

    private fun searchHolodule() {
        // 検索文字列
        searchString = findViewById<EditText>(R.id.etSearch).text.toString()
        // リストアダプタ
        val listAdapter = findViewById<ListView>(R.id.lvList).adapter as ListAdapter
        // リストのクリア
        holoduleList.clear()
        // コルーチンで API アクセス
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // トークンの取得
                val token = service.createToken(auth)
                // データの取得
                val jwtToken = "Bearer " + token.access_token.toString()
                val dateString = selectedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val list = service.getHolodules(jwtToken, dateString)
                // データの絞り込み
                val filteredList = list.filter {
                    x -> x.title?.contains(searchString) ?: false || x.description?.contains(searchString) ?: false
                }
                // リストの追加
                if (filteredList != null && filteredList.any()) {
                    holoduleList.addAll(filteredList)
                }
            }
            catch (e: Exception) {
                Log.d("Search", e.toString())
            }
            finally {
                listAdapter.notifyDataSetChanged()
            }
        }
    }
}
