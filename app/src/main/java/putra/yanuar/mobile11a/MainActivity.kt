package putra.yanuar.mobile11a

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
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
import com.github.dhaval2404.imagepicker.ImagePicker
import com.permissionx.guolindev.PermissionX

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import putra.yanuar.mobile11a.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var b: ActivityMainBinding
    lateinit var mediaHelper: MediaHelper
    lateinit var mhsAdapter: AdapterDataMhs
    lateinit var prodiAdapter: ArrayAdapter<String>

    var daftarMhs = mutableListOf<HashMap<String, String>>()
    var daftarProdi = mutableListOf<String>()

    val urlRoot = "http://10.218.144.139/"
    val url  = "${urlRoot}kampus/show_data.php"
    val url2 = "${urlRoot}kampus/get_nama_prodi.php"
    val url3 = "${urlRoot}kampus/query_upd_del_ins.php"

    var imStr = ""
    var pilihProdi = ""

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imUpload -> {
                // Modifikasi: Menambahkan PopupMenu untuk memilih Galeri atau Kamera
                val popUp = PopupMenu(this, v)
                popUp.menu.add(0, 0, 0, "Galeri")
                popUp.menu.add(0, 1, 1, "Kamera")
                popUp.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        0 -> { // Galeri
                            val intent = Intent()
                            intent.type = "image/*"
                            intent.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(intent, mediaHelper.getRcGallery())
                            true
                        }
                        1 -> { // Kamera dengan PermissionX
                            PermissionX.init(this)
                                .permissions(Manifest.permission.CAMERA)
                                .request { allGranted, _, _ ->
                                    if (allGranted) {
                                        val mediaPath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "App_Bab_12")
                                        if (!mediaPath.exists()) mediaPath.mkdirs()

                                        ImagePicker.with(this)
                                            .cameraOnly()
                                            .saveDir(mediaPath)
                                            .start()
                                    } else {
                                        Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            true
                        }
                        else -> false
                    }
                }
                popUp.show()
            }
            R.id.btnFind -> showDataMhs(b.edNamaMhs.text.toString().trim())
            R.id.btnInsert -> validateAndQuery("insert")
            R.id.btnUpdate -> validateAndQuery("update")
            R.id.btnDelete -> validateAndQuery("delete")
        }
    }

    private fun validateAndQuery(mode: String) {
        val nim = b.edNim.text.toString().trim()
        val nama = b.edNamaMhs.text.toString().trim()

        if (mode != "delete" && (nim.isEmpty() || nama.isEmpty())) {
            Toast.makeText(this, "NIM dan Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (mode == "delete" && nim.isEmpty()) {
            Toast.makeText(this, "NIM tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        queryData(mode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        mediaHelper = MediaHelper(this)
        mhsAdapter = AdapterDataMhs(this, daftarMhs, this, b)
        prodiAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, daftarProdi)

        b.listMhs.layoutManager = LinearLayoutManager(this)
        b.listMhs.adapter = mhsAdapter
        b.spinProdi.adapter = prodiAdapter

        // Setup listener
        b.spinProdi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                pilihProdi = daftarProdi[pos]
            }
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                imStr = mediaHelper.getBitmapToString(uri, b.imUpload)
            }
        }
    }

    fun queryData(mode: String) {
        val request = object : StringRequest(Method.POST, url3,
            Response.Listener { response ->
                // Penanganan respon sesuai format JSON dari server
                try {
                    val jsonRes = JSONObject(response)
                    if (jsonRes.getString("kode") == "000") {
                        Toast.makeText(this, "Operasi Berhasil", Toast.LENGTH_SHORT).show()
                        showDataMhs("")
                        clearForm()
                    } else {
                        Toast.makeText(this, "Gagal: ${jsonRes.getString("kode")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val hm = HashMap<String, String>()
                // Nama file otomatis berdasarkan waktu
                val nmFile = "IMG_" + SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date()) + ".jpg"

                hm["mode"] = mode // Menggunakan 'mode' sesuai referensi Bab 12
                hm["nim"] = b.edNim.text.toString().trim()
                hm["nama"] = b.edNamaMhs.text.toString().trim()
                hm["nama_prodi"] = pilihProdi
                hm["image"] = imStr
                hm["file"] = nmFile
                return hm
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun clearForm() {
        b.edNim.setText("")
        b.edNamaMhs.setText("")
        b.imUpload.setImageResource(android.R.color.holo_blue_dark)
        imStr = ""
    }

    fun getNamaProdi() {
        val request = StringRequest(Request.Method.POST, url2,
            { response ->
                try {
                    daftarProdi.clear()
                    val jsonArray = JSONArray(response)
                    for (x in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(x)
                        daftarProdi.add(jsonObject.getString("nama_prodi"))
                    }
                    prodiAdapter.notifyDataSetChanged()
                } catch (e: JSONException) { e.printStackTrace() }
            },
            { error -> /* Handle Error */ }
        )
        Volley.newRequestQueue(this).add(request)
    }

    fun showDataMhs(namaMhs: String) {
        val request = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    daftarMhs.clear()
                    val jsonArray = JSONArray(response)
                    for (x in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(x)
                        val mhs = HashMap<String, String>()
                        mhs["nim"] = jsonObject.getString("nim")
                        mhs["nama"] = jsonObject.getString("nama")
                        mhs["nama_prodi"] = jsonObject.getString("nama_prodi")
                        mhs["url"] = jsonObject.getString("url")
                        daftarMhs.add(mhs)
                    }
                    mhsAdapter.notifyDataSetChanged()
                } catch (e: JSONException) { e.printStackTrace() }
            },
            Response.ErrorListener { /* Handle Error */ }) {
            override fun getParams(): MutableMap<String, String> {
                val hm = HashMap<String, String>()
                hm["nama"] = namaMhs
                return hm
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}