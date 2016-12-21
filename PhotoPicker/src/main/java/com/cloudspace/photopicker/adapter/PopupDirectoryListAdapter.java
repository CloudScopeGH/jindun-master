package com.cloudspace.photopicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import com.cloudspace.photopicker.R;
import com.cloudspace.photopicker.entity.PhotoDirectory;

public class PopupDirectoryListAdapter extends BaseAdapter {

  private Context context;

  private List<PhotoDirectory> directories = new ArrayList<>();

  private LayoutInflater mLayoutInflater;


  public PopupDirectoryListAdapter(Context context, List<PhotoDirectory> directories) {
    this.context = context;
    this.directories = directories;

    mLayoutInflater = LayoutInflater.from(context);
  }


  @Override public int getCount() {
    return directories.size();
  }


  @Override public PhotoDirectory getItem(int position) {
    return directories.get(position);
  }


  @Override public long getItemId(int position) {
    return directories.get(position).hashCode();
  }


  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = mLayoutInflater.inflate(R.layout.photopicker_item_directory, parent, false);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.bindData(directories.get(position));

    return convertView;
  }


  private class ViewHolder {

    public ImageView ivCover;
    public TextView tvName;
    public TextView tvDirCount;

    public ViewHolder(View rootView) {
      ivCover = (ImageView) rootView.findViewById(R.id.iv_dir_cover);
      tvName  = (TextView)  rootView.findViewById(R.id.tv_dir_name);
      tvDirCount = (TextView) rootView.findViewById(R.id.tv_dir_count);
    }

    public void bindData(PhotoDirectory directory) {
      Glide.with(context)
          .load(directory.getCoverPath())
          .thumbnail(0.1f)
          .into(ivCover);
      tvName.setText(directory.getName());
      if (directory.getPhotos().size() > 0) {
        tvDirCount.setText(String.format("(%s)", directory.getPhotos().size()));
      } else {
        tvDirCount.setText("");
      }
    }
  }

}
