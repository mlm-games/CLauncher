package app.clauncher.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.clauncher.R
import app.clauncher.databinding.FragmentWidgetPickerBinding
import app.clauncher.helper.applySystemBarInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WidgetPickerFragment : Fragment() {

    private var _binding: FragmentWidgetPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWidgetPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applySystemBarInsets(applyTop = false, applyBottom = false)
        binding.widgetList.layoutManager = LinearLayoutManager(requireContext())
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        loadWidgets()
    }

    private fun loadWidgets() {
        CoroutineScope(Dispatchers.IO).launch {
            val manager = AppWidgetManager.getInstance(requireContext())
            val pm = requireContext().packageManager
            val grouped = try {
                manager.installedProviders
                    .groupBy { it.provider.packageName }
                    .mapNotNull { (pkg, infos) ->
                        try {
                            val appInfo = pm.getApplicationInfo(pkg, 0)
                            val appName = pm.getApplicationLabel(appInfo).toString()
                            appName to infos.sortedBy { it.loadLabel(pm) }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .sortedBy { it.first }
            } catch (_: Exception) {
                emptyList()
            }
            val flat = mutableListOf<Row>()
            grouped.forEach { (appName, infos) ->
                flat.add(Row.Header(appName))
                infos.forEach { flat.add(Row.Item(it)) }
            }
            withContext(Dispatchers.Main) {
                if (flat.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.emptyView.text = getString(R.string.no_widgets_found)
                } else {
                    binding.emptyView.visibility = View.GONE
                }
                binding.widgetList.adapter = Adapter(flat) { info ->
                    val flattened = info.provider.flattenToString()
                    try {
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle?.set("widgetProvider", flattened)
                    } catch (_: Exception) {
                    }
                    parentFragmentManager.setFragmentResult(
                        "widgetPicked",
                        Bundle().apply {
                            putString("provider", flattened)
                        }
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    sealed class Row {
        data class Header(val appName: String) : Row()
        data class Item(val info: AppWidgetProviderInfo) : Row()
    }

    private class Adapter(
        private val rows: List<Row>,
        private val onPick: (AppWidgetProviderInfo) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int = when (rows[position]) {
            is Row.Header -> 0
            is Row.Item -> 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inf = LayoutInflater.from(parent.context)
            return if (viewType == 0) {
                val v = inf.inflate(android.R.layout.simple_list_item_1, parent, false)
                HeaderVH(v)
            } else {
                val v = inf.inflate(android.R.layout.simple_list_item_2, parent, false)
                ItemVH(v, onPick)
            }
        }

        override fun getItemCount(): Int = rows.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val r = rows[position]) {
                is Row.Header -> (holder as HeaderVH).bind(r.appName)
                is Row.Item -> (holder as ItemVH).bind(r.info)
            }
        }

        private class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
            private val t: TextView = v.findViewById(android.R.id.text1)
            fun bind(name: String) {
                t.text = name
            }
        }

        private class ItemVH(
            v: View,
            private val onPick: (AppWidgetProviderInfo) -> Unit
        ) : RecyclerView.ViewHolder(v) {
            private val t1: TextView = v.findViewById(android.R.id.text1)
            private val t2: TextView = v.findViewById(android.R.id.text2)
            private var info: AppWidgetProviderInfo? = null

            init {
                v.setOnClickListener { info?.let(onPick) }
            }

            fun bind(i: AppWidgetProviderInfo) {
                info = i
                t1.text = try {
                    i.loadLabel(itemView.context.packageManager)
                } catch (_: Exception) {
                    i.provider.className
                }
                t2.text = "${i.minWidth}x${i.minHeight}dp"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
