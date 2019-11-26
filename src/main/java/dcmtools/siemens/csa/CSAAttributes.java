package dcmtools.siemens.csa;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import dcmtools.io.BinaryInputStream;

public class CSAAttributes {

    private static final Logger logger = LogManager.getLogger(CSAAttributes.class);

    public final String patientName;
    public final String patientID;
    public final String patientSex;
    public final String patientBirthDate;
    public final String studyDate;
    public final String studyTime;
    public final String studyDescription;
    public final String patientAge;
    public final String patientWeight;
    public final String seriesDate;
    public final String seriesTime;
    public final String seriesDescription;
    public final String protocolName;
    public final String patientPosition;
    public final String seriesNumber;
    public final String institutionName;
    public final String stationName;
    public final String manufacturerModelName;
    public final String deviceSerialNumber;
    public final String softwareVersions;
    public final String instanceCreationDate;
    public final String instanceCreationTime;
    public final String instanceNumber;
    public final String imageComments;
    public final String acquisitionNumber;

    public final CSAHeaderInfo imageHeaderInfo;
    public final CSAHeaderInfo seriesHeaderInfo;
    public float[] data;

    protected CSAAttributes(Attributes attributes, CSAHeaderInfo imageHeaderInfo, CSAHeaderInfo seriesHeaderInfo,
            float[] data) {
        this.patientName = attributes.getString(Tag.PatientName, "");
        this.patientID = attributes.getString(Tag.PatientID, "");
        this.patientSex = attributes.getString(Tag.PatientSex, "");
        this.patientBirthDate = attributes.getString(Tag.PatientBirthDate, "");
        this.studyDate = attributes.getString(Tag.StudyDate, "");
        this.studyTime = attributes.getString(Tag.StudyTime, "");
        this.studyDescription = attributes.getString(Tag.StudyDescription, "");
        this.patientAge = attributes.getString(Tag.PatientAge, "");
        this.patientWeight = attributes.getString(Tag.PatientWeight, "");
        this.seriesDate = attributes.getString(Tag.SeriesDate, "");
        this.seriesTime = attributes.getString(Tag.SeriesTime, "");
        this.seriesDescription = attributes.getString(Tag.SeriesDescription, "");
        this.protocolName = attributes.getString(Tag.ProtocolName, "");
        this.patientPosition = attributes.getString(Tag.PatientPosition, "");
        this.seriesNumber = attributes.getString(Tag.SeriesNumber, "");
        this.institutionName = attributes.getString(Tag.InstitutionName, "");
        this.stationName = attributes.getString(Tag.StationName, "");
        this.manufacturerModelName = attributes.getString(Tag.ManufacturerModelName, "");
        this.deviceSerialNumber = attributes.getString(Tag.DeviceSerialNumber, "");
        this.softwareVersions = attributes.getString(Tag.SoftwareVersions, "");
        this.instanceCreationDate = attributes.getString(Tag.InstanceCreationDate, "");
        this.instanceCreationTime = attributes.getString(Tag.InstanceCreationTime, "");
        this.instanceNumber = attributes.getString(Tag.InstanceNumber, "");
        this.imageComments = attributes.getString(Tag.ImageComments, "");
        this.acquisitionNumber = attributes.getString(Tag.AcquisitionNumber, "");
        this.imageHeaderInfo = imageHeaderInfo;
        this.seriesHeaderInfo = seriesHeaderInfo;
        this.data = data;
    }

    public CSAHeaderInfo.Element element(String name) {
        CSAHeaderInfo.Element e = this.imageHeaderInfo.element(name);
        if (e == null) {
            e = this.seriesHeaderInfo.element(name);
        }
        return e;
    }

    public List<CSAHeaderInfo.Element> elements() {
        List<CSAHeaderInfo.Element> elements = new ArrayList<CSAHeaderInfo.Element>(size());
        elements.addAll(this.imageHeaderInfo.elements());
        elements.addAll(this.seriesHeaderInfo.elements());
        return elements;
    }

    public int size() {
        return this.imageHeaderInfo.size() + this.seriesHeaderInfo.size();
    }

    public static CSAAttributes read(File f) throws IOException {
        return read(f.toPath());
    }

    public static CSAAttributes read(Path f) throws IOException {
        try (InputStream is = Files.newInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(is);
                DicomInputStream dis = new DicomInputStream(bis)) {
            Attributes dicomAttributes = new Attributes(dis.bigEndian(), 64);
            dicomAttributes.addAll(dis.getFileMetaInformation());
            dis.readAttributes(dicomAttributes, -1, CSADicomTag.CSAImageHeaderInfo);
            logger.debug(String.format("[CSAImageHeaderInfo] offset:%08XH length:%d", dis.getPosition(), dis.length()));
            CSAHeaderInfo imageHeaderInfo = CSAHeaderInfo.read(dis);
            logger.debug(String.format("[CSASeriesHeaderInfo] offset:%08XH length:%d", dis.getPosition(), dis.length()));
            dis.readAttributes(dicomAttributes, -1, CSADicomTag.CSASeriesHeaderInfo);
            CSAHeaderInfo seriesHeaderInfo = CSAHeaderInfo.read(dis);
            dis.readAttributes(dicomAttributes, -1, CSADicomTag.CSAData);
            float[] data = readData(dis);
            return new CSAAttributes(dicomAttributes, imageHeaderInfo, seriesHeaderInfo, data);
        }
    }

    private static float[] readData(DicomInputStream dis) throws IOException {
        float[] data = new float[dis.length() / 4];
        try (BinaryInputStream bis = new BinaryInputStream(dis, ByteOrder.LITTLE_ENDIAN, false)) {
            for (int i = 0; i < data.length; i++) {
                data[i] = bis.readFloat();
            }
            return data;
        }
    }

}
