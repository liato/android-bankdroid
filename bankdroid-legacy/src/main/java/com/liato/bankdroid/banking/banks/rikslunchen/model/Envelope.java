package com.liato.bankdroid.banking.banks.rikslunchen.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/*
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <ns2:getBalanceResponse xmlns:ns2="urn:PhoneService">
            <return>
                <amount>1101.14</amount>
                <lastTopUpDate>2014-01-01</lastTopUpDate>
            </return>
        </ns2:getBalanceResponse>
        <soap:Fault>
            <faultcode>soap:Server</faultcode>
            <faultstring>card for 48565643 cannot be found!</faultstring>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>
 */

@Root
public class Envelope {

    @Element(name = "Body")
    public Body body;

    public static class Body {

        @Element(required = false)
        public GetBalanceResponse getBalanceResponse;

        @Element(name = "Fault", required = false)
        public Fault fault;

        public static class GetBalanceResponse {

            @Element(name = "return")
            public Return responseReturn;

            public static class Return {

                @Element
                public String amount;

                @Element
                public String lastTopUpDate;
            }
        }

        public static class Fault {

            @Element
            public String faultcode;

            @Element
            public String faultstring;
        }
    }
}