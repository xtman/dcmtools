package dcmtools.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.DicomInputStream;

public class DicomFiles extends TreeSet<DicomFileInfo> {

    private static Logger logger = LogManager.getLogger(DicomFiles.class);

    private static final long serialVersionUID = -2990819678387165831L;

    private SAXParser _saxParser;

    private Map<String, String> _tss = new LinkedHashMap<String, String>();

    public void add(List<Path> sources, boolean followLinks) throws Throwable {
        if (sources != null) {
            for (Path src : sources) {
                add(src, followLinks);
            }
        }
    }

    public void add(Path f, boolean followLinks) throws Exception {
        if (Files.isDirectory(f)) {
            addDirectory(f, followLinks);
        } else {
            addFile(f);
        }
    }

    public boolean addFile(Path f) throws Exception {
        if (f.toString().toLowerCase().endsWith(".xml")) {
            if (_saxParser == null) {
                _saxParser = SAXParserFactory.newInstance().newSAXParser();
            }
            Attributes ds = new Attributes();
            ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
            _saxParser.parse(f.toFile(), ch);
            Attributes fmi = ch.getFileMetaInformation();
            if (fmi == null) {
                fmi = ds.createFileMetaInformation(UID.ExplicitVRLittleEndian);
            }
            logger.info("adding parsed DICOM file: '" + f + "'");
            DicomFileInfo dfi = new DicomFileInfo(f, -1, fmi, ds);
            _tss.put(dfi.mediaStorageSOPClassUID, dfi.transferSyntaxUID);
            return add(dfi);
        } else if (DicomFileUtils.isDicomFile(f)) {
            try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(f)))) {
                Attributes fmi = dis.getFileMetaInformation();
                long dsOffset = dis.getPosition();
                Attributes ds = dis.readDataset(-1, -1);
                if (fmi == null || !fmi.containsValue(Tag.TransferSyntaxUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID)) {
                    fmi = ds.createFileMetaInformation(dis.getTransferSyntax());
                }
                logger.info("adding DICOM file: '" + f + "'");
                DicomFileInfo dfi = new DicomFileInfo(f, dsOffset, fmi, ds);
                _tss.put(dfi.mediaStorageSOPClassUID, dfi.transferSyntaxUID);
                return add(dfi);
            }
        } else {
            logger.info("file: '" + f + "' is not a DICOM file. Skipped.");
        }
        return false;
    }

    public void addDirectory(Path dir, boolean followLinks) throws IOException {

        Files.walkFileTree(dir,
                followLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class),
                Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                        try {
                            addFile(f);
                        } catch (Throwable e) {
                            if (e instanceof IOException) {
                                throw (IOException) e;
                            } else {
                                throw new IOException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ioe) {
                        logger.warn("Failed to visit file: '" + file + "'");
                        if (ioe != null) {
                            logger.error(ioe.getMessage(), ioe);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException ioe) {
                        if (ioe != null) {
                            logger.error(ioe.getMessage(), ioe);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return super.preVisitDirectory(dir, attrs);
                    }
                });

    }

    public Map<String, String> transferSyntax() {
        return Collections.unmodifiableMap(_tss);
    }

    public static DicomFiles scan(Path... paths) throws Exception {
        return scan(true, paths);
    }

    public static DicomFiles scan(boolean followLinks, Path... paths) throws Exception {
        DicomFiles dfs = new DicomFiles();
        for (Path path : paths) {
            dfs.add(path, followLinks);
        }
        return dfs;
    }
}
