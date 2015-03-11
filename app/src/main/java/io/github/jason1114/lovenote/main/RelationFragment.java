package io.github.jason1114.lovenote.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;

public class RelationFragment extends Fragment {


    private AccountBean mAccountBean;

    public RelationFragment() {
        // Required empty public constructor
    }

    public static RelationFragment newInstance(AccountBean accountBean) {
        RelationFragment fragment = new RelationFragment();
        Bundle args = new Bundle();
        args.putParcelable("account", accountBean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mAccountBean = getArguments().getParcelable("account");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.relationfragment_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_relationfragment, menu);
        getActivity().getActionBar().setIcon(R.drawable.ic_menu_heart);
        getActivity().getActionBar().setTitle(R.string.relation);
        getActivity().getActionBar().setDisplayShowCustomEnabled(false);
    }
}
