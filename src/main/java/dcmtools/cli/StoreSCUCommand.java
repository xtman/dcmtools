package dcmtools.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import dcmtools.data.ApplicationEntitySpec;
import dcmtools.data.AttributeSpec;
import dcmtools.network.HttpProxySpec;
import dcmtools.network.TLSCiphers;
import dcmtools.network.TLSProtocols;
import dcmtools.network.storescu.Options;
import dcmtools.network.storescu.Progress;
import dcmtools.network.storescu.StoreSCU;
import dcmtools.util.LoggingUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "storescu", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE\n  ", customSynopsis = {
        "storescu [OPTIONS] -c <title@host:port> DICOM_FILES..." }, descriptionHeading = "\nDESCRIPTION\n  ", description = "Store SCU client.", parameterListHeading = "\nPARAMETERS\n", optionListHeading = "\nOPTIONS\n", sortOptions = false, version = StoreSCUCommand.VERSION, separator = " ")
public class StoreSCUCommand implements Callable<Integer> {

    public static final String VERSION = "1.0.0";

    @Option(names = { "-c",
            "--called-ae" }, description = "called application entity", arity = "1", required = true, paramLabel = "<title@host:port>", converter = ApplicationEntitySpec.CalledAEConverter.class)
    private ApplicationEntitySpec calledAE;

    @Option(names = { "-b",
            "--calling-ae" }, description = "calling application entity", arity = "1", required = false, paramLabel = "<title[@host:port]>", converter = ApplicationEntitySpec.CallingAEConverter.class)
    private ApplicationEntitySpec callingAE = new ApplicationEntitySpec(StoreSCU.DEFAULT_AE_TITLE);

    @Option(names = { "-a",
            "--attribute" }, description = "specify attributes added to the sent object(s). attr can be specified by keyword or tag value (in hex), e.g. PatientName or 00100010. Attributes in nested Datasets can be specified by including the keyword/tag value of the sequence attribute, e.g. 00400275/00400009 for Scheduled Procedure Step ID in the Request Attributes Sequence.", required = false, paramLabel = "<tag=value>", converter = AttributeSpec.Converter.class)
    private AttributeSpec[] attributeSpecs;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-v", "--verbose" }, description = "verbose")
    private boolean verbose = false;

    @Option(names = {
            "--http-proxy" }, description = "specify http proxy to tunnel DICOM connection.", required = false, paramLabel = "<[user:password@]proxy_host:port>", converter = HttpProxySpec.Converter.class)
    private HttpProxySpec httpProxy = null;

    @Option(names = {
            "--uid-suffix" }, description = "specify suffix to be appended to Study, Series and SOP Instance UID of the sent objects.", required = false, paramLabel = "<uid>")
    private String uidSuffix = null;

    @Option(names = {
            "--max-ops-invoked" }, description = "maximum number of operations this AE may invoke asynchronously, unlimited by default.", arity = "1", required = false, paramLabel = "<n>", defaultValue = "0")
    private int maxOpsInvoked;

    @Option(names = {
            "--max-ops-performed" }, description = "maximum number of operations this AE may perform asynchronously, unlimited by default.", arity = "1", required = false, paramLabel = "<n>", defaultValue = "0")
    private int maxOpsPerformed;

    @Option(names = {
            "--pack-pdv" }, description = "enable to pack command and data PDV in one P-DATA-TF PDU. Disabled by default.", required = false)
    private boolean packPDV = false;

    @Option(names = {
            "--accept-timeout" }, description = "timeout in milliseconds for receiving A-ASSOCIATE-AC. No timeout by default", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int acceptTimeout;

    @Option(names = {
            "--connect-timeout" }, description = "timeout in milliseconds for TCP connect. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int connectTimeout;

    @Option(names = {
            "--idle-timeout" }, description = "timeout in milliseconds for receiving DIMSE-RQ. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int idleTimeout;

    @Option(names = {
            "--release-timeout" }, description = "timeout in milliseconds for receiving A-RELEASE-RP. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int releaseTimeout;

    @Option(names = {
            "--response-timeout" }, description = "timeout in milliseconds for receiving outstanding response messages. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int responseTimeout;

    @Option(names = {
            "--tcp-delay" }, description = "set TCP_NODELAY socket option to false. Defaults to true.", required = false)
    private boolean tcpDelay;

    @Option(names = {
            "--tls" }, description = "enable TLS connection, equivalent to --tls-ciphers SSL_RSA_WITH_NULL_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA --tls-protocols TLSv1.2,TLSv1.1,TLSv1,SSLv3", required = false)
    private boolean tls = false;

    @Option(names = {
            "--tls-protocols", }, description = "TLS protocols. Separate with comma to specify multiple protocols. Supported protocols are: TLSv1.2, TLSv1.1, TLSv1, SSLv3 and SSLv2Hello", arity = "1", required = false, converter = TLSProtocols.Converter.class)
    private String[] tlsProtocols;

    @Option(names = {
            "--tls-ciphers", }, description = "TLS ciphers. Separate with comma to specify multiple ciphers.", split = ",", required = false, converter = TLSCiphers.Converter.class)
    private String[] tlsCiphers;

    @Option(names = { "--tls-no-auth" }, description = "disable client authentication for TLS", required = false)
    private boolean tlsNoAuth = false;

    @Option(names = {
            "--key-store" }, description = "Path to the key store file.", defaultValue = StoreSCU.DEFAULT_KEY_STORE_URL)
    private String keyStore;

    @Option(names = { "--key-store-type" }, description = "Key store type. Defaults to "
            + StoreSCU.DEFAULT_KEY_STORE_TYPE, defaultValue = StoreSCU.DEFAULT_KEY_STORE_TYPE)
    private String keyStoreType;

    @Option(names = { "--key-store-pass" }, description = "Key store password. Defaults to "
            + StoreSCU.DEFAULT_KEY_STORE_PASS, defaultValue = StoreSCU.DEFAULT_KEY_STORE_PASS)
    private String keyStorePass;

    @Option(names = { "--key-pass" }, description = "Password to access the key in the key store. Defaults to "
            + StoreSCU.DEFAULT_KEY_PASS, defaultValue = StoreSCU.DEFAULT_KEY_PASS)
    private String keyPass;

    @Option(names = {
            "--trust-store" }, description = "Path to the trust store file.", defaultValue = StoreSCU.DEFAULT_TRUST_STORE_URL)
    private String trustStore;

    @Option(names = { "--trust-store-type" }, description = "Trust store type. Defaults to "
            + StoreSCU.DEFAULT_TRUST_STORE_TYPE, defaultValue = StoreSCU.DEFAULT_TRUST_STORE_TYPE)
    private String trustStoreType;

    @Option(names = { "--trust-store-pass" }, description = "Trust store password. Defaults to "
            + StoreSCU.DEFAULT_TRUST_STORE_PASS, defaultValue = StoreSCU.DEFAULT_TRUST_STORE_PASS)
    private String trustStorePass;

    @Option(names = { "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Parameters(description = "DICOM files or directories", arity = "1..", paramLabel = "DICOM_FILES")
    private Path[] dcmFiles;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        LoggingUtils.setLogLevel(this.verbose ? Level.INFO : Level.WARN);
        Options.Builder ob = new Options.Builder();
        ob.setApplicationEntity(this.callingAE);
        ob.setRemoteApplicationEntity(this.calledAE);
        ob.setHttpProxy(this.httpProxy);
        ob.setUidSuffix(this.uidSuffix);
        ob.setMaxOpsInvoked(this.maxOpsInvoked);
        ob.setMaxOpsPerformed(this.maxOpsPerformed);
        ob.setPackPDV(this.packPDV);
        ob.setConnectTimeout(this.connectTimeout);
        ob.setAcceptTimeout(this.acceptTimeout);
        ob.setIdleTimeout(this.idleTimeout);
        ob.setReleaseTimeout(this.releaseTimeout);
        ob.setResponseTimeout(this.responseTimeout);
        ob.setTcpDelay(this.tcpDelay);
        ob.setTls(this.tls);
        ob.addTlsProtocols(this.tlsProtocols);
        ob.addTlsCiphers(this.tlsCiphers);
        ob.setTlsNoAuth(this.tlsNoAuth);
        ob.addAttributes(attributeSpecs);
        ob.setKeyStore(this.keyStore, this.keyStoreType, this.keyStorePass);
        ob.setKeyPass(this.keyPass);
        ob.setTrustStore(this.trustStore, this.trustStoreType, this.trustStorePass);
        Options options = ob.build();

        Progress progress = new StoreSCU(options).send(dcmFiles);
        System.out.println(String.format("Sent %s", progress));
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new StoreSCUCommand()).execute(args));
    }

}
