package kerobot.android.holoduleapp

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kerobot.android.holoduleapp.api.IApiService
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate state:" + lifecycle.currentState)
        // 初期化
        selectedDate = LocalDate.now()
        searchString = ""
        // 設定読込
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val baseUrl = appInfo.metaData.getString("API_URL")
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
            // 日付と検索文字列を利用したホロジュールの取得
            searchString = findViewById<EditText>(R.id.etSearch).text.toString()
            searchHolodule(selectedDate, searchString)
        }
    }

    private fun searchHolodule(date: LocalDate, search: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // トークンの取得
                val token = service.createToken(auth)
                // データの取得
                val jwtToken = "JWT " + token.access_token.toString()
                val dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val result = service.getHolodules(jwtToken, dateString)
                // データの絞り込み
                val filteredList = result.holodules?.filter { x -> x.title?.contains(search) ?: false ||
                        x.description?.contains(search) ?: false
                }
                // データの表示
                if (filteredList != null && filteredList.any()) {
                    val listView = findViewById<ListView>(R.id.lvList)
                    val listAdapter = ListAdapter(applicationContext, ArrayList(filteredList))
                    listView.adapter = listAdapter
                }
            }
            catch (e: Exception) {
                Log.d("Search", e.toString())
            }
        }
    }
}
