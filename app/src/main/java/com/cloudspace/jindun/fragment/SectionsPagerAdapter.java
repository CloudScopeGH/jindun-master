package com.cloudspace.jindun.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.cloudspace.jindun.R;
import com.cloudspace.jindun.UCAPIApp;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<Fragment> fragmentSparseArray = new SparseArray(3);

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0: {
                Fragment fragment = fragmentSparseArray.get(position);
                if (fragment == null) {
                    fragment = DianhuaFragment.newInstance(position);
                    fragmentSparseArray.put(position, fragment);
                }
                return fragment;
            }
            case 1: {
                Fragment fragment = fragmentSparseArray.get(position);
                if (fragment == null) {
                    fragment = DuijiangFragment.newInstance(position);
                    fragmentSparseArray.put(position, fragment);
                }
                return fragment;
            }
            case 2: {
                Fragment fragment = fragmentSparseArray.get(position);
                if (fragment == null) {
                    fragment = WoyaoFragment.newInstance(position);
                    fragmentSparseArray.put(position, fragment);
                }
                return fragment;
            }
            case 3: {
                Fragment fragment = fragmentSparseArray.get(position);
                if (fragment == null) {
                    fragment = XiaoxiFragment.newInstance(position);
                    fragmentSparseArray.put(position, fragment);
                }
                return fragment;
            }
            case 4: {
                Fragment fragment = fragmentSparseArray.get(position);
                if (fragment == null) {
                    fragment = DingweiFragment.newInstance(position);
                    fragmentSparseArray.put(position, fragment);
                }
                return fragment;
            }
            default:
                // error
                return null;
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return UCAPIApp.getInstance().getString(R.string.tab_1_title);
            case 1:
                return UCAPIApp.getInstance().getString(R.string.tab_2_title);
            case 2:
                return UCAPIApp.getInstance().getString(R.string.tab_3_title);
            case 3:
                return UCAPIApp.getInstance().getString(R.string.tab_1_title);
            case 4:
                return UCAPIApp.getInstance().getString(R.string.tab_2_title);
            default:
                break;
        }
        return null;
    }

}
