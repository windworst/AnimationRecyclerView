package com.android.animationrecyclerview;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AnimationRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private int mSpanCount;
    private SortedMap<Integer, Integer> mOffsetMap = new TreeMap<>();
    private int mItemOffset = 0;

    public AnimationRecyclerViewAdapter(int spanCount) {
        setSpanCount(spanCount);
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setSpanCount(int spanCount) {
        if (spanCount <= 0) {
            throw new IllegalArgumentException("spanCount must greater than 0");
        }
        mSpanCount = spanCount;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        setAnimation(holder.itemView, position);
    }

    //在index处插入新项后, 设置动画
    public AnimationRecyclerViewAdapter insertItem(int index) {
        ++index; //记录插入点, 插入动画只影响插入点后面元素
        if (!mOffsetMap.containsKey(index)) {
            mOffsetMap.put(index, 0);
        }
        mOffsetMap.put(index, 1 + mOffsetMap.get(index));
        return this;
    }

    //在index处移除新项, 设置动画
    public AnimationRecyclerViewAdapter removeItem(int index) {
        if (!mOffsetMap.containsKey(index)) {
            mOffsetMap.put(index, 0);
        }
        mOffsetMap.put(index, -1 + mOffsetMap.get(index)); //记录删除点
        return this;
    }

    //清除动画
    public void clearAnimation() {
        mOffsetMap.clear();
        mItemOffset = 0;
    }

    //移动动画, 传入需要移动的x, y, 可被覆盖
    protected Animation moveAnimation(int deltaX, int deltaY) {
        Animation animation = new TranslateAnimation(deltaX, 0, deltaY, 0);
        animation.setDuration(500);
        return animation;
    }

    //执行动画
    public void commit() {
        //将相邻的插入删除动画合并
        //若有相邻的插入删除动画, 计算这些相邻的改变项最终总共是增加还是减少
        //若增加则将总共改变值设置给这些相邻项的最后一项, 若减少则设置给这些相邻项的第一项
        {
            Integer first = null, last = null, sum = 0;
            Iterator<Map.Entry<Integer, Integer>> it = mOffsetMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> entry = it.next();
                if (last != null && entry.getKey() - 1 == last) {
                    sum += entry.getValue();
                } else {
                    if (sum != 0) {
                        mOffsetMap.put(sum > 0 ? last : first, sum);
                    }
                    sum = entry.getValue();
                    first = entry.getKey();
                }
                last = entry.getKey();
                it.remove();
            }
            if (sum != 0) {
                mOffsetMap.put(sum > 0 ? last : first, sum);
            }
        }
        mItemOffset = 0;
        super.notifyDataSetChanged();
        new Handler().post(new Runnable() {// notifyDataSetChanged 完成后执行
            @Override
            public void run() {
                clearAnimation();
            }
        });
    }

    //设置animation
    private void setAnimation(final View view, final int position) {
        if (mOffsetMap.containsKey(position)) {
            mItemOffset += mOffsetMap.get(position);
            mOffsetMap.remove(position);
        }
        if (0 == mItemOffset) {
            return;
        }
        final int itemOffset = mItemOffset;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int[] src = getPositionXY(view, position - itemOffset);
                int[] dst = getPositionXY(view, position);
                view.startAnimation(moveAnimation(src[0] - dst[0], src[1] - dst[1]));
            }
        });
    }

    //根据position拿到相应view的x,y坐标
    private int[] getPositionXY(View view, int position) {
        if (position == 0 || null == view) {
            return new int[]{0, 0};
        }
        int horizontalSpacing = 0, verticalSpacing = 0;
        return new int[]{
                (position % mSpanCount) * (view.getMeasuredWidth() + horizontalSpacing),
                (position / mSpanCount) * (view.getMeasuredHeight() + verticalSpacing)
        };
    }
}
