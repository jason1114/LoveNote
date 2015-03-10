package io.github.jason1114.lovenote.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.firebase.FireBaseService;
import io.github.jason1114.lovenote.main.MainNotesActivity;
import io.github.jason1114.lovenote.main.WriteNoteActivity;
import io.github.jason1114.lovenote.utils.GlobalContext;

public class NoteListFragment extends Fragment {
    private static final String ACCOUNT_BEAN = "mAccountBean";
    private static final String TOKEN = "mToken";

    private AccountBean mAccountBean;
    private String mToken;
    private UserBean mUserSelf;

    Firebase mFireBaseData;
    Firebase mUserNode;
    Firebase mNotesNode;
    Firebase mConnectedNode;


    NoteListAdapter mAdapter;
    ChildEventListener notesChangeListener = new NotesChangeListener();
    ConnectionStateListener mConnectionStateListener;

    MainNotesActivity parentActivity;

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

    public MessageBean noteDataSnapshotToMessageBean(DataSnapshot snapshot) {
        Map<String, Object> newNote = (Map<String, Object>) snapshot.getValue();
        MessageBean msg = new MessageBean();
        String content = (String) newNote.get(getString(R.string.note_content));
        msg.setListViewSpannableString(new SpannableString(content));
        msg.setId(snapshot.getKey());
        msg.setUser(mUserSelf);
        msg.setMills((Long)newNote.get(getString(R.string.note_create_at)));
        return msg;
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

        mFireBaseData = FireBaseService.getInstance();
        String userNodeName = mAccountBean.getUid();
        mUserNode = mFireBaseData
                .child(getString(R.string.users))
                .child(userNodeName);

        mNotesNode = mUserNode.child(getString(R.string.notes));

        mConnectedNode = mFireBaseData
                .child(getString(R.string.info))
                .child(getString(R.string.connected));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.listview_layout, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        mAdapter = new NoteListAdapter(this, listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageBean message = (MessageBean) mAdapter.getItem(position);
                Intent intent = WriteNoteActivity.startForEdit(mAccountBean, message);
                startActivity(intent);
            }
        });

        // setup FireBase callback
        mNotesNode.addChildEventListener(notesChangeListener);
        if (mConnectionStateListener == null) {
            mConnectionStateListener = new ConnectionStateListener(view.findViewById(R.id.progressbar));
        }
        mConnectedNode.addValueEventListener(mConnectionStateListener);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // remove FireBase callback
        mNotesNode.removeEventListener(notesChangeListener);
        mConnectedNode.removeEventListener(mConnectionStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setIcon(R.drawable.ic_menu_home);
        parentActivity = ((MainNotesActivity) getActivity());
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
            MessageBean msg = noteDataSnapshotToMessageBean(dataSnapshot);
            mAdapter.addMessage(msg);

            if (GlobalContext.getInstance().getCurrentRunningActivity() == parentActivity &&
                    parentActivity.getMenuFragment().getCurrentIndex() == LeftMenuFragment.HOME_INDEX ) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            MessageBean msg = noteDataSnapshotToMessageBean(dataSnapshot);
            mAdapter.changeMessage(msg);
            if (GlobalContext.getInstance().getCurrentRunningActivity() == parentActivity &&
                    parentActivity.getMenuFragment().getCurrentIndex() == LeftMenuFragment.HOME_INDEX ) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            MessageBean msg = noteDataSnapshotToMessageBean(dataSnapshot);
            mAdapter.removeMessage(msg);
            if (GlobalContext.getInstance().getCurrentRunningActivity() == parentActivity &&
                    parentActivity.getMenuFragment().getCurrentIndex() == LeftMenuFragment.HOME_INDEX ) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }

    private class ConnectionStateListener implements ValueEventListener {
        private View progressBar;

        public ConnectionStateListener (View progressBar) {
            this.progressBar = progressBar;
        }
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            if (connected) {
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
