package app.clauncher.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.clauncher.R
import app.clauncher.data.AppModel
import app.clauncher.data.Constants
import app.clauncher.databinding.AdapterAppDrawerBinding
import app.clauncher.helper.hideKeyboard
import app.clauncher.helper.isSystemApp
import app.clauncher.helper.showKeyboard
import java.text.Normalizer
import app.clauncher.data.Prefs

class AppDrawerAdapter(
    private var flag: Int,
    private val appLabelGravity: Int,
    private val appClickListener: (AppModel) -> Unit,
    private val appInfoListener: (AppModel) -> Unit,
    private val appDeleteListener: (AppModel) -> Unit,
    private val appHideListener: (AppModel, Int) -> Unit,
    private val appRenameListener: (AppModel, String) -> Unit,
) : ListAdapter<AppModel, AppDrawerAdapter.ViewHolder>(DIFF_CALLBACK), Filterable {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppModel>() {
            override fun areItemsTheSame(oldItem: AppModel, newItem: AppModel): Boolean =
                oldItem.appPackage == newItem.appPackage && oldItem.user == newItem.user

            override fun areContentsTheSame(oldItem: AppModel, newItem: AppModel): Boolean =
                oldItem == newItem
        }
    }

    private var autoLaunch = true
    private var isBangSearch = false
    private val appFilter = createAppFilter()

    var appsList: MutableList<AppModel> = mutableListOf()
    var appFilteredList: MutableList<AppModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(AdapterAppDrawerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (appFilteredList.isEmpty() || position == RecyclerView.NO_POSITION) return
            val appModel = appFilteredList[holder.bindingAdapterPosition]


            holder.bind(
                flag,
                appLabelGravity,
                appModel,
                appClickListener,
                appDeleteListener,
                appInfoListener,
                appHideListener,
                appRenameListener
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getFilter(): Filter = this.appFilter

    private fun createAppFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSearch: CharSequence?): FilterResults {
                isBangSearch = charSearch?.startsWith("!") ?: false
                autoLaunch = charSearch?.startsWith(" ")?.not() ?: true

                val appFilteredList = (if (charSearch.isNullOrBlank()) appsList
                else appsList.filter { app ->
                    appLabelMatches(app.appLabel, charSearch)
//                }.sortedByDescending {
//                    charSearch.contentEquals(it.appLabel, true)
                } as MutableList<AppModel>)

                val filterResults = FilterResults()
                filterResults.values = appFilteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as MutableList<AppModel>
                    appFilteredList = items
                    submitList(appFilteredList) {
                        autoLaunch()
                    }
                }
            }
        }
    }

    private fun autoLaunch() {
        try {
            if (itemCount == 1
                && autoLaunch
                && isBangSearch.not()
                && flag == Constants.FLAG_LAUNCH_APP
                && appFilteredList.isNotEmpty()
            ) appClickListener(appFilteredList[0])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appLabelMatches(appLabel: String, charSearch: CharSequence): Boolean {
        return (appLabel.contains(charSearch.trim(), true) or
                Normalizer.normalize(appLabel, Normalizer.Form.NFD)
                    .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                    .replace(Regex("[-_+,. ]"), "")
                    .contains(charSearch, true))
    }

    fun setAppList(appsList: MutableList<AppModel>) {
        // Add empty app for bottom padding in recyclerview
        appsList.add(AppModel("", null, "", "", false, android.os.Process.myUserHandle()))
        this.appsList = appsList
        this.appFilteredList = appsList
        submitList(appsList)
    }

    fun launchFirstInList() {
        if (appFilteredList.isNotEmpty())
            appClickListener(appFilteredList[0])
    }

    class ViewHolder(private val binding: AdapterAppDrawerBinding) : RecyclerView.ViewHolder(binding.root) {

        val prefs = Prefs(itemView.context)

        fun bind(
            flag: Int,
            appLabelGravity: Int,
            appModel: AppModel,
            clickListener: (AppModel) -> Unit,
            appDeleteListener: (AppModel) -> Unit,
            appInfoListener: (AppModel) -> Unit,
            appHideListener: (AppModel, Int) -> Unit,
            appRenameListener: (AppModel, String) -> Unit,
        ) =
            with(binding) {
                appHideLayout.visibility = View.GONE
                renameLayout.visibility = View.GONE

                // Determine if app name should be visible

                appTitle.visibility = View.INVISIBLE
                if (prefs.toggleAppVisibility) {
                    appTitle.visibility = View.VISIBLE
                    appTitle.text = buildString {
                        append(appModel.appLabel)
                        append(if (appModel.isNew == true) " (New)" else "")
                    }
                    appTitle.gravity = appLabelGravity
                    //otherProfileIndicator.isVisible = appModel.user != myUserHandle

                    appTitle.setOnClickListener { clickListener(appModel) }
                    appTitle.setOnLongClickListener {
                        if (appModel.appPackage.isNotEmpty()) {
                            appDelete.alpha =
                                if (root.context.isSystemApp(appModel.appPackage)) 0.5f else 1.0f
                            appHide.text = if (flag == Constants.FLAG_HIDDEN_APPS)
                                root.context.getString(R.string.adapter_show)
                            else
                                root.context.getString(R.string.adapter_hide)
                            appTitle.visibility = View.INVISIBLE
                            appHideLayout.visibility = View.VISIBLE
                            appRename.isVisible = flag != Constants.FLAG_HIDDEN_APPS
                        }
                        true
                    }
                    appRename.setOnClickListener {
                        if (appModel.appPackage.isNotEmpty()) {
                            etAppRename.hint = getAppName(etAppRename.context, appModel.appPackage)
                            etAppRename.setText(appModel.appLabel)
                            etAppRename.setSelectAllOnFocus(true)
                            renameLayout.visibility = View.VISIBLE
                            appHideLayout.visibility = View.GONE
                            etAppRename.showKeyboard()
                            etAppRename.imeOptions = EditorInfo.IME_ACTION_DONE
                        }
                    }
                    etAppRename.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus)
                            appTitle.visibility = View.INVISIBLE
                        else
                            appTitle.visibility = View.VISIBLE
                    }
                    etAppRename.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            etAppRename.hint = getAppName(etAppRename.context, appModel.appPackage)
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {
                            etAppRename.hint = ""
                        }
                    })
                    etAppRename.setOnEditorActionListener { _, actionCode, _ ->
                        if (actionCode == EditorInfo.IME_ACTION_DONE) {
                            val renameLabel = etAppRename.text.toString().trim()
                            if (renameLabel.isNotBlank() && appModel.appPackage.isNotBlank()) {
                                appRenameListener(appModel, renameLabel)
                                renameLayout.visibility = View.GONE
                            }
                        }
                        false
                    }
                    tvSaveRename.setOnClickListener {
                        etAppRename.hideKeyboard()
                        val renameLabel = etAppRename.text.toString().trim()
                        if (renameLabel.isNotBlank() && appModel.appPackage.isNotBlank()) {
                            appRenameListener(appModel, renameLabel)
                            renameLayout.visibility = View.GONE
                        } else {
                            val appName = getAppName(etAppRename.context, appModel.appPackage)
                            appRenameListener(appModel, appName)
                            renameLayout.visibility = View.GONE
                        }
                    }
                    appInfo.setOnClickListener { appInfoListener(appModel) }
                    appDelete.setOnClickListener { appDeleteListener(appModel) }
                    appMenuClose.setOnClickListener {
                        appHideLayout.visibility = View.GONE
                        appTitle.visibility = View.VISIBLE
                    }
                    appRenameClose.setOnClickListener {
                        renameLayout.visibility = View.GONE
                        appTitle.visibility = View.VISIBLE
                    }
                    appHide.setOnClickListener { appHideListener(appModel, bindingAdapterPosition) }
                }
            }

        private fun getAppName(context: Context, appPackage: String): String {
            val packageManager = context.packageManager
            return packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(appPackage, 0)
            ).toString()
        }
    }
}
