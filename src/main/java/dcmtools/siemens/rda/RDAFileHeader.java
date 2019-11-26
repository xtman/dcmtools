package dcmtools.siemens.rda;

public class RDAFileHeader {

    public static final String HEADER_BEGIN = ">>> Begin of header <<<";
    public static final String HEADER_END = ">>> End of header <<<";

    // PatientName: PAIN_DNIC_TT^HF43.5
    public String PatientName = "";

    // PatientID: R423081
    public String PatientID = "";

    // PatientSex: F
    public String PatientSex = "";

    // PatientBirthDate: 19870402
    public String PatientBirthDate = "";

    // StudyDate: 20080826
    public String StudyDate = "";

    // StudyTime: 143511.531000
    public String StudyTime = "";

    // StudyDescription: Research Projects^Howard Florey
    public String StudyDescription = "";

    // PatientAge: 021Y
    public String PatientAge = "";

    // PatientWeight: 55.000000
    public String PatientWeight = "";

    // SeriesDate: 20080826
    public String SeriesDate = "";

    // SeriesTime: 150744.312000
    public String SeriesTime = "";

    // SeriesDescription: svs_se_30_Cold_a
    public String SeriesDescription = "";

    // ProtocolName: svs_se_30_Cold_a
    public String ProtocolName = "";

    // PatientPosition: HFS
    public String PatientPosition = "";

    // SeriesNumber: 9
    public String SeriesNumber = "";

    // InstitutionName: Children's MRI Centre @ RCH
    public String InstitutionName = "";

    // StationName: MRC35113
    public String StationName = "";

    // ModelName: TrioTim
    public String ModelName = "";

    // DeviceSerialNumber: 35113
    public String DeviceSerialNumber = "";

    // SoftwareVersion: syngo MR B15
    public String SoftwareVersion = "";

    // InstanceDate: 20080826
    public String InstanceDate = "";

    // InstanceTime: 150744.312000
    public String InstanceTime = "";

    // InstanceNumber: 1
    public String InstanceNumber = "";

    // InstanceComments: _nc_4
    public String InstanceComments = "";

    // AcquisitionNumber: 1
    public String AcquisitionNumber = "";

    // SequenceName: *svs_se
    public String SequenceName = "";

    // SequenceDescription: *svs_se
    public String SequenceDescription = "";

    // TR: 3000.000000
    public String TR = "";

    // TE: 30.000000
    public String TE = "";

    // TM: 0.000000
    public String TM = "";

    // TI: 0.000000
    public String TI = "";

    // DwellTime: 833
    public String DwellTime = "";

    // EchoNumber: 0
    public String EchoNumber = "";

    // NumberOfAverages: 64.000000
    public String NumberOfAverages = "";

    // MRFrequency: 123.246181
    public String MRFrequency = "";

    // Nucleus: 1H
    public String Nucleus = "";

    // MagneticFieldStrength: 3.000000
    public String MagneticFieldStrength = "";

    // NumOfPhaseEncodingSteps: 1
    public String NumOfPhaseEncodingSteps = "";

    // FlipAngle: 90.000000
    public String FlipAngle = "";

    // VectorSize: 1024
    public String VectorSize = "";

    // CSIMatrixSize[0]: 1
    // CSIMatrixSize[1]: 1
    // CSIMatrixSize[2]: 1
    public String[] CSIMatrixSize;

    // CSIMatrixSizeOfScan[0]: 1
    // CSIMatrixSizeOfScan[1]: 1
    // CSIMatrixSizeOfScan[2]: 1
    public String[] CSIMatrixSizeOfScan;

    // CSIGridShift[0]: 0
    // CSIGridShift[1]: 0
    // CSIGridShift[2]: 0
    public String[] CSIGridShift;

    // HammingFilter: Off
    public String HammingFilter = "";

    // FrequencyCorrection: NO
    public String FrequencyCorrection = "";

    // TransmitCoil: Body
    public String TransmitCoil = "";

    // TransmitRefAmplitude: 272.618000
    public String TransmitRefAmplitude = "";

    // SliceThickness: 15.000000
    public String SliceThickness = "";

    // PositionVector[0]: -44.741978
    // PositionVector[1]: -36.778029
    // PositionVector[2]: 18.265146
    public String[] PositionVector;

    // RowVector[0]: -0.000000
    // RowVector[1]: 0.998342
    // RowVector[2]: 0.057575
    public String[] RowVector;

    // ColumnVector[0]: 0.999849
    // ColumnVector[1]: 0.001003
    // ColumnVector[2]: -0.017394
    public String[] ColumnVector;

    // -37.243119
    public String VOIPositionSag = "";

    // -21.795376
    public String VOIPositionCor = "";

    // 18.998312
    public String VOIPositionTra = "";

    // VOIThickness: 15.000000
    public String VOIThickness = "";

    // VOIPhaseFOV: 15.000000
    public String VOIPhaseFOV = "";

    // VOIReadoutFOV: 30.000000
    public String VOIReadoutFOV = "";

    // VOINormalSag: 0.017423
    public String VOINormalSag = "";

    // VOINormalCor: -0.057566
    public String VOINormalCor = "";

    // VOINormalTra: 0.998190
    public String VOINormalTra = "";

    // VOIRotationInPlane: 1.570796
    public String VOIRotationInPlane = "";

    // FoVHeight: 15.000000
    public String FoVHeight = "";

    // FoVWidth: 30.000000
    public String FoVWidth = "";

    // FoV3D: 15.000000
    public String FoV3D = "";

    // PercentOfRectFoV: 1.000000
    public String PercentOfRectFoV = "";

    // NumberOfRows: 1
    public String NumberOfRows = ""; // 1

    // NumberOfColumns: 1
    public String NumberOfColumns = ""; // 1

    // NumberOf3DParts: 1
    public String NumberOf3DParts = "";

    // PixelSpacingRow: 15.000000
    public String PixelSpacingRow = "";

    // PixelSpacingCol: 30.000000
    public String PixelSpacingCol = "";

    // PixelSpacing3D: 15.000000
    public String PixelSpacing3D = "";

    public void save(StringBuilder sb) {

        // >>> Begin of header <<<
        sb.append(HEADER_BEGIN).append("\r\n");

        // PatientName: PAIN_DNIC_TT^HF43.5
        sb.append("PatientName: ").append(PatientName).append("\r\n");

        // PatientID: R423081
        sb.append("PatientID: ").append(PatientID).append("\r\n");

        // PatientSex: F
        sb.append("PatientSex: ").append(PatientSex).append("\r\n");

        // PatientBirthDate: 19870402
        sb.append("PatientBirthDate: ").append(PatientBirthDate).append("\r\n");

        // StudyDate: 20080826
        sb.append("StudyDate: ").append(StudyDate).append("\r\n");

        // StudyTime: 143511.531000
        sb.append("StudyTime: ").append(StudyTime).append("\r\n");

        // StudyDescription: Research Projects^Howard Florey
        sb.append("StudyDescription: ").append(StudyDescription).append("\r\n");

        // PatientAge: 021Y
        sb.append("PatientAge: ").append(PatientAge).append("\r\n");

        // PatientWeight: 55.000000
        sb.append("PatientWeight: ").append(PatientWeight).append("\r\n");

        // SeriesDate: 20080826
        sb.append("SeriesDate: ").append(SeriesDate).append("\r\n");

        // SeriesTime: 150744.312000
        sb.append("SeriesTime: ").append(SeriesTime).append("\r\n");

        // SeriesDescription: svs_se_30_Cold_a
        sb.append("SeriesDescription: ").append(SeriesDescription).append("\r\n");

        // ProtocolName: svs_se_30_Cold_a
        sb.append("ProtocolName: ").append(ProtocolName).append("\r\n");

        // PatientPosition: HFS
        sb.append("PatientPosition: ").append(PatientPosition).append("\r\n");

        // SeriesNumber: 9
        sb.append("SeriesNumber: ").append(SeriesNumber).append("\r\n");

        // InstitutionName: Children's MRI Centre @ RCH
        sb.append("InstitutionName: ").append(InstitutionName).append("\r\n");

        // StationName: MRC35113
        sb.append("StationName: ").append(StationName).append("\r\n");

        // ModelName: TrioTim
        sb.append("ModelName: ").append(ModelName).append("\r\n");

        // DeviceSerialNumber: 35113
        sb.append("DeviceSerialNumber: ").append(DeviceSerialNumber).append("\r\n");

        // SoftwareVersion[0]: syngo MR B15
        sb.append("SoftwareVersion[0]: ").append(SoftwareVersion).append("\r\n");

        // InstanceDate: 20080826
        sb.append("InstanceDate: ").append(InstanceDate).append("\r\n");

        // InstanceTime: 150744.312000
        sb.append("InstanceTime: ").append(InstanceTime).append("\r\n");

        // InstanceNumber: 1
        sb.append("InstanceNumber: ").append(InstanceNumber).append("\r\n");

        // InstanceComments: _nc_4
        sb.append("InstanceComments: ").append(InstanceComments).append("\r\n");

        // AcquisitionNumber: 1
        sb.append("AcquisitionNumber: ").append(AcquisitionNumber).append("\r\n");

        // SequenceName: *svs_se
        sb.append("SequenceName: ").append(SequenceName).append("\r\n");

        // SequenceDescription: *svs_se
        sb.append("SequenceDescription: ").append(SequenceDescription).append("\r\n");

        // TR: 3000.000000
        sb.append("TR: ").append(TR).append("\r\n");

        // TE: 30.000000
        sb.append("TE: ").append(TE).append("\r\n");

        // TM: 0.000000
        sb.append("TM: ").append(TM).append("\r\n");

        // TI: 0.000000
        sb.append("TI: ").append(TI).append("\r\n");

        // DwellTime: 833
        sb.append("DwellTime: ").append(Integer.parseInt(DwellTime) / 1000).append("\r\n");

        // EchoNumber: 0
        sb.append("EchoNumber: ").append(EchoNumber).append("\r\n");

        // NumberOfAverages: 64.000000
        sb.append("NumberOfAverages: ").append(NumberOfAverages).append("\r\n");

        // MRFrequency: 123.246181
        sb.append("MRFrequency: ").append(MRFrequency).append("\r\n");

        // Nucleus: 1H
        sb.append("Nucleus: ").append(Nucleus).append("\r\n");

        // MagneticFieldStrength: 3.000000
        sb.append("MagneticFieldStrength: ").append(MagneticFieldStrength).append("\r\n");

        // NumOfPhaseEncodingSteps: 1
        sb.append("NumOfPhaseEncodingSteps: ").append(NumOfPhaseEncodingSteps).append("\r\n");

        // FlipAngle: 90.000000
        sb.append("FlipAngle: ").append(FlipAngle).append("\r\n");

        // VectorSize: 1024
        sb.append("VectorSize: ").append(VectorSize).append("\r\n");

        // CSIMatrixSize[0]: 1
        sb.append("CSIMatrixSize[0]: ").append(CSIMatrixSize[0]).append("\r\n");

        // CSIMatrixSize[1]: 1
        sb.append("CSIMatrixSize[1]: ").append(CSIMatrixSize[1]).append("\r\n");

        // CSIMatrixSize[2]: 1
        sb.append("CSIMatrixSize[2]: ").append(CSIMatrixSize[2]).append("\r\n");

        // CSIMatrixSizeOfScan[0]: 1
        sb.append("CSIMatrixSizeOfScan[0]: ").append(CSIMatrixSizeOfScan[0]).append("\r\n");

        // CSIMatrixSizeOfScan[1]: 1
        sb.append("CSIMatrixSizeOfScan[1]: ").append(CSIMatrixSizeOfScan[1]).append("\r\n");

        // CSIMatrixSizeOfScan[2]: 1
        sb.append("CSIMatrixSizeOfScan[2]: ").append(CSIMatrixSizeOfScan[2]).append("\r\n");

        // CSIGridShift[0]: 0
        sb.append("CSIGridShift[0]: ").append(CSIGridShift[0]).append("\r\n");

        // CSIGridShift[1]: 0
        sb.append("CSIGridShift[1]: ").append(CSIGridShift[1]).append("\r\n");

        // CSIGridShift[2]: 0
        sb.append("CSIGridShift[2]: ").append(CSIGridShift[2]).append("\r\n");

        // HammingFilter: Off
        sb.append("HammingFilter: ").append(HammingFilter).append("\r\n");

        // FrequencyCorrection: NO
        sb.append("FrequencyCorrection: ").append(FrequencyCorrection).append("\r\n");

        // TransmitCoil: Body
        sb.append("TransmitCoil: ").append(TransmitCoil).append("\r\n");

        // TransmitRefAmplitude[1H]: 272.618000
        sb.append("TransmitRefAmplitude[1H]: ").append(TransmitRefAmplitude).append("\r\n");

        // SliceThickness: 15.000000
        sb.append("SliceThickness: ").append(SliceThickness).append("\r\n");

        // PositionVector[0]: -44.741978
        sb.append("PositionVector[0]: ").append(PositionVector[0]).append("\r\n");

        // PositionVector[1]: -36.778029
        sb.append("PositionVector[1]: ").append(PositionVector[1]).append("\r\n");

        // PositionVector[2]: 18.265146
        sb.append("PositionVector[2]: ").append(PositionVector[2]).append("\r\n");

        // RowVector[0]: -0.000000
        sb.append("RowVector[0]: ").append(RowVector[0]).append("\r\n");

        // RowVector[1]: 0.998342
        sb.append("RowVector[1]: ").append(RowVector[1]).append("\r\n");

        // RowVector[2]: 0.057575
        sb.append("RowVector[2]: ").append(RowVector[2]).append("\r\n");

        // ColumnVector[0]: 0.999849
        sb.append("ColumnVector[0]: ").append(ColumnVector[0]).append("\r\n");

        // ColumnVector[1]: 0.001003
        sb.append("ColumnVector[1]: ").append(ColumnVector[1]).append("\r\n");

        // ColumnVector[2]: -0.017394
        sb.append("ColumnVector[2]: ").append(ColumnVector[2]).append("\r\n");

        // VOIPositionSag: -37.243119
        sb.append("VOIPositionSag: ").append(VOIPositionSag).append("\r\n");

        // VOIPositionCor: -21.795376
        sb.append("VOIPositionCor: ").append(VOIPositionCor).append("\r\n");

        // VOIPositionTra: 18.998312
        sb.append("VOIPositionTra: ").append(VOIPositionTra).append("\r\n");

        // VOIThickness: 15.000000
        sb.append("VOIThickness: ").append(VOIThickness).append("\r\n");

        // VOIPhaseFOV: 15.000000
        sb.append("VOIPhaseFOV: ").append(VOIPhaseFOV).append("\r\n");

        // VOIReadoutFOV: 30.000000
        sb.append("VOIReadoutFOV: ").append(VOIReadoutFOV).append("\r\n");

        // VOINormalSag: 0.017423
        sb.append("VOINormalSag: ").append(VOINormalSag).append("\r\n");

        // VOINormalCor: -0.057566
        sb.append("VOINormalCor: ").append(VOINormalCor).append("\r\n");

        // VOINormalTra: 0.998190
        sb.append("VOINormalTra: ").append(VOINormalTra).append("\r\n");

        // VOIRotationInPlane: 1.570796
        sb.append("VOIRotationInPlane: ").append(VOIRotationInPlane).append("\r\n");

        // FoVHeight: 15.000000
        sb.append("FoVHeight: ").append(FoVHeight).append("\r\n");

        // FoVWidth: 30.000000
        sb.append("FoVWidth: ").append(FoVWidth).append("\r\n");

        // FoV3D: 15.000000
        sb.append("FoV3D: ").append(FoV3D).append("\r\n");

        // PercentOfRectFoV: 1.000000
        sb.append("PercentOfRectFoV: ").append(PercentOfRectFoV).append("\r\n");

        // NumberOfRows: 1
        sb.append("NumberOfRows: ").append(NumberOfRows).append("\r\n");

        // NumberOfColumns: 1
        sb.append("NumberOfColumns: ").append(NumberOfColumns).append("\r\n");

        // NumberOf3DParts: 1
        sb.append("NumberOf3DParts: ").append(NumberOf3DParts).append("\r\n");

        // PixelSpacingRow: 15.000000
        sb.append("PixelSpacingRow: ").append(PixelSpacingRow).append("\r\n");

        // PixelSpacingCol: 30.000000
        sb.append("PixelSpacingCol: ").append(PixelSpacingCol).append("\r\n");

        // PixelSpacing3D: 15.000000
        sb.append("PixelSpacing3D: ").append(PixelSpacing3D).append("\r\n");

        // >>> End of header <<<
        sb.append(HEADER_END).append("\r\n");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        save(sb);
        return sb.toString();
    }
}
