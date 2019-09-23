package dcmtools.network;

import dcmtools.cli.converters.StringArrayConverter;

public class TLSProtocols {

    public static final String[] DEFAULT = new String[] { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3" };

    public static final String DEFAULT_AS_STRING = String.join(",", DEFAULT);

    public static final String[] SUPPORTED = new String[] { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3", "SSLv2Hello" };

    public static final String SUPPORTED_AS_STRING = String.join(",", SUPPORTED);

    public static class Converter extends StringArrayConverter {

        public Converter() {
            super(",", SUPPORTED);
        }

    }

}
