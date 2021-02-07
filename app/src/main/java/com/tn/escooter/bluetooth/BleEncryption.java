package com.tn.escooter.bluetooth;

import android.util.Log;

import com.tn.escooter.utils.BytesUtils;

public class BleEncryption {
    static char[] inv = BytesUtils.string2Char("BDB866B69FB91A7EB8CCE206762C6659");
    static char[] key = BytesUtils.string2Char("E25D1FE9A55CD89CEBD4DC5338A08A04");
    static char[] key_tmp = new char[16];
    static char[] s_box = {'c', '|', 'w', '{', 242, 'k', 'o', 197, '0', 1, 'g', '+', 254, 215, 171, 'v', 202, 130, 201, '}', 250, 'Y', 'G', 240, 173, 212, 162, 175, 156, 164, 'r', 192, 183, 253, 147, '&', '6', '?', 247, 204, '4', 165, 229, 241, 'q', 216, '1', 21, 4, 199, '#', 195, 24, 150, 5, 154, 7, 18, 128, 226, 235, '\'', 178, 'u', 9, 131, ',', 26, 27, 'n', 'Z', 160, 'R', ';', 214, 179, ')', 227, '/', 132, 'S', 209, 0, 237, ' ', 252, 177, '[', 'j', 203, 190, '9', 'J', 'L', 'X', 207, 208, 239, 170, 251, 'C', 'M', '3', 133, 'E', 249, 2, 127, 'P', '<', 159, 168, 'Q', 163, '@', 143, 146, 157, '8', 245, 188, 182, 218, '!', 16, 255, 243, 210, 205, 12, 19, 236, '_', 151, 'D', 23, 196, 167, '~', '=', 'd', ']', 25, 's', '`', 129, 'O', 220, '\"', '*', 144, 136, 'F', 238, 184, 20, 222, '^', 11, 219, 224, '2', ':', 10, 'I', 6, '$', '\\', 194, 211, 172, 'b', 145, 149, 228, 'y', 231, 200, '7', 'm', 141, 213, 'N', 169, 'l', 'V', 244, 234, 'e', 'z', 174, 8, 186, 'x', '%', '.', 28, 166, 180, 198, 232, 221, 't', 31, 'K', 189, 139, 138, 'p', '>', 181, 'f', 'H', 3, 246, 14, 'a', '5', 'W', 185, 134, 193, 29, 158, 225, 248, 152, 17, 'i', 217, 142, 148, 155, 30, 135, 233, 206, 'U', '(', 223, 140, 161, 137, 13, 191, 230, 'B', 'h', 'A', 153, '-', 15, 176, 'T', 187, 22};
    char[] bonding_hash = new char[4];
    char[] bonding_nonce = new char[6];

    /* access modifiers changed from: 0000 */
    public void sub_bytes(char[] src, int len) {
        for (int idx = 0; idx < len; idx++) {
            src[idx] = s_box[src[idx] & 255];
        }
    }

    /* access modifiers changed from: 0000 */
    public void bonding_key_generate() {
        char[] tmp = new char[4];
        System.arraycopy(key, 0, key_tmp, 0, 16);
        sub_bytes(key_tmp, 16);
        System.arraycopy(key_tmp, 0, tmp, 0, 4);
        for (char i = 0; i < 12; i = (char) (i + 1)) {
            key_tmp[i] = key_tmp[i + 4];
        }
        System.arraycopy(tmp, 0, key_tmp, 12, 4);
    }

    /* access modifiers changed from: 0000 */
    public void bonding_hash_generate() {
        char[] nonce_exp = new char[32];
        System.arraycopy(bonding_nonce, 0, nonce_exp, 0, 6);
        System.arraycopy(bonding_nonce, 0, nonce_exp, 6, 6);
        System.arraycopy(bonding_nonce, 0, nonce_exp, 12, 4);
        System.arraycopy(nonce_exp, 1, nonce_exp, 16, 15);
        nonce_exp[31] = nonce_exp[0];
        bonding_key_generate();
        for (char i = 0; i < 16; i = (char) (i + 1)) {
            nonce_exp[i] = (char) ((nonce_exp[i] ^ key_tmp[i]) ^ inv[i]);
            nonce_exp[i + 16] = (char) ((nonce_exp[i + 16] ^ key_tmp[i]) ^ inv[i]);
        }
        for (char i2 = 1; i2 < 16; i2 = (char) (i2 + 1)) {
            nonce_exp[i2] = (char) (nonce_exp[i2] ^ nonce_exp[i2 - 1]);
            nonce_exp[i2 + 16] = (char) (nonce_exp[i2 + 16] ^ nonce_exp[(i2 + 16) - 1]);
        }
        for (char i3 = 0; i3 < 4; i3 = (char) (i3 + 1)) {
            bonding_hash[i3] = (char) (((((((nonce_exp[i3 + 0] ^ nonce_exp[i3 + 4]) ^ nonce_exp[i3 + 8]) ^ (nonce_exp[i3 + 12] + nonce_exp[i3 + 16])) ^ nonce_exp[i3 + 20]) ^ nonce_exp[i3 + 24]) ^ nonce_exp[i3 + 28]) & 255);
        }
    }

    public String encryptionStringOfValue(String valueString) {
        for (int i = 0; i < 6; i++) {
            String hexString = valueString.substring(i * 2, (i * 2) + 2);
            bonding_nonce[i] = (char) Integer.parseInt(hexString, 16);
        }
        bonding_hash_generate();
        String result = "";
        for (int i2 = 0; i2 < 4; i2++) {
            String valueHi = Integer.toHexString(bonding_hash[i2]);
            if (valueHi.length() < 2) {
                valueHi = "0" + valueHi;
            }
            result = result + valueHi;
        }
        Log.d("", "------------------------encryptionStringOfValue------------------------\n------------------------value:" + valueString + "---------\n------------------------result:" + result + "-------");
        return result;
    }

    private void hexStringToCharArr(String hexString, char[] charArr) {
        for (int i = 0; i < hexString.length() - 1; i += 2) {
            charArr[i] = (char) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
    }
}
