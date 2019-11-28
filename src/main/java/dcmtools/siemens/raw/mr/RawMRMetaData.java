package dcmtools.siemens.raw.mr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcmtools.io.BinaryInputStream;
import dcmtools.io.SizedInputStream;
import dcmtools.util.LoggingUtils;
import dcmtools.util.ObjectUtils;
import dcmtools.util.StringUtils;

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
        List<Element> elements = elements(protocol);
        if (elements != null) {
            for (Element element : elements) {
                if (element.type().equals(type) || ObjectUtils.equals(element.name(), name)) {
                    return element;
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

    public static void dump(Path f, String... protocols) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(f))) {
            dump(in, System.out, protocols);
        }
    }

    public static void dump(Path f, Path of, String... protocols) throws IOException {
        try (OutputStream os = Files.newOutputStream(of); BufferedOutputStream bos = new BufferedOutputStream(os)) {
            dump(f, os, protocols);
        }
    }

    public static void dump(Path f, OutputStream out, String... protocols) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(f))) {
            dump(in, out, protocols);
        }
    }

    public static void dump(InputStream in, OutputStream out, String... protocols) throws IOException {
        try (OutputStreamWriter osr = new OutputStreamWriter(out); BufferedWriter br = new BufferedWriter(osr)) {
            dump(in, osr, protocols);
        }
    }

    public static void dump(InputStream in, Writer w, String... protocols) throws IOException {
        Set<String> ps = new LinkedHashSet<String>();
        if (protocols != null) {
            for (String p : protocols) {
                ps.add(p.toLowerCase());
            }
        }
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
                if (ps.isEmpty() || ps.contains(protocolName.toLowerCase())) {
                    try (SizedInputStream sis = new SizedInputStream(bis, protocolLength, false);
                            InputStreamReader isr = new InputStreamReader(sis);
                            BufferedReader br = new BufferedReader(isr)) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            w.write(line);
                            w.write(System.lineSeparator());
                            w.flush();
                        }
                    }
                } else {
                    bis.skipFully(protocolLength);
                }
            }
            offset = bis.count();
            logger.debug(String.format("%08XH: protocols.end", offset));
        }
    }

    public static RawMRMetaData parse(Path f) throws Throwable {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(f))) {
            return parse(in);
        }
    }

    public static RawMRMetaData parse(InputStream in) throws IOException {

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
                                // TODO
                            } else if (line.matches(".*<(.*?)>\\ *([^\\{\\ ]+)\\ *")) {
                                // <Class> "MeasContext@MrParc"
                                // TODO
                            }
                        }
                    }
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

    private static String readString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b = (byte) in.read();
        while (b != 0) {
            sb.append((char) b);
            b = (byte) in.read();
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) throws Throwable {
        LoggingUtils.setLogLevel(RawMRMetaData.class, Level.WARN);
//        dump(Paths.get("/Users/wliu5/Downloads/meas_MID36_EdLineFullKlineXSLAC146V_FID33313.dat"), "dicom");
        RawMRMetaData md = parse(Paths.get("/Users/wliu5/Downloads/meas_MID36_EdLineFullKlineXSLAC146V_FID33313.dat"));
        Set<String> protocols = md.protocols();
        if (protocols != null) {
            for (String protocol : protocols) {
                System.out.println("protocol: " + protocol);
                List<Element> es = md.elements(protocol);
                if (es != null) {
                    for (Element e : es) {
                        System.out.println("    " + e.type() + "." + e.name() + ": " + e.value());
                    }
                }
            }
        }
//        dump(Paths.get("/tmp/meas_MID110_gre_9echo_bi_p2_0p75isoQSM_FID5572.dat"), System.out);

//        String s = "<ParamString.\"tPatientName\"> { \"AAA\" }";
////        System.out.println(s.matches(".*<[\\w\\.\"]+>.*"));
//        Pattern p = Pattern.compile("<(.*?)>.*\\{(.*?)\\}");
//        Matcher m = p.matcher(s);
//        if (m.find()) {
//            System.out.println(m.group(1));
//            System.out.println(m.group(2));
//        }

    }
}
