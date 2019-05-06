package com.redislabs.redisgraph;

/**
 * Utilities class
 */
public class Utils {

    /**
     *
     * @param str - a string
     * @return the input string surounded with quotation marks, if needed
     */
    public static String quoteString(String str){
        StringBuilder sb = new StringBuilder();
        if(str.charAt(0)!='"'){
            sb.append('"');
        }
        sb.append(str);
        if (str.charAt(str.length()-1)!= '"'){
            sb.append('"');
        }
        return sb.toString();
    }
}