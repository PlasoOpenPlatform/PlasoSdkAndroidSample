package cn.plaso.liveclasssdkdemo.resourcecenter

import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cn.plaso.liveclasssdkdemo.R

data class FileItem(
    val title: String,
    val url: String,
    val totalPages: Int,
    val id: String,
    val preparsed: Boolean,
    val info: Any? = null // optional structured info when url is blank
)

private val sItemCallback = object : DiffUtil.ItemCallback<FileItem>() {
    override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return TextUtils.equals(oldItem.url, newItem.url)
    }

    override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return TextUtils.equals(oldItem.title, newItem.title) && TextUtils.equals(oldItem.url, newItem.url)
    }
}

class FileListAdapter : RecyclerView.Adapter<FileListAdapter.Holder>() {

    var differ = AsyncListDiffer(this, sItemCallback)

    var onItemClick: ((FileItem) -> Unit)? = null

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: FileItem) {
            tvFileName.text = data.title
        }

        val tvFileName = itemView.findViewById<TextView>(R.id.tvFileName)

        init {
            tvFileName.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getItem(pos)?.let { item -> onItemClick?.invoke(item) }
                }
            }
        }
    }

    private fun getFileName(url: String): CharSequence? {
        val uri = Uri.parse(url)
        return uri.lastPathSegment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_list_item_file, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val data = getItem(position)
        data?.let {
            holder.bind(data)
        }
    }

    private fun getItem(position: Int): FileItem? {
        return differ.currentList[position]
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submit(it: List<FileItem>?) {
        differ.submitList(it)
    }

}