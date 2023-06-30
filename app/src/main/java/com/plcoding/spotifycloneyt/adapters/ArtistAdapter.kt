package com.plcoding.spotifycloneyt.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import kotlinx.android.synthetic.main.list_artist_item.view.*
import kotlinx.android.synthetic.main.swipe_item.view.*
import javax.inject.Inject

class ArtistAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseQuranAdapter(R.layout.list_artist_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) {
        val chapterOfQuran = quran[position]
        holder.itemView.apply {
            tvArtistName.text = chapterOfQuran.subtitle
            glide.load(chapterOfQuran.imageUrl).into(artistImg)

            setOnClickListener {
                onItemClickListener?.let {
                    it(chapterOfQuran)
                }
            }
        }
    }
}