package com.ews.stguo.testproject.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class SQSTest {

    @Test
    public void test01() throws Exception {
        String temp1 = "{\n" +
                "  \"partner\": \"Common\",\n" +
                "  \"format\": \"jsonl\",\n" +
                "  \"brand\": \"All\",\n" +
                "  \"locale\": \"%s\",\n" +
                "  \"currency\": \"%s\",\n" +
                "  \"type\": \"%s\",\n" +
                "  \"requestTypes\": [\n" +
                "    \"HOTEL_INFO\"\n" +
//                "    \",DATELESS_PRICE\"\n" +
                "  ]\n" +
                "}";
        String[] localesAndCurrencies = new String[] {
                "ar-SA:SAR","cs-CZ:CZK","da-DK:DKK","de-DE:EUR","el-GR:EUR","en-GB:GBP","en-US:USD","es-ES:EUR",
                "es-MX:MXN","es-US:USD","et-EE:EUR","fi-FI:EUR","fr-CA:CAD","fr-FR:EUR","he-IL:ILS","hr-HR:HRK",
                "hu-HU:HUF","id-ID:IDR","is-IS:ISK","it-IT:EUR","ja-JP:JPY","ko-KR:KRW","lt-LT:EUR","lv-LV:EUR",
                "ms-MY:MYR","nb-NO:NOK","nl-NL:EUR","pl-PL:PLN","pt-BR:BRL","pt-PT:EUR","ru-RU:RUB","sk-SK:EUR",
                "sv-SE:SEK","th-TH:THB","tl-PH:PHP","tr-TR:TRY","uk-UA:UAH","vi-VN:VND","zh-CN:CNY"
        };
        String type = "VacationRental";
        String queueUrl = "https://sqs.us-west-2.amazonaws.com/577480909982/lpp_static_feed_event_queue_prod";
        AmazonSQS amazonSQSClient = SQSClientFactory.createAmazonSQSClient();
        for (String localesAndCurrency : localesAndCurrencies) {
            String[] columns = localesAndCurrency.split(":");
            String locale = columns[0];
            String currency = columns[1];
            String message = String.format(temp1, locale, currency, type);
            amazonSQSClient.sendMessage(queueUrl, message);
            System.out.println(message);
        }
    }

}
