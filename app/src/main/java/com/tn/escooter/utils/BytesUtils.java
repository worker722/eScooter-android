package com.tn.escooter.utils;

import java.util.Vector;

public class BytesUtils {
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() < 2) {
            return null;
        }
        String bString = "";
        for (int i = 0; i < hexString.length(); i++) {
            String tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString = bString + tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static char[] string2Char(String s) {
        char[] charReturn = new char[(s.length() / 2)];
        for (int i = 0; i < s.length() / 2; i++) {
            charReturn[i] = (char) Integer.parseInt(s.substring(i * 2, (i + 1) * 2), 16);
        }
        return charReturn;
    }

    public static int getLength(float f, float f2, float f3, float f4) {
        return (int) Math.sqrt(Math.pow((double) (f - f3), 2.0d) + Math.pow((double) (f2 - f4), 2.0d));
    }
    private BytesUtils() {
    }
    private static final String hexString = "0123456789ABCDEF";

    public static byte[] hexStringToBytes(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        String upperCase = str.toUpperCase();
        int length = upperCase.length() / 2;
        char[] charArray = upperCase.toCharArray();
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) (charToByte(charArray[i2 + 1]) | (charToByte(charArray[i2]) << 4));
        }
        return bArr;
    }

    private static byte charToByte(char c) {
        return (byte) hexString.indexOf(c);
    }
//    public static String bytesToHexString(byte[] src) {
//        StringBuilder stringBuilder = new StringBuilder("");
//        if (src == null || src.length <= 0) {
//            return null;
//        }
//        for (byte b : src) {
//            String hv = Integer.toHexString(b & 255);
//            if (hv.length() < 2) {
//                stringBuilder.append(0);
//            }
//            stringBuilder.append(hv);
//        }
//        return stringBuilder.toString();
//    }
    private static final char[] HEX_ARRAY = hexString.toCharArray();
    public static String bytesToHexString(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int b = bArr[i] & 255;
            int i2 = i * 2;
            char[] cArr2 = HEX_ARRAY;
            cArr[i2] = cArr2[b >>> 4];
            cArr[i2 + 1] = cArr2[b & 15];
        }
        return new String(cArr);
    }
    public static byte char2hex(char ch) {
        if (ch >= 'a' && ch <= 'f') {
            return (byte) (ch - 'W');
        }
        if (ch >= 'A' && ch <= 'F') {
            return (byte) (ch - '7');
        }
        if (ch < '0' || ch > '9') {
            return 0;
        }
        return (byte) (ch - '0');
    }

    public static char hex2char(byte byt) {
        if (byt <= 9) {
            return (char) (byt + 48);
        }
        if (byt <= 15) {
            return (char) (byt + 55);
        }
        return (char) byt;
    }

    public static char byte2char(byte byt) {
        if (byt < 32 || byt > 126) {
            return '*';
        }
        return (char) byt;
    }

    public static String byteArrayToString(byte[] a) {
        if (a == null) {
            return "[null]";
        }
        if (a.length == 0) {
            return "[empty]";
        }
        String result = "";
        for (byte byteToString : a) {
            result = result + byteToString(byteToString) + " ";
        }
        return result;
    }

    public static byte[] appendToByteArray(byte[] first, byte[] second) {
        int secondLength;
        if (second != null) {
            secondLength = second.length;
        } else {
            secondLength = 0;
        }
        return appendToByteArray(first, second, 0, secondLength);
    }

    public static byte[] appendToByteArray(byte[] first, byte[] second, int offset, int length) {
        int firstLength;
        if (second == null || second.length == 0) {
            return first;
        }
        if (first != null) {
            firstLength = first.length;
        } else {
            firstLength = 0;
        }
        if (length < 0 || offset < 0 || second.length < length + offset) {
            throw new ArrayIndexOutOfBoundsException();
        }
        byte[] result = new byte[(firstLength + length)];
        if (firstLength > 0) {
            System.arraycopy(first, 0, result, 0, firstLength);
        }
        System.arraycopy(second, offset, result, firstLength, length);
        return result;
    }

    public static byte[] subByteArray(byte[] array, int offset, int length) {
        return appendToByteArray(null, array, offset, length);
    }

    public static String byteToString(int b) {
        String s;
        String s2 = Integer.toHexString(b);
        if (s2.length() == 1) {
            s = "0" + s2;
        } else {
            s = s2.substring(s2.length() - 2);
        }
        return "0x" + s.toUpperCase();
    }
    public static String Bytes2HexString(byte[] bArr) {
        String str = "";
        for (byte b : bArr) {
            String hexString2 = Integer.toHexString(b & 255);
            if (hexString2.length() == 1) {
                StringBuilder sb = new StringBuilder();
                sb.append('0');
                sb.append(hexString2);
                hexString2 = sb.toString();
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(hexString2.toUpperCase());
            str = sb2.toString();
        }
        return str;
    }
    public static String BytesToString(byte[] bytes) {
        return BytesToString(bytes, 0, bytes.length);
    }

    public static String bytesToHexStringTwo(byte[] bArr, int i) {
        StringBuilder sb = new StringBuilder(bArr.length);
        for (int i2 = 0; i2 < i; i2++) {
            String hexString2 = Integer.toHexString(bArr[i2] & 255);
            if (hexString2.length() < 2) {
                sb.append(0);
            }
            sb.append(hexString2.toUpperCase());
        }
        return sb.toString();
    }
    public static String BytesToString(byte[] bytes, int offset, int length) {
        String str = "";
        if (bytes.length < length) {
            length = bytes.length;
        }
        for (int i = offset; i < length; i++) {
            str = (str + String.valueOf(hex2char((byte) ((bytes[i] & 240) >> 4)))) + String.valueOf(hex2char((byte) (bytes[i] & 15)));
        }
        return str;
    }

    public static byte[] StringToBytes(String str) {
        return StringToBytes(str, 0, str.length());
    }

    public static byte[] StringToBytes(String str, int offset, int length) {
        int i = (offset + length) - 1;
        byte[] bytes = new byte[0];
        if (str.length() <= i) {
            int length2 = str.length() - offset;
        }
        while (i >= offset) {
            if (i == offset) {
                bytes = appendToByteArray(new byte[]{(byte) (char2hex(str.charAt(i)) & 15)}, bytes);
            } else {
                bytes = appendToByteArray(new byte[]{(byte) ((char2hex(str.charAt(i)) & 15) | ((char2hex(str.charAt(i - 1)) << 4) & -16))}, bytes);
            }
            i -= 2;
        }
        return bytes;
    }

    public static byte[] addPadding(byte[] data) {
        int dataLength = data == null ? 0 : data.length;
        if (dataLength >= 23) {
            return appendToByteArray(new byte[]{0}, data);
        } else if (dataLength == 22) {
            return appendToByteArray(new byte[]{2}, data);
        } else {
            byte[] padding = new byte[(23 - dataLength)];
            padding[0] = 1;
            padding[(23 - dataLength) - 1] = 1;
            return appendToByteArray(padding, data);
        }
    }

    public static byte[] removePadding(byte[] data) {
        int dataLength = data == null ? 0 : data.length;
        if (data[0] == 0 || data[0] == 2) {
            return subByteArray(data, 1, dataLength - 1);
        }
        int i = 1;
        while (data[i] == 0) {
            i++;
        }
        return subByteArray(data, i + 1, (dataLength - i) - 1);
    }

    public static boolean isExpectedBlock(byte chainingIndicator, byte expectedBlock) {
        if ((chainingIndicator & 2) == 2) {
            if (expectedBlock == 1) {
                return true;
            }
            return false;
        } else if (expectedBlock != 0) {
            return false;
        } else {
            return true;
        }
    }

    public static Vector dataToBlockVector(byte[] data, int blockSize) {
        return dataToBlockVector(data, blockSize, true, true);
    }

    public static Vector dataToBlockVector(byte[] data, int blockSize, boolean chainingIndicator, boolean addBlockNumbers) {
        boolean z;
        int blkSize = 0;
        int blkSize2 = 0;
        int blkDataSize;
        Vector v = new Vector();
        int dataPointer = 0;
        int dataLength = data == null ? 0 : data.length;
        if (dataLength == 0 && chainingIndicator) {
            v.addElement(new byte[]{0});
        } else if (dataLength == 0) {
            v.addElement(new byte[0]);
        } else {
            if (blockSize < 2) {
                z = true;
            } else {
                z = false;
            }
            if (z && chainingIndicator) {
                throw new IllegalArgumentException("block size should be >= 2 when using the chaining indicator");
            } else if (blockSize < 1) {
                throw new IllegalArgumentException("block size should be >= 1");
            } else {
                while (dataLength > 0) {
                    if (chainingIndicator) {
                        blkSize2 = dataLength >= blockSize ? blockSize : dataLength + 1;
                        blkDataSize = blkSize2 - 1;
                    } else {
                        if (dataLength >= blockSize) {
                            blkSize = blockSize;
                        } else {
                            blkSize = dataLength;
                        }
                        blkDataSize = blkSize2;
                    }
                    byte[] blk = new byte[blkSize2];
                    System.arraycopy(data, dataPointer, blk, blkSize2 - blkDataSize, blkDataSize);
                    dataPointer += blkDataSize;
                    dataLength -= blkDataSize;
                    if (chainingIndicator) {
                        blk[0] = 1;
                    }
                    v.addElement(blk);
                }
                if (chainingIndicator) {
                    ((byte[]) v.lastElement())[0] = 0;
                    if (addBlockNumbers) {
                        for (int i = 0; i < v.size(); i++) {
                            ((byte[]) v.elementAt(i))[0] = (byte) (((byte[]) v.elementAt(i))[0] | ((i % 2) << 1));
                        }
                    }
                }
            }
        }
        return v;
    }

    public static byte[] blockVectorToData(Vector bv) {
        return blockVectorToData(bv, true);
    }

    public static byte[] blockVectorToData(Vector bv, boolean chainingIndicator) {
        if (bv == null || bv.size() == 0) {
            throw new IllegalArgumentException("invalid block vector");
        }
        byte[] data = new byte[0];
        for (int i = 0; i < bv.size(); i++) {
            byte[] block = (byte[]) bv.elementAt(i);
            if (chainingIndicator) {
                block = subByteArray(block, 1, block.length - 1);
            }
            data = appendToByteArray(data, block);
        }
        return data;
    }

    public static int getBlockNumber(byte[] data) {
        if (data == null || data.length == 0) {
            return -1;
        }
        if ((data[0] & 2) == 2) {
            return 1;
        }
        return 0;
    }

    public static boolean isChained(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        if ((data[0] & 1) != 1) {
            return false;
        }
        return true;
    }

    public static boolean isEndBlock(byte[] data) {
        if (data == null || data.length == 0 || (data[0] & 4) != 4) {
            return false;
        }
        return true;
    }

    public static boolean isEmptyBlock(byte[] data) {
        if (data == null || data.length == 0 || (data[0] & 8) != 8) {
            return false;
        }
        return true;
    }

    public static boolean isNullBlock(byte[] data) {
        return data == null || data.length == 0;
    }

    public static boolean isDummyBlock(byte[] data) {
        if (data == null || data.length == 0 || (data[0] & 136) != 136) {
            return false;
        }
        return true;
    }

    public static boolean arrayCompare(byte[] a, byte[] a2) {
        if (a == a2) {
            return true;
        }
        if (a == null || a2 == null) {
            return false;
        }
        int length = a.length;
        if (a2.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (a[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value += (b[i + offset] & 255) << ((3 - i) * 8);
        }
        return value;
    }
}
