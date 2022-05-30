package com.ews.stguo.testproject.configuration;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Configuration
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ((TomcatServletWebServerFactory) factory).addConnectorCustomizers(connector -> {
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            protocol.setKeepAliveTimeout(5000);
            protocol.setMaxKeepAliveRequests(10000);
        });
    }

}
