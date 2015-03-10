package io.github.jason1114.lovenote.main;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.file.FileLocationMethod;
import io.github.jason1114.lovenote.firebase.FireBaseService;
import io.github.jason1114.lovenote.ui.AbstractAppActivity;
import io.github.jason1114.lovenote.ui.CheatSheet;
import io.github.jason1114.lovenote.ui.ClearContentDialog;
import io.github.jason1114.lovenote.ui.KeyboardControlEditText;
import io.github.jason1114.lovenote.utils.GlobalContext;
import io.github.jason1114.lovenote.utils.ImageUtility;
import io.github.jason1114.lovenote.utils.TextNumWatcher;
import io.github.jason1114.lovenote.utils.Utility;
import io.github.jason1114.lovenote.utils.ViewUtility;

/**
 * User: qii
 * Date: 12-7-29
 */
public class WriteNoteActivity extends AbstractAppActivity
        implements DialogInterface.OnClickListener,
        ClearContentDialog.IClear {

    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;
    private static final int PIC_RESULT_KK = 2;
    public static final int AT_USER = 3;

    public static final String ACTION_SEND_FAILED = "io.github.jason1114.lovenote.SEND_FAILED";

    private AccountBean accountBean;
    protected String token = "";

    private MessageBean oldMessage;

    private String picPath = "";
    private Uri imageFileUri = null;


    private ImageView preview = null;
    private KeyboardControlEditText content = null;
    private RelativeLayout container = null;


    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = new Intent(GlobalContext.getInstance(), WriteNoteActivity.class);
        intent.putExtra("token", accountBean.getAccessToken());
        intent.putExtra("account", accountBean);
        return intent;
    }

    public static Intent startForEdit(AccountBean accountBean, MessageBean message) {
        Intent intent = new Intent(GlobalContext.getInstance(),WriteNoteActivity.class);
        intent.putExtra("token", accountBean.getAccessToken());
        intent.putExtra("account", accountBean);
        intent.putExtra("message", message);
        intent.putExtra("content", message.getListViewSpannableString().toString());
        return intent;
    }

    private void handleFailedOperation(Intent intent) {
        accountBean = intent.getParcelableExtra("account");
        token = accountBean.getAccessToken();
        getActionBar().setSubtitle(accountBean.getUserNick());
        String stringExtra = intent.getStringExtra("content");
        content.setText(stringExtra);
        String failedReason = intent.getStringExtra("failedReason");
        content.setError(failedReason);
        picPath = intent.getStringExtra("picPath");
        if (!TextUtils.isEmpty(picPath)) {
            enablePicture();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                Uri lastUri = Utility.getLatestCameraPicture(WriteNoteActivity.this);
                if (lastUri != null) {
                    imageFileUri = lastUri;
                    createTmpUploadFileFromUri();
                    return;
                }

                Toast.makeText(WriteNoteActivity.this,
                        getString(R.string.dont_have_the_last_picture), Toast.LENGTH_SHORT).show();

                break;
            case 1:
                imageFileUri = getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                new ContentValues());
                if (imageFileUri != null) {
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                    if (Utility.isIntentSafe(WriteNoteActivity.this, i)) {
                        startActivityForResult(i, CAMERA_RESULT);
                    } else {
                        Toast.makeText(WriteNoteActivity.this,
                                getString(R.string.dont_have_camera_app), Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(WriteNoteActivity.this, getString(R.string.cant_insert_album),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (Utility.isKK()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    /**
                     * not all 4.4 version devices have system app which accept Intent.ACTION_OPEN_DOCUMENT
                     */
                    if (Utility.isIntentSafe(WriteNoteActivity.this, intent)) {
                        startActivityForResult(intent, PIC_RESULT_KK);
                    } else {
                        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(choosePictureIntent, PIC_RESULT);
                    }
                } else {
                    Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(choosePictureIntent, PIC_RESULT);
                }
                break;
        }
    }

    private void createTmpUploadFileFromUri() {
//        ConvertUriToCachePathAsyncTaskFragment fragment = ConvertUriToCachePathAsyncTaskFragment
//                .newInstance(imageFileUri);
//        getSupportFragmentManager().beginTransaction().add(fragment, "").commit();
    }

    public void picConvertSucceedKK(String path) {
        if (TextUtils.isEmpty(content.getText().toString())) {
//            content.setText(getString(R.string.share_pic));
            content.setSelection(content.getText().toString().length());
        }

        picPath = path;
        enablePicture();
    }

    private void enablePicture() {
        Bitmap bitmap = ImageUtility.getWriteWeiboPictureThumblr(picPath);
        if (bitmap != null) {
            ((ImageButton) findViewById(R.id.menu_add_pic)).setImageBitmap(bitmap);
        }
        bitmap = ImageUtility.decodeBitmapFromSDCard(picPath, Utility.getScreenWidth(),
                Utility.getScreenHeight());
        if (bitmap != null) {
            preview.setVisibility(View.VISIBLE);
            preview.setImageBitmap(bitmap);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_RESULT:
                    createTmpUploadFileFromUri();
                    break;
                case PIC_RESULT:
                case PIC_RESULT_KK:
                    imageFileUri = intent.getData();
                    createTmpUploadFileFromUri();
                    break;
                case AT_USER:
                    String name = intent.getStringExtra("name");
                    String ori = content.getText().toString();
                    int index = content.getSelectionStart();
                    StringBuilder stringBuilder = new StringBuilder(ori);
                    stringBuilder.insert(index, name);
                    content.setText(stringBuilder.toString());
                    content.setSelection(index + name.length());
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("picPath", picPath);
        outState.putParcelable("imageFileUri", imageFileUri);
        outState.putParcelable("accountBean", accountBean);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            picPath = savedInstanceState.getString("picPath");
            if (!TextUtils.isEmpty(picPath)) {
                enablePicture();
            }

            imageFileUri = savedInstanceState.getParcelable("imageFileUri");
            accountBean = savedInstanceState.getParcelable("accountBean");
            token = accountBean.getAccessToken();

            getActionBar().setSubtitle(getAccount().getUserNick());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GlobalContext.getInstance().checkUserIsLogin()) {
            Toast.makeText(this, this.getString(R.string.share_failed_because_of_no_account),
                    Toast.LENGTH_SHORT).show();
            Intent intent = AccountActivity.newIntent();
            startActivity(intent);
            finish();
            return;
        }

        buildInterface();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (!TextUtils.isEmpty(action)) {
                // run this activity in another app, share directly
                if (action.equals(Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
                    if ("text/plain".equals(type)) {
                        handleSendText(intent);
                    } else if (type.startsWith("image/")) {
                        handleSendImage(intent);
                    }
                } else if (action.equals(WriteNoteActivity.ACTION_SEND_FAILED)) {
                    handleFailedOperation(intent);
                }
            } else {
                handleNormalOperation(intent);
            }
        }
    }



    private void handleNormalOperation(Intent intent) {
        accountBean = intent.getParcelableExtra("account");
        token = accountBean.getAccessToken();
        getActionBar().setSubtitle(accountBean.getUserNick());
        String contentStr = intent.getStringExtra("content");
        if (!TextUtils.isEmpty(contentStr)) {
            content.setText(contentStr + " ");
            content.setSelection(content.getText().toString().length());
        }
        oldMessage = intent.getParcelableExtra("message");
    }

    private void buildInterface() {
        setContentView(R.layout.writenoteactivity_layout);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.write_weibo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        int avatarWidth = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
        int avatarHeight = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);

        Bitmap bitmap = ImageUtility.getWriteWeiboRoundedCornerPic(
                GlobalContext.getInstance().getAccountBean().getInfo().getAvatar_large(),
                avatarWidth, avatarHeight, FileLocationMethod.avatar_large);
        if (bitmap == null) {
            bitmap = ImageUtility.getWriteWeiboRoundedCornerPic(
                    GlobalContext.getInstance().getAccountBean().getInfo().getProfile_image_url(),
                    avatarWidth, avatarHeight, FileLocationMethod.avatar_small);
        }
        if (bitmap != null) {
            actionBar.setIcon(new BitmapDrawable(getResources(), bitmap));
        }
        actionBar.setDisplayShowCustomEnabled(true);
        content = ((KeyboardControlEditText) findViewById(R.id.status_new_content));
        content.addTextChangedListener(
                new TextNumWatcher((TextView) findViewById(R.id.menu_send), content, this));
        content.setDrawingCacheEnabled(true);

        preview = ViewUtility.findViewById(this, R.id.status_image_preview);

        View.OnClickListener onClickListener = new BottomButtonClickListener();
        findViewById(R.id.menu_add_pic).setOnClickListener(onClickListener);
        findViewById(R.id.menu_send).setOnClickListener(onClickListener);


//        CheatSheet
//                .setup(WriteNoteActivity.this, findViewById(R.id.menu_add_pic), R.string.add_pic);
        CheatSheet.setup(WriteNoteActivity.this, findViewById(R.id.menu_send), R.string.send);
        container = (RelativeLayout) findViewById(R.id.container);
    }

    private void setUpDefaultAccountInfo() {
        AccountBean account = GlobalContext.getInstance().getAccountBean();
        if (account != null) {
            accountBean = account;
            token = account.getAccessToken();
            getActionBar().setSubtitle(account.getUserNick());
        }
    }

    private void handleSendText(Intent intent) {
        setUpDefaultAccountInfo();
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            content.setText(sharedText);
            content.setSelection(content.getText().toString().length());
        }
    }

    private void handleSendImage(Intent intent) {
        handleSendText(intent);

        setUpDefaultAccountInfo();

        Uri sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (sharedImageUri != null) {
            this.imageFileUri = sharedImageUri;
            createTmpUploadFileFromUri();
        }
    }

    private boolean canSend() {
        boolean haveContent = !TextUtils.isEmpty(content.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);


        if (haveContent && haveToken) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this,
                        getString(R.string.content_cant_be_empty_and_dont_have_account),
                        Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                content.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_statusnewactivity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                intent = MainNotesActivity.newIntent(getAccount());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case R.id.menu_clear:
                clearContentMenu();
                break;
        }
        return true;
    }


    protected void clearContentMenu() {
        ClearContentDialog dialog = new ClearContentDialog();
        dialog.show(getFragmentManager(), "");
    }

    public void clear() {
        content.setText("");
    }

    private void send() {
        String value = content.getText().toString();
        if (canSend()) {
            executeTask(value);
        }
    }

    private void addPic() {
//        SelectPictureDialog.newInstance().show(getFragmentManager(), "");
    }

    private void showPic() {
//        DeleteSelectedPictureDialog.newInstance().show(getFragmentManager(), "");
    }

    protected void executeTask(String contentString) {
        Firebase firebase = FireBaseService.getInstance();
        String userNodeName = accountBean.getUid();
        Firebase userNode = firebase.child(getString(R.string.users)).child(userNodeName);
        Firebase notesNode = userNode.child(getString(R.string.notes));
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.note_content), contentString);
        if (oldMessage == null) {
            data.put(getString(R.string.note_create_at), new Date().getTime());
            notesNode.push().setValue(data);
        } else {
            data.put(getString(R.string.note_create_at), oldMessage.getMills());
            notesNode.child(oldMessage.getId()).setValue(data);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public AccountBean getAccount() {
        return accountBean;
    }

    private class BottomButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_add_pic:
                    if (TextUtils.isEmpty(picPath)) {
                        addPic();
                    } else {
                        showPic();
                    }
                    break;

                case R.id.menu_send:
                    send();
                    break;
            }
        }
    }

}
