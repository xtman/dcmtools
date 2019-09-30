package dcmtools.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dcmtools.data.DicomTidy;
import dcmtools.util.DicomFilePathPattern;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "dcmtidy", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE:\n  ", descriptionHeading = "\nDESCRIPTION:\n  ", description = "Re-organize the DICOM files in the source directory.", parameterListHeading = "\nPARAMETERS:\n", optionListHeading = "\nOPTIONS:\n", sortOptions = false, version = DicomTidyCommand.VERSION, separator = " ")
public class DicomTidyCommand implements Callable<Integer> {

    public static final String VERSION = "1.0.0";

    @Option(names = { "-p",
            "--pattern" }, required = false, description = "the pattern to generate file path at the destination.")
    private String pattern = DicomFilePathPattern.DEFAULT_PATTERN;

    @Option(names = { "-o",
            "--overwrite" }, required = false, description = "overwrite if the file already exists at the destination")
    private boolean overwrite = false;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "-v", "--verbose" }, description = "verbose")
    private boolean verbose = false;

    @Option(names = { "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Parameters(description = "source directory contains DICOM files", index = "0", paramLabel = "SRC_DIR")
    private Path srcDir;

    @Parameters(description = "destination directory", index = "1", paramLabel = "DST_DIR")
    private Path dstDir;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        try {
            validateArguments();
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            new CommandLine(this).usage(System.err);
            return 1;
        }
        new DicomTidy(pattern, overwrite).tidy(srcDir, dstDir);
        return 0;
    }

    private void validateArguments() throws IllegalArgumentException {
        if (!Files.exists(srcDir)) {
            throw new IllegalArgumentException("Directory: '" + srcDir + "' is not found.");
        }
        if (!Files.isDirectory(srcDir)) {
            throw new IllegalArgumentException("'" + srcDir + "' is not a directory.");
        }
        if (!Files.exists(dstDir)) {
            throw new IllegalArgumentException("Directory: '" + dstDir + "' is not found.");
        }
        if (!Files.isDirectory(dstDir)) {
            throw new IllegalArgumentException("'" + dstDir + "' is not a directory.");
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new DicomTidyCommand()).execute(args));
    }

}
