package com.cloudspace.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudspace.photopicker.entity.Photo;
import com.cloudspace.photopicker.event.OnItemCheckListener;
import com.cloudspace.photopicker.fragment.ImagePagerFragment;
import com.cloudspace.photopicker.fragment.PhotoPickerFragment;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class PhotoPickerActivity extends AppCompatActivity implements View.OnClickListener , PhotoPickerFragment.OnPhotoBrowsered {

  private PhotoPickerFragment pickerFragment;
  private ImagePagerFragment imagePagerFragment;

  public final static String EXTRA_MAX_COUNT     = "MAX_COUNT";
  public final static String EXTRA_SHOW_CAMERA   = "SHOW_CAMERA";
  public final static String EXTRA_SHOW_TITLE   = "SHOW_TITLE";
  public final static String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";

  private TextView menuDoneItem;

  public final static int DEFAULT_MAX_COUNT = 9;

  private int maxCount = DEFAULT_MAX_COUNT;

  /** to prevent multiple calls to inflate menu */
  private boolean menuIsInflated = false;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.photopicker_activity_photo_picker);

    RelativeLayout mToolbar = (RelativeLayout) findViewById(R.id.act_titlebar);

    String title = getIntent().getStringExtra(EXTRA_SHOW_TITLE);
    if (TextUtils.isEmpty(title)) {
      ((TextView)mToolbar.findViewById(R.id.photopicker_titleview_title)).setText(R.string.images);
    } else {
      ((TextView)mToolbar.findViewById(R.id.photopicker_titleview_title)).setText(title);
    }

    menuDoneItem = (TextView)mToolbar.findViewById(R.id.photopicker_titleview_right);

    mToolbar.findViewById(R.id.photopicker_titleview_back).setOnClickListener(this);
    menuDoneItem.setOnClickListener(this);

    maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
    boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);

    pickerFragment =
        (PhotoPickerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPickerFragment);

    initButtons(false);

    pickerFragment.setPhotoBrowseredListener(this);

    pickerFragment.getPhotoGridAdapter().setShowCamera(showCamera);

    pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
      @Override
      public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

        int total = selectedItemCount + (isCheck ? -1 : 1);

        initButtons(total > 0);

        if (maxCount <= 1) {
          List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
          if (!photos.contains(photo)) {
            photos.clear();
            pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
          }
          return true;
        }

        if (total > maxCount) {
          Toast.makeText(getApplicationContext(), getString(R.string.over_max_count_tips, maxCount),
                  LENGTH_LONG).show();
          return false;
        }
        menuDoneItem.setText(getString(R.string.done_with_count, total, maxCount));
        return true;
      }
    });
  }

  private void initButtons(boolean isEnable) {
    menuDoneItem.setEnabled(isEnable);
    pickerFragment.setEnablePreview(isEnable);
  }


  /**
   * Overriding this method allows us to run our exit animation first, then exiting
   * the activity when it complete.
   */
  @Override public void onBackPressed() {
    if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
      imagePagerFragment.runExitAnimation(new Runnable() {
        public void run() {
          if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
          }
          onPhotoBrowserAfter();
        }
      });
    } else {
      super.onBackPressed();
    }
  }


  public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
    this.imagePagerFragment = imagePagerFragment;
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, this.imagePagerFragment)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.photopicker_titleview_back) {
      super.onBackPressed();
    } else if (v.getId() == R.id.photopicker_titleview_right) {
      Intent intent = new Intent();
      ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
      // 如果没选择任何图片,发送当前显示的
      if (selectedPhotos.size() == 0) {
        int currentItem = imagePagerFragment.getCurrentItem();
        ArrayList<String> paths = imagePagerFragment.getPaths();
        String str = paths.get(currentItem);
        if (!TextUtils.isEmpty(str)) {
          selectedPhotos.add(str);
        }
      }
      intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
      setResult(RESULT_OK, intent);
      finish();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
      return true;
    }
    if (item.getItemId() == R.id.done) {
      Intent intent = new Intent();
      ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
      intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
      setResult(RESULT_OK, intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPhotoBrowser() {
    menuDoneItem.setEnabled(true);
  }

  public void onPhotoBrowserAfter() {
    ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
    int size = selectedPhotos.size();
    if (size > 0) {
      initButtons(true);
    } else {
      initButtons(false);
    }
  }

  public PhotoPickerActivity getActivity() {
    return this;
  }
}
