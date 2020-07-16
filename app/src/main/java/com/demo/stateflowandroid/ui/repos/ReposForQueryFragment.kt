package com.demo.stateflowandroid.ui.repos

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.ParcelUuid
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.demo.stateflowandroid.R
import com.demo.stateflowandroid.databinding.ReposForQueryFragmentBinding
import com.demo.stateflowandroid.domain.RepoOwner
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.support.v18.scanner.*
import kotlin.collections.ArrayList


private const val GRID_COLUMN_COUNT = 2

@AndroidEntryPoint
class ReposForQueryFragment : Fragment() {

    private val nordicBluetoothScanner by lazy {
        BluetoothLeScannerCompat.getScanner()
    }

    init {
        lifecycleScope.launchWhenStarted {
            initBluetooth()
        }

    }

    private val recyclerViewAdapter = RepoAdapter { repoOwner, avatarImageView ->
        onRepoOwnerClicked(repoOwner, avatarImageView)
    }

    private val viewModel: ReposForQueryViewModel by viewModels()

    private var _binding: ReposForQueryFragmentBinding? = null
    private val binding get() = _binding!!

    private val refreshAfterErrorListener = View.OnClickListener {
        viewModel.refresh()
    }

    private fun initBluetooth() {
        val settings: ScanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(true)
            .build()
        val filters: MutableList<ScanFilter> = ArrayList()
        nordicBluetoothScanner.startScan(filters, settings, object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                println("Scan Result successful $result")
            }

            override fun onScanFailed(errorCode: Int) {
                println("Scan failed with errorcode  $errorCode")
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ReposForQueryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reposForQueryRecyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(activity, GRID_COLUMN_COUNT)
        }

        viewModel.repos.observe(viewLifecycleOwner) {
            recyclerViewAdapter.submitList(it)
        }

        viewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (!isError) return@observe

            Snackbar.make(
                binding.root,
                R.string.error_message,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.refresh_button_text, refreshAfterErrorListener)
                .show()
        }

        viewModel.showSpinner.observe(viewLifecycleOwner) { showSpinner ->
            binding.pullToRefresh.isRefreshing = showSpinner
            binding.scrimView.visibility = if (showSpinner) View.VISIBLE else View.GONE
        }

        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repos_menu, menu)

        val manager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(requireActivity().componentName))

        val querySearchListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query ?: return false

                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                viewModel.lookupRepos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }
        searchView.setOnQueryTextListener(querySearchListener)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun onRepoOwnerClicked(repoOwner: RepoOwner, avatarImageView: ImageView) {
        val action = ReposForQueryFragmentDirections
            .actionReposToUserDetails(repoOwner.login)

        val extras = FragmentNavigatorExtras(
            avatarImageView to repoOwner.login
        )
        findNavController().navigate(action, extras)
    }
}
