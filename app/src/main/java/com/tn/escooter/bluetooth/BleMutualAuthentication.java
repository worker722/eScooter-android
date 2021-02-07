package com.tn.escooter.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BleMutualAuthentication {
    public static BleMutualAuthentication bleAuthentication;
    public static LFBluetootService lfBluetootService;
    boolean _isCredible;
    boolean _isNeedAuth;
    boolean _isNeedRequest;
    boolean _isTrusted;
    String _receivedHash;
    int _requestTime;
    String _sendCode;
    String _sendHash;
    private List arr = new ArrayList();

    public static BleMutualAuthentication getBleAuthentication() {
        if (bleAuthentication == null) {
            bleAuthentication = new BleMutualAuthentication();
            lfBluetootService = LFBluetootService.getInstent();
        }
        return bleAuthentication;
    }

    /* access modifiers changed from: 0000 */
    public BleMutualAuthentication reset() {
        _isCredible = false;
        _isTrusted = false;
        _sendHash = null;
        _sendCode = null;
        _receivedHash = null;
        _isNeedAuth = true;
        _requestTime = 0;
        _isNeedRequest = true;
        return bleAuthentication;
    }

    /* access modifiers changed from: 0000 */
    public void startAuth() {
        askIsNeedAuth();
    }

    public final void askIsNeedAuth() {
        lfBluetootService.sendString("+VER?");
    }

    public boolean recieveResult(String result) {
        if (!_isNeedAuth || isMutualAuthentication()) {
            return false;
        }
        return mutualAuthenticationWithString(result);
    }

    /* access modifiers changed from: 0000 */
    public boolean isMutualAuthentication() {
        return _isCredible && _isTrusted;
    }

    /* access modifiers changed from: 0000 */
    public void requstAuth() {
        if (_isNeedRequest) {
            lfBluetootService.sendString("+PM?");
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean mutualAuthenticationWithString(String string) {
        BleEncryption bleEncryption = new BleEncryption();
        String string2 = string.toUpperCase();
        if (_isTrusted || string2.length() < 6 || !string2.substring(0, 3).equals("+PM")) {
            if (!_isCredible && string2.length() == 12 && string2.substring(0, 4).equals("+PA>")) {
                _isCredible = _receivedHash.equals(string2.substring(4));
            } else if (string2.length() > 8 && string2.substring(0, 5).equals("+VER=")) {
                requstAuth();
            }
        } else if (string2.length() == 16 && string2.charAt(3) == '>') {
            _isNeedRequest = false;
            _sendHash = bleEncryption.encryptionStringOfValue(string2.substring(4));
            lfBluetootService.sendString("+PM<" + _sendHash);
        } else if (string2.equals("+PM=OK") || string2.equals("+PM>OK")) {
            _isTrusted = true;
            Random random = new Random();
            String code = "";
            for (int i = 0; i < 6; i++) {
                code = code + (random.nextInt() & 255);
            }
            _sendCode = code;
            lfBluetootService.sendString("+PA<" + _sendCode);
            _receivedHash = bleEncryption.encryptionStringOfValue(_sendCode).toUpperCase();
        } else if (string2.equals("+PM=NK")) {
            _isTrusted = false;
        }
        if (!_isCredible || !_isTrusted) {
            return false;
        }
        return true;
    }
}
