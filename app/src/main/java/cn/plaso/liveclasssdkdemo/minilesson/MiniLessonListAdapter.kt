package cn.plaso.liveclasssdkdemo.minilesson

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import com.bumptech.glide.Glide
import org.w3c.dom.Text

class MiniLessonListAdapter : RecyclerView.Adapter<MiniLessonListAdapter.VH>() {

    var lessonList: MutableList<LessonInfoWrap>? = null

    private var focusedDraft: LessonInfoWrap? = null

    var onDelete: ((LessonInfoWrap) -> Unit)? = null
    var onEdit: ((LessonInfoWrap) -> Unit)? = null
    var onUpload: ((LessonInfoWrap) -> Unit)? = null
    var onPreview: ((LessonInfoWrap) -> Unit)? = null
    var onItemClick: ((LessonInfoWrap) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        var tvUpload = itemView.findViewById<TextView>(R.id.tvUpload)
        var tvEdit = itemView.findViewById<TextView>(R.id.tvEdit)
        var tvDelete = itemView.findViewById<TextView>(R.id.tvDelete)
        var progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
        var tvTag = itemView.findViewById<TextView>(R.id.tvTag)
        var ivMenu = itemView.findViewById<ImageView>(R.id.ivMenu)
        var llMenu = itemView.findViewById<LinearLayout>(R.id.llMenu)
        var ivCover = itemView.findViewById<ImageView>(R.id.ivCover)
        var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)
        var tvName = itemView.findViewById<TextView>(R.id.tvName)
        fun bind(it: LessonInfoWrap) {
            println(it)
            when (it.status) {
                EDITING -> {
                    tvUpload.visibility = GONE
                    tvEdit.visibility = VISIBLE
                    tvDelete.visibility = VISIBLE
                    progressBar.visibility = GONE
                    tvTag.text = "制作中"
                }
                RECORDED -> {
                    tvUpload.visibility = VISIBLE
                    tvEdit.visibility = GONE
                    tvDelete.visibility = VISIBLE
                    progressBar.visibility = GONE
                    tvTag.text = "未上传"
                }
                UPLOADED -> {
                    tvUpload.visibility = GONE
                    tvEdit.visibility = GONE
                    tvDelete.visibility = VISIBLE
                    progressBar.visibility = GONE
                    tvTag.text = "已上传"
                }
                UPLOADING -> {
                    tvUpload.visibility = GONE
                    tvEdit.visibility = GONE
                    tvDelete.visibility = VISIBLE
                    progressBar.visibility = VISIBLE
                    progressBar.progress = it.progress ?: 0
                    tvTag.text = "上传中"
                }

            }

            if (it == focusedDraft) {
                llMenu.visibility = VISIBLE
            } else {
                llMenu.visibility = GONE
            }
            Glide.with(itemView).load(it.lessonInfo.coverName).into(ivCover)
            tvDuration.text = it.getDuration()
            tvName.text = it.getName()
        }

        init {
            ivMenu.setOnClickListener {
                llMenu.let {
                    lessonList?.get(adapterPosition)?.let {
                        changeFocus(it)
                    }

                }
            }
            tvDelete.setOnClickListener {
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(it)
                    onDelete?.invoke(it)
                }
            }
            tvEdit.setOnClickListener {
                llMenu.visibility = View.GONE
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(it)
                    onEdit?.invoke(it)
                }
            }

            tvUpload.setOnClickListener {
                llMenu.visibility = View.GONE
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(it)
                    onUpload?.invoke(it)
                }
            }
            itemView.setOnClickListener {
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(null)
                    when (it.status) {
                        EDITING -> {
                            onEdit?.invoke(it)
                        }
                        RECORDED -> {
                            onPreview?.invoke(it)
                        }
                        else -> {
                            onItemClick?.invoke(it)
                        }
                    }
                }
            }
        }

    }

    private fun changeFocus(current: LessonInfoWrap?) {
        focusedDraft = if (focusedDraft == current) {
            null
        } else {
            current
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_mini_lesson, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        lessonList?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return lessonList?.size ?: 0
    }

    fun updateProgress(recordId: String?, progress: Int) {
        val list = lessonList ?: return
        for ((position, lessonInfo) in list.withIndex()) {
            if (lessonInfo.lessonInfo.recordId == recordId) {
                lessonInfo.progress = progress
                lessonInfo.status = UPLOADING
                println("updateProgress $lessonInfo")
                notifyItemChanged(position)
                DemoApp.app.saveLessonInfo(lessonInfo)
                return
            }
        }
    }

}
