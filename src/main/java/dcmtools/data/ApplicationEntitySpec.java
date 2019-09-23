package dcmtools.data;

import java.text.ParseException;

import picocli.CommandLine.ITypeConverter;

public class ApplicationEntitySpec {

    public final String title;
    public final String host;
    public final int port;

    public ApplicationEntitySpec(String title, String host, int port) {
        this.title = title;
        this.host = host;
        this.port = port;
    }

    public ApplicationEntitySpec(String title) {
        this(title, null, 0);
    }

    public static class Converter implements ITypeConverter<ApplicationEntitySpec> {

        @Override
        public ApplicationEntitySpec convert(String value) throws Exception {
            String title = null;
            String host = null;
            int port = 0;
            if (value != null) {
                value = value.trim();
                int idx = value.indexOf('@');
                if (idx >= 0) {
                    title = value.substring(0, idx);
                    String hostAndPort = value.substring(idx + 1);
                    idx = hostAndPort.indexOf(':');
                    if (idx >= 0) {
                        host = hostAndPort.substring(0, idx);
                        port = Integer.parseInt(hostAndPort.substring(idx + 1));
                        if (port <= 0 || port > 65535) {
                            throw new ParseException("Failed to parse application entity. Invalid port: " + port,
                                    value.indexOf(':'));
                        }
                    } else {
                        host = hostAndPort;
                    }
                    if (host == null || host.trim().isEmpty()) {
                        throw new ParseException("Failed to parse application entity host: " + hostAndPort,
                                value.indexOf('@'));
                    }
                } else {
                    title = value;
                }
            }
            if (title == null || title.trim().isEmpty()) {
                throw new ParseException("Failed to parse application entity: " + value, 0);
            }
            return new ApplicationEntitySpec(title.trim(), host == null ? null : host.trim(), port);
        }
    }

    public static class CallingAEConverter extends Converter {
        @Override
        public ApplicationEntitySpec convert(String value) throws Exception {
            return super.convert(value);
        }
    }

    public static class CalledAEConverter extends Converter {
        @Override
        public ApplicationEntitySpec convert(String value) throws Exception {
            ApplicationEntitySpec ae = super.convert(value);
            if (ae.host == null || ae.host.isEmpty()) {
                throw new ParseException("Failed to parse application entity: '" + value + "'. Missing host.", 0);
            }
            if (ae.port <= 0 || ae.port > 65535) {
                throw new ParseException("Failed to parse application entity: '" + value + "'. Invalid port.", 0);
            }
            return ae;
        }
    }

}
