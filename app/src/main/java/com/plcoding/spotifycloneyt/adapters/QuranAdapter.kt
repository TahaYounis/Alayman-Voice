package com.plcoding.spotifycloneyt.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.plcoding.spotifycloneyt.R
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

// adapter to display our list of songs
class QuranAdapter @Inject constructor(
    // we need instance to our glide to load image
    private val glide: RequestManager
) : BaseQuranAdapter(R.layout.list_item){

    override val differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: BaseQuranAdapter.QuranViewHolder, position: Int) {
        // get reference to current song
        val chapterOfQuran = quran[position]
        holder.itemView.apply {
            tvSongTitle.isSelected = true
            tvSongTitle.text = chapterOfQuran.title
            tvSecondary.text = chapterOfQuran.subtitle
            glide.load(chapterOfQuran.imageUrl).into(ivItemImage)

            setOnClickListener{
                onItemClickListener?.let {
                    it(chapterOfQuran)
                }
            }
        }
    }
}