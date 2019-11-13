package com.hxh.recyclerview_lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class RefreshRecyclerViewContainer extends LinearLayout {

    public static final int STATE_REFRESH_MORE = 0;
    public static final int STATE_REFRESH_END = 1;
    public static final int STATE_REFRESH_ERROR = 2;

    private int refreshState = STATE_REFRESH_MORE;

    private SwipeRefreshLayout srl_refresher;
    private FrameLayout flayout_container;
    private RecyclerView rvList;

    private OnRefreshListListener refreshListListener;

    public RefreshRecyclerViewContainer(Context context) {
        super(context);
        init();
    }

    public RefreshRecyclerViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init()
    {
        initViews();
        setListeners();
    }

    private void setListeners() {
        srl_refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(refreshListListener != null)
                {
                    refreshListListener.refreshList(true);
                }
            }
        });

        LinearRecyclerViewOnScroller mScrollListener = new LinearRecyclerViewOnScroller(srl_refresher) {
            @Override
            public void loadMore() {
                if(refreshListListener != null)
                {
                    refreshListListener.refreshList(false);
                }
            }
        };

        rvList.addOnScrollListener(mScrollListener);
    }

    public void handLoadMore()
    {
        setStateAndRefreshEnd(STATE_REFRESH_MORE);
        if(refreshListListener != null)
        {
            refreshListListener.refreshList(false);
        }
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_refresh_recyclerview, this, true);
        srl_refresher = findViewById(R.id.srl_refresher);
        flayout_container = findViewById(R.id.flayout_container);

        rvList = addRecyclerView();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        flayout_container.addView(rvList, lp);
    }

    public RecyclerView addRecyclerView()
    {
        EmptyRecyclerView rv = new EmptyRecyclerView(getContext());
        return rv;
    }

    public void hideRefreshingIcon()
    {
        srl_refresher.setRefreshing(false);
    }

    public void setWrapAdapter(RecyclerView.Adapter innerAdapter) {
        RefreshRecyclerViewContainer.RefreshAdapterWrapter adapter = new RefreshRecyclerViewContainer.RefreshAdapterWrapter(this, innerAdapter);
        rvList.setAdapter(adapter);
    }

    public RefreshRecyclerViewContainer.RefreshAdapterWrapter getWrapAdapter()
    {
        return (RefreshAdapterWrapter) rvList.getAdapter();
    }

    public abstract class LinearRecyclerViewOnScroller extends RecyclerView.OnScrollListener {

        private boolean mCanLoadMore = false;
        private SwipeRefreshLayout mSwipeRefreshLayout;

        public LinearRecyclerViewOnScroller(SwipeRefreshLayout swipeRefreshLayout) {
            mSwipeRefreshLayout = swipeRefreshLayout;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mSwipeRefreshLayout != null) {
                try {
                    int topRowVerticalPosition = recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                    mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                View childAt = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                int childBottom = childAt.getBottom();
                int bottom = recyclerView.getBottom();
                int bottomDecorationHeight = layoutManager.getBottomDecorationHeight(childAt);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                mCanLoadMore = layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 1
                        && childBottom + marginLayoutParams.bottomMargin + bottomDecorationHeight
                        + recyclerView.getPaddingBottom() >= bottom;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (mCanLoadMore && newState == RecyclerView.SCROLL_STATE_IDLE && refreshState == STATE_REFRESH_MORE){
                loadMore();
            }
        }

        public abstract void loadMore();
    }

    public void setRefreshEnd()
    {
        setStateAndRefreshEnd(STATE_REFRESH_END);
    }

    public void setRefreshMore()
    {
        setStateAndRefreshEnd(STATE_REFRESH_MORE);
    }

    public void setRefreshError()
    {
        setStateAndRefreshEnd(STATE_REFRESH_ERROR);
    }

    private void setStateAndRefreshEnd(int state)
    {
        setRefreshState(state);
        RefreshRecyclerViewContainer.RefreshAdapterWrapter adapter = (RefreshAdapterWrapter) rvList.getAdapter();
        adapter.setState(state);
    }

    public void setRefreshListListener(OnRefreshListListener refreshListListener) {
        this.refreshListListener = refreshListListener;
    }

    public OnRefreshListListener getRefreshListListener() {
        return refreshListListener;
    }

    public RecyclerView getRvList() {
        return rvList;
    }

    public void setRefreshState(int refreshState) {
        this.refreshState = refreshState;
    }

    public interface OnRefreshListListener
    {
        public void refreshList(boolean isRefresh);
    }

    public static class RefreshAdapterWrapter extends RecyclerView.Adapter
    {
        public static final int TYPE_END = -1;
        private RecyclerView.Adapter innerAdapter = null;
        private int state = STATE_REFRESH_MORE;

        private RefreshRecyclerViewContainer container;

        public RefreshAdapterWrapter(RefreshRecyclerViewContainer refreshRecyclerViewContainer, RecyclerView.Adapter innerAdapter)
        {
            this.container = refreshRecyclerViewContainer;
            this.innerAdapter = innerAdapter;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == TYPE_END)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_refresh_more, parent, false);
                return new HolderEnd(view);
            }else
            {
                return innerAdapter.onCreateViewHolder(parent, viewType);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof HolderEnd)
            {
                ((HolderEnd)holder).refreshState(state);
            }else
            {
                innerAdapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position < innerAdapter.getItemCount())
            {
                return innerAdapter.getItemViewType(position);
            }else
            {
                return TYPE_END;
            }
        }

        @Override
        public int getItemCount() {
            return innerAdapter.getItemCount() + 1;
        }

        public void setState(int state) {
            this.state = state;
            notifyItemChanged(innerAdapter.getItemCount());
        }

        class HolderEnd extends RecyclerView.ViewHolder{
            private ProgressBar pb_more;
            private TextView tv_alert;
            public HolderEnd(@NonNull View itemView) {
                super(itemView);

                pb_more = itemView.findViewById(R.id.pb_more);
                tv_alert = itemView.findViewById(R.id.tv_alert);
            }

            public void refreshState(int state) {
                switch (state)
                {
                    case STATE_REFRESH_MORE:
                        pb_more.setVisibility(View.VISIBLE);
                        tv_alert.setText("正在加载更多...");
                        tv_alert.setOnClickListener(null);
                        break;
                    case STATE_REFRESH_END:
                        pb_more.setVisibility(View.GONE);
                        tv_alert.setText("没有更多了");
                        tv_alert.setOnClickListener(null);
                        break;
                    case STATE_REFRESH_ERROR:
                        pb_more.setVisibility(View.GONE);
                        tv_alert.setText("加载失败，点击重试");
                        tv_alert.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                container.handLoadMore();
                            }
                        });
                        break;
                }
            }
        }
    }
}
