package com.cloudspace.photopicker.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cloudspace.photopicker.PhotoPickerActivity;
import com.cloudspace.photopicker.R;
import com.cloudspace.photopicker.adapter.PhotoGridAdapter;
import com.cloudspace.photopicker.adapter.PopupDirectoryListAdapter;
import com.cloudspace.photopicker.entity.Photo;
import com.cloudspace.photopicker.entity.PhotoDirectory;
import com.cloudspace.photopicker.event.OnPhotoClickListener;
import com.cloudspace.photopicker.utils.ImageCaptureManager;
import com.cloudspace.photopicker.utils.MediaStoreHelper;

import static android.app.Activity.RESULT_OK;
import static com.cloudspace.photopicker.utils.MediaStoreHelper.INDEX_ALL_PHOTOS;

public class PhotoPickerFragment extends Fragment {

  private ImageCaptureManager captureManager;
  private PhotoGridAdapter photoGridAdapter;

  private OnPhotoBrowsered photoBrowseredListener;
  private Button btPreview;

  private PopupDirectoryListAdapter listAdapter;
  private List<PhotoDirectory> directories;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    directories = new ArrayList<>();

    captureManager = new ImageCaptureManager(getActivity());


    MediaStoreHelper.getPhotoDirs(getActivity(),
        new MediaStoreHelper.PhotosResultCallback() {
          @Override public void onResultCallback(List<PhotoDirectory> directories) {
            PhotoPickerFragment.this.directories.clear();
            PhotoPickerFragment.this.directories.addAll(directories);
            photoGridAdapter.notifyDataSetChanged();
            listAdapter.notifyDataSetChanged();
          }
        });
  }


  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    setRetainInstance(true);

    final View rootView = inflater.inflate(R.layout.photopicker_fragment_photo_picker, container, false);

    photoGridAdapter = new PhotoGridAdapter(getActivity(), directories);
    listAdapter  = new PopupDirectoryListAdapter(getActivity(), directories);


    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
    StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL);
    layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(photoGridAdapter);
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    final Button btSwitchDirectory = (Button) rootView.findViewById(R.id.button);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      btSwitchDirectory.setBackgroundResource(R.drawable.photopicker_allalbum_bg);
    }
    btPreview = (Button) rootView.findViewById(R.id.button_preview);
    btPreview.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int index = 0;

        browserPhoto(v,photoGridAdapter.getSelectedPhotoPaths(),1, index);
        }
    });

    final ListPopupWindow listPopupWindow = new ListPopupWindow(getActivity());
    listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
    listPopupWindow.setAnchorView(btSwitchDirectory);
    listPopupWindow.setAdapter(listAdapter);
    listPopupWindow.setModal(true);
    listPopupWindow.setDropDownGravity(Gravity.BOTTOM);
    listPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);

    listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listPopupWindow.dismiss();

        PhotoDirectory directory = directories.get(position);

        btSwitchDirectory.setText(directory.getName());

        photoGridAdapter.setCurrentDirectoryIndex(position);
        photoGridAdapter.notifyDataSetChanged();
      }
    });

    photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
      @Override public void onClick(View v, int position, boolean showCamera) {
        final int index = showCamera ? position - 1 : position;

        browserPhoto(v,photoGridAdapter.getCurrentPhotoPaths(), 0, index);
      }
    });

    photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        try {
          Intent intent = captureManager.dispatchTakePictureIntent();
          startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    btSwitchDirectory.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {

        if (listPopupWindow.isShowing()) {
          listPopupWindow.dismiss();
        } else if (!getActivity().isFinishing()) {
          listPopupWindow.setHeight(Math.round(rootView.getHeight() * 0.8f));
          listPopupWindow.show();
        }

      }
    });

    return rootView;
  }

  public void browserPhoto(View v, List<String> photos ,int path_type, int index) {
    int [] screenLocation = new int[2];
    v.getLocationOnScreen(screenLocation);
    ImagePagerFragment imagePagerFragment =
            ImagePagerFragment.newInstance(photoGridAdapter, photos, path_type, index, screenLocation,
                    v.getWidth(), v.getHeight());

    ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);

    if (photoBrowseredListener != null) {
      photoBrowseredListener.onPhotoBrowser();
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
      captureManager.galleryAddPic();
      if (directories.size() > 0) {
        String path = captureManager.getCurrentPhotoPath();
        PhotoDirectory directory = directories.get(INDEX_ALL_PHOTOS);
        directory.getPhotos().add(INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
        directory.setCoverPath(path);
        photoGridAdapter.notifyDataSetChanged();
      }
    }
  }

  public void setEnablePreview(boolean enablePreview) {
    btPreview.setEnabled(enablePreview);
  }

  public PhotoGridAdapter getPhotoGridAdapter() {
    return photoGridAdapter;
  }


  @Override public void onSaveInstanceState(Bundle outState) {
    captureManager.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }


  @Override public void onViewStateRestored(Bundle savedInstanceState) {
    captureManager.onRestoreInstanceState(savedInstanceState);
    super.onViewStateRestored(savedInstanceState);
  }

  public ArrayList<String> getSelectedPhotoPaths() {
    return photoGridAdapter.getSelectedPhotoPaths();
  }

  public void setPhotoBrowseredListener(OnPhotoBrowsered listener) {
    photoBrowseredListener = listener;
  }

  public static interface OnPhotoBrowsered {
    public void onPhotoBrowser();
  }

}
