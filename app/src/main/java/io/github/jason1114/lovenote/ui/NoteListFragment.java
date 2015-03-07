package io.github.jason1114.lovenote.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.firebase.FireBaseService;
import io.github.jason1114.lovenote.main.WriteNoteActivity;

public class NoteListFragment extends Fragment {
    private static final String ACCOUNT_BEAN = "mAccountBean";
    private static final String TOKEN = "mToken";

    private AccountBean mAccountBean;
    private String mToken;
    private UserBean mUserSelf;

    Firebase mFireBaseData;
    NoteListAdapter mAdapter;
    ChildEventListener notesChangeListener = new NotesChangeListener();

    public static NoteListFragment newInstance(AccountBean accountBean, String token) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ACCOUNT_BEAN, accountBean);
        args.putString(TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mAccountBean = getArguments().getParcelable(ACCOUNT_BEAN);
            mToken = getArguments().getString(TOKEN);
        }
        mUserSelf = new UserBean();
        mUserSelf.setScreen_name(mAccountBean.getUserNick());
        mUserSelf.setProfile_image_url(mAccountBean.getAvatarUrl());

        Firebase.setAndroidContext(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.listview_layout, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        mAdapter = new NoteListAdapter(this, listView);
        listView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        mFireBaseData = FireBaseService.getInstance();
        String userNodeName = mAccountBean.getUid();
        Firebase userNode = mFireBaseData.child(getString(R.string.users)).child(userNodeName);
        Firebase notesNode = userNode.child(getString(R.string.notes));
        notesNode.addChildEventListener(notesChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        String userNodeName = mAccountBean.getUid();
        Firebase userNode = mFireBaseData.child(getString(R.string.users)).child(userNodeName);
        Firebase notesNode = userNode.child(getString(R.string.notes));
        notesNode.removeEventListener(notesChangeListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setIcon(R.drawable.ic_menu_home);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_friendstimelinefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.write_weibo) {
            Intent intent = WriteNoteActivity.newIntent(mAccountBean);
            startActivity(intent);
        } else {
            // don't know
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class NotesChangeListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Map<String, Object> newNote = (Map<String, Object>) dataSnapshot.getValue();
            MessageBean msg = new MessageBean();
            String content = (String) newNote.get(getString(R.string.note_content));
            msg.setListViewSpannableString(new SpannableString(content));
            msg.setId(dataSnapshot.getKey());
            msg.setUser(mUserSelf);
            msg.setMills((Long)newNote.get(getString(R.string.note_create_at)));
            mAdapter.getData().add(0,msg);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
