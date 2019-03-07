package com.ai.deep.andy.carrecognizer.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ai.deep.andy.carrecognizer.R

import com.ai.deep.andy.carrecognizer.model.ClassificationItem
import java.nio.file.Files.size
import android.support.annotation.NonNull
import android.widget.Toast
import com.ai.deep.andy.carrecognizer.services.VolleyOnEventListener
import com.ai.deep.andy.carrecognizer.services.core.ListClassificationService
import com.ai.deep.andy.carrecognizer.services.users.LoginService
import kotlinx.android.synthetic.main.fragment_classification_list.*
import org.json.JSONObject
import java.nio.file.Files.size


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ClassificationListFragment.OnListFragmentInteractionListener] interface.
 */
class ClassificationListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null
    private var adapter: MyClassificationRecyclerViewAdapter? = null
    private var recycleView: RecyclerView? = null

    private var items: MutableList<ClassificationItem?> = mutableListOf()
    private var currentPage: Int = 0
    private var defaultPageSize: Int = 10
    var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_classification_list, container, false)
        recycleView = view.findViewById(R.id.recycle_list)

        // Set the adapter
        if (recycleView is RecyclerView) {
            ListClassificationService(context!!, object : VolleyOnEventListener<List<ClassificationItem>> {
                override fun onSuccess(obj: List<ClassificationItem>) {
                    currentPage += defaultPageSize
                    recycleView = recycleView
                    initAdapter(recycleView!!)
                    initScrollListener(recycleView!!)
                    isLoading = false
                    showEmptyPlaceHolderIfNeeded()
                }

                override fun onFailure(e: Exception) {
                    Toast.makeText(context, "Failed to login because " + e.message, Toast.LENGTH_SHORT).show()
                    isLoading = false
                    showEmptyPlaceHolderIfNeeded()

                }
            }).getItems(currentPage)
        }
        return view
    }

    private fun initAdapter(view: RecyclerView) {
        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            this.adapter = MyClassificationRecyclerViewAdapter(items, listener, context)
        }
    }

    private fun initScrollListener(view: RecyclerView) {
        view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == items.size - 1) {
                        //bottom of list!
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        isLoading = true
        //items.addAll(MutableList(defaultPageSize) { null })
        items.add(null)
        adapter?.notifyItemInserted(items.size - 1)

        ListClassificationService(context!!, object : VolleyOnEventListener<List<ClassificationItem>> {
            override fun onSuccess(obj: List<ClassificationItem>) {
                items.dropLast(1)
                val scrollPosition = items.size
                adapter?.notifyItemRemoved(scrollPosition)
                items.addAll(obj)
                adapter?.notifyDataSetChanged()
                isLoading = false
                showEmptyPlaceHolderIfNeeded()
                currentPage += defaultPageSize;
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(context, "Failed to login because " + e.message, Toast.LENGTH_SHORT).show()
                items.dropLast(1)
                val scrollPosition = items.size
                adapter?.notifyItemRemoved(scrollPosition)
                isLoading = false
                showEmptyPlaceHolderIfNeeded()

            }
        }).getItems(currentPage)
    }

    private fun showEmptyPlaceHolderIfNeeded() {
        if (items.isEmpty()) {
            recycleView?.visibility = View.INVISIBLE
            empty_view?.visibility = View.VISIBLE

        } else {
            recycleView?.visibility = View.VISIBLE
            empty_view?.visibility = View.INVISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            //throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
            //TODO its not ok, implement is
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: ClassificationItem?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                ClassificationListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
