package kerobot.android.holoduleapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kerobot.android.holoduleapp.model.Holodule
import java.util.*
import kotlin.collections.ArrayList

class ListAdapter (private val context: Context, private val holodules: ArrayList<Holodule>) : BaseAdapter() {

    private val faceMap = mapOf(
        "ときのそら" to R.drawable.tokino_sora,
        "ロボ子さん" to R.drawable.robokosan,
        "さくらみこ" to R.drawable.sakura_miko,
        "星街すいせい" to R.drawable.hoshimachi_suisei,
        "夜空メル" to R.drawable.yozora_mel,
        "アキ・ローゼンタール" to R.drawable.aki_rosenthal,
        "赤井はあと" to R.drawable.haachama,
        "白上フブキ" to R.drawable.shirakami_fubuki,
        "夏色まつり" to R.drawable.natsuiro_matsuri,
        "湊あくあ" to R.drawable.minato_aqua,
        "紫咲シオン" to R.drawable.murasaki_shion,
        "百鬼あやめ" to R.drawable.nakiri_ayame,
        "癒月ちょこ" to R.drawable.yuzuki_choco,
        "大空スバル" to R.drawable.oozora_subaru,
        "大神ミオ" to R.drawable.ookami_mio,
        "猫又おかゆ" to R.drawable.nekomata_okayu,
        "戌神ころね" to R.drawable.inugami_korone,
        "兎田ぺこら" to R.drawable.usada_pekora,
        "潤羽るしあ" to R.drawable.uruha_rushia,
        "不知火フレア" to R.drawable.shiranui_flare,
        "白銀ノエル" to R.drawable.shirogane_noel,
        "宝鐘マリン" to R.drawable.housyou_marine,
        "天音かなた" to R.drawable.amane_kanata,
        "桐生ココ" to R.drawable.kiryu_coco,
        "角巻わため" to R.drawable.tsunomaki_watame,
        "常闇トワ" to R.drawable.tokoyami_towa,
        "姫森ルーナ" to R.drawable.himemori_luna,
        "獅白ぼたん" to R.drawable.shishiro_botan,
        "雪花ラミィ" to R.drawable.yukihana_lamy,
        "尾丸ポルカ" to R.drawable.omaru_polka,
        "桃鈴ねね" to R.drawable.momosuzu_nene,
        "魔乃アロエ" to R.drawable.mano_aloe,
    )

    @SuppressLint("ViewHolder", "InflateParams", "SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.holodule_row, null)
        val time = view.findViewById<TextView>(R.id.tvTime)
        val name = view.findViewById<TextView>(R.id.tvName)
        val title = view.findViewById<TextView>(R.id.tvTitle)
        val face = view.findViewById<ImageView>(R.id.ivFace)

        val holodule = holodules[position]
        val dateTime = SimpleDateFormat("yyyyMMdd HHmmss").parse(holodule.datetime)
        time.text = SimpleDateFormat("HH:mm").format(dateTime)
        if(dateTime > Date()) {
            time.setTextColor(Color.BLUE)
            time.typeface = Typeface.DEFAULT_BOLD
        }
        name.text = holodule.name
        title.text = holodule.title
        val faceId = faceMap.getOrElse(holodule.name!!, {0})
        if(faceId != 0 ) {
            face.setImageResource(faceId)
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return holodules[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return holodules.size
    }
}
