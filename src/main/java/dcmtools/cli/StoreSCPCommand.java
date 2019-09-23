package dcmtools.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import dcmtools.network.TLSCiphers;
import dcmtools.network.TLSProtocols;
import dcmtools.network.storescp.Options;
import dcmtools.network.storescp.StoreSCP;
import dcmtools.util.LoggingUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "storescp", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE\n  ", customSynopsis = {
        "storescp [OPTIONS] -c <title@host:port> DICOM_FILES..." }, descriptionHeading = "\nDESCRIPTION\n  ", description = "Store SCP server.", parameterListHeading = "\nPARAMETERS\n", optionListHeading = "\nOPTIONS\n", sortOptions = false, version = StoreSCUCommand.VERSION, separator = " ")
public class StoreSCPCommand implements Callable<Integer> {

    public static final String VERSION = "1.0.0";

    @Option(names = { "-d",
            "--directory" }, description = "Output directory. Defaults to current working directory.", required = false, paramLabel = "<directory>")
    private Path directory = Paths.get(System.getProperty("user.dir"));

    @Option(names = { "--aet" }, description = "local application entity title. Defaults to "
            + StoreSCP.DEFAULT_AE_TITLE, required = false, paramLabel = "<title>")
    private String aeTitle = StoreSCP.DEFAULT_AE_TITLE;

    @Option(names = {
            "--host" }, description = "local host address to bind to.", required = false, paramLabel = "<host>")
    private String host;

    @Option(names = { "--port" }, description = "local port to bind to. Defaults to "
            + StoreSCP.DEFAULT_PORT, required = false, paramLabel = "<port>")
    private int port = StoreSCP.DEFAULT_PORT;

    @Option(names = { "--sop-classes" }, description = "file path or URL of list of accepted SOP Classes. Defaults to "
            + StoreSCP.DEFAULT_SOP_CLASSES_PROPERTIES_FILE, required = false, paramLabel = "<file>")
    private String sopClassesFile;

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
            "--idle-timeout" }, description = "timeout in milliseconds for receiving DIMSE-RQ. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int idleTimeout;

    @Option(names = {
            "--release-timeout" }, description = "timeout in milliseconds for receiving A-RELEASE-RP. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int releaseTimeout;

    @Option(names = {
            "--request-timeout" }, description = "timeout in milliseconds for receiving outstanding response messages. No timeout by default.", arity = "1", required = false, paramLabel = "<ms>", defaultValue = "0")
    private int requestTimeout;

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
            "--key-store" }, description = "Path to the key store file.", defaultValue = StoreSCP.DEFAULT_KEY_STORE_URL)
    private String keyStore;

    @Option(names = { "--key-store-type" }, description = "Key store type. Defaults to "
            + StoreSCP.DEFAULT_KEY_STORE_TYPE, defaultValue = StoreSCP.DEFAULT_KEY_STORE_TYPE)
    private String keyStoreType;

    @Option(names = { "--key-store-pass" }, description = "Key store password. Defaults to "
            + StoreSCP.DEFAULT_KEY_STORE_PASS, defaultValue = StoreSCP.DEFAULT_KEY_STORE_PASS)
    private String keyStorePass;

    @Option(names = { "--key-pass" }, description = "Password to access the key in the key store. Defaults to "
            + StoreSCP.DEFAULT_KEY_PASS, defaultValue = StoreSCP.DEFAULT_KEY_PASS)
    private String keyPass;

    @Option(names = {
            "--trust-store" }, description = "Path to the trust store file.", defaultValue = StoreSCP.DEFAULT_TRUST_STORE_URL)
    private String trustStore;

    @Option(names = { "--trust-store-type" }, description = "Trust store type. Defaults to "
            + StoreSCP.DEFAULT_TRUST_STORE_TYPE, defaultValue = StoreSCP.DEFAULT_TRUST_STORE_TYPE)
    private String trustStoreType;

    @Option(names = { "--trust-store-pass" }, description = "Trust store password. Defaults to "
            + StoreSCP.DEFAULT_TRUST_STORE_PASS, defaultValue = StoreSCP.DEFAULT_TRUST_STORE_PASS)
    private String trustStorePass;

    @Option(names = { "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-v", "--verbose" }, description = "verbose")
    private boolean verbose = false;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        LoggingUtils.setLogLevel(this.verbose ? Level.INFO : Level.WARN);
        Options.Builder ob = new Options.Builder();
        if (!Files.exists(this.directory) || !Files.isDirectory(this.directory)) {
            throw new IllegalArgumentException("Directory: " + this.directory + " does not exist.");
        }
        ob.setDirectory(this.directory);
        ob.setApplicationEntity(this.aeTitle, this.host, this.port);
        ob.setMaxOpsInvoked(this.maxOpsInvoked);
        ob.setMaxOpsPerformed(this.maxOpsPerformed);
        ob.setPackPDV(this.packPDV);
        ob.setIdleTimeout(this.idleTimeout);
        ob.setReleaseTimeout(this.releaseTimeout);
        ob.setRequestTimeout(this.releaseTimeout);
        ob.setTcpDelay(this.tcpDelay);
        ob.setTls(this.tls);
        ob.addTlsProtocols(this.tlsProtocols);
        ob.addTlsCiphers(this.tlsCiphers);
        ob.setTlsNoAuth(this.tlsNoAuth);
        ob.setKeyStore(this.keyStore, this.keyStoreType, this.keyStorePass);
        ob.setKeyPass(this.keyPass);
        ob.setTrustStore(this.trustStore, this.trustStoreType, this.trustStorePass);
        if (this.sopClassesFile != null) {
            ob.loadSopClasses(Paths.get(sopClassesFile));
        } else {
            ob.loadDefaultSopClasses();
        }
        Options options = ob.build();

        new StoreSCP(options).start();

        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new StoreSCPCommand()).execute(args);
    }

}