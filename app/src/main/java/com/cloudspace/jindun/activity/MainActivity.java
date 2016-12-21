package com.cloudspace.jindun.activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.cloudspace.jindun.R;
import com.cloudspace.jindun.fragment.DianhuaFragment;
import com.cloudspace.jindun.fragment.DingweiFragment;
import com.cloudspace.jindun.fragment.DuijiangFragment;
import com.cloudspace.jindun.fragment.WoyaoFragment;
import com.cloudspace.jindun.fragment.XiaoxiFragment;
import com.nineoldandroids.view.ViewHelper;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener  {
	private RadioGroup mRadioGroup;
	private DianhuaFragment mDianhuaFragment;//电话
	private DingweiFragment mDingweiFragment;//定位
	private DuijiangFragment mDuijiangFragment;//对讲机
	private WoyaoFragment mWoyaoFragment ; //我要
	private XiaoxiFragment mXiaoxiFragment;//消息
	private DrawerLayout drawerLayout;//抽屉
	private static View fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jd_main_activity);
		initView();
		setListener();
		viewMonitor();
	}
	private void setListener() {
		mRadioGroup.setOnCheckedChangeListener(this);
	}
	private void initView() {
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		fragment= findViewById(R.id.id_left_menu);
		mRadioGroup = (RadioGroup) findViewById(R.id.rg_main1_radioGroup);
		select(2);
		((RadioButton) mRadioGroup.getChildAt(2)).setChecked(true);
	}
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int count = group.getChildCount();
		for (int i = 0; i < count; i++) {
			RadioButton button = (RadioButton) mRadioGroup.getChildAt(i);
			if (button.isChecked()) {
				select(i);
				button.setChecked(true);
				break;
			}
		}
	}
	private void select(int i) {
		//实例事物
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		//隐藏所有fragment
		hideFragment(transaction);
		switch (i) {
			case 0:
					if (mDianhuaFragment == null) {
						mDianhuaFragment = new DianhuaFragment();
						transaction.add(R.id.fl_main1_fragment, mDianhuaFragment);
					} else {
						transaction.show(mDianhuaFragment);
					}

				break;
			case 1:
				if (mDuijiangFragment == null) {
					mDuijiangFragment = new DuijiangFragment();
					transaction.add(R.id.fl_main1_fragment, mDuijiangFragment);
				} else {
					transaction.show(mDuijiangFragment);
				}
				break;
			case 2:
				if (mWoyaoFragment == null) {
					mWoyaoFragment = new WoyaoFragment();
					transaction.add(R.id.fl_main1_fragment, mWoyaoFragment);
				} else {
					transaction.show(mWoyaoFragment);
				}

				break;
			case 3:
				if (mXiaoxiFragment == null) {
					mXiaoxiFragment = new XiaoxiFragment();
					transaction.add(R.id.fl_main1_fragment, mXiaoxiFragment);
				} else {
					transaction.show(mXiaoxiFragment);
				}
				break;

			case 4:
				if (mDingweiFragment == null) {
					mDingweiFragment = new DingweiFragment();
					transaction.add(R.id.fl_main1_fragment, mDingweiFragment);
				} else {
					transaction.show(mDingweiFragment);
				}
				break;
		}
		transaction.commit();
	}
	private void hideFragment(FragmentTransaction transaction) {

			if (mDingweiFragment != null) {
				transaction.hide(mDingweiFragment);
			}
			if (mDianhuaFragment != null) {
				transaction.hide(mDianhuaFragment);
			}
			if (mXiaoxiFragment != null) {
				transaction.hide(mXiaoxiFragment);
			}
		if (mWoyaoFragment != null) {
			transaction.hide(mWoyaoFragment);
		}
		if (mDuijiangFragment != null) {
			transaction.hide(mDuijiangFragment);
		}
	}
	private void viewMonitor() {

		drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener()
		{
			@Override
			public void onDrawerStateChanged(int newState)
			{
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset)
			{
				View mContent = drawerLayout.getChildAt(0);
				View mMenu = drawerView;
				float scale = 1 - slideOffset;
				float rightScale = 0.8f + scale * 0.2f;

				if (drawerView.getTag().equals("LEFT"))
				{

					float leftScale = 1 - 0.3f * scale;

//                    ViewHelper.setScaleX(mMenu, leftScale);
//                    ViewHelper.setScaleY(mMenu, leftScale);
					ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
					ViewHelper.setTranslationX(mContent,
							mMenu.getMeasuredWidth() * (1 - scale));
					ViewHelper.setPivotX(mContent, 0);
					ViewHelper.setPivotY(mContent,
							mContent.getMeasuredHeight() / 2);
					mContent.invalidate();
//                    ViewHelper.setScaleX(mContent, rightScale);
//                    ViewHelper.setScaleY(mContent, rightScale);
				} else
				{
					ViewHelper.setTranslationX(mContent,
							-mMenu.getMeasuredWidth() * slideOffset);
					ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
					ViewHelper.setPivotY(mContent,
							mContent.getMeasuredHeight() / 2);
					mContent.invalidate();
//                    ViewHelper.setScaleX(mContent, rightScale);
//                    ViewHelper.setScaleY(mContent, rightScale);
				}

			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
			}

			@Override
			public void onDrawerClosed(View drawerView)
			{
				drawerLayout.setDrawerLockMode(
						DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
			}
		});

	}
}
