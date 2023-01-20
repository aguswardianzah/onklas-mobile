package id.onklas.app.pages.prokes.prokesteacher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.onklas.app.databinding.ScreeningStudentBinding
import id.onklas.app.databinding.ScreeningStudentItemBinding
import id.onklas.app.di.component
import id.onklas.app.pages.BasePage
import id.onklas.app.pages.klaspay.riwayat.KlaspayRiwayatPage
import id.onklas.app.pages.pembayaran.SuccessPayPage
import id.onklas.app.pages.prokes.ListStudentItem
import id.onklas.app.pages.prokes.ProkesViewmodel
import id.onklas.app.utils.PagingAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreeningStudent : BasePage() {

    companion object {
        fun open(activity: Activity, className: String, clasId: Int, majorName: String) {
            activity.startActivity(
                    Intent(activity, ScreeningStudent::class.java)
                            .putExtra("class_name", className)
                            .putExtra("class_id", clasId)
                            .putExtra("major_name", majorName)
            )
        }
    }

    private val binding by lazy { ScreeningStudentBinding.inflate(layoutInflater) }
    private val viewmodel by viewModels<ProkesViewmodel> { component.prokesVmFactory }

    private var className = ""
    private var classId = 0
    private var majorName = ""

    private var firstLoad = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        classId = intent.getIntExtra("class_id", 0)
        className = intent.getStringExtra("class_name").toString()
        majorName = intent.getStringExtra("major_name").toString()

        lifecycleScope.launch {
            viewmodel.fetchStudent(classId = classId,majorName = majorName)
            initData(classId = classId)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = className
            binding.toolbar.title = className
        }
        binding.rvStudent.apply {
            adapter = StudentAdapter
        }

        binding.inSearch.doOnTextChanged { text, start, before, count ->
            Handler().postDelayed({
                initData(text.toString(), classId)
            }, 500)
        }

        viewmodel.LoadingShow.observe(this, Observer {
            Timber.e("loading show $it")
            if (it) {
                loading()
            } else {
                dismissloading()
                binding.swipeRefresh.isRefreshing=it
            }
        })

        viewmodel.errorString.observe(this, Observer {
            toast(it)
        })




        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }
            title = majorName
            binding.toolbar.title = majorName
        }

        initData(classId = classId)

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewmodel.fetchStudent(classId = classId,majorName = majorName)
                initData(classId = classId)
            }
        }

    }


    private var currLiveData: LiveData<PagedList<ListStudentItem>>? = null
    private fun initData(searchKey: String = "", classId: Int) {
        firstLoad = true
        viewmodel.lastProduct = -1
        viewmodel.hasNextProduct = true
        currLiveData?.removeObservers(this)
        currLiveData = viewmodel.ListStudentItem(
                searchKey,
                classId,
                majorName
                ).apply {
            observe(this@ScreeningStudent, listObserver)
        }

    }

    private val listObserver by lazy {
        Observer<PagedList<ListStudentItem>> {
            Timber.e("loaded data: ${it.size}")
            StudentAdapter.submitList(it) {
                if (!firstLoad) {
                    val layoutManager = (binding.rvStudent.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.rvStudent.scrollToPosition(position)
                    }
                } else {
                    binding.rvStudent.scrollToPosition(0)
                    firstLoad = false
                }

            }
        }
    }


    private val StudentAdapter by lazy {
        object : PagingAdapter<ListStudentItem, StudentVh>(object :
                DiffUtil.ItemCallback<ListStudentItem>() {
            override fun areItemsTheSame(
                    oldItem: ListStudentItem,
                    newItem: ListStudentItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: ListStudentItem,
                    newItem: ListStudentItem
            ): Boolean =
                    oldItem == newItem
        }) {
            override fun createItemViewholder(parent: ViewGroup, viewType: Int): StudentVh =
                    StudentVh(parent)

            override fun bindItemViewholder(holder: StudentVh, position: Int) {
                getItem(position)?.let { holder.bind(it) }
            }

            override fun bindItemViewholder(holder: StudentVh, position: Int, payloads: MutableList<Any>) {
                getItem(position)?.let { holder.bind(it) }
            }
        }
    }

    private inner class StudentVh(
            parent: ViewGroup,
            val binding: ScreeningStudentItemBinding = ScreeningStudentItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListStudentItem) {
            binding.item = item
            binding.root.setOnClickListener {
                if(!item.already_screening){
                    select(item)
                }else{
                    openScreening(item)
                }
            }
        }

        private fun select(item: ListStudentItem) {
            ScreeningFormPage.open(
                    this@ScreeningStudent,
                    item.name,
                    item.id,
                    item.user_avatar_image,
                    item.nis,
                    item.majorName
            )
        }
        private fun openScreening(item: ListStudentItem){
            CekScreeningPage.open(
                this@ScreeningStudent,
                item.name,
                item.id,
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 213 && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                viewmodel.fetchStudent(classId = classId,majorName = majorName)
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}