package com.lagradost.quicknovel.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.lagradost.quicknovel.ChapterData
import com.lagradost.quicknovel.R
import com.lagradost.quicknovel.databinding.SimpleChapterBinding
import com.lagradost.quicknovel.ui.BaseDiffCallback
import com.lagradost.quicknovel.ui.NoStateAdapter
import com.lagradost.quicknovel.ui.ViewHolderState
import com.lagradost.quicknovel.ui.newSharedPool
import com.lagradost.quicknovel.util.UIHelper.popupMenu

class ChapterAdapter(val viewModel: ResultViewModel) :
    NoStateAdapter<ChapterData>(
        diffCallback = BaseDiffCallback(
            itemSame = { a, b -> a.url == b.url },
            contentSame = { a, b -> a == b }
        )) {

    companion object {
        val sharedPool =
            newSharedPool {
                setMaxRecycledViews(CONTENT, 10)
            }
    }

    override fun onCreateContent(parent: ViewGroup): ViewHolderState<Any> {
        return ViewHolderState(
            SimpleChapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private fun refresh(
        binding: SimpleChapterBinding,
        card: ChapterData,
        viewModel: ResultViewModel
    ) {
        val alpha = if (viewModel.hasReadChapter(chapter = card)) 0.5F else 1.0F

        binding.name.alpha = alpha
        binding.releaseDate.alpha = alpha
    }

    override fun onBindContent(holder: ViewHolderState<Any>, item: ChapterData, position: Int) {
        val binding = holder.view as? SimpleChapterBinding ?: return
        binding.apply {
            name.text = item.name
            releaseDate.text = item.dateOfRelease
            releaseDate.isGone = item.dateOfRelease.isNullOrBlank()
            root.setOnClickListener {
                viewModel.streamRead(item)
                viewModel.isResume = true//to update read status
            }
            root.setOnLongClickListener { view ->
                val isRead = viewModel.hasReadChapter(item)
                val items = listOf(
                    1 to if (isRead) R.string.mark_as_unread else R.string.mark_as_read,
                    2 to R.string.mark_previous_as_read,
                    3 to R.string.mark_previous_as_unread,
                )
                view.popupMenu(items, null) {
                    when (itemId) {
                        1 -> {
                            viewModel.setReadChapter(chapter = item, !isRead)
                            refresh(binding, item, viewModel)
                        }
                        2 -> {
                            viewModel.setReadUpToChapter(item, true)
                            notifyDataSetChanged()
                        }
                        3 -> {
                            viewModel.setReadUpToChapter(item, false)
                            notifyDataSetChanged()
                        }
                    }
                }
                return@setOnLongClickListener true
            }
            refresh(binding, item, viewModel)
        }
    }
}