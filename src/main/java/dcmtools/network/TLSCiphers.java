package dcmtools.network;

import dcmtools.cli.converters.StringArrayConverter;

public class TLSCiphers {

    public static final String[] DEFAULT = new String[] { "SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA" };

    public static final String DEFAULT_AS_STRING = String.join(",", DEFAULT);

    public static class Converter extends StringArrayConverter {

        public Converter() {
            super(",", null);
        }

    }

}
