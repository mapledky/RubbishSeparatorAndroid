package com.maple.rubbishseparator.util;


//该类用于控制自定义view
public class ViewControl {
    private static Long lasttime = Long.valueOf("0");

    //该方法用于防止view的重复点击
    public static boolean avoidRetouch(){
        Long now = System.currentTimeMillis();
        if((now-lasttime)<500){
            lasttime = now;
            return false;
        } else {
            lasttime = now;
            return true;
        }
    }
}
