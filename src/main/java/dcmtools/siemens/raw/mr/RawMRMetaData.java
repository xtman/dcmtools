package dcmtools.siemens.raw.mr;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcmtools.io.BinaryInputStream;
import dcmtools.io.SizedInputStream;
import dcmtools.util.StringUtils;

/**
 * NOTE: This is an incomplete parser. It only parses the primitive param types.
 * 
 * @author wliu5
 *
 */
public class RawMRMetaData {

    private static final Logger logger = LogManager.getLogger(RawMRMetaData.class);

    Map<String, List<Element>> elements;

    public final List<Element> elements(String protocol) {
        if (this.elements != null) {
            List<Element> es = this.elements.get(protocol);
            return es == null ? null : Collections.unmodifiableList(es);
        }
        return null;
    }

    public final Set<String> protocols() {
        return this.elements == null ? null : this.elements.keySet();
    }

    public final Element element(String protocol, String type, String name) {

        if (type == null && name == null) {
            throw new IllegalArgumentException("either element type or name must be supplied.");
        }

        if (this.elements != null && !this.elements.isEmpty()) {
            if (protocol != null) {
                List<Element> elements = this.elements.get(protocol);
                if (elements != null) {
                    for (Element e : elements) {
                        if ((type == null || type.equals(e.type())) && (name == null || name.equals(e.name()))) {
                            return e;
                        }
                    }
                }
            } else {
                Collection<List<Element>> ess = this.elements.values();
                for (List<Element> es : ess) {
                    for (Element e : es) {
                        if ((type == null || type.equals(e.type())) && (name == null || name.equals(e.name()))) {
                            return e;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void addElement(String protocol, Element e) {
        if (this.elements == null) {
            this.elements = new LinkedHashMap<String, List<Element>>();
        }
        List<Element> es = this.elements.get(protocol);
        if (es == null) {
            es = new ArrayList<Element>();
            this.elements.put(protocol, es);
        }
        es.add(e);
    }

    public String patientName() {
        Element e = element("Config", "ParamString", "tPatientName");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String patientID() {
        Element e = element("Config", "ParamString", "PatientID");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String patientBirthDate() {
        Element e = element("Config", "ParamString", "PatientBirthDay");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String patientSex() {
        Element e = element("Config", "ParamLong", "PatientSex");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String studyDescription() {
        Element e = element("Config", "ParamString", "tStudyDescription");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String patientPosition() {
        Element e = element("Config", "ParamString", "PatientPosition");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String frameOfReference() {
        Element e = element("Config", "ParamString", "FrameOfReference");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String softwareVersions() {
        Element e = element("Dicom", "ParamString", "SoftwareVersions");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String manufacturer() {
        Element e = element("Dicom", "ParamString", "Manufacturer");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String manufacturerModelName() {
        Element e = element("Dicom", "ParamString", "ManufacturersModelName");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String institutionAddress() {
        Element e = element("Dicom", "ParamString", "InstitutionAddress");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String deviceSeriesNumber() {
        Element e = element("Dicom", "ParamString", "DeviceSerialNumber");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String institutionName() {
        Element e = element("Dicom", "ParamString", "InstitutionName");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String modality() {
        Element e = element("Dicom", "ParamString", "Modality");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String protocolName() {
        Element e = element("Dicom", "ParamString", "tProtocolName");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String magneticFieldStrength() {
        Element e = element("Dicom", "ParamDouble", "flMagneticFieldStrength");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String transmitCoilName() {
        Element e = element("Dicom", "ParamString", "TransmittingCoil");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public String flipAngle() {
        Element e = element("Dicom", "ParamDouble", "adFlipAngleDegree");
        if (e != null) {
            return e.value();
        }
        return null;
    }

    public static RawMRMetaData parse(Path f, String... protocols) throws Throwable {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(f))) {
            return parse(in, protocols);
        }
    }

    public static RawMRMetaData parse(InputStream in, String... protocols) throws IOException {

        Set<String> ps = null;
        if (protocols != null && protocols.length > 0) {
            ps = new HashSet<String>(protocols.length);
            for (String protocol : protocols) {
                ps.add(protocol.toLowerCase());
            }
        }
        RawMRMetaData md = new RawMRMetaData();

        try (BinaryInputStream bis = new BinaryInputStream(in, ByteOrder.LITTLE_ENDIAN, false)) {

            long offset = bis.count();
            logger.debug(String.format("%08XH: protocols.begin", offset));

            int protocolsLength = bis.readInt();
            logger.debug(String.format("%08XH: protocols.length: %d", offset, protocolsLength));

            offset = bis.count();
            int protocolsCount = bis.readInt();
            logger.debug(String.format("%08XH: protocols.length: %d", offset, protocolsCount));

            for (int i = 0; i < protocolsCount; i++) {
                offset = bis.count();
                String protocolName = readString(bis);
                logger.debug(String.format("%08XH: protocol.name: %s", offset, protocolName));

                offset = bis.count();
                int protocolLength = bis.readInt();
                logger.debug(String.format("%08XH: protocol.length: %s", offset, protocolLength));
                if (ps == null || ps.contains(protocolName.toLowerCase())) {
                    try (SizedInputStream sis = new SizedInputStream(bis, protocolLength, false);
                            InputStreamReader isr = new InputStreamReader(sis);
                            BufferedReader br = new BufferedReader(isr)) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith("### ASCCONV BEGIN ###")) {
                                do {
                                    line = br.readLine();
                                } while (line != null && !line.startsWith("### ASCCONV END ###"));
                            }
                            Element e = new Element();
                            if (line.matches(".*<(.*?)>.*")) {
                                Pattern p = Pattern.compile("<(.*?)>");
                                Matcher m = p.matcher(line);
                                if (m.find()) {
                                    String[] tag = parseTag(m.group(1));
                                    e.type = tag[0];
                                    e.name = tag[1];
                                }
                                if (line.matches(".*<(.*?)>.*\\{(.*?)\\}.*")) {
                                    p = Pattern.compile("\\{(.*?)\\}");
                                    m = p.matcher(line);
                                    if (m.find()) {
                                        parseElement(e, m.group(1));
                                    }
                                    if (e.type().startsWith("Param")) {
                                        logger.debug("matching line: " + line);
                                        md.addElement(protocolName, e);
                                    }
                                } else if (line.matches(".*<(.*?)>\\ *")) {
                                    // <ParamArray."B">
                                    // TODO not implemented
                                } else if (line.matches(".*<(.*?)>\\ *([^\\{\\ ]+)\\ *")) {
                                    // <Class> "MeasContext@MrParc"
                                    // TODO not implemented
                                }
                            }
                        }
                    }
                } else {
                    bis.skipFully(protocolLength);
                }
            }
            offset = bis.count();
            logger.debug(String.format("%08XH: protocols.end", offset));
            return md;
        }
    }

    private static void parseElement(Element e, String s) {
        s = s.trim();
        while (!s.isEmpty()) {
            int idx;
            if (s.startsWith("\"")) {
                idx = s.indexOf("\"", 1);
                e.addValue(s.substring(1, idx));
                s = s.substring(idx + 1).trim();
            } else if (s.startsWith("<")) {
                idx = s.indexOf(">", 1);
                String[] tag = parseTag(s.substring(1, idx));
                Attribute a = new Attribute();
                a.type = tag[0];
                a.name = tag[1];
                s = s.substring(idx + 1).trim();
                if (s.startsWith("\"")) {
                    idx = s.indexOf("\"", 1);
                    a.value = s.substring(1, idx);
                    s = s.substring(idx + 1).trim();
                } else {
                    idx = s.indexOf(' ');
                    if (idx == -1) {
                        a.value = s;
                        s = "";
                    } else {
                        a.value = s.substring(0, idx);
                        s = s.substring(idx + 1).trim();
                    }
                }
            } else {
                idx = s.indexOf(' ');
                if (idx == -1) {
                    e.addValue(s);
                    s = "";
                } else {
                    e.addValue(s.substring(0, idx));
                    s = s.substring(idx + 1).trim();
                }
            }
        }

    }

    private static String[] parseTag(String tag) {
        int idx = tag.indexOf('.');
        if (idx == -1) {
            return new String[] { tag, null };
        }
        String type = tag.substring(0, idx);
        String name = StringUtils.trim(tag.substring(idx + 1), '"');
        return new String[] { type, name };
    }

    static String readString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b = (byte) in.read();
        while (b != 0) {
            sb.append((char) b);
            b = (byte) in.read();
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) throws Throwable {

        // @formatter:off
        /*
        LoggingUtils.setLogLevel(RawMRMetaData.class, Level.WARN);
        RawMRMetaData md = parse(Paths.get("/tmp/meas_MID36_EdLineFullKlineXSLAC146V_FID33313.dat"),
                "Config", "Dicom");
        System.out.println("patient.name: " + md.patientName());
        System.out.println("patient.id: " + md.patientID());
        System.out.println("patient.sex: " + md.patientSex());
        System.out.println("patient.birth.date: " + md.patientBirthDate());
        System.out.println("study.description: " + md.studyDescription());
        System.out.println("patient.position: " + md.patientPosition());
        System.out.println("frame.of.reference: " + md.frameOfReference());
        System.out.println("manufacturer: " + md.manufacturer());
        System.out.println("manufacturer.model.name: " + md.manufacturerModelName());
        System.out.println("institution.address: " + md.institutionAddress());
        System.out.println("deviceSeries.number: " + md.deviceSeriesNumber());
        System.out.println("institution.name: " + md.institutionName());
        System.out.println("modality: " + md.modality());
        System.out.println("protocol.name: " + md.protocolName());
        System.out.println("magnetic.field.strength: " + md.magneticFieldStrength());
        System.out.println("transmit.coil.name: " + md.transmitCoilName());
        System.out.println("flip.angle: " + md.flipAngle());
        */
        // @formatter:on
    }
}
