package com.plcoding.spotifycloneyt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.plcoding.spotifycloneyt.data.entities.QuranModel

/* we will try write this adapter in optimal way to saving a lot of code and minimizing boilerplate code
because this adapter is similar, we will write abstract base adapter class that define the default behavior
every adapter in our project should have and then we will have 2 adapter inherit from this base adapter
and this will give them all properties functions and variables and if we want to change something then we can
do that individually for each adapter so we can set individual layout for those */
abstract class BaseQuranAdapter(
    private val layoutId: Int
)  : RecyclerView.Adapter<BaseQuranAdapter.QuranViewHolder>(){



    class QuranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /* normally you pass list of article in constructor and every time you want to add an article add it in
    in the list and call adapter.notifyDataSetChange but this is very inefficient because using notifyDataSetChange
    will update whole recyclerView items even the items didn't change, to solve this problem we can use de future
    it calculate the differences between two lists and enables us update just the different items, another advantages
    it will actually happen in the background so we don't block our main thread */

    // create the callback for async list differ
    protected val diffCallback = object : DiffUtil.ItemCallback<QuranModel>() {
        override fun areItemsTheSame(oldItem: QuranModel, newItem: QuranModel): Boolean {
            // return identifier of each item in this case is the media id
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: QuranModel, newItem: QuranModel): Boolean {
            //return if the exact same item like image is same or title is same ...
            return oldItem.hashCode() == newItem.hashCode() // calculate the hash value of two items
        }
    }
    //async list differ it the tool that compare between our two list and only update changed items
    protected abstract val differ:AsyncListDiffer<QuranModel>

    var quran: List<QuranModel>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        return QuranViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }


    // we will pass the current QuranModel when we click on an item to that lambda function
    protected var onItemClickListener: ((QuranModel) -> Unit)? = null

    fun setItemClickListener(listener: (QuranModel) -> Unit) {
        //set our own listener to our passed listener
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return quran.size
    }
}