package dcmtools.siemens.csa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import dcmtools.siemens.csa.CSAHeaderInfo.Element;
import dcmtools.siemens.rda.RDAFile;
import dcmtools.siemens.rda.RDAFileHeader;
import dcmtools.util.DicomFileUtils;

/**
 * A utility class to validate CSA DICOM file, convert it to Siemens RDA format
 * file.
 * 
 * @see <a href="http://nipy.org/nibabel/dicom/siemens_csa.html">About Siemens
 *      CSA Header</a>
 * @author Wilson Liu
 * 
 */
public class CSADicomFile {

    /**
     * Prevent from being instantiated.
     */
    private CSADicomFile() {
    }

    /**
     * Check if the file is an Siemens CSA DICOM file.
     * 
     * @param f
     * @return
     * @throws Throwable
     */
    public static boolean isCSADicomFile(File f) throws IOException {
        if (!DicomFileUtils.isDicomFile(f)) {
            return false;
        }
        try (DicomInputStream dis = new DicomInputStream(f)) {
            Attributes attrs = DicomFileUtils.getDicomAttributes(dis, false, true);
            return CSADicomUID.CSANonImageStorage.equals(attrs.getString(Tag.MediaStorageSOPClassUID))
                    || CSADicomUID.CSANonImageStorage.equals(attrs.getString(Tag.SOPClassUID));
        }
    }

    public static boolean isCSADicomFile(Path f) throws IOException {
        return isCSADicomFile(f.toFile());
    }

    public static CSAAttributes readCSAAttributes(File f) throws IOException {
        return CSAAttributes.read(f);
    }

    public static CSAAttributes readCSAAttributes(Path f) throws IOException {
        return CSAAttributes.read(f);
    }
    
    /**
     * Convert a Siemens CSA DICOM file to Siemens RDA file.
     * 
     * @param csaFile
     * @param rdaFile
     * @throws Throwable
     */
    public static void toRDA(File csaFile, File rdaFile) throws Throwable {
        toRDA(csaFile.toPath(), rdaFile.toPath());
    }

    /**
     * Convert a Siemens CSA DICOM file to Siemens RDA file.
     * 
     * @param csaFile
     * @param rdaFile
     * @throws Throwable
     */
    public static void toRDA(Path csaFile, Path rdaFile) throws Throwable {
        CSAAttributes csa = readCSAAttributes(csaFile);
        RDAFileHeader rdaHeader = new RDAFileHeader();
        rdaHeader.PatientName = csa.patientName;
        rdaHeader.PatientID = csa.patientID;
        rdaHeader.PatientSex = csa.patientSex;
        rdaHeader.PatientBirthDate = csa.patientBirthDate;
        rdaHeader.StudyDate = csa.studyDate;
        rdaHeader.StudyTime = csa.studyTime;
        rdaHeader.StudyDescription = csa.studyDescription;
        rdaHeader.PatientAge = csa.patientAge;
        rdaHeader.PatientWeight = csa.patientWeight;
        rdaHeader.SeriesDate = csa.seriesDate;
        rdaHeader.SeriesTime = csa.seriesTime;
        rdaHeader.SeriesDescription = csa.seriesDescription;
        rdaHeader.ProtocolName = csa.protocolName;
        rdaHeader.PatientPosition = csa.patientPosition;
        rdaHeader.SeriesNumber = csa.seriesNumber;
        rdaHeader.InstitutionName = csa.institutionName;
        rdaHeader.StationName = csa.stationName;
        rdaHeader.ModelName = csa.manufacturerModelName;
        rdaHeader.DeviceSerialNumber = csa.deviceSerialNumber;
        rdaHeader.SoftwareVersion = csa.softwareVersions;
        rdaHeader.InstanceDate = csa.instanceCreationDate;
        rdaHeader.InstanceTime = csa.instanceCreationTime;
        rdaHeader.InstanceNumber = csa.instanceNumber;
        rdaHeader.InstanceComments = csa.imageComments;
        rdaHeader.AcquisitionNumber = csa.acquisitionNumber;

        rdaHeader.SequenceName = csa.element("SequenceName").item(0, "");
        rdaHeader.SequenceDescription = csa.element("SequenceName").item(0, "");
        rdaHeader.TR = csa.element("RepetitionTime").item(0, "");
        rdaHeader.TE = csa.element("EchoTime").item(0, "");
        rdaHeader.TM = csa.element("MixingTime").item(0, "");
        rdaHeader.TI = csa.element("InversionTime").item(0, "");
        rdaHeader.DwellTime = csa.element("RealDwellTime").item(0, "");
        rdaHeader.EchoNumber = csa.element("EchoNumbers").item(0, "");
        rdaHeader.NumberOfAverages = csa.element("NumberOfAverages").item(0, "");
        rdaHeader.MRFrequency = csa.element("ImagingFrequency").item(0, "");
        rdaHeader.Nucleus = csa.element("ImagedNucleus").item(0, "");
        rdaHeader.MagneticFieldStrength = csa.element("MagneticFieldStrength").item(0, "");
        rdaHeader.NumOfPhaseEncodingSteps = csa.element("NumberOfPhaseEncodingSteps").item(0, "");
        rdaHeader.FlipAngle = csa.element("FlipAngle").item(0, "");
        rdaHeader.VectorSize = csa.element("SpectroscopyAcquisitionDataColumns").item(0, "");
        rdaHeader.CSIMatrixSize = new String[3];
        rdaHeader.CSIMatrixSize[0] = csa.element("Columns").item(0, "");
        rdaHeader.CSIMatrixSize[1] = csa.element("Rows").item(0, "");
        rdaHeader.CSIMatrixSize[2] = csa.element("NumberOfFrames").item(0, "");
        rdaHeader.CSIMatrixSizeOfScan = new String[3];
        rdaHeader.CSIMatrixSizeOfScan[0] = csa.element("SpectroscopyAcquisitionPhaseColumns").item(0, "");
        rdaHeader.CSIMatrixSizeOfScan[1] = csa.element("SpectroscopyAcquisitionPhaseRows").item(0, "");
        rdaHeader.CSIMatrixSizeOfScan[2] = csa.element("SpectroscopyAcquisitionOut-of-planePhaseSteps").item(0, "");
        rdaHeader.CSIGridShift = new String[3];

        Element csiGridshiftVector = csa.element("CsiGridshiftVector");
        if (csiGridshiftVector.nItems == 3) {
            rdaHeader.CSIGridShift[0] = csiGridshiftVector.item(0, "0");
            rdaHeader.CSIGridShift[1] = csiGridshiftVector.item(1, "0");
            rdaHeader.CSIGridShift[2] = csiGridshiftVector.item(2, "0");
        } else {
            rdaHeader.CSIGridShift[0] = "0";
            rdaHeader.CSIGridShift[1] = "0";
            rdaHeader.CSIGridShift[2] = "0";
        }
        String HammingFilterWidth = csa.element("HammingFilterWidth").item(0, "");
        if (Float.parseFloat(HammingFilterWidth) == 0.0) {
            rdaHeader.HammingFilter = "Off";
        } else {
            rdaHeader.HammingFilter = "On";
        }

        rdaHeader.FrequencyCorrection = csa.element("FrequencyCorrection").item(0, "");
        rdaHeader.TransmitCoil = csa.element("TransmittingCoil").item(0, "");
        rdaHeader.TransmitRefAmplitude = csa.element("TransmitterReferenceAmplitude").item(0, "");
        rdaHeader.SliceThickness = csa.element("SliceThickness").item(0, "");

        rdaHeader.PositionVector = new String[3];
        rdaHeader.PositionVector[0] = csa.element("ImagePositionPatient").item(0, "");
        rdaHeader.PositionVector[1] = csa.element("ImagePositionPatient").item(1, "");
        rdaHeader.PositionVector[2] = csa.element("ImagePositionPatient").item(2, "");

        rdaHeader.RowVector = new String[3];
        rdaHeader.RowVector[0] = csa.element("ImageOrientationPatient").item(0, "");
        rdaHeader.RowVector[1] = csa.element("ImageOrientationPatient").item(1, "");
        rdaHeader.RowVector[2] = csa.element("ImageOrientationPatient").item(2, "");

        rdaHeader.ColumnVector = new String[3];
        rdaHeader.ColumnVector[0] = csa.element("ImageOrientationPatient").item(3, "");
        rdaHeader.ColumnVector[1] = csa.element("ImageOrientationPatient").item(4, "");
        rdaHeader.ColumnVector[2] = csa.element("ImageOrientationPatient").item(5, "");

        rdaHeader.VOIPositionSag = csa.element("VoiPosition").item(0, "");
        rdaHeader.VOIPositionCor = csa.element("VoiPosition").item(1, "");
        rdaHeader.VOIPositionTra = csa.element("VoiPosition").item(2, "");

        rdaHeader.VOIThickness = csa.element("VoiThickness").item(0, "");
        rdaHeader.VOIPhaseFOV = csa.element("VoiPhaseFoV").item(0, "");
        rdaHeader.VOIReadoutFOV = csa.element("VoiReadoutFoV").item(0, "");

        rdaHeader.VOINormalSag = csa.element("VoiOrientation").item(0, "");
        rdaHeader.VOINormalCor = csa.element("VoiOrientation").item(1, "");
        rdaHeader.VOINormalTra = csa.element("VoiOrientation").item(2, "");

        rdaHeader.VOIRotationInPlane = csa.element("VoiInPlaneRotation").item(0, "");
        rdaHeader.FoVHeight = csa.element("VoiPhaseFoV").item(0, "");
        rdaHeader.FoVWidth = csa.element("VoiReadoutFoV").item(0, "");
        rdaHeader.FoV3D = csa.element("VoiThickness").item(0, "");
        rdaHeader.PercentOfRectFoV = csa.element("PercentPhaseFieldOfView").item(0, "");
        rdaHeader.NumberOfRows = csa.element("Rows").item(0, "");
        rdaHeader.NumberOfColumns = csa.element("Columns").item(0, "");
        rdaHeader.NumberOf3DParts = csa.element("NumberOfFrames").item(0, "");
        rdaHeader.PixelSpacingRow = csa.element("PixelSpacing").item(0, "");
        rdaHeader.PixelSpacingCol = csa.element("PixelSpacing").item(1, "");
        rdaHeader.PixelSpacing3D = csa.element("SliceThickness").item(0, "");

        double[] rdaData = new double[csa.data.length];
        for (int i = 0; i < csa.data.length; i++) {
            rdaData[i] = (double) csa.data[i];
        }
        RDAFile.create(rdaHeader, rdaData, rdaFile);

    }

    /**
     * The main method can be used to test the conversion from CSA DICOM file to
     * Siemens RDA file.
     * 
     * <pre>
     * * 
     * Usage: java -cp <classpath> nig.image.dicom.siemens.CSAFileUtils  <csa-dicom-file> <rda-file>
     * </pre>
     * 
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {

//        if (args.length != 2) {
//            System.err.println("Error parsing arguments.");
//            System.out.println("Usage: " + CSADicomFile.class.getName() + " <csa-dicom-file> <rda-file>");
//            System.exit(1);
//        }
//        convertToSiemensRDA(new File(args[0]), new File(args[1]));
    }

}
