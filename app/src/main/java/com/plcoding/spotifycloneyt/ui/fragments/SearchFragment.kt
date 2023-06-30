package com.plcoding.spotifycloneyt.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.QuranAdapter
import com.plcoding.spotifycloneyt.other.Resource
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.ui.interfaces.SendDataFromFragmentToActivity
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import taha.younis.el7loapp.util.showBottomNavigationView
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.fragment_search) {

    private val viewModel by viewModels<SearchViewModel>()

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var quranAdapter: QuranAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupAllProductRv()

        showKeyboardAutomatically()

        onHomeClick()

        searchProduct()

            viewModel.search.observe(viewLifecycleOwner) {
                when(it.status){
                    Status.LOADING ->{
                        showLoading()
                    }
                    Status.SUCCESS ->{
                        quranAdapter.quran = it.data!!
                        hideLoading()
                        showCancelTv()
                    }
                    Status.ERROR ->{
                        Toast.makeText(requireActivity(), it.message.toString(), Toast.LENGTH_SHORT).show()
                        hideLoading()
                        showCancelTv()
                    }
            }
        }

        onCancelTvClick()

        quranAdapter.setItemClickListener {
            val sendDataToActivity: SendDataFromFragmentToActivity =
                activity as SendDataFromFragmentToActivity
            sendDataToActivity.transferredDataFromFragment(listOf(it), it)
            mainViewModel.playOrToggleQuran(it) // we will not keep toggle false because we want just play the song and not toggle`
        }
    }

    private fun hideLoading() {
        progressbar_categories.visibility = View.GONE
    }

    private fun showLoading() {
        progressbar_categories.visibility = View.VISIBLE
    }

    private fun setupAllProductRv() {
        rv_search.apply {
            adapter = quranAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun onCancelTvClick() {
        tv_cancel.setOnClickListener {
            quranAdapter.quran = emptyList()
            ed_search.setText("")
            hideCancelTv()
        }
    }

    private fun showCancelTv() {
        tv_cancel.visibility = View.VISIBLE
    }

    var job: Job? = null
    private fun searchProduct(){
        ed_search.addTextChangedListener { query ->
            val queryTrim = query.toString().trim()
            if (queryTrim.isNotEmpty()){
                val searchQuery = query.toString().substring(0,1).uppercase()
                    .plus(query.toString().substring(1))
                job?.cancel()
                job = CoroutineScope(Dispatchers.IO).launch {
                    delay(100L)
                    viewModel.searchProduct(searchQuery)
                }
            }else{
                quranAdapter.quran = emptyList()
                hideCancelTv()
            }
        }
    }

    private fun hideCancelTv() {
        tv_cancel.visibility = View.GONE
    }

    private fun onHomeClick() {
        val btm = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        btm?.menu?.getItem(0)?.setOnMenuItemClickListener {
            activity?.onBackPressed()
            true
        }
    }

    private fun showKeyboardAutomatically() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ed_search.requestFocus()
        imm.showSoftInput(ed_search,0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ed_search.clearFocus()
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}