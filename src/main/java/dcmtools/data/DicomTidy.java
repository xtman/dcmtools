package dcmtools.data;

import dcmtools.util.DicomFilePathPattern;
import dcmtools.util.DicomFileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.AttributesFormat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class DicomTidy {

    private static final Logger logger = LogManager.getLogger(DicomTidy.class);

    private final AttributesFormat _format;
    private boolean _overwrite = false;

    public DicomTidy(String pattern, boolean replaceIfExists) {
        _format = new AttributesFormat(pattern == null ? DicomFilePathPattern.DEFAULT_PATTERN : pattern);
        _overwrite = replaceIfExists;
    }

    public DicomTidy() {
        this(null, false);
    }

    public void tidy(Path srcDir, Path dstDir) throws Exception {
        Set<Path> dicomFiles = DicomFileUtils.getDicomFiles(srcDir, false);
        logger.info("found " + dicomFiles.size() + " dicom files in '" + srcDir + "'");
        for (Path srcDicomFile : dicomFiles) {
            Attributes attrs = DicomFileUtils.getDicomAttributes(srcDicomFile);
            Path dstDicomFile = Paths.get(dstDir.toString(), _format.format(attrs));
            if (Files.exists(dstDicomFile)) {
                if (_overwrite) {
                    logger.info("moving '" + srcDicomFile + "' to '" + dstDicomFile + "' (already exists. Replace.)");
                    Files.move(srcDicomFile, dstDicomFile, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    logger.info("'" + dstDicomFile + "' already exists. Ignored.");
                }
            } else {
                logger.info("moving '" + srcDicomFile + "' to '" + dstDicomFile + "'");
                Path dstParentDir = dstDicomFile.getParent();
                if (dstParentDir != null && !Files.exists(dstParentDir)) {
                    Files.createDirectories(dstParentDir);
                }
                Files.move(srcDicomFile, dstDicomFile);
            }
        }
    }
}
