package com.cloudspace.photopicker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.cloudspace.photopicker.PhotoPickerActivity;

public class PhotoPickerIntent extends Intent {

  private PhotoPickerIntent() {
  }

  private PhotoPickerIntent(Intent o) {
    super(o);
  }

  private PhotoPickerIntent(String action) {
    super(action);
  }

  private PhotoPickerIntent(String action, Uri uri) {
    super(action, uri);
  }

  private PhotoPickerIntent(Context packageContext, Class<?> cls) {
    super(packageContext, cls);
  }

  public PhotoPickerIntent(Context packageContext) {
    super(packageContext, PhotoPickerActivity.class);
  }

  public void setPhotoCount(int photoCount) {
    this.putExtra(PhotoPickerActivity.EXTRA_MAX_COUNT, photoCount);
  }

  public void setTitle(String title) {
    this.putExtra(PhotoPickerActivity.EXTRA_SHOW_TITLE, title);
  }

  public void setShowCamera(boolean showCamera) {
    this.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, showCamera);
  }

}
