package com.plcoding.spotifycloneyt.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.plcoding.spotifycloneyt.R
import kotlinx.android.synthetic.main.swipe_item.view.*

// adapter to display our list of songs
class SwipeQuranAdapter: BaseQuranAdapter(R.layout.swipe_item){

    override val differ = AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: BaseQuranAdapter.QuranViewHolder, position: Int) {
        // get reference to current song
        val quran = quran[position]
        holder.itemView.apply {
            val text = "${quran.title} - ${quran.subtitle}"
            tvPrimary.text = text
            setOnClickListener{
                onItemClickListener?.let { click ->
                    click(quran)
                }
            }
        }
    }
}