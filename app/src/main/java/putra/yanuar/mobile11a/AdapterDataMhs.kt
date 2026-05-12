package putra.yanuar.mobile11a

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.getstream.photoview.PhotoView
import putra.yanuar.mobile11a.databinding.ActivityMainBinding

class AdapterDataMhs(
    val context: Context,
    val dataMhs: List<HashMap<String, String>>,
    val mainActivity: MainActivity,
    val b: ActivityMainBinding
) : RecyclerView.Adapter<AdapterDataMhs.HolderDataMhs>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HolderDataMhs {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.row_data_mhs, p0, false)
        return HolderDataMhs(v)
    }

    override fun getItemCount(): Int = dataMhs.size

    override fun onBindViewHolder(p0: HolderDataMhs, p1: Int) {
        val data = dataMhs[p1]
        p0.txNim.text = data["nim"]
        p0.txNama.text = data["nama"]
        p0.txProdi.text = data["nama_prodi"]

        if (p1 % 2 == 0) p0.cLayout.setBackgroundColor(Color.rgb(230, 245, 240))
        else p0.cLayout.setBackgroundColor(Color.rgb(255, 255, 245))

        p0.cLayout.setOnClickListener {
            val pos = mainActivity.daftarProdi.indexOf(data["nama_prodi"])
            b.spinProdi.setSelection(pos)
            b.edNim.setText(data["nim"])
            b.edNamaMhs.setText(data["nama"])
            Picasso.get().load(data["url"]).into(b.imUpload)
        }

        if (data["url"] != "") {
            Picasso.get().load(data["url"]).into(p0.photo)
        }

        p0.photo.setOnClickListener {
            val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar)
            dialog.setContentView(R.layout.dialog_show_image)
            val photoView = dialog.findViewById<PhotoView>(R.id.phptoView)
            val btn = dialog.findViewById<ImageButton>(R.id.btn_close)
            Picasso.get().load(data["url"]).into(photoView)
            btn.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    class HolderDataMhs(v: View) : RecyclerView.ViewHolder(v) {
        val txNim = v.findViewById<TextView>(R.id.txNim)
        val txNama = v.findViewById<TextView>(R.id.txNama)
        val txProdi = v.findViewById<TextView>(R.id.txProdi)
        val photo = v.findViewById<ImageView>(R.id.imageView)
        val cLayout = v.findViewById<ConstraintLayout>(R.id.cLayout)
    }
}
