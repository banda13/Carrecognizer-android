package com.ai.deep.andy.carrecognizer.fragments

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.R
import com.ai.deep.andy.carrecognizer.dialogs.DialogFactory


import com.ai.deep.andy.carrecognizer.fragments.ClassificationListFragment.OnListFragmentInteractionListener
import com.ai.deep.andy.carrecognizer.model.ClassificationItem
import kotlinx.android.synthetic.main.fragment_classification_item.view.*
import kotlinx.android.synthetic.main.fragment_classification_item_loading.view.*
import com.ai.deep.andy.carrecognizer.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.ai.deep.andy.carrecognizer.fragments.MyClassificationRecyclerViewAdapter.LoadingViewHolder
import com.ai.deep.andy.carrecognizer.fragments.MyClassificationRecyclerViewAdapter.ItemViewHolder
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.services.core.CategoryDetailsService
import com.ai.deep.andy.carrecognizer.utils.Logger
import org.json.JSONObject


class MyClassificationRecyclerViewAdapter(
        private val mValues: List<ClassificationItem?>,
        private val mListener: OnListFragmentInteractionListener?,
        private val mContext : Context)
: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ClassificationItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_classification_item, parent, false)
            return ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_classification_item_loading, parent, false)
            return LoadingViewHolder(view)
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun getItemCount(): Int = mValues.size

    override fun getItemViewType(position: Int): Int {
        return (if (mValues[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM)
    }

    inner class ItemViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.classification_id_text
        val mImageView : ImageView = mView.classification_image
        val mDateView : TextView = mView.classification_date_text
        val mResultsView : TextView = mView.classification_result_text

        override fun toString(): String {
            return "ItemViewHolder(mView=$mView, mIdView=$mIdView, mImageView=$mImageView, mDateView=$mDateView, mResultsView=$mResultsView)"
        }
    }

    inner class LoadingViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mProgressBarView : ProgressBar = mView.progressBar

        override fun toString(): String {
            return "LoadingViewHolder(mView=$mView, mProgressBarView=$mProgressBarView)"
        }
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        Log.i(Logger.LOGTAG, "Showing progress bar at position $position")
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        Log.i(Logger.LOGTAG, "Populating new item in position $position")
        val item = mValues[position]
        viewHolder.mIdView.text = item?.classificationId.toString()
        viewHolder.mDateView.text = DateUtils.toSimpleString(item?.creationDate)
        viewHolder.mResultsView.text = item?.getFormattedResults()

        Glide.with(mContext).load(item?.img_url).into(viewHolder.mImageView)

        viewHolder.mView.setOnClickListener {
            run {
                val title = item!!.getResultsList()
                val dialog = DialogFactory().showLoadingDialog(title[0], mContext)
                CategoryDetailsService(mContext, object : VolleyOnEventListener<JSONObject> {
                    override fun onSuccess(obj: JSONObject) {
                        dialog.dismiss()
                        DialogFactory().showItemDetails(title, mContext, obj)
                    }

                    override fun onFailure(e: Exception) {
                        dialog.dismiss()
                        Toast.makeText(mContext, "Sry, can't load the details for this classification, try later", Toast.LENGTH_LONG).show()
                    }
                }).getDetails(title[0])
            }
        }
    }


}

