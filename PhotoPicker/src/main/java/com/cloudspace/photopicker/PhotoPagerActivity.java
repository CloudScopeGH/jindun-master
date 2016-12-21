package com.cloudspace.photopicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cloudspace.photopicker.fragment.ImagePagerFragment;

import java.util.List;

public class PhotoPagerActivity extends AppCompatActivity {

  private ImagePagerFragment pagerFragment;

  public final static String EXTRA_CURRENT_ITEM = "current_item";
  public final static String EXTRA_PHOTOS = "photos";

  private ActionBar actionBar;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(com.cloudspace.photopicker.R.layout.photopicker_activity_photo_pager);

    int currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);
    List<String> paths = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);

    pagerFragment =
        (ImagePagerFragment) getSupportFragmentManager().findFragmentById(com.cloudspace.photopicker.R.id.photoPagerFragment);
    pagerFragment.setPhotos(paths, currentItem);

    Toolbar mToolbar = (Toolbar) findViewById(com.cloudspace.photopicker.R.id.toolbar);
    setSupportActionBar(mToolbar);

    actionBar = getSupportActionBar();

    actionBar.setDisplayHomeAsUpEnabled(true);
    updateActionBarTitle();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      actionBar.setElevation(25);
    }


    pagerFragment.getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        updateActionBarTitle();
      }

      @Override public void onPageSelected(int i) {

      }

      @Override public void onPageScrollStateChanged(int i) {

      }
    });

  }


  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(com.cloudspace.photopicker.R.menu.photopicker_menu_preview, menu);
    return true;
  }


  @Override public void onBackPressed() {

    Intent intent = new Intent();
    intent.putExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS, pagerFragment.getPaths());
    setResult(RESULT_OK, intent);
    finish();

    super.onBackPressed();
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }

    if (item.getItemId() == com.cloudspace.photopicker.R.id.delete) {
      final int index = pagerFragment.getCurrentItem();

      final String deletedPath =  pagerFragment.getPaths().get(index);

      Snackbar snackbar = Snackbar.make(pagerFragment.getView(), com.cloudspace.photopicker.R.string.deleted_a_photo,
          Snackbar.LENGTH_LONG);

      if (pagerFragment.getPaths().size() <= 1) {

        // show confirm dialog
        new AlertDialog.Builder(this)
            .setTitle(com.cloudspace.photopicker.R.string.confirm_to_delete)
            .setPositiveButton(com.cloudspace.photopicker.R.string.yes, new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                setResult(RESULT_OK);
                finish();
              }
            })
            .setNegativeButton(com.cloudspace.photopicker.R.string.cancel, new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
              }
            })
            .show();

      } else {

        snackbar.show();

        pagerFragment.getPaths().remove(index);
        //pagerFragment.getViewPager().removeViewAt(index);
        pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
      }

      snackbar.setAction(com.cloudspace.photopicker.R.string.undo, new View.OnClickListener() {
        @Override public void onClick(View view) {
          if (pagerFragment.getPaths().size() > 0) {
            pagerFragment.getPaths().add(index, deletedPath);
          } else {
            pagerFragment.getPaths().add(deletedPath);
          }
          pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
          pagerFragment.getViewPager().setCurrentItem(index, true);
        }
      });

      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void updateActionBarTitle() {
    actionBar.setTitle(
        getString(com.cloudspace.photopicker.R.string.image_index, pagerFragment.getViewPager().getCurrentItem() + 1,
            pagerFragment.getPaths().size()));
  }
}
