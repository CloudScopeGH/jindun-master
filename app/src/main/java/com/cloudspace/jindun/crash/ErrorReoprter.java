package com.cloudspace.jindun.crash;

import com.cloudspace.jindun.module.Module;
import com.cloudspace.jindun.utils.Global;


public class ErrorReoprter implements Module<Global> {

    @Override
    public void initialize(Global box) {
    }

    @Override
    public void destroy() {

    }

    public static void report() {
        //TODO crash report


    }
}
