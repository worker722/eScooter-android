package com.tn.escooter.utils;

public class YiHuoUtils {
    public static String xor(String[] strHex) {
        String result = "";
        String result2 = "";
        String anotherBinary = Integer.toBinaryString(Integer.valueOf(strHex[0], 16).intValue());
        for (int i = 1; i < strHex.length; i++) {
            String thisBinary = Integer.toBinaryString(Integer.valueOf(strHex[i], 16).intValue());
            if (anotherBinary.length() != 8) {
                for (int j = anotherBinary.length(); j < 8; j++) {
                    anotherBinary = "0" + anotherBinary;
                }
            }
            if (thisBinary.length() != 8) {
                for (int j2 = thisBinary.length(); j2 < 8; j2++) {
                    thisBinary = "0" + thisBinary;
                }
            }
            for (int k = 0; k < anotherBinary.length(); k++) {
                if (thisBinary.charAt(k) == anotherBinary.charAt(k)) {
                    result = result + "0";
                } else {
                    result = result + "1";
                }
            }
            anotherBinary = result;
            result2 = "";
        }
        return Integer.toHexString(Integer.valueOf(anotherBinary, 2).intValue());
    }
}
