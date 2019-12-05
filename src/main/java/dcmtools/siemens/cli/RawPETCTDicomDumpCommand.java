package dcmtools.siemens.cli;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.io.DicomInputStream;

import dcmtools.data.DicomDump;
import dcmtools.siemens.raw.pet.RawPETCTFile;
import dcmtools.util.LoggingUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "siemens-raw-petct-dicom-dump", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE:\n  ", descriptionHeading = "\nDESCRIPTION:\n  ", description = "Dumps the meta data header of Siemens raw MR file.", parameterListHeading = "\nPARAMETERS:\n", optionListHeading = "\nOPTIONS:\n", sortOptions = false, version = RawMRDumpCommand.VERSION, separator = " ")
public class RawPETCTDicomDumpCommand implements Callable<Integer> {

    static final Logger logger = LogManager.getLogger(RawPETCTDicomDumpCommand.class);

    public static final String VERSION = "0.0.1";

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-V", "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Option(names = { "-v", "--verbose" }, defaultValue = "false", description = "verbose mode")
    private boolean verbose;

    @Parameters(description = "The Siemens raw PET CT file.", index = "0", arity = "1", paramLabel = "RAW_PET_CT_FILE")
    private Path rawPTFile;

    @Override
    public Integer call() throws Exception {

        LoggingUtils.setLogLevel(this.verbose ? Level.ALL : Level.WARN);

        if (!Files.exists(this.rawPTFile)) {
            throw new IllegalArgumentException("File: '" + this.rawPTFile + "' does not exist.");
        }

        if (Files.isDirectory(this.rawPTFile)) {
            throw new IllegalArgumentException("File: '" + this.rawPTFile + "' is a directory.");
        }

        try {
            try (InputStream is = Files.newInputStream(this.rawPTFile);
                    BufferedInputStream bis = new BufferedInputStream(is)) {
                long offset = RawPETCTFile.seekToDicomAttributes(bis);
                try (DicomInputStream dis = new DicomInputStream(bis)) {
                    new DicomDump().dump(offset, dis, RawPETCTFile.DATA_TAG);
                }
            }
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
        System.exit(new CommandLine(new RawPETCTDicomDumpCommand()).execute(args));
    }

}
