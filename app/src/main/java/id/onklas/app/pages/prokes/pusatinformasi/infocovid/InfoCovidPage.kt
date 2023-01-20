package id.onklas.app.pages.prokes.pusatinformasi.infocovid

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import id.onklas.app.R
import id.onklas.app.databinding.InfoCovidPageBinding
import id.onklas.app.di.component
import id.onklas.app.pages.prokes.ListInfoCovid
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.pages.Privatepage
import java.lang.Exception

class InfoCovidPage : Privatepage() {
    private val binding by lazy { InfoCovidPageBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }
    private val dataList by lazy {
        listOf(
                ListInfoCovid(1, 0, R.drawable.banner_cvd1, "Informasi Umum", 1, false),
                ListInfoCovid(2, 0, 0, "Apa itu COVID -19", 2, false, listOf(ListInfoCovid(20, 2, 0, "COVID-19 atau biasa disebut Virus Corona, merupakan keluarga besar virus yang menyebabkan penyakit pada manusia dan hewan. Pada manusia biasanya menyebabkan penyakit infeksi saluran pernapasan, mulai flu biasa hingga penyakit yang serius seperti Middle East Respiratory Syndrome (MERS) dan Sindrom Pernafasan Akut Berat/ Severe Acute Respiratory Syndrome (SARS). Coronavirus jenis baru yang ditemukan pada manusia sejak kejadian luar biasa muncul di Wuhan Cina, pada Desember 2019, kemudian diberi nama Severe Acute Respiratory Syndrome Coronavirus 2 (SARS-COV2), dan menyebabkan penyakit Coronavirus Disease-2019 (COVID-19).", 3, false))),
                ListInfoCovid(3, 0, 0, "Apa gejala COVID-19", 2, false, listOf(ListInfoCovid(21, 3, 0, "COVID-19 dapat mempengaruhi orang dengan cara berbeda-beda, namum kebanyakan orang terinfeksi mengalami gejala umum berupa demam ≥38°C, batuk kering, kelelahan dan sesak napas, sedangkan gejala yang kurang umumnya adalah rasa sakit dan nyeri, sakit tenggorokan, diare, konjungtivitis, sakit kepala, kehilangan indera perasa dan bau serta ruam pada kulit. Jika ada orang yang dalam 14 hari sebelum muncul gejala tersebut pernah melakukan perjalanan ke negara terjangkit, atau pernah merawat/kontak erat dengan penderita COVID-19, maka terhadap orang tersebut akan dilakukan pemeriksaan laboratorium, seperti tes rapid dan sejenisnya. Rata-rata dibutuhkan waktu 5-6 hari seseorang terinfeksi virus untuk menunjukan gejala, segera cek tes di rumah sakit terdekat / pemeriksaan lab lebih lanjut untuk memastikan diagnosisnya.", 3, false))),
                ListInfoCovid(4, 0, 0, "Bagaimana mencegah COVID-19", 2, false, listOf(ListInfoCovid(22, 4, 0, "COVID-19 bisa dicegah dengan 5M: Menggunakan masker, Mencuci tangan secara rutin, menjaga jarak aman, menjauhi kerumunan, dan mengurangi mobilitas.\n" +
                        "\n" +
                        "Selain 5M, ada beberapa hal lain yang bisa dilakukan untuk meningkatkan daya tahan tubuh, salah satunya dengan berolahraga, rajin membersihkan diri, hindari berjabat tangan atau menjaga jarak dengan pasien yang positif COVID-19, dan dengan mengkonsumsi makanan yang bergizi serta vitamin C dan Vitamin D, usahakan untuk mengkonsumsi dua vitamin tersebut secara rutin setiap harinya.", 3, false))),
                ListInfoCovid(5, 0, R.drawable.banner_cvd2, "Diagnosis", 1, false),
                ListInfoCovid(6, 0, 0, "Bagaimana mendiagnosis COVID-19", 2, false, listOf(ListInfoCovid(23, 6, 0, "Diagnosis COVID-19 dilakukan berdasarkan tanda dan gejala, serta apakah kamu telah melakukan kontak dekat dengan seseorang yang didiagnosis dengan COVID-19. Untuk menguji virus COVID-19, penyedia layanan kesehatan akan mengambil sampel dari hidung, dari nafas atau tenggorokan, sampel tersebut akan dikirimkan ke lab untuk diuji. Sejauh ini tiga jenis tes tersedia untuk pengujian diagnosa dan direkomendasikan untuk dilakukan yaitu tes virus, pemeriksaan radiologi dan tes antigen", 3, false))),
                ListInfoCovid(7, 0, 0, "Berapa lama masa inkubasi virus COVID-19", 2, false, listOf(ListInfoCovid(24, 7, 0, "Waktu yang diperlukan sejak tertular/terinfeksi hingga muncul gejala disebut masa inkubasi. Saat ini masa inkubasi COVID-19 diperkirakan antara 1-14 hari, dan perkiraan ini dapat berubah sewaktu-waktu sesuai perkembangan kasus.", 3, false))),
                ListInfoCovid(8, 0, R.drawable.banner_cvd3, "Tentang Covid-19", 1, false, ),
                ListInfoCovid(9, 0, 0, "Siapa yang beresiko terinfeksi COVID-19", 2, false, listOf(ListInfoCovid(25, 9, 0, "Orang yang tinggal atau bepergian di daerah di mana virus COVID-19 bersirkulasi sangat mungkin berisiko terinfeksi. Mereka yang terinfeksi adalah orang-orang yang dalam 14 hari sebelum muncul gejala melakukan perjalanan dari negara terjangkit, atau yang kontak erat, seperti anggota keluarga, rekan kerja atau tenaga medis yang merawat pasien sebelum mereka tahu pasien tersebut terinfeksi COVID-19. Petugas kesehatan yang merawat pasien yang terinfeksi COVID-19 berisiko lebih tinggi dan harus konsisten melindungi diri mereka sendiri dengan prosedur pencegahan dan pengendalian infeksi yang tepat.\n" +
                        "\n" +
                        "Penelitian juga menunjukan orang yang lanjut usia atau yang juga memiliki riwayat penyakit bawaan dan imun yang lemah rentan mengalami resiko tertular COVID-19", 3, false))),
                ListInfoCovid(10, 0, 0, "Bagaimana infeksi COVID-19 menular", 2, false, listOf(ListInfoCovid(26, 10, 0, "Seseorang dapat terinfeksi dari penderita COVID-19. Penyakit ini dapat menyebar melalui tetesan kecil (droplet) dari hidung atau mulut pada saat batuk atau bersin. Droplet tersebut kemudian jatuh pada benda di sekitarnya. Kemudian jika ada orang lain menyentuh benda yang sudah terkontaminasi dengan droplet tersebut, lalu orang itu menyentuh mata, hidung atau mulut (segitiga wajah), maka orang itu dapat terinfeksi COVID19. Atau bisa juga seseorang terinfeksi COVID-19 ketika tanpa sengaja menghirup droplet dari penderita. Inilah sebabnya mengapa kita penting untuk menjaga jarak hingga kurang lebih satu meter dari orang yang sakit. Sampai saat ini, para ahli masih terus melakukan penyelidikan untuk menentukan sumber virus, jenis paparan, dan cara penularannya. Tetap pantau sumber informasi yang akurat dan resmi mengenai perkembangan penyakit ini.", 3, false))),
                ListInfoCovid(11, 0, 0, "Seberapa bahaya COVID-19", 2, false, listOf(ListInfoCovid(27, 11, 0, "Seperti penyakit pernapasan lainnya, COVID-19 dapat menyebabkan gejala ringan termasuk pilek, sakit tenggorokan, batuk, dan demam. Sekitar 80% kasus dapat pulih tanpa perlu perawatan khusus. Sekitar 1 dari setiap 6 orang mungkin akan menderita sakit yang parah, seperti disertai pneumonia atau kesulitan bernafas, yang biasanya muncul secara bertahap. Walaupun angka kematian penyakit ini masih rendah (sekitar 3%), namun bagi orang yang berusia lanjut, dan orang-orang dengan kondisi medis yang sudah ada sebelumnya (seperti diabetes, tekanan darah tinggi dan penyakit jantung), mereka biasanya lebih rentan untuk menjadi sakit parah. Melihat perkembangan hingga saat ini, lebih dari 50% kasus konfirmasi telah dinyatakan membaik, dan angka kesembuhan akan terus meningkat.", 3, false))),
                ListInfoCovid(12, 0, 0, "Berapa lama virus bertahan di permukaan", 2, false, listOf(ListInfoCovid(28, 12, 0, "Sampai saat ini belum diketahui dengan pasti berapa lama COVID-19 mampu bertahan di permukaan suatu benda, meskipun studi awal menunjukkan bahwa COVID-19 dapat bertahan hingga beberapa jam, tergantung jenis permukaan, suhu, atau kelembaban lingkungan. Namun disinfektan sederhana dapat membunuh virus tersebut sehingga tidak mungkin menginfeksi orang lagi. Dan membiasakan cuci tangan dengan air dan sabun, atau hand-rub berbasis alkohol, serta hindari menyentuh mata, mulut atau hidung (segitiga wajah) lebih efektif melindungi diri Anda.", 3, false))),
                ListInfoCovid(13, 0, R.drawable.banner_cvd4, "Prognosis", 1, false, ),
                ListInfoCovid(14, 0, 0, "Komplikasi yang disebabkan COVID-19", 2, false, listOf(ListInfoCovid(29, 14, 0, "Sekitar 1 dari 6 orang yang terinfeksi COVID-19, telah mengalami kondisi yang dikenal sebagai sindrom pelepasan sitokin. Komplikasi terjadi saat infeksi COVID-19 memicu sistem kekebalan tubuh memenuhi aliran darah dengan protein inflamasi yang disebut sitokon. Peristiwa ini bisa membunuh jaringan dan merusak organ tubuh, termasuk paru-paru, jantung dan ginjal. Beberapa komplikasi COVID-19 diantaranya adalah:\n" +
                        "-Pneumonia dan kesulitan bernafas\n" +
                        "-Kegagalan fungsi organ\n" +
                        "-Masalah jantung\n" +
                        "-Sindrom gangguan pernafasan akut\n" +
                        "-Darah menggumpal\n" +
                        "-Cedera ginjal akut\n" +
                        "-Infeksi virus dan bakteri lainnya\n", 3, false))),
                ListInfoCovid(15, 0, 0, "Apakah penderita COVID-19 bisa sembuh", 2, false, listOf(ListInfoCovid(30, 15, 0, "Sejauh ini, tidak ada obat yang terbukti khusus mengobati COVID-19. Namun, pengidap COVID-19 bisa sembuh jika ditangani sesuai dengan gejala yang mendasarinya. Penderita dan pasien bisa ditangani oleh ahli dan tenaga medis ataupun perawatan atau isolasi mandiri adalah jenis penanganan yang direkomendasikan untuk orang yang terpapar COVID-19.", 3, false)))

        )
    }

    val mainData by lazy { MutableLiveData<List<ListInfoCovid>>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val dummyFirstTime = ArrayList(dataList)
        mainData.postValue(dummyFirstTime)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = "Protokol Kesehatan"
            binding.toolbar.title = "Protokol Kesehatan"
        }

        binding.rvInfo.apply {
            adapter = this@InfoCovidPage.adapter

        }

        mainData.observe(this, Observer {
            adapter.submitList(it?.toMutableList())
        })

    }

    private val adapter by lazy {
        InfoCovidAdapter { item, position ->
            mainData.value?.let { currentList ->
                val checkList = ArrayList(currentList)
                var childActiveIndex = 0
                var parentClickedIndex = 0
                try {
                    val childActive = checkList.first { it.isShowChild }.copy()
                    childActiveIndex = checkList.indexOf(childActive)
                } catch (e: Exception) { }

                val parentClicked = checkList.first { it.id == item.id }.copy()
                parentClickedIndex = checkList.indexOf(parentClicked)

                val newList = ArrayList(currentList)
                try {
                    if(childActiveIndex !=parentClickedIndex){
                        val childActive = checkList.first { it.isShowChild }.copy()
                        childActiveIndex = checkList.indexOf(childActive)
                        newList[childActiveIndex] = childActive.apply { isShowChild = !childActive.isShowChild }
                        newList.removeAll{it.parentId!=0}
                    }
                }catch (e:Exception){}

                val newItem = newList.first { it.id == item.id }.copy()
                val newIsShow = !newItem.isShowChild
                val index = newList.indexOf(newItem)
                try {
                    newList[index] = newItem.apply { isShowChild = newIsShow }
                    if (newIsShow)
                        newList.addAll(index + 1, item.items)
                    else
                        newList.removeAll { it.parentId == item.id }
                    mainData.postValue(newList)
                } catch (e: Exception) {}


            }
        }
    }
}