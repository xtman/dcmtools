package dcmtools.network;

import java.text.ParseException;

import picocli.CommandLine.ITypeConverter;

public class HttpProxySpec {

    public final String user;
    public final String password;
    public final String host;
    public final int port;

    public HttpProxySpec(String user, String password, String host, int port) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.user != null && !this.user.isEmpty()) {
            sb.append(this.user);
            if (this.password != null && !this.password.isEmpty()) {
                sb.append(":").append(this.password);
            }
            sb.append("@");
        }
        sb.append(this.host);
        sb.append(":").append(this.port);
        return sb.toString();
    }

    public static HttpProxySpec parse(String v) throws ParseException {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        v = v.trim();
        String userName = null;
        String userPass = null;
        String host = null;
        int port = 0;
        int idx = v.indexOf('@');
        String userPart = idx == -1 ? null : v.substring(0, idx);
        String hostPart = idx == -1 ? v : v.substring(idx + 1);
        if (userPart != null) {
            userPart = userPart.trim();
            int idx2 = userPart.indexOf(':');
            if (idx2 == -1) {
                userName = userPart;
            } else {
                userName = userPart.substring(0, idx2);
                userPass = userPart.substring(idx2 + 1);
            }
        }
        idx = hostPart.indexOf(':');
        if (idx == -1) {
            throw new ParseException("Failed to parse http proxy address: " + v, 0);
        }
        host = hostPart.substring(0, idx);
        try {
            port = Integer.parseInt(hostPart.substring(idx + 1));
        } catch (NumberFormatException e) {
            throw new ParseException("Failed to parse http proxy address: " + v + ". Invalid port!", 0);
        }
        if (port <= 0 || port > 65535) {
            throw new ParseException("Failed to parse http proxy address: " + v + ". Invalid port!", 0);
        }
        return new HttpProxySpec(userName, userPass, host, port);
    }

    public static class Converter implements ITypeConverter<HttpProxySpec> {

        @Override
        public HttpProxySpec convert(String value) throws Exception {
            return HttpProxySpec.parse(value);
        }

    }
}
