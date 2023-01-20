package id.onklas.app.pages.prokes.pusatinformasi

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.R
import id.onklas.app.databinding.ProkesPusatInfoBinding
import id.onklas.app.di.component
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.pages.prokes.TipsItem
import id.onklas.app.pages.prokes.TipsMain
import id.onklas.app.pages.prokes.pusatinformasi.infocovid.InfoCovidPage

class ProkesPusatInfo : Fragment() {
    private lateinit var binding: ProkesPusatInfoBinding
    private val viewmodel by activityViewModels<ProkesViewmodel> { component.prokesVmFactory }
    private val itemMain by lazy {
        listOf(
            TipsMain(
                1,
                R.drawable.img_prokesinfo_tips1,
                itemsub1,
                "<strong>Note:</strong> Gunakan bahan masker 3 lapis, ganti masker secara rutin untuk efektif melindungi dari penyakit ",
                "Gunakan <strong>MASKER</strong> dengan <strong>TEPAT!</strong>"
            ),
            TipsMain(
                2,
                R.drawable.img_prokesinfo_tips2,
                itemsub2,
                "<strong>Note:</strong> Mengabaikan aturan akan beresiko tinggi dalam penularan pribadi maupun keluarga di rumah ",
                "<strong>DILARANG </strong>melakukan ini"
            ),
            TipsMain(
                3,
                R.drawable.img_prokesinfo_tips3,
                itemsub3,
                "<strong>Note:</strong> Terapkan pola hidup sehat dengan mengikuti anjuran prokes, rajin membersihkan diri dan selalu gunakan disinfektan agar terhindar dari virus corona ",
                "<strong>TIPS SEHAT</strong> jaga daya tubuh"
            )
        )
    }

    private val itemsub1 by lazy {
        listOf(
            TipsItem(
                R.drawable.ic_masker1,
                "Tutup mulut hidung dan dagu Anda, pastikan memasang masker di bagian yang sesuai"
            ),
            TipsItem(
                R.drawable.ic_masker2,
                "Pastikan hidung tertutup, jangan gunakan masker longgar dan melorot di bawah hidung"
            ),
            TipsItem(
                R.drawable.ic_masker3,
                "Jangan melepas masker saat berbicara dalam jarak <1 meter dan membiarkan masker dibawah dagu"
            ),
            TipsItem(
                R.drawable.ic_masker4,
                "Masker bukan aksesoris fashion, gunakan masker sesuai fungsi dan sebagaimana fungsinya"
            ),
            TipsItem(
                R.drawable.ic_masker5,
                "Masker bukan penutup mata, jika ingin perlindungan mata gunakan masker dengan face shield"
            ),
        )
    }
    private val itemsub2 by lazy {
        listOf(
            TipsItem(
                R.drawable.ic_dont1,
                "Batuk atau bersin di keramaian, lakukan etika dengan menutup hidung/mulut menggunakan tisu"
            ),
            TipsItem(
                R.drawable.ic_dont2,
                "Bersalaman atau berjabat tangan, sebagai pengganti, lambaikan tangan atau salam siku"
            ),
            TipsItem(
                R.drawable.ic_dont3,
                "Menghadiri kegiatan belajar di Sekolah jika Anda terindikasi mengalami gejala atau positif Covid"
            ),
        )
    }
    private val itemsub3 by lazy {
        listOf(
            TipsItem(
                R.drawable.ic_healty1,
                "Rutin memperkuat tubuh dengan berolahraga, berpikiran positif dan cukup beristirahat"
            ),
            TipsItem(
                R.drawable.ic_healty2,
                "Komsumsi buah, sayur dan makan makanan yang bergizi tinggi, serta perbanyak minum air putih"
            ),
            TipsItem(
                R.drawable.ic_healty1,
                "Jika perlu tingkatkan imunitas dan daya tahan tubuh dengan konsumsi vitamin dan suplemen"
            ),

            )
    }

    val mainData by lazy { MutableLiveData<List<TipsMain>>() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ProkesPusatInfoBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text =
            Html.fromHtml("<strong>CARA PENCEGAHAN </strong> Virus <strong>COVID-19</strong>")

        mainData.postValue(itemMain)

        mainData.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it?.toMutableList())
        })

        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = this@ProkesPusatInfo.adapter
        }

        binding.btnInfo.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    InfoCovidPage::class.java
                )
            )
        }
    }

    private val adapter by lazy {
        ProkesTipsAdapter { item, position ->
            mainData.value?.let { currentList ->
                //check apakah ada child yang aktif
                val checkList = ArrayList(currentList)
                var childActiveIndex = 0
                var parentClickedIndex = 0
                try {
                    val childActive = checkList.first { it.isShowChild }.copy()
                    childActiveIndex = checkList.indexOf(childActive)
                } catch (e: Exception) {
                }

                val parentClicked = checkList.first { it.id == item.id }.copy()
                parentClickedIndex = checkList.indexOf(parentClicked)

                val newList = ArrayList(currentList)
                try {
                    if (childActiveIndex != parentClickedIndex) { //  jika index child yang di klik tidak sama langusng reset data
                        val clearItem = newList.first { it.isShowChild }.copy()
                        val clearIndex = newList.indexOf(clearItem)
                        newList[clearIndex] =
                            clearItem.apply { isShowChild = !clearItem.isShowChild }
                    }
                    // jika index child yang aktif sama dengan index yang sedang di klik langsung close child

                } catch (e: Exception) {
                }

                val newItem = newList.first { it.id == item.id }.copy()
                val newIsShow = !newItem.isShowChild
                val index = newList.indexOf(newItem)
                try {
                    if (index > -1) {
                        newList[index] = newItem.apply { isShowChild = newIsShow }
                        mainData.postValue(newList)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }
}