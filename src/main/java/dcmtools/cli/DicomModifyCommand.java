package dcmtools.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import dcmtools.DicomModify;
import dcmtools.data.AttributeSpec;
import dcmtools.util.DicomFiles;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "dcmodify", abbreviateSynopsis = true, usageHelpWidth = 120, synopsisHeading = "\nUSAGE\n  ", descriptionHeading = "\nDESCRIPTION\n  ", description = "Modify DICOM file header.", parameterListHeading = "\nPARAMETERS\n", optionListHeading = "\nOPTIONS\n", sortOptions = false, version = DicomDumpCommand.VERSION, separator = " ")
public class DicomModifyCommand implements Callable<Integer> {

    public static final String VERSION = "1.0.0";

    @Option(names = { "-a",
            "--attribute" }, description = "specify attributes to update. It can be specified by keyword or tag value (in hex), e.g. PatientName or 00100010. Attributes in nested Datasets can be specified by including the keyword/tag value of the sequence attribute, e.g. 00400275/00400009 for Scheduled Procedure Step ID in the Request Attributes Sequence.", required = false, paramLabel = "<tag=value>", converter = AttributeSpec.Converter.class)
    private List<AttributeSpec> updateAttrs;

    @Option(names = { "-d",
            "--delete-attribute" }, description = "specify attributes to delete. It can be specified by keyword or tag value (in hex), e.g. PatientName or 00100010. Attributes in nested Datasets can be specified by including the keyword/tag value of the sequence attribute, e.g. 00400275/00400009 for Scheduled Procedure Step ID in the Request Attributes Sequence.", required = false, paramLabel = "<tag>", converter = AttributeSpec.Converter.class)
    private List<AttributeSpec> deleteAttrs;

    @Option(names = { "-b", "--backup" }, required = false, description = "keep the original file as backup.")
    private boolean backup;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "output usage information")
    private boolean printHelp;

    @Option(names = { "--version" }, versionHelp = true, description = "output version information")
    private boolean printVersion;

    @Parameters(description = "DICOM files or directories", arity = "1..", paramLabel = "DICOM_FILES")
    private Path[] dcmFiles;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        DicomFiles dicomFiles = DicomFiles.scan(dcmFiles);
        DicomModify.updateDicomFiles(dicomFiles, backup, updateAttrs, deleteAttrs);
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new DicomModifyCommand()).execute(args));
    }

}
