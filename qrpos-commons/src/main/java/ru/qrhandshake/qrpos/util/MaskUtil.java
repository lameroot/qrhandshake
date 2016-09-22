package ru.qrhandshake.qrpos.util;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by lameroot on 31.05.16.
 */
public class MaskUtil {

    private static final Pattern PAN_PATTERN = Pattern.compile("\\d{13,19}");
    private static String mask = "**";

    public static String mask(String string) {
        if (!StringUtils.isBlank(string)) {
            return StringUtils.rightPad("", string.length(), "*");
        }
        return string;
    }

    //todo: сделать нормальное маскирование, чтобы было видно оператора и последние цифры
    public static String getMaskedMobileNumber(String phone) {
        if (StringUtils.isBlank(phone)) return phone;

        if (phone.length() > 8 ) {
            return phone.substring(0,4) + mask + phone.substring(phone.length()-4);
        }
        return phone;
    }

    public static String getMaskedPan(String pan) {
        if (StringUtils.isBlank(pan)) return pan;

        if (!PAN_PATTERN.matcher(pan).matches()) return pan;

        return pan.substring(0, 6) + mask + pan.substring(pan.length() - 4);
    }


}
