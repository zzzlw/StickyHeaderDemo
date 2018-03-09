package com.test.stickyheader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Wells on 2018/3/2.
 */
// TODO: 2018/3/9 在CoordinatorLayout外面嵌套 下拉刷新布局后有手势冲突问题待解决
public class StickHeader2Fragement extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    Unbinder unbinder;
    @BindView(R.id.layout_stick_header)
    LinearLayout layoutStickHeader;
    @BindView(R.id.expandableLayout)
    ExpandableLayout expandableLayout;
    @BindView(R.id.layout_filter)
    LinearLayout layoutFilter;
    @BindView(R.id.recyclerView_filter)
    RecyclerView recyclerViewFilter;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.banner)
    TextView banner;


    public static StickHeader2Fragement newInstance(int type) {

        Bundle args = new Bundle();
        args.putInt("type", type);
        StickHeader2Fragement fragment = new StickHeader2Fragement();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stickheader2, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int type = getArguments().getInt("type");
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        CommonAdapter commonAdapter = new CommonAdapter(R.layout.item_common_text);
        if (type == 2) {
            layoutStickHeader.setVisibility(View.GONE);
        } else if (type == 3) {
            banner.setVisibility(View.GONE);

        }
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("item" + i);
        }
        commonAdapter.addData(data);
        recyclerView.setAdapter(commonAdapter);

        layoutStickHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });

        List<String> dataFilter = new ArrayList<>();
        for (int j = 0; j < 15; j++) {
            dataFilter.add("全部" + j);
        }
        final CommonAdapter filterAdapter = new CommonAdapter(R.layout.item_filter);
        filterAdapter.addData(dataFilter);
        recyclerViewFilter.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerViewFilter.setAdapter(filterAdapter);

        layoutFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.collapse();
            }
        });
        expandableLayout.setOnExpansionUpdateListener(new ExpandableLayout.OnExpansionUpdateListener() {
            @Override
            public void onExpansionUpdate(float expansionFraction, int state) {
                if (state == 0) {
                    layoutFilter.setVisibility(View.GONE);
                }
            }
        });

    }

    private void showFilter() {
//        appBar.setExpanded(false);
        layoutFilter.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutFilter.setVisibility(View.VISIBLE);
                expandableLayout.expand();
            }
        }, 300);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
