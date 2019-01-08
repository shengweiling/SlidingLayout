package com.zztl.slidinglayout

import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.view.View

/**
 * @desc
 * @auth ${user}
 * @time 2019/1/7  15:20
 */

class SlidingLayoutBehavior : CoordinatorLayout.Behavior<SlidingLayout>() {
    var initOffset: Int = 0  //最初距离顶部的距离
    override fun onMeasureChild(parent: CoordinatorLayout, child: SlidingLayout, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        val childOffset = getChildOffset(parent, child)
        child.measure(parentWidthMeasureSpec, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(parentHeightMeasureSpec) - childOffset, View.MeasureSpec.EXACTLY))
        return true
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: SlidingLayout, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        /* getPreView(parent, child)?.let {
             child.offsetTopAndBottom(it.top+it.headerHeight)
         }*/
        child.offsetTopAndBottom(parent.indexOfChild(child) * child.headerHeight)
        initOffset = child.top
        return true
    }


    private fun getChildOffset(parent: CoordinatorLayout, child: SlidingLayout): Int {
        var offset: Int = 0
        for (i in 0 until parent.childCount) {
            parent.getChildAt(i)?.apply {
                if (this != child && this is SlidingLayout) {
                    offset += this.headerHeight
                }
            }
        }
        return offset
    }


    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: SlidingLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0) and (directTargetChild == child)
    }


    /**
     * 在嵌套滑动的子View未滑动之前告诉过来的准备滑动的情况,之所以要写这个方法 ，是为了让target在滚动时，child能进行上下移动，并把移动的距离赋值给consumed[1],
     *  告知子View滑动做出相应的调整
     *
     * @param parent
     * @param child
     * @param target   具体嵌套滑动的那个子类
     * @param dx       水平方向嵌套滑动的子View想要变化的距离
     * @param dy       垂直方向嵌套滑动的子View想要变化的距离
     * @param consumed 这个参数，告诉子View当前父View消耗的距离
     * consumed[0] 水平消耗的距离，consumed[1] 垂直消耗的距离
     */
    override fun onNestedPreScroll(parent: CoordinatorLayout, child: SlidingLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (child.top > initOffset) {
            //1 自己移动
            val distance = scrooll(parent, child, dy)
            //2 上下的View移动
            stickScroll(parent, child, distance)
        }
    }


    override fun onNestedScroll(parent: CoordinatorLayout, child: SlidingLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        if (parent.indexOfChild(child) != 0) {
            //1 自己移动
            val distance = scrooll(parent, child, dyUnconsumed)
            //2 上下的View移动
            stickScroll(parent, child, distance)
        }
    }


    private fun scrooll(parent: CoordinatorLayout, child: SlidingLayout, dy: Int): Int {
        val i = parent.childCount - parent.indexOfChild(child)
        val scollDistance = Math.min(Math.max(initOffset, child.top - dy), parent.height - child.headerHeight*i) - child.top
        child.offsetTopAndBottom(scollDistance)
        return -scollDistance   //负数表示向下滑，正数表示向上滑
    }

    private fun stickScroll(parent: CoordinatorLayout, child: SlidingLayout, distance: Int) {
        if (distance == 0) {
            return
        }
        if (distance > 0) {  //向上滑
            var currentView = child
            var preView = getPreView(parent, currentView)
            while (null != preView) {
                (preView.top + preView.headerHeight - currentView.top).let {
                    if (it > 0) {
                        preView!!.offsetTopAndBottom(-it)
                    }
                }
                currentView = preView
                preView = getPreView(parent, currentView)
            }
        } else { //向下滑
            var currentView = child
            var nextView = getNextView(parent, currentView)
            while (null != nextView) {
                (currentView.top + currentView.headerHeight - nextView.top).let {
                    if (it > 0) {
                        nextView!!.offsetTopAndBottom(it)
                    }
                }

                currentView = nextView
                nextView = getNextView(parent, currentView)
            }
        }
    }

    /**
     * 获取当前view的下一个view
     */
    private fun getNextView(parent: CoordinatorLayout, currentView: SlidingLayout): SlidingLayout? {
        val indexOfChild = parent.indexOfChild(currentView)
        for (i in indexOfChild + 1 until parent.childCount) {
            if (parent.getChildAt(i) is SlidingLayout) {
                return parent.getChildAt(i) as SlidingLayout
            }
        }
        return null
    }


    /**
     * 获取当前view上一个view
     */
    private fun getPreView(parent: CoordinatorLayout, child: SlidingLayout): SlidingLayout? {
        val indexOfChild = parent.indexOfChild(child)
        for (i in indexOfChild - 1 downTo 0) {
            if (parent.getChildAt(i) is SlidingLayout) {
                return parent.getChildAt(i) as SlidingLayout
            }
        }
        return null
    }

}
