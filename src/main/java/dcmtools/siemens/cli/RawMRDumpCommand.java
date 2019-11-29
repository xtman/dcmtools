package dcmtools.siemens.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcmtools.siemens.raw.mr.RawMRFile;
import dcmtools.util.LoggingUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "siemens-raw-mr-dump", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE:\n  ", descriptionHeading = "\nDESCRIPTION:\n  ", description = "Dumps the meta data header of Siemens raw MR file.", parameterListHeading = "\nPARAMETERS:\n", optionListHeading = "\nOPTIONS:\n", sortOptions = false, version = RawMRDumpCommand.VERSION, separator = " ")
public class RawMRDumpCommand implements Callable<Integer> {

    static final Logger logger = LogManager.getLogger(RawMRDumpCommand.class);

    public static final String VERSION = "0.0.1";

    @Option(names = { "-m", "--metadata" }, defaultValue = "false", description = "prints metadata header.")
    private boolean metadata = false;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-V", "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Option(names = { "-v", "--verbose" }, defaultValue = "false", description = "verbose mode")
    private boolean verbose;

    @Parameters(description = "The Siemens raw MR file.", index = "0", arity = "1", paramLabel = "RAW_MR_FILE")
    private Path rawMRFile;

    @Override
    public Integer call() throws Exception {

        LoggingUtils.setLogLevel(this.verbose ? Level.ALL : Level.WARN);

        if (!Files.exists(this.rawMRFile)) {
            throw new IllegalArgumentException("File: '" + this.rawMRFile + "' does not exist.");
        }

        if (Files.isDirectory(this.rawMRFile)) {
            throw new IllegalArgumentException("File: '" + this.rawMRFile + "' is a directory.");
        }

        try {
            RawMRFile.dump(this.rawMRFile, this.metadata);
        } catch (Throwable e) {
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                throw new Exception(e);
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new RawMRDumpCommand()).execute(args));
    }

}
