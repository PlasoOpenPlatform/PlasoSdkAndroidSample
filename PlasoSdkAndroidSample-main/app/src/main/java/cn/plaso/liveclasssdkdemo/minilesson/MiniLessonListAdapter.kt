package cn.plaso.liveclasssdkdemo.minilesson

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.listitem_mini_lesson.view.*

class MiniLessonListAdapter : RecyclerView.Adapter<MiniLessonListAdapter.VH>() {

    var lessonList: MutableList<LessonInfoWrap>? = null

    private var focusedDraft: LessonInfoWrap? = null

    var onDelete: ((LessonInfoWrap) -> Unit)? = null
    var onEdit: ((LessonInfoWrap) -> Unit)? = null
    var onUpload: ((LessonInfoWrap) -> Unit)? = null
    var onPreview: ((LessonInfoWrap) -> Unit)? = null
    var onItemClick: ((LessonInfoWrap) -> Unit)? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(it: LessonInfoWrap) {
            println(it)
            when (it.status) {
                EDITING -> {
                    itemView.tvUpload.visibility = GONE
                    itemView.tvEdit.visibility = VISIBLE
                    itemView.tvDelete.visibility = VISIBLE
                    itemView.progressBar.visibility = GONE
                    itemView.tvTag.text = "制作中"
                }
                RECORDED -> {
                    itemView.tvUpload.visibility = VISIBLE
                    itemView.tvEdit.visibility = GONE
                    itemView.tvDelete.visibility = VISIBLE
                    itemView.progressBar.visibility = GONE
                    itemView.tvTag.text = "未上传"
                }
                UPLOADED -> {
                    itemView.tvUpload.visibility = GONE
                    itemView.tvEdit.visibility = GONE
                    itemView.tvDelete.visibility = VISIBLE
                    itemView.progressBar.visibility = GONE
                    itemView.tvTag.text = "已上传"
                }
                UPLOADING -> {
                    itemView.tvUpload.visibility = GONE
                    itemView.tvEdit.visibility = GONE
                    itemView.tvDelete.visibility = VISIBLE
                    itemView.progressBar.visibility = VISIBLE
                    itemView.progressBar.progress = it.progress ?: 0
                    itemView.tvTag.text = "上传中"
                }

            }

            if (it == focusedDraft) {
                itemView.llMenu.visibility = VISIBLE
            } else {
                itemView.llMenu.visibility = GONE
            }
            Glide.with(itemView).load(it.lessonInfo.coverName).into(itemView.ivCover)
            itemView.tvDuration.text = it.getDuration()
            itemView.tvName.text = it.getName()
        }

        init {
            itemView.ivMenu.setOnClickListener {
                itemView.llMenu.let {
                    lessonList?.get(adapterPosition)?.let {
                        changeFocus(it)
                    }

                }
            }
            itemView.tvDelete.setOnClickListener {
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(it)
                    onDelete?.invoke(it)
                }
            }
            itemView.tvEdit.setOnClickListener {
                itemView.llMenu.visibility = View.GONE
                lessonList?.get(adapterPosition)?.let {
                    changeFocus(it)
                    onEdit?.invoke(it)
                }
            }

            itemView.tvUpload.setOnClickListener {
                itemView.llMenu.visibility = View.GONE
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