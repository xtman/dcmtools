package dcmtools.network;

import java.util.LinkedHashSet;
import java.util.Set;

import org.dcm4che3.net.Connection;

import dcmtools.data.ApplicationEntitySpec;

public class TransferOptions {

    private final ApplicationEntitySpec _ae;
    private final int _maxSndPDULength;
    private final int _maxRcvPDULength;
    private final int _maxOpsInvoked;
    private final int _maxOpsPerformed;
    private final boolean _packPDV;
    private final int _requestTimeout;
    private final int _releaseTimeout;
    private final int _idleTimeout;
    private final int _socketCloseDelay;
    private final int _socketSndBufferSize;
    private final int _socketRcvBufferSize;
    private final boolean _tcpNoDelay;
    private final String[] _tlsProtocols;
    private final String[] _tlsCiphers;
    private final boolean _tlsNoAuth;
    private final KeyStoreSpec _keyStore;
    private final String _keyPass;
    private final KeyStoreSpec _trustStore;

    protected TransferOptions(ApplicationEntitySpec ae, int maxSndPDULength, int maxRcvPDULength,
            int maxOpsInvoked, int maxOpsPerformed, boolean packPDV, int requestTimeout, int releaseTimeout,
            int idleTimeout, int socketCloseDelay, int socketSndBufferSize, int socketRcvBufferSize, boolean tcpNoDelay,
            String[] tlsProtocols, String[] tlsCiphers, boolean tlsNoAuth, KeyStoreSpec keyStore, String keyPass,
            KeyStoreSpec trustStore) {
        _ae = ae;
        _maxSndPDULength = maxSndPDULength;
        _maxRcvPDULength = maxRcvPDULength;
        _maxOpsInvoked = maxOpsInvoked;
        _maxOpsPerformed = maxOpsPerformed;
        _packPDV = packPDV;
        _requestTimeout = requestTimeout;
        _releaseTimeout = releaseTimeout;
        _idleTimeout = idleTimeout;
        _socketCloseDelay = socketCloseDelay;
        _socketSndBufferSize = socketSndBufferSize;
        _socketRcvBufferSize = socketRcvBufferSize;
        _tcpNoDelay = tcpNoDelay;
        _tlsProtocols = tlsProtocols;
        _tlsCiphers = tlsCiphers;
        _tlsNoAuth = tlsNoAuth;
        _keyPass = keyPass;
        _keyStore = keyStore;
        _trustStore = trustStore;
    }

    public final ApplicationEntitySpec applicationEntity() {
        return _ae;
    }

    public final int maxSndPDULength() {
        return _maxSndPDULength;
    }

    public final int maxRcvPDULength() {
        return _maxRcvPDULength;
    }

    public final int maxOpsInvoked() {
        return _maxOpsInvoked;
    }

    public final int maxOpsPerformed() {
        return _maxOpsPerformed;
    }

    public final boolean packPDV() {
        return _packPDV;
    }

    public final int requestTimeout() {
        return _requestTimeout;
    }

    public final int releaseTimeout() {
        return _releaseTimeout;
    }

    public final int idleTimeout() {
        return _idleTimeout;
    }

    public final int socketCloseDelay() {
        return _socketCloseDelay;
    }

    public final int socketSndBufferSize() {
        return _socketSndBufferSize;
    }

    public final int socketRcvBufferSize() {
        return _socketRcvBufferSize;
    }

    public final boolean tcpNoDelay() {
        return _tcpNoDelay;
    }

    public final String[] tlsProtocols() {
        return _tlsProtocols;
    }

    public final String[] tlsCiphers() {
        return _tlsCiphers;
    }

    public final boolean tlsNoAuth() {
        return _tlsNoAuth;
    }

    public final String keyPass() {
        return _keyPass;
    }

    public final KeyStoreSpec keyStore() {
        return _keyStore;
    }

    public final KeyStoreSpec trustStore() {
        return _trustStore;
    }

    public static abstract class Builder<T extends TransferOptions> {

        protected ApplicationEntitySpec ae;
        protected int maxSndPDULength = Connection.DEF_MAX_PDU_LENGTH;
        protected int maxRcvPDULength = Connection.DEF_MAX_PDU_LENGTH;
        protected int maxOpsInvoked;
        protected int maxOpsPerformed;
        protected boolean packPDV;
        protected int requestTimeout;
        protected int releaseTimeout;
        protected int idleTimeout;
        protected int socketCloseDelay = Connection.DEF_SOCKETDELAY;
        protected int socketSndBufferSize;
        protected int socketRcvBufferSize;
        protected boolean tcpNoDelay;
        protected Set<String> tlsProtocols;
        protected Set<String> tlsCiphers;
        protected boolean tlsNoAuth;
        protected String keyPass;
        protected KeyStoreSpec keyStore;
        protected KeyStoreSpec trustStore;

        public final void setApplicationEntity(ApplicationEntitySpec ae) {
            this.ae = ae;
        }

        public final void setApplicationEntity(String title, String host, int port) {
            this.ae = new ApplicationEntitySpec(title, host, port);
        }

        public final void setApplicationEntity(String title) {
            this.ae = new ApplicationEntitySpec(title);
        }

        public final void setMaxSndPDULength(int maxSndPDULength) {
            this.maxSndPDULength = maxSndPDULength;
        }

        public final void setMaxRcvPDULength(int maxRcvPDULength) {
            this.maxRcvPDULength = maxRcvPDULength;
        }

        public final void setMaxOpsInvoked(int maxOpsInvoked) {
            this.maxOpsInvoked = maxOpsInvoked;
        }

        public final void setMaxOpsPerformed(int maxOpsPerformed) {
            this.maxOpsPerformed = maxOpsPerformed;
        }

        public final void setPackPDV(boolean packPDV) {
            this.packPDV = packPDV;
        }

        public final void setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public final void setReleaseTimeout(int releaseTimeout) {
            this.releaseTimeout = releaseTimeout;
        }

        public final void setIdleTimeout(int idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public final void setSocketCloseDelay(int socketCloseDelay) {
            this.socketCloseDelay = socketCloseDelay;
        }

        public final void setSocketSndBufferSize(int socketSndBufferSize) {
            this.socketSndBufferSize = socketSndBufferSize;
        }

        public final void setSocketRcvBufferSize(int socketRcvBufferSize) {
            this.socketRcvBufferSize = socketRcvBufferSize;
        }

        public final void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public final void setTcpDelay(boolean tcpDelay) {
            this.tcpNoDelay = !tcpDelay;
        }

        public final void addTlsCiphers(String... tlsCiphers) {
            if (tlsCiphers != null && tlsCiphers.length > 0) {
                if (this.tlsCiphers == null) {
                    this.tlsCiphers = new LinkedHashSet<String>();
                }
                for (String tlsCipher : tlsCiphers) {
                    this.tlsCiphers.add(tlsCipher);
                }
            }
        }

        public final void setTls(boolean enableTLS) {
            if (enableTLS) {
                addTlsProtocols(TLSProtocols.DEFAULT);
                addTlsCiphers(TLSCiphers.DEFAULT);
            } else {
                if (this.tlsProtocols != null) {
                    this.tlsProtocols.clear();
                }
                if (this.tlsCiphers != null) {
                    this.tlsCiphers.clear();
                }
            }
        }

        public final void enableTLS() {
            setTls(true);
        }

        public final void disableTLS() {
            setTls(false);
        }

        public final void addTlsProtocols(String... tlsProtocols) {
            if (tlsProtocols != null && tlsProtocols.length > 0) {
                if (this.tlsProtocols == null) {
                    this.tlsProtocols = new LinkedHashSet<String>();
                }
                for (String tlsProtocol : tlsProtocols) {
                    this.tlsProtocols.add(tlsProtocol);
                }
            }
        }

        protected final String[] tlsProtocolsAsArray() {
            return this.tlsProtocols == null ? null : this.tlsProtocols.toArray(new String[this.tlsProtocols.size()]);
        }

        protected final String[] tlsCiphersAsArray() {
            return this.tlsCiphers == null ? null : this.tlsCiphers.toArray(new String[this.tlsCiphers.size()]);
        }

        public final void setTlsNoAuth(boolean tlsNoAuth) {
            this.tlsNoAuth = tlsNoAuth;
        }

        public final void setKeyPass(String keyPass) {
            this.keyPass = keyPass;
        }

        public final void setKeyStore(KeyStoreSpec keyStore) {
            this.keyStore = keyStore;
        }

        public final void setKeyStore(String keyStorePath, String keyStoreType, String keyStorePass) {
            if (keyStorePath == null) {
                this.keyStore = null;
            } else {
                this.keyStore = new KeyStoreSpec(keyStorePath, keyStoreType, keyStorePass);
            }
        }

        public final void setTrustStore(String trustStorePath, String trustStoreType, String trustStorePass) {
            if (trustStorePath == null) {
                this.trustStore = null;
            } else {
                this.trustStore = new KeyStoreSpec(trustStorePath, trustStoreType, trustStorePass);
            }
        }

        public final void setTrustStore(KeyStoreSpec trustStore) {
            this.trustStore = trustStore;
        }

        public abstract T build();

    }

}
