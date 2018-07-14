package com.zitiger.plugin.converter.util;

public class StringUtils {

    private StringUtils(){
        //
    }

    public  static  String toCamelCase(String str){
        if(null == str || str.length() ==0){
            return str;
        }
        else if(str.length() == 1){
            return  str.toLowerCase();
        }

        return str.substring(0, 1).toLowerCase() + str.substring(1);

    }
}
