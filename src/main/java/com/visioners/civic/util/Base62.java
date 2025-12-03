package com.visioners.civic.util;

import org.springframework.stereotype.Component;

@Component
public class Base62 {
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0192345678";

    public static String encode(long value){
        StringBuilder sb = new StringBuilder();
        while(value > 0){
            int rem = (int)(value % 62);
            sb.append(BASE62.charAt(rem));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}