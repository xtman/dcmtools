package dcmtools.network.storescp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import dcmtools.data.ApplicationEntitySpec;
import dcmtools.network.KeyStoreSpec;
import dcmtools.network.TransferOptions;

public class Options extends TransferOptions {

    private final Path _directory;
    private final String _pathPattern;
    private final boolean _ignore;
    private final int _responseDelay;
    private final Map<String, String> _sopClasses;
    private final boolean _acceptUnknown;
    private final int _status;

    protected Options(ApplicationEntitySpec localAE, Path directory, String pathPattern, boolean ignore,
            int maxSndPDULength, int maxRcvPDULength, int maxOpsInvoked, int maxOpsPerformed, boolean packPDV,
            int requestTimeout, int releaseTimeout, int idleTimeout, int socketCloseDelay, int socketSndBufferSize,
            int socketRcvBufferSize, boolean tcpNoDelay, int responseDelay, String[] tlsProtocols, String[] tlsCiphers,
            boolean tlsNoAuth, KeyStoreSpec keyStore, String keyPass, KeyStoreSpec trustStore,
            Map<String, String> sopClasses, boolean acceptUnknown, int status) {
        super(localAE, maxSndPDULength, maxRcvPDULength, maxOpsInvoked, maxOpsPerformed, packPDV, requestTimeout,
                releaseTimeout, idleTimeout, socketCloseDelay, socketSndBufferSize, socketRcvBufferSize, tcpNoDelay,
                tlsProtocols, tlsCiphers, tlsNoAuth, keyStore, keyPass, trustStore);
        _directory = directory;
        _pathPattern = pathPattern;
        _ignore = ignore;
        _responseDelay = responseDelay;
        _sopClasses = sopClasses;
        _acceptUnknown = acceptUnknown;
        _status = status;
    }

    public final Path directory() {
        return _directory;
    }

    public final String pathPattern() {
        return _pathPattern;
    }

    public final boolean ignore() {
        return _ignore;
    }

    public final int responseDelay() {
        return _responseDelay;
    }

    public final Map<String, String> sopClasses() {
        return _sopClasses;
    }

    public final boolean acceptUnknown() {
        return _acceptUnknown;
    }

    public final int status() {
        return _status;
    }

    public static class Builder extends TransferOptions.Builder<Options> {

        private Path directory;
        private String pathPattern = StoreSCP.DEFAULT_PATH_PATTERN;
        private boolean ignore;
        private int responseDelay;
        private Map<String, String> sopClasses;
        private boolean acceptUnknown;
        private int status;

        public Builder() {
            this.ae = new ApplicationEntitySpec(StoreSCP.DEFAULT_AE_TITLE, null, StoreSCP.DEFAULT_PORT);
        }

        public final void setDirectory(Path directory) {
            this.directory = directory;
        }

        public final void setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
        }

        public final void setIgnore(boolean ignore) {
            this.ignore = ignore;
        }

        public final void setResponseDelay(int responseDelay) {
            this.responseDelay = responseDelay;
        }

        public final void setSopClasses(Map<String, String> sopClasses) {
            this.sopClasses = sopClasses;
        }

        public final void loadSopClasses(InputStream in) throws IOException {
            Properties properties = new Properties();
            properties.load(in);
            if (!properties.isEmpty()) {
                if (this.sopClasses == null) {
                    this.sopClasses = new LinkedHashMap<String, String>();
                } else {
                    this.sopClasses.clear();
                }
                for (String classUID : properties.stringPropertyNames()) {
                    this.sopClasses.put(classUID, properties.getProperty(classUID).trim());
                }
            }
        }

        public final void loadSopClasses(Path relatedSOPClassesFile) throws IOException {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(relatedSOPClassesFile))) {
                loadSopClasses(in);
            }
        }

        public final void loadDefaultSopClasses() throws IOException {
            try (InputStream in = new BufferedInputStream(
                    getClass().getClassLoader().getResourceAsStream(StoreSCP.DEFAULT_SOP_CLASSES_PROPERTIES_FILE))) {
                loadSopClasses(in);
            }
        }

        public final void setAcceptUnknown(boolean acceptUnknown) {
            this.acceptUnknown = acceptUnknown;
        }

        public final void setStatus(int status) {
            this.status = status;
        }

        @Override
        public Options build() {
            return new Options(this.ae, this.directory, this.pathPattern, this.ignore, this.maxSndPDULength,
                    this.maxRcvPDULength, this.maxOpsInvoked, this.maxOpsPerformed, this.packPDV, this.requestTimeout,
                    this.releaseTimeout, this.idleTimeout, this.socketCloseDelay, this.socketSndBufferSize,
                    this.socketRcvBufferSize, this.tcpNoDelay, this.responseDelay, this.tlsProtocolsAsArray(),
                    this.tlsCiphersAsArray(), this.tlsNoAuth, this.keyStore, this.keyPass, this.trustStore,
                    this.sopClasses, this.acceptUnknown, this.status);
        }

    }

}
