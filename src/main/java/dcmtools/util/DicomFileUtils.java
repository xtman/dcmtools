package dcmtools.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;

public class DicomFileUtils {

	private static final Logger logger = LogManager.getLogger(DicomFileUtils.class);

	private static final int BUFFER_SIZE = 160;

	private static int readFully(InputStream in, byte[] buffer) throws IOException {
		int offset = 0;
		int remaining = buffer.length;
		while (remaining > 0) {
			int n = in.read(buffer, offset, remaining);
			if (n == -1) {
				break;
			}
			remaining -= n;
			offset += n;
		}
		return offset;
	}

	private static final int extractUnsigned16(byte[] buffer, int offset, boolean bigEndian) {
		short v1 = (short) (buffer[offset + 0] & 0xff);
		short v2 = (short) (buffer[offset + 1] & 0xff);
		return (short) (bigEndian ? (v1 << 8) | v2 : (v2 << 8) | v1);
	}

	private static final long extractUnsigned32(byte[] buffer, int offset, boolean bigEndian) {
		long v1 = ((long) buffer[offset + 0]) & 0xff;
		long v2 = ((long) buffer[offset + 1]) & 0xff;
		long v3 = ((long) buffer[offset + 2]) & 0xff;
		long v4 = ((long) buffer[offset + 3]) & 0xff;
		return bigEndian ? (((((v1 << 8) | v2) << 8) | v3) << 8) | v4 : (((((v4 << 8) | v3) << 8) | v2) << 8) | v1;
	}

	public static boolean isDicomFile(BufferedInputStream in) throws IOException {
		in.mark(BUFFER_SIZE);
		try {
			byte[] b = new byte[BUFFER_SIZE];
			int length = readFully(in, b);
			// @formatter:off
			if (length >= 136 && new String(b, 128, 4).equals("DICM") && extractUnsigned16(b, 132, false) == 0x0002) {
				// little endian header
				return true;
			} else if (length >= 136 && new String(b, 128, 4).equals("DICM")
					&& extractUnsigned16(b, 132, true) == 0x0002) {
				// big endian header
				return true;
			} else if (length >= 8 && extractUnsigned16(b, 0, false) == 0x0008
					&& extractUnsigned16(b, 2, false) <= 0x0018 /* SOPInstanceUID */
					&& (extractUnsigned32(b, 4, false) <= 0x0100/* valid VL */
							|| (Character.isUpperCase((char) (b[4]))
									&& Character.isUpperCase((char) (b[5]))) /* Upper case VR/ */)) {
				return true;
			} else if (length >= 8 && extractUnsigned16(b, 0, true) == 0x0008
					&& extractUnsigned16(b, 2, true) <= 0x0018 /* SOPInstanceUID */
					&& (extractUnsigned32(b, 4, true) <= 0x0100/* valid VL */
							|| (Character.isUpperCase((char) (b[4]))
									&& Character.isUpperCase((char) (b[5]))) /* Upper case VR? */)) {
				return true;
			}
			return false;
			// @formatter:on
		} finally {
			in.reset();
		}
	}

	public static boolean isDicomFile(File f) throws IOException {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
			if (isDicomFile(in)) {
				return true;
			} else {
				return hasNecessaryAttributes(in);
			}
		}
	}

	public static boolean isDicomFile(Path f) throws IOException {
		if (Files.size(f) <= 0) {
			return false;
		}
		try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(f))) {
			if (isDicomFile(in)) {
				return true;
			} else {
				try {
					return hasNecessaryAttributes(in);
				} catch (Throwable e) {
					return false;
				}
			}
		}
	}

	public static boolean hasNecessaryAttributes(InputStream in) throws IOException {
		try (DicomInputStream dis = new DicomInputStream(in)) {
			Attributes attrs = dis.readDataset(-1, Tag.PixelData);
			boolean hasSopInstanceUID = attrs.getString(Tag.SOPInstanceUID) != null
					|| attrs.getString(Tag.MediaStorageSOPInstanceUID) != null;
			boolean hasSopClassUID = attrs.getString(Tag.SOPClassUID) != null
					|| attrs.getString(Tag.MediaStorageSOPClassUID) != null;
			boolean hasTransferSyntax = attrs.getString(Tag.TransferSyntaxUID) != null
					|| dis.getTransferSyntax() != null;
			return hasSopClassUID && hasSopInstanceUID && hasTransferSyntax;
		}
	}

	public static Set<Path> getDicomFiles(Path dir, boolean followLinks) throws IOException {
		Set<Path> dicomFiles = new LinkedHashSet<Path>();
		addDicomFiles(dir, followLinks, dicomFiles);
		return dicomFiles;
	}

	public static void addDicomFiles(Path root, boolean followLinks, Set<Path> dicomFiles) throws IOException {
		Files.walkFileTree(root,
				followLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class),
				Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
						if (isDicomFile(f)) {
							dicomFiles.add(f);
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

	public static Attributes getDicomAttributes(DicomInputStream dis, int stopTag, boolean includeBulkData,
			boolean includeFileMetaInfo) throws IOException {
		if (!includeBulkData && dis.getIncludeBulkData() != IncludeBulkData.NO) {
			dis.setIncludeBulkData(IncludeBulkData.NO);
		}
		Attributes attrs = dis.readDataset(-1, stopTag);
		if (includeFileMetaInfo) {
			attrs.addAll(dis.getFileMetaInformation());
		}
		return attrs;
	}

	public static Attributes getDicomAttributes(DicomInputStream dis, boolean includeBulkData,
			boolean includeFileMetaInfo) throws IOException {
		return getDicomAttributes(dis, Tag.PixelData, includeBulkData, includeFileMetaInfo);
	}

	public static Attributes getDicomAttributes(DicomInputStream dis) throws IOException {
		return getDicomAttributes(dis, Tag.PixelData, false, true);
	}

	public static Attributes getDicomAttributes(Path f, boolean includeBulkData) throws IOException {
		try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(f)))) {
			return getDicomAttributes(dis, includeBulkData, true);
		}
	}

	public static Attributes getDicomAttributes(Path f) throws IOException {
		return getDicomAttributes(f, false);
	}

	public static Attributes getDicomAttributes(File f, boolean includeBulkData) throws IOException {
		try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(new FileInputStream(f)))) {
			return getDicomAttributes(dis, includeBulkData, true);
		}
	}

	public static Attributes getDicomAttributes(File f) throws IOException {
		return getDicomAttributes(f, false);
	}
}
