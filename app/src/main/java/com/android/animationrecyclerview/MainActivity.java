package com.android.animationrecyclerview;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    @ViewById
    RecyclerView recyclerView;

    @AfterViews
    void init() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        final AnimationRecyclerViewAdapter adapter = new AnimationRecyclerViewAdapter(4) {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(new TextView(MainActivity.this)) {
                };
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                super.onBindViewHolder(holder, position);
                ((TextView) holder.itemView).setText("" + position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                removeItem(position);
                                commit();
                            }
                        });
                    }
                });
            }

            @Override
            public int getItemCount() {
                return 10;
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
