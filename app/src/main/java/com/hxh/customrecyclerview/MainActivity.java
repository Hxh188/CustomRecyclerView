package com.hxh.customrecyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hxh.recyclerview_lib.RefreshRecyclerViewContainer;
import com.hxh.recyclerview_lib.decoration.SpacesItemDecoration;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RefreshRecyclerViewContainer refre_rv = findViewById(R.id.refre_rv);

        for(int i = 0;i < 15;i++)
        {
            list.add("");
        }

        refre_rv.setWrapAdapter(new SimpleAdapter(list));
        refre_rv.setRefreshListListener(new RefreshRecyclerViewContainer.OnRefreshListListener() {
            @Override
            public void refreshList(final boolean isRefresh) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isRefresh)
                        {
                            ArrayList<String> child = new ArrayList<>();
                            for(int i = 0;i < 5;i++)
                            {
                                child.add("");
                            }
                            int start = list.size();
                            list.addAll(child);
                            refre_rv.setRefreshError();
                            refre_rv.getWrapAdapter().notifyItemRangeChanged(start, child.size());
                        }
                        refre_rv.hideRefreshingIcon();
                    }
                }, 1500);

            }
        });

        refre_rv.getRvList().setLayoutManager(new LinearLayoutManager(this));
        refre_rv.getRvList().addItemDecoration(new SpacesItemDecoration(10));

    }

    class SimpleAdapter extends RecyclerView.Adapter
    {
        private ArrayList<String> list = new ArrayList<>();
        public SimpleAdapter(ArrayList<String> list)
        {
            this.list = list;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new HoderItem(view);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class HoderItem extends RecyclerView.ViewHolder{
            public HoderItem(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
