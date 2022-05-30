package com.ews.stguo.testproject.aws.qubole;

import com.qubole.qds.sdk.java.client.DefaultQdsConfiguration;
import com.qubole.qds.sdk.java.client.QdsClient;
import com.qubole.qds.sdk.java.client.QdsClientFactory;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class QuboleClientFactory {

    public static QdsClient createQdsClient(String apiToken) {
        return QdsClientFactory.newClient(new DefaultQdsConfiguration(apiToken));
    }

    public static QdsClient createQdsClient(String apiEndpoint, String apiToken, String apiVersion) {
        return QdsClientFactory.newClient(new DefaultQdsConfiguration(apiEndpoint, apiToken, apiVersion));
    }
}
