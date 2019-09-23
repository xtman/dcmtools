package dcmtools.network.storescu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;

import dcmtools.data.ApplicationEntitySpec;
import dcmtools.data.AttributeSpec;
import dcmtools.network.HttpProxySpec;
import dcmtools.network.KeyStoreSpec;
import dcmtools.network.TransferOptions;

public class Options extends TransferOptions {

    private final ApplicationEntitySpec _remoteAE;
    private final HttpProxySpec _httpProxy;
    private final String _username;
    private final String _userPassword;
    private final boolean _userRSP;
    private final int _connectTimeout;
    private final int _acceptTimeout;
    private final int _responseTimeout;
    private final int _retrieveTimeout;
    private final int _retrieveTimeoutTotal;
    private final String _uidSuffix;
    private final int _priority;
    private final Attributes _attributes;
    private final Map<String, CommonExtendedNegotiation> _relatedSOPClasses;

    public Options(ApplicationEntitySpec ae, ApplicationEntitySpec remoteAE, HttpProxySpec httpProxy, String username,
            String userPassword, boolean userRSP, int maxSndPDULength, int maxRcvPDULength, int maxOpsInvoked,
            int maxOpsPerformed, boolean packPDV, int connectTimeout, int requestTimeout, int acceptTimeout,
            int releaseTimeout, int responseTimeout, int retrieveTimeout, int retrieveTimeoutTotal, int idleTimeout,
            int socketCloseDelay, int socketSndBufferSize, int socketRcvBufferSize, boolean tcpNoDelay,
            String[] tlsProtocols, String[] tlsCiphers, boolean tlsNoAuth, KeyStoreSpec keyStore, String keyPass,
            KeyStoreSpec trustStore, Map<String, CommonExtendedNegotiation> relatedSOPClasses, String uidSuffix,
            int priority, Attributes attributes) {
        super(ae, maxSndPDULength, maxRcvPDULength, maxOpsInvoked, maxOpsPerformed, packPDV, requestTimeout,
                releaseTimeout, idleTimeout, socketCloseDelay, socketSndBufferSize, socketRcvBufferSize, tcpNoDelay,
                tlsProtocols, tlsCiphers, tlsNoAuth, keyStore, keyPass, trustStore);
        _remoteAE = remoteAE;
        _httpProxy = httpProxy;
        _username = username;
        _userPassword = userPassword;
        _userRSP = userRSP;
        _connectTimeout = connectTimeout;
        _acceptTimeout = acceptTimeout;
        _responseTimeout = responseTimeout;
        _retrieveTimeout = retrieveTimeout;
        _retrieveTimeoutTotal = retrieveTimeoutTotal;
        _uidSuffix = uidSuffix;
        _priority = priority;
        _attributes = attributes;
        _relatedSOPClasses = relatedSOPClasses;
    }

    public final ApplicationEntitySpec remoteApplicationEntity() {
        return _remoteAE;
    }

    public final HttpProxySpec httpProxy() {
        return _httpProxy;
    }

    public final String username() {
        return _username;
    }

    public final String userPassword() {
        return _userPassword;
    }

    public final boolean userRSP() {
        return _userRSP;
    }

    public final int connectTimeout() {
        return _connectTimeout;
    }

    public final int acceptTimeout() {
        return _acceptTimeout;
    }

    public final int responseTimeout() {
        return _responseTimeout;
    }

    public final int retrieveTimeout() {
        return _retrieveTimeout;
    }

    public final int retrieveTimeoutTotal() {
        return _retrieveTimeoutTotal;
    }

    public final String uidSuffix() {
        return _uidSuffix;
    }

    public final int priority() {
        return _priority;
    }

    public final Attributes attributes() {
        return _attributes;
    }

    public boolean hasAttributes() {
        return _attributes != null && !_attributes.isEmpty();
    }

    public final Map<String, CommonExtendedNegotiation> relatedSOPClasses() {
        return _relatedSOPClasses == null ? null : Collections.unmodifiableMap(_relatedSOPClasses);
    }

    public static class Builder extends TransferOptions.Builder<Options> {
        private ApplicationEntitySpec remoteAE;
        private HttpProxySpec httpProxy;
        private String username;
        private String userPassword;
        private boolean userRSP;
        private int connectTimeout;
        private int acceptTimeout;
        private int responseTimeout;
        private int retrieveTimeout;
        private int retrieveTimeoutTotal;
        private boolean relationshipExtendedNegotiation;
        private String uidSuffix;
        private int priority = Priority.NORMAL;
        private Attributes attributes;
        private Map<String, CommonExtendedNegotiation> relatedSOPClasses;

        public Builder() {
            this.ae = new ApplicationEntitySpec(StoreSCU.DEFAULT_AE_TITLE);
        }

        public Options build() throws IllegalArgumentException {

            return new Options(this.ae, this.remoteAE, this.httpProxy, this.username, this.userPassword, this.userRSP,
                    this.maxSndPDULength, this.maxRcvPDULength, this.maxOpsInvoked, this.maxOpsPerformed, this.packPDV,
                    this.connectTimeout, this.requestTimeout, this.acceptTimeout, this.releaseTimeout,
                    this.responseTimeout, this.retrieveTimeout, this.retrieveTimeoutTotal, this.idleTimeout,
                    this.socketCloseDelay, this.socketSndBufferSize, this.socketRcvBufferSize, this.tcpNoDelay,
                    this.tlsProtocolsAsArray(), this.tlsCiphersAsArray(), this.tlsNoAuth, this.keyStore, this.keyPass,
                    this.trustStore, this.relatedSOPClasses, this.uidSuffix, this.priority, this.attributes);

        }

        public final void setRemoteApplicationEntity(ApplicationEntitySpec remoteAE) {
            this.remoteAE = remoteAE;
        }

        public final void setRemoteApplicationEntity(String title, String host, int port) {
            this.remoteAE = new ApplicationEntitySpec(title, host, port);
        }

        public final void setHttpProxy(HttpProxySpec httpProxy) {
            this.httpProxy = httpProxy;
        }

        public final void setUsername(String username) {
            this.username = username;
        }

        public final void setUserPassword(String userPassword) {
            this.userPassword = userPassword;
        }

        public final void setUserRSP(boolean userRSP) {
            this.userRSP = userRSP;
        }

        public final void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public final void setAcceptTimeout(int acceptTimeout) {
            this.acceptTimeout = acceptTimeout;
        }

        public final void setResponseTimeout(int responseTimeout) {
            this.responseTimeout = responseTimeout;
        }

        public final void setRetrieveTimeout(int retrieveTimeout) {
            this.retrieveTimeout = retrieveTimeout;
        }

        public final void setRetrieveTimeoutTotal(int retrieveTimeoutTotal) {
            this.retrieveTimeoutTotal = retrieveTimeoutTotal;
        }

        public final void setRelationshipExtendedNegotiation(boolean relationshipExtendedNegotiation)
                throws IOException {
            this.relationshipExtendedNegotiation = relationshipExtendedNegotiation;
            if (this.relationshipExtendedNegotiation) {
                loadDefaultRelatedSopClasses();
            }
        }

        public final void loadRelatedSopClasses(InputStream in) throws IOException {
            Properties properties = new Properties();
            properties.load(in);
            if (!properties.isEmpty()) {
                if (this.relatedSOPClasses == null) {
                    this.relatedSOPClasses = new LinkedHashMap<String, CommonExtendedNegotiation>();
                } else {
                    this.relatedSOPClasses.clear();
                }
                for (String classUID : properties.stringPropertyNames()) {
                    this.relatedSOPClasses.put(classUID, new CommonExtendedNegotiation(classUID,
                            UID.StorageServiceClass, properties.getProperty(classUID).trim().split("\\ *,\\ *")));
                }
            }
        }

        public final void loadRelatedSopClasses(Path relatedSOPClassesFile) throws IOException {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(relatedSOPClassesFile))) {
                loadRelatedSopClasses(in);
            }
        }

        public final void loadDefaultRelatedSopClasses() throws IOException {
            try (InputStream in = new BufferedInputStream(getClass().getClassLoader()
                    .getResourceAsStream(StoreSCU.DEFAULT_RELATED_SOP_CLASSES_PROPERTIES_FILE))) {
                loadRelatedSopClasses(in);
            }
        }

        public final void setUidSuffix(String uidSuffix) {
            this.uidSuffix = uidSuffix;
        }

        public final void setPriority(int priority) {
            if (priority >= Priority.NORMAL && priority <= Priority.LOW) {
                this.priority = priority;
            }
        }

        public final void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public final void addAttributes(AttributeSpec... attributeSpecs) {
            if (attributeSpecs != null && attributeSpecs.length > 0) {
                if (this.attributes == null) {
                    this.attributes = new Attributes();
                }
                for (AttributeSpec a : attributeSpecs) {
                    AttributeSpec.addAttribute(this.attributes, a);
                }
            }
        }
    }
}
