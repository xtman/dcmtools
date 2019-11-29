package dcmtools.siemens.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcmtools.siemens.csa.CSADicomFile;
import dcmtools.util.LoggingUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "siemens-csa2rda", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE:\n  ", descriptionHeading = "\nDESCRIPTION:\n  ", description = "Converts Siemens CSA NON Image DICOM file to Siemens RDA file.", parameterListHeading = "\nPARAMETERS:\n", optionListHeading = "\nOPTIONS:\n", sortOptions = false, version = CSA2RDACommand.VERSION, separator = " ")
public class CSA2RDACommand implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(CSA2RDACommand.class);
    
    public static final String VERSION = "0.0.1";

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-V", "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Option(names = { "-v", "--verbose" }, defaultValue="false", description = "verbose mode")
    private boolean verbose;

    @Parameters(description = "Input Siemens CSA DICOM file.", index = "0", paramLabel = "CSA_FILE")
    private Path csaFile;

    @Parameters(description = "Output Siemens RDA file. If not specified, output to the same directory as the input file.", index = "1", arity = "0..1", paramLabel = "RDA_FILE")
    private Path rdaFile;

    @Override
    public Integer call() throws Exception {

        LoggingUtils.setLogLevel(this.verbose ? Level.ALL : Level.WARN);

        if (!Files.exists(this.csaFile)) {
            throw new IllegalArgumentException("File: '" + this.csaFile + "' does not exist.");
        }

        if (Files.isDirectory(this.csaFile)) {
            throw new IllegalArgumentException("File: '" + this.csaFile + "' is a directory.");
        }

        if (!CSADicomFile.isCSADicomFile(this.csaFile)) {
            throw new IllegalArgumentException("File: '" + this.csaFile + "' is not a CSA DICOM file.");
        }

        if (this.rdaFile == null) {
            String dir = this.csaFile.getParent().toString();
            String filename = this.csaFile.getFileName().toString();
            if (filename.toLowerCase().endsWith(".dcm")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            filename += ".rda";
            this.rdaFile = Paths.get(dir, filename);
        }

        try {
            logger.info(String.format("converting '%s' to '%s'", this.csaFile, this.rdaFile));
            CSADicomFile.toRDA(this.csaFile, this.rdaFile);
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
        System.exit(new CommandLine(new CSA2RDACommand()).execute(args));
    }

}
