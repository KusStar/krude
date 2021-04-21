package com.kuss.krude.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener


class ScaleGestureItemTouchListener(context: Context?, listener: Callback) :
    OnItemTouchListener, OnScaleGestureListener {
    private val mScaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private val mListener: Callback
    private var mDisallowInterceptTouchEvent = false
    private var mScaleFactor = 4f

    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView, event: MotionEvent
    ): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        mScaleGestureDetector.onTouchEvent(event)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        mDisallowInterceptTouchEvent = disallowIntercept
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        mScaleFactor *= detector.scaleFactor

        mScaleFactor = 0.1f.coerceAtLeast(mScaleFactor.coerceAtMost(4.0f))

        mListener.onScaleFactor(mScaleFactor)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    interface Callback {
        fun onScaleFactor(scaleFactor: Float)
    }

    init {
        mScaleGestureDetector.isQuickScaleEnabled = false
        mListener = listener
    }
}