package com.stupidtree.hitax.ui.main.timeline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_DATE_CHANGED
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stupidtree.hitax.data.model.timetable.EventItem
import com.stupidtree.hitax.databinding.FragmentTimelineBinding
import com.stupidtree.hitax.ui.widgets.WidgetUtils
import com.stupidtree.hitax.utils.EventsUtils
import com.stupidtree.hitax.utils.TimeTools
import com.stupidtree.hitax.utils.TimeTools.TTY_WK_FOLLOWING
import com.stupidtree.style.base.BaseFragmentWithReceiver
import com.stupidtree.style.base.BaseListAdapter
import java.util.*
import kotlin.Comparator

class FragmentTimeLine : BaseFragmentWithReceiver<FragmentTimelineViewModel, FragmentTimelineBinding>() {
    private var listAdapter: TimelineListAdapter? = null
    private var mainPageController: MainPageController? = null


    override var receiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if(p1?.action== ACTION_TIME_TICK||p1?.action==ACTION_DATE_CHANGED||p1?.action==Intent.ACTION_TIME_CHANGED){
                viewModel.startRefresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainPageController) {
            mainPageController = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainPageController = null
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRefresh()
        mainPageController?.setTimelineTitleText(TimeTools.getDateString(
            requireContext(),
            Calendar.getInstance(),true, TTY_WK_FOLLOWING
        ))
    }


    private fun initListAndAdapter() {
        listAdapter = TimelineListAdapter(this.requireContext(), mutableListOf())
        binding?.list?.setItemViewCacheSize(Int.MAX_VALUE)
        binding?.list?.adapter = listAdapter
        binding?.list?.layoutManager = LinearLayoutManager(requireContext())
        listAdapter?.setOnItemClickListener(object :
            BaseListAdapter.OnItemClickListener<EventItem> {
            override fun onItemClick(data: EventItem?, card: View?, position: Int) {
                data?.let { EventsUtils.showEventItem(requireContext(), it) }
            }
        })
        listAdapter?.setOnHintConfirmedListener(object :
            TimelineListAdapter.OnHintConfirmedListener {

            override fun onConfirmed(v: View?, position: Int, hint: EventItem?) {
                v?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                val newL: MutableList<EventItem> = ArrayList(listAdapter?.beans)
                newL.remove(hint)
                listAdapter!!.notifyItemChangedSmooth(newL)
            }
        })
    }


    override fun initViews(view: View) {
        initListAndAdapter()
        viewModel.todayEventsLiveData.observe(this) {
            Collections.sort(it) { p0, p1 -> p0.from.compareTo(p1.from) }
            listAdapter?.notifyItemChangedSmooth(it)
            val holder: RecyclerView.ViewHolder? =
                binding?.list?.findViewHolderForAdapterPosition(0)
            if (holder != null) {
                val header: TimelineListAdapter.timelineHeaderHolder =
                    holder as TimelineListAdapter.timelineHeaderHolder
                header.UpdateHeadView()
            }
            activity?.let { it1 -> WidgetUtils.sendRefreshToAll(it1) }
        }

    }

    override fun initViewBinding(): FragmentTimelineBinding {
        return FragmentTimelineBinding.inflate(layoutInflater)
    }


    override fun getViewModelClass(): Class<FragmentTimelineViewModel> {
        return FragmentTimelineViewModel::class.java
    }

    interface MainPageController {
        fun setTimelineTitleText(string: String)
    }

    override fun getIntentFilter(): IntentFilter {
        val inf = IntentFilter()
        inf.addAction(ACTION_DATE_CHANGED)
        inf.addAction(ACTION_TIME_TICK)
        inf.addAction(Intent.ACTION_TIME_CHANGED)
        return inf
    }



}