package com.cloudspace.photopicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.cloudspace.photopicker.R;
import com.cloudspace.photopicker.entity.Photo;
import com.cloudspace.photopicker.entity.PhotoDirectory;
import com.cloudspace.photopicker.event.OnItemCheckListener;
import com.cloudspace.photopicker.event.OnPhotoClickListener;
import com.cloudspace.photopicker.utils.MediaStoreHelper;

public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder> {

  private LayoutInflater inflater;

  private Context mContext;

  private OnItemCheckListener onItemCheckListener    = null;
  private OnPhotoClickListener onPhotoClickListener  = null;
  private View.OnClickListener onCameraClickListener = null;

  public final static int ITEM_TYPE_CAMERA = 100;
  public final static int ITEM_TYPE_PHOTO  = 101;

  private boolean hasCamera = true;

  public PhotoGridAdapter(Context mContext, List<PhotoDirectory> photoDirectories) {
    this.photoDirectories = photoDirectories;
    this.mContext = mContext;
    inflater = LayoutInflater.from(mContext);
  }


  @Override public int getItemViewType(int position) {
    return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
  }

  public String getPhotoPath(int position) {
    return getCurrentPhotoPaths().get(position);
  }

  @Override public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = inflater.inflate(R.layout.photopicker_item_photo, parent, false);
    PhotoViewHolder holder = new PhotoViewHolder(itemView);
    if (viewType == ITEM_TYPE_CAMERA) {
      holder.vSelected.setVisibility(View.GONE);
      holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
      holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (onCameraClickListener != null) {
            onCameraClickListener.onClick(view);
          }
        }
      });
    }
    return holder;
  }


  @Override public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

    if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

      List<Photo> photos = getCurrentPhotos();
      final Photo photo;

      if (showCamera()) {
        photo = photos.get(position - 1);
      } else {
        photo = photos.get(position);
      }

      if (photo == null) {
        holder.ivPhoto.setImageResource(R.drawable.photopicker_ic_broken_image_black_48dp);
        return;
      }
      File file = new File(photo.getPath());
      if (file.exists()) {
        Glide.with(mContext)
                .load(file)
                .centerCrop()
                .thumbnail(0.1f)
                .placeholder(R.drawable.photopicker_ic_photo_black_48dp)
                .error(R.drawable.photopicker_ic_broken_image_black_48dp)
                .into(holder.ivPhoto);
        final boolean isChecked = isSelected(photo);

        holder.vSelected.setSelected(isChecked);
        holder.ivPhoto.setSelected(isChecked);

        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View view) {
            if (onPhotoClickListener != null) {
              onPhotoClickListener.onClick(view, position, showCamera());
            }
          }
        });
        holder.vSelected.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            onPhotoSelected(position, photo, isChecked);
          }
        });
      } else {
        holder.ivPhoto.setImageResource(R.drawable.photopicker_ic_broken_image_black_48dp);
      }
    } else {
      holder.ivPhoto.setImageResource(R.drawable.photopicker_camera);
    }
  }


  public void onPhotoSelected(int position, Photo photo, boolean isChecked) {
    boolean isEnable = true;

    if (onItemCheckListener != null) {
      isEnable = onItemCheckListener.OnItemCheck(position, photo, isChecked, getSelectedPhotos().size());
    }
    if (isEnable) {
      toggleSelection(photo);
      notifyItemChanged(position);
    }
  }

  @Override public int getItemCount() {
    int photosCount =
        photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
    if (showCamera()) {
      return photosCount + 1;
    }
    return photosCount;
  }


  public static class PhotoViewHolder extends RecyclerView.ViewHolder {
    private ImageView ivPhoto;
    private View vSelected;

    public PhotoViewHolder(View itemView) {
      super(itemView);
      ivPhoto   = (ImageView) itemView.findViewById(R.id.iv_photo);
      vSelected = itemView.findViewById(R.id.v_selected);
    }
  }


  public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
    this.onItemCheckListener = onItemCheckListener;
  }


  public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
    this.onPhotoClickListener = onPhotoClickListener;
  }


  public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
    this.onCameraClickListener = onCameraClickListener;
  }


  public ArrayList<String> getSelectedPhotoPaths() {
    ArrayList<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

    for (Photo photo : selectedPhotos) {
      selectedPhotoPaths.add(photo.getPath());
    }

    return selectedPhotoPaths;
  }


  public void setShowCamera(boolean hasCamera) {
    this.hasCamera = hasCamera;
  }


  public boolean showCamera() {
    return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
  }
}
