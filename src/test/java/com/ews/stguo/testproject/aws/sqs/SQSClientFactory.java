package com.ews.stguo.testproject.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SQSClientFactory {

    public static AmazonSQS createAmazonSQSClient() {
        return AmazonSQSClientBuilder.standard().build();
    }

}
