package com.supercilex.robotscouter.ui.scouting.scoutlist.viewholder

import android.support.annotation.CallSuper
import android.support.transition.TransitionManager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.supercilex.robotscouter.R
import com.supercilex.robotscouter.data.model.Metric
import com.supercilex.robotscouter.ui.scouting.MetricViewHolderBase
import com.supercilex.robotscouter.ui.scouting.scoutlist.CounterValueDialog
import kotterknife.bindView

open class CounterViewHolder(itemView: View) :
        MetricViewHolderBase<Metric.Number, Long, TextView>(itemView), View.OnClickListener, View.OnLongClickListener {
    protected val count: TextView by bindView(R.id.count)
    private val increment: ImageButton by bindView(R.id.increment_counter)
    private val decrement: ImageButton by bindView(R.id.decrement_counter)

    private val valueWithoutUnit: String get() {
        val unit: String? = metric.unit
        val count = count.text.toString()
        return if (TextUtils.isEmpty(unit)) count else count.replace(unit!!, "")
    }

    public override fun bind() {
        super.bind()
        updateValue(metric.value)
        increment.setOnClickListener(this)
        decrement.setOnClickListener(this)
        count.setOnLongClickListener(this)
    }

    @CallSuper
    override fun onClick(v: View) {
        val id = v.id
        var value = valueWithoutUnit.toLong()

        TransitionManager.beginDelayedTransition(itemView as ViewGroup)
        if (id == R.id.increment_counter) {
            updateValue(++value)
        } else if (id == R.id.decrement_counter) {
            updateValue(--value)
        }
        updateMetricValue(value)
    }

    protected open fun setValue(value: Long) {
        val unit: String? = metric.unit
        count.text = if (TextUtils.isEmpty(unit)) value.toString() else value.toString() + unit!!
    }

    private fun updateValue(value: Long) {
        setValue(value)
        decrement.isEnabled = value > 0 // No negative values
    }

    override fun onLongClick(v: View): Boolean {
        CounterValueDialog.show(manager, metric.ref, valueWithoutUnit)
        return true
    }
}
