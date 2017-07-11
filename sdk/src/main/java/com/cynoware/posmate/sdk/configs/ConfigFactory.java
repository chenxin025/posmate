package com.cynoware.posmate.sdk.configs;

/**
 * Created by john on 2016/9/20.
 */

public class ConfigFactory {

    public static int mType = 0;


    public static BaseConfig createConfig(final int type){
        BaseConfig config = null;

        switch (type){
            case BaseConfig.SYS_DEV_VERSION_1:
                mType = BaseConfig.SYS_DEV_VERSION_1;
                config = new NP10Config();
                break;
            case BaseConfig.SYS_DEV_VERSION_2:
                mType = BaseConfig.SYS_DEV_VERSION_2;
                config = new NP11Config();
                break;
            default:
                break;
        }
        return  config;
    }

    public static BaseConfig getConfigType(){
        return createConfig(0);
    }
}
