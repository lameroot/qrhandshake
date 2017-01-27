package ru.qrhandshake.qrpos.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lameroot on 19.05.16.
 */
public enum IntegrationSupport {
    RBS_SBRF_OWN,
    RBS_SBRF,
    RBS_SBRF_HTTP,
    RBS_SBRF_P2P,
    RBS_ALFA,
    TINKOFF,
    YANDEX_WALLET,
    QIWI_WALLET,
    GOOGLE_WALLET;

    private final static List<IntegrationSupport> USERPASSWORD_CREDENTIALS = Arrays.asList(RBS_ALFA,RBS_SBRF,RBS_SBRF_OWN,RBS_SBRF_P2P);

    public static boolean isUserPasswordCredentials(IntegrationSupport integrationSupport) {
        return USERPASSWORD_CREDENTIALS.contains(integrationSupport);
    }
}
