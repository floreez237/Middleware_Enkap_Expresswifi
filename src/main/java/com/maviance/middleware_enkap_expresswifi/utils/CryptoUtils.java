package com.maviance.middleware_enkap_expresswifi.utils;


import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public class CryptoUtils {
    public static String generateHMAC(String toEncode, String secret) {
        HmacUtils hmac256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
        //hm256 object can be used again and again
        return hmac256.hmacHex(toEncode);
    }

}
