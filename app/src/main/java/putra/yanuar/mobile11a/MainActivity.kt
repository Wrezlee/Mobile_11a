package putra.yanuar.mobile11a

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import putra.yanuar.mobile11a.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var b: ActivityMainBinding
    lateinit var mediaHelper: MediaHelper
    lateinit var mhsAdapter: AdapterDataMhs
    lateinit var prodiAdapter: ArrayAdapter<String>
    var daftarMhs = mutableListOf<HashMap<String, String>>()
    var daftarProdi = mutableListOf<String>()

    val urlRoot = "http://192.168.41.91/"
    val url = "$urlRoot/kampus/show_data.php"
    val url2 = "$urlRoot/kampus/get_nama_prodi.php"
    val url3 = "$urlRoot/kampus/query_upd_del_ins.php"

    var imStr = ""
    var pilihProdi = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //menyimpan sementara data yg diperoleh dari web service
        ViewCompat.setOnApplyWindowInsetsListener(b.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        mhsAdapter = AdapterDataMhs(this, daftarMhs, this, b) //new
        mediaHelper = MediaHelper(this)

        b.listMhs.layoutManager = LinearLayoutManager(this)
        b.listMhs.adapter = mhsAdapter

        //adapter spinner prodi
        prodiAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, daftarProdi)
        b.spinProdi.adapter = prodiAdapter
        b.spinProdi.onItemSelectedListener = itemSelected

        // Registrasi klik untuk semua tombol
        b.imUpload.setOnClickListener(this)
        b.btnFind.setOnClickListener(this)
        b.btnInsert.setOnClickListener(this)
        b.btnUpdate.setOnClickListener(this)
        b.btnDelete.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        showDataMhs("")
        getNamaProdi()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imUpload -> {
                val intent = Intent()
                intent.setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(intent, mediaHelper.getRcGallery())
            }
            R.id.btnFind -> {
                showDataMhs(b.edNamaMhs.text.toString().trim())
            }
            // Masing-masing tombol mengirim parameter aksi yang berbeda
            R.id.btnInsert -> queryData("insert")
            R.id.btnUpdate -> queryData("update")
            R.id.btnDelete -> queryData("delete")
        }
    }

    val itemSelected = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            b.spinProdi.setSelection(0)
            pilihProdi = daftarProdi.get(0)
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            pilihProdi = daftarProdi.get(position)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == mediaHelper.getRcGallery()) {
                imStr = mediaHelper.getBitmapToString(data!!.data!!, b.imUpload)
            }
        }
    }

    fun getNamaProdi() {
        val request = StringRequest(Request.Method.POST, url2,
            { response ->
                daftarProdi.clear()
                val jsonArray = JSONArray(response)
                for (x in 0 until (jsonArray.length())) {
                    val jsonObject = jsonArray.getJSONObject(x)
                    daftarProdi.add(jsonObject.getString("nama_prodi"))
                    prodiAdapter.notifyDataSetChanged()
                }
            },
            { error -> }
        )
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    fun showDataMhs(namaMhs: String) {
        val request = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                daftarMhs.clear()
                val jsonArray = JSONArray(response)
                for (x in 0 until (jsonArray.length())) {
                    val jsonObject = jsonArray.getJSONObject(x)
                    val mhs = HashMap<String, String>()
                    mhs.put("nim", jsonObject.getString("nim"))
                    mhs.put("nama", jsonObject.getString("nama"))
                    mhs.put("nama_prodi", jsonObject.getString("nama_prodi"))
                    mhs.put("url", jsonObject.getString("url"))
                    daftarMhs.add(mhs)
                }
                mhsAdapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Terjadi kesalahan koneksi ke server", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val hm = HashMap<String, String>()
                hm.put("nama", namaMhs)
                return hm
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    // Fungsi Utama untuk Insert, Update, dan Delete
    fun queryData(aksi: String) {
        val request = object : StringRequest(Request.Method.POST, url3,
            Response.Listener { response ->
                // Munculkan notifikasi hasil dari PHP
                Toast.makeText(this, response, Toast.LENGTH_LONG).show()

                // Refresh list mahasiswa
                showDataMhs("")

                // Kosongkan form inputan
                b.edNim.text?.clear()
                b.edNamaMhs.text?.clear()
                b.imUpload.setImageResource(android.R.color.holo_blue_dark)
                imStr = ""
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val hm = HashMap<String, String>()
                hm.put("aksi", aksi)
                hm.put("nim", b.edNim.text.toString().trim())
                hm.put("nama", b.edNamaMhs.text.toString().trim())

                // Menyesuaikan index prodi
                val idProdi = b.spinProdi.selectedItemPosition + 1
                hm.put("id_prodi", idProdi.toString())

                // Menyisipkan gambar
                hm.put("foto", imStr)
                return hm
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }
}