package dcmtools.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Attributes.UpdatePolicy;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.TagUtils;

import dcmtools.util.DicomFileInfo;
import dcmtools.util.DicomFiles;

public class DicomModify {

	private static final Logger logger = LogManager.getLogger(DicomModify.class);

	public static void updateDicomFiles(DicomFiles dicomFiles, boolean backup, Attributes fmiUpdate,
			List<int[]> fmiRemove, Attributes dsUpdate, List<int[]> dsRemove) throws IOException {
		if (dicomFiles != null && !dicomFiles.isEmpty()) {
			for (DicomFileInfo fi : dicomFiles) {
				updateDicomFile(fi.path, backup, fmiUpdate, fmiRemove, dsUpdate, dsRemove);
			}
		}
	}

	public static void updateDicomFile(Path dicomFile, boolean backup, Attributes fmiUpdate, List<int[]> fmiRemove,
			Attributes dsUpdate, List<int[]> dsRemove) throws IOException {
		Path dir = dicomFile.getParent();
		Path dstDicomFile = Files.createTempFile(dir, dicomFile.getFileName().toString(), ".tmp");
		logger.info("updating DICOM file: " + dicomFile);
		updateDicomFile(dicomFile, dstDicomFile, fmiUpdate, fmiRemove, dsUpdate, dsRemove);
		if (backup) {
			Path backupFile = Paths.get(dir.toString(), dicomFile.getFileName().toString() + ".bak");
			Files.move(dicomFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
		}
		Files.move(dstDicomFile, dicomFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void updateDicomFile(Path dicomFile, Path dstDicomFile, Attributes fmiUpdate, List<int[]> fmiRemove,
			Attributes dsUpdate, List<int[]> dsRemove) throws IOException {
		try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(dicomFile)))) {
			Attributes fmi = dis.readFileMetaInformation();
			Attributes ds = dis.readDataset(-1, -1);
			updateAttributes(fmi, fmiUpdate, fmiRemove, ds, dsUpdate, dsRemove);
			try (DicomOutputStream dos = new DicomOutputStream(
					new BufferedOutputStream(Files.newOutputStream(dstDicomFile)), UID.ExplicitVRLittleEndian)) {
				dos.writeDataset(fmi, ds);
			}
		}
	}

	public static void updateDicomFiles(DicomFiles dicomFiles, boolean backup, List<AttributeSpec> update,
			List<AttributeSpec> remove) throws IOException {
		if (dicomFiles != null && !dicomFiles.isEmpty()) {
			for (DicomFileInfo fi : dicomFiles) {
				updateDicomFile(fi.path, backup, update, remove);
			}
		}
	}

	public static void updateDicomFile(Path dicomFile, boolean backup, List<AttributeSpec> update,
			List<AttributeSpec> remove) throws IOException {
		Path dir = dicomFile.getParent();
		Path dstDicomFile = Files.createTempFile(dir, dicomFile.getFileName().toString(), ".tmp");
		logger.info("updating DICOM file: " + dicomFile);
		updateDicomFile(dicomFile, dstDicomFile, update, remove);
		if (backup) {
			Path backupFile = Paths.get(dir.toString(), dicomFile.getFileName().toString() + ".bak");
			Files.move(dicomFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
		}
		Files.move(dstDicomFile, dicomFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void updateDicomFile(Path dicomFile, Path dstDicomFile, List<AttributeSpec> update,
			List<AttributeSpec> remove) throws IOException {

		try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(dicomFile)))) {
			Attributes fmi = dis.readFileMetaInformation();
			Attributes ds = dis.readDataset(-1, -1);
			if ((update != null && !update.isEmpty()) || (remove != null && !remove.isEmpty())) {
				Attributes fmiUpdate = new Attributes();
				Attributes dsUpdate = new Attributes();
				if (update != null) {
					for (AttributeSpec a : update) {
						int[] tags = a.tags();
						if (TagUtils.isFileMetaInformation(tags[0])) {
							a.addToAttributes(fmiUpdate);
						} else {
							a.addToAttributes(dsUpdate);
						}
					}
				}
				List<int[]> dsRemove = new ArrayList<int[]>();
				List<int[]> fmiRemove = new ArrayList<int[]>();
				if (remove != null) {
					for (AttributeSpec a : remove) {
						int[] tags = a.tags();
						if (TagUtils.isFileMetaInformation(tags[0])) {
							fmiRemove.add(tags);
						} else {
							dsRemove.add(tags);
						}
					}
				}
				updateAttributes(fmi, fmiUpdate, fmiRemove);
				updateAttributes(ds, dsUpdate, dsRemove);
			}
			if (remove != null && !remove.isEmpty()) {

			}
			try (DicomOutputStream dos = new DicomOutputStream(
					new BufferedOutputStream(Files.newOutputStream(dstDicomFile)), UID.ExplicitVRLittleEndian)) {
				dos.writeDataset(fmi, ds);
			}
		}
	}

	public static void updateAttributes(Attributes fmi, Attributes fmiUpdate, List<int[]> fmiRemove, Attributes ds,
			Attributes dsUpdate, List<int[]> dsRemove) {
		/*
		 * file meta information
		 */
		if (fmiUpdate != null && !fmiUpdate.isEmpty()) {
			fmi.update(UpdatePolicy.OVERWRITE, fmiUpdate, null);
		}
		if (fmiRemove != null && !fmiRemove.isEmpty()) {
			removeAttributes(fmi, fmiRemove);
		}
		/*
		 * dataset
		 */
		if (dsUpdate != null && !dsUpdate.isEmpty()) {
			ds.update(UpdatePolicy.OVERWRITE, dsUpdate, null);
		}
		if (dsRemove != null && !dsRemove.isEmpty()) {
			removeAttributes(ds, dsRemove);
		}
	}

	public static void updateAttributes(Attributes attrs, Attributes update, List<int[]> remove) {
		if (update != null && !update.isEmpty()) {
			attrs.update(UpdatePolicy.OVERWRITE, update, null);
		}
		if (remove != null && !remove.isEmpty()) {
			removeAttributes(attrs, remove);
		}
	}

	/**
	 * Remove the specified attributes from the source attributes.
	 * 
	 * @param attrs  The source attributes.
	 * @param remove The tags of the attributes to remove.
	 */
	public static void removeAttributes(Attributes attrs, List<int[]> remove) {
		if (remove == null || remove.isEmpty()) {
			return;
		}
		for (int[] tags : remove) {
			removeAttribute(attrs, tags);
		}
	}

	/**
	 * Remove the specified attribute from the source attributes.
	 * 
	 * @param attrs  The source attributes.
	 * @param remove The tags of the attribute to remove. For top level attribute,
	 *               it's a single element array. For nested attribute, it should
	 *               also contain the parent tags.
	 */
	public static void removeAttribute(Attributes attrs, int[] remove) {
		if (remove == null || remove.length <= 0) {
			return;
		}
		if (!attrs.contains(remove[0])) {
			return;
		}
		if (remove.length == 1) {
			attrs.remove(remove[0]);
		} else {
			Sequence sequence = attrs.getSequence(remove[0]);
			for (Attributes attributes : sequence) {
				removeAttribute(attributes, Arrays.copyOfRange(remove, 1, remove.length));
			}
		}
	}

}
