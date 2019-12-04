package dcmtools.siemens.raw.pet;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

public class RawPETFile {

    private static final Logger logger = LogManager.getLogger(RawPETFile.class);

    public static final int DICOM_PREAMBLE_LENGTH = 132;

    public static Attributes readDicomAttributes(File f) throws IOException {
        return readDicomAttributes(f.toPath());
    }

    public static Attributes readDicomAttributes(Path f) throws IOException {
        long length = Files.size(f);
        if (length < DICOM_PREAMBLE_LENGTH) {
            throw new IOException("File: '" + f + "' does not contain DICOM preamble (132 bytes).");
        }
        try (InputStream is = Files.newInputStream(f); BufferedInputStream bis = new BufferedInputStream(is)) {
            Buffer buffer = new Buffer();
            long offset = 0;
            while (!buffer.isPreamble()) {
                int v = bis.read();
                if (v == -1) {
                    throw new EOFException("DICOM header is not found in file: '" + f + "'");
                }
                buffer.put((byte) v);
                offset++;
            }
            logger.debug(String.format("Found DICOM preamble at offset: %08XH", (offset - DICOM_PREAMBLE_LENGTH)));
            try (DicomInputStream dis = new DicomInputStream(bis)) {
                Attributes attrs = dis.readFileMetaInformation();
                dis.readAttributes(attrs, -1, 0x7fe11010);
                return attrs;
            }
        }
    }

    public static Attributes getDicomAttributes(File f) {
        return getDicomAttributes(f.toPath());
    }

    public static Attributes getDicomAttributes(Path f) {
        try {
            return readDicomAttributes(f);
        } catch (Throwable e) {
            logger.debug("Failed to read DICOM attributes from file: '" + f + "'", e);
            return null;
        }
    }

    public static boolean hasDicomAttributes(File f) {
        return hasDicomAttributes(f.toPath());
    }

    public static boolean hasDicomAttributes(Path f) {
        Attributes attrs = getDicomAttributes(f);
        return attrs != null && !attrs.isEmpty();
    }

    private static class Buffer {

        private byte[] _b = new byte[132];
        private int _i = 0;

        public void put(byte b) {
            if (_i == _b.length) {
                shift();
            }
            _b[_i] = b;
            _i++;
        }

        private void shift() {
            for (int i = 0; i < _b.length - 1; i++) {
                _b[i] = _b[i + 1];
            }
            _i--;
        }

        public boolean isPreamble() {
            if (!"DICM".equals(new String(_b, 128, 4))) {
                return false;
            }
            for (int i = 0; i < 128; i++) {
                if (_b[i] != 0) {
                    return false;
                }
            }
            return true;
        }

    }

    public static void main(String[] args) throws IOException {
        // @formatter:off
        /*
        Path f1 = Paths.get(
                "/tmp/Jay_Adams.PT.PET_tbi_nav4694_50_70_(Adult).602.PET_COUNTRATE.2019.07.29.16.03.20.924000.2.0.5828969.ptd");
        Path f2 = Paths.get(
                "/tmp/Jay_Adams.PT.PET_tbi_nav4694_50_70_(Adult).602.PETCT_SPL.2019.07.29.16.03.20.919000.2.0.5828919.ptd");
        Attributes attrs1 = readDicomAttributes(f1);
        System.out.println(attrs1.getString(Tag.StudyDate));
        Attributes attrs2 = readDicomAttributes(f2);
        System.out.println(attrs2.getString(Tag.StudyDate));
        */
        // @formatter:on
    }

}
