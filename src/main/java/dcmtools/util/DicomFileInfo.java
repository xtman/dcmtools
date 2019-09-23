package dcmtools.util;

import java.nio.file.Path;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

public class DicomFileInfo implements Comparable<DicomFileInfo> {
    public final Path path;
    public final long datasetOffset;
    public final String mediaStorageSOPClassUID;
    public final String mediaStorageSOPInstanceUID;
    public final String transferSyntaxUID;
    public final String studyInstanceUID;
    public final String seriesInstanceUID;
    public final int seriesNumber;
    public final String sopInstanceUID;
    public final int instanceNumber;

    DicomFileInfo(Path path, long datasetOffset, Attributes fileMetaInformation, Attributes dataset) {
        this.path = path;
        this.datasetOffset = datasetOffset;
        this.mediaStorageSOPClassUID = fileMetaInformation.getString(Tag.MediaStorageSOPClassUID);
        this.mediaStorageSOPInstanceUID = fileMetaInformation.getString(Tag.MediaStorageSOPInstanceUID);
        this.transferSyntaxUID = fileMetaInformation.getString(Tag.TransferSyntaxUID);
        this.studyInstanceUID = dataset.getString(Tag.StudyInstanceUID);
        this.seriesInstanceUID = dataset.getString(Tag.SeriesInstanceUID);
        this.seriesNumber = dataset.getInt(Tag.SeriesNumber, 0);
        this.sopInstanceUID = dataset.getString(Tag.SOPInstanceUID);
        this.instanceNumber = dataset.getInt(Tag.InstanceNumber, 0);
    }

    @Override
    public int compareTo(DicomFileInfo fi) {
        if (fi == null) {
            return 1;
        }
        int r = ObjectUtils.compareTo(this.studyInstanceUID, fi.studyInstanceUID);
        if (r != 0) {
            return r;
        }
        r = ObjectUtils.compareTo(this.seriesInstanceUID, fi.seriesInstanceUID);
        if (r != 0) {
            if (this.seriesNumber > 0 && fi.seriesNumber > 0) {
                r = Integer.compare(this.seriesNumber, fi.seriesNumber);
            }
            return r;
        }
        r = ObjectUtils.compareTo(this.sopInstanceUID, fi.sopInstanceUID);
        if (r != 0) {
            if (this.instanceNumber > 0 && fi.instanceNumber > 0) {
                r = Integer.compare(this.instanceNumber, fi.instanceNumber);
            }
            return r;
        }
        return r;
    }

}
