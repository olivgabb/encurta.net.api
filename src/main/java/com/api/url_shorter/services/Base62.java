package com.api.url_shorter.services;

public class Base62 {
	private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = CHARSET.length();
    

    private static String lastEncode(long value) {
        StringBuilder sb = new StringBuilder();

        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(CHARSET.charAt(remainder));
            value /= BASE;
        }

        return sb.reverse().toString();
    }
    
    public static String encode(long id) {
        long result = id * (871837l + 871836l);
        return lastEncode(result);
        
    }
    
    public static long decode(String code) {
       long decoded = lastDecode(code);
       return decoded / (871837l + 871836l);
    }

    private static long lastDecode(String str) {
        long result = 0;

        for (int i = 0; i < str.length(); i++) {
            result = result * BASE + CHARSET.indexOf(str.charAt(i));
        }

        return result;
    }
}
