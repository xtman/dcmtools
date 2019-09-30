package dcmtools.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import dcmtools.data.DicomDump;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "dcmdump", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE\n  ", descriptionHeading = "\nDESCRIPTION\n  ", description = "Prints attributes of the DICOM file.", parameterListHeading = "\nPARAMETERS\n", optionListHeading = "\nOPTIONS\n", sortOptions = false, version = DicomDumpCommand.VERSION, separator = " ")
public class DicomDumpCommand implements Callable<Integer> {

    public static final String VERSION = "1.0.0";

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Parameters(description = "DICOM file", arity = "1", index = "0", paramLabel = "DICOM_FILE")
    private Path dicomFile;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        DicomDump.dump(dicomFile);
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new DicomDumpCommand()).execute(args));
    }

}
