package dora.widget

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import dora.widget.emptylayout.R

open class DoraEmptyLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    private var stateViews = ArrayMap<Int, View>()
    private var empty: (View.(emptyText: String) -> Unit)? = null
    private var error: (View.(exception: Exception) -> Unit)? = null
    private var loading: (View.() -> Unit)? = null
    protected var content: (View.() -> Unit)? = null
    protected lateinit var contentView: View
    @LayoutRes
    private var emptyLayoutResId: Int = NO_ID
        get() = if (field == NO_ID) Config.emptyLayout else field
    @LayoutRes
    private var errorLayoutResId: Int = NO_ID
        get() = if (field == NO_ID) Config.errorLayout else field
    @LayoutRes
    private var loadingLayoutResId: Int = NO_ID
        get() = if (field == NO_ID) Config.loadingLayout else field

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DoraEmptyLayout, defStyleAttr, 0)
        emptyLayoutResId = a.getResourceId(R.styleable.DoraEmptyLayout_dview_emptyLayout, NO_ID)
        errorLayoutResId = a.getResourceId(R.styleable.DoraEmptyLayout_dview_errorLayout, NO_ID)
        loadingLayoutResId = a.getResourceId(R.styleable.DoraEmptyLayout_dview_loadingLayout, NO_ID)
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 1) {
            throw UnsupportedOperationException("EmptyLayout can have only one child")
        }
        contentView = getChildAt(0)
        stateViews[STATE_CONTENT] = contentView
        showStateView(STATE_EMPTY)
    }

    fun showEmpty(emptyText: String = "") {
        runMain {
            val view = showStateView(STATE_EMPTY)
            empty?.invoke(view, emptyText)
        }
    }

    fun showError(e: Exception) {
        runMain {
            val view = showStateView(STATE_ERROR)
            error?.invoke(view, e)
        }
    }

    fun showLoading() {
        runMain {
            val view = showStateView(STATE_LOADING)
            loading?.invoke(view)
        }
    }

    open fun showContent() {
        runMain {
            if (contentView is RecyclerView) {
                if ((contentView as RecyclerView).adapter == null ||
                        (contentView as RecyclerView).adapter!!.itemCount == 0) {
                    showEmpty()
                    return@runMain
                }
            }
            // 1.12开始支持遍历容器，确保一个EmptyLayout里面只能放一个RecyclerView
            if (contentView is ViewGroup) {
                for (i in 0 until childCount) {
                    val view = getChildAt(i)
                    if (view is RecyclerView) {
                        if (view.adapter == null ||
                            view.adapter!!.itemCount == 0) {
                            showEmpty()
                            return@runMain
                        }
                    }
                }
            }
            val view = showStateView(STATE_CONTENT)
            this.content?.invoke(view)
        }
    }

    protected fun showStateView(state: Int) : View {
        val targetView = getStateView(state)
        for (stateView in stateViews.values) {
            if (targetView == stateView) {
                stateView.visibility = VISIBLE
            } else {
                stateView.visibility = GONE
            }
        }
        return targetView
    }

    fun onEmpty(block: View.(emptyText: String) -> Unit) = apply {
        empty = block
    }

    fun onError(block: View.(exception: Exception) -> Unit) = apply {
        error = block
    }

    fun onLoading(block: View.() -> Unit) = apply {
        loading = block
    }

    fun onRefresh(block: View.() -> Unit) = apply {
        content = block
    }

    private fun getStateView(state: Int) : View {
        stateViews[state]?.let { return it }
        if (state == STATE_CONTENT) {
            stateViews[STATE_CONTENT] = contentView
            return contentView
        } else {
            val layoutId = when (state) {
                STATE_EMPTY -> emptyLayoutResId
                STATE_ERROR -> errorLayoutResId
                STATE_LOADING -> loadingLayoutResId
                else -> NO_ID
            }
            if (layoutId == NO_ID) {
                when (state) {
                    STATE_EMPTY -> throw Resources.NotFoundException("empty layout is not set")
                    STATE_ERROR -> throw Resources.NotFoundException("error layout is not set")
                    STATE_LOADING -> throw Resources.NotFoundException("loading layout is not set")
                }
            }
            val stateView = LayoutInflater.from(context).inflate(layoutId, this, false)
            stateView.layoutParams = Config.layoutParams
            addView(stateView)
            stateViews[state] = stateView
            return stateView
        }
    }

    protected fun runMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            Handler(Looper.getMainLooper()).post { block() }
        }
    }

    object Config {
        @LayoutRes var emptyLayout: Int = R.layout.dview_layout_empty
        @LayoutRes var errorLayout: Int = R.layout.dview_layout_error
        @LayoutRes var loadingLayout: Int = R.layout.dview_layout_loading
        var layoutParams: LayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        init {
            layoutParams.addRule(CENTER_IN_PARENT)
        }
    }

    companion object {
        const val STATE_EMPTY : Int = 0
        const val STATE_CONTENT : Int = 1
        const val STATE_ERROR : Int = 2
        const val STATE_LOADING : Int = 3
    }
}