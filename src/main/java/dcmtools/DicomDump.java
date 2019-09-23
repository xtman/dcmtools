package dcmtools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;

import dcmtools.util.StringUtils;

public class DicomDump implements DicomInputHandler {

    public static final int MAX_VALUE_LENGTH = 128;

    @Override
    public void startDataset(DicomInputStream dis) throws IOException {
        printPreamble(dis.getPreamble());
    }

    @Override
    public void endDataset(DicomInputStream dis) throws IOException {
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
        StringBuilder line = new StringBuilder();
        appendOffset(dis, line);
        appendTag(dis, line);
        appendVR(dis, line);
        appendVL(dis, line);
        appendAttributeName(dis, line);
        VR vr = dis.vr();
        int vl = dis.length();
        boolean undefinedLength = vl == -1;
        if (vr == VR.SQ || undefinedLength) {
            appendValue(null, line);
            System.out.println(line);

            dis.readValue(dis, attrs);
            if (undefinedLength) {
                line.setLength(0);
                appendOffset(dis, line);
                appendTag(dis, line);
                appendVR(dis, line);
                appendVL(dis, line);
                appendAttributeName(dis, line);
                appendValue(null, line);
                System.out.println(line);
            }
            return;
        }

        byte[] b = dis.readValue();
        int tag = dis.tag();
        appendValue(line, dis, vr, b, attrs.getSpecificCharacterSet());
        System.out.println(line);

        if (tag == Tag.FileMetaInformationGroupLength)
            dis.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID || tag == Tag.SpecificCharacterSet || TagUtils.isPrivateCreator(tag))
            attrs.setBytes(tag, vr, b);
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
        StringBuilder line = new StringBuilder();
        appendOffset(dis, line);
        appendTag(dis, line);
        appendVR(dis, line);
        appendVL(dis, line);
        appendAttributeName(dis, line);
        line.append(" #").append(seq.size() + 1); // sequence size;
        System.out.println(line);

        boolean undeflen = dis.length() == -1;
        dis.readValue(dis, seq);
        if (undeflen) {
            line.setLength(0);
            appendOffset(dis, line);
            appendTag(dis, line);
            appendVR(dis, line);
            appendVL(dis, line);
            appendAttributeName(dis, line);
            appendValue(null, line);
            System.out.println(line);
        }
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
        StringBuilder line = new StringBuilder();
        appendOffset(dis, line);
        appendTag(dis, line);
        appendVR(dis, line);
        appendVL(dis, line);
        appendAttributeName(dis, line);
        appendValue(line, dis, frags.vr(), dis.readValue(), null);
        System.out.println(line);
    }

    private void appendOffset(long offset, StringBuilder line) {
        line.append(String.format("%08X", offset)).append(":");
    }

    private void appendOffset(DicomInputStream dis, StringBuilder line) {
        appendOffset(dis.getTagPosition(), line);
    }

    private void appendTag(DicomInputStream dis, StringBuilder line) {
        line.append(' ');
        String indent = dis.level() > 0 ? StringUtils.repeat('>', dis.level()) : "";
        String tag = TagUtils.toString(dis.tag());
        line.append(String.format("%-15s", indent + tag));
    }

    private void appendVR(DicomInputStream dis, StringBuilder line) {
        line.append(' ');
        VR vr = dis.vr();
        line.append(vr == null ? "  " : vr).append(' ');
    }

    private void appendVL(DicomInputStream dis, StringBuilder line) {
        line.append(' ');
        line.append(String.format("%8d", dis.length()));
    }

    private void appendAttributeName(DicomInputStream dis, StringBuilder line) {
        line.append(' ');
        String tagName = ElementDictionary.keywordOf(dis.tag(), null);
        line.append(String.format("%-32s", tagName == null ? "??" : tagName));
    }

    private void appendValue(String value, StringBuilder line) {
        line.append(' ');
        if (value != null && !value.isEmpty()) {
            line.append('[');
            line.append(value == null ? "" : value.trim());
            line.append(']');
        }
    }

    private void appendValue(StringBuilder line, DicomInputStream dis, VR vr, byte[] b,
            SpecificCharacterSet specificCharacterSet) throws IOException {
        if (b.length > 16 && (vr.equals(VR.OB) || vr.equals(VR.OW) || vr.equals(VR.UN))) {
            return;
        }
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), specificCharacterSet, MAX_VALUE_LENGTH, line)) {
            line.append(']');
        }
    }

    private void printPreamble(byte[] preamble) {
        if (preamble == null) {
            return;
        }
        StringBuilder line = new StringBuilder();
        line.append(String.format("%08X:                     %8d PREAMBLE", 0, preamble.length));
        System.out.println(line);

        line.setLength(0);
        line.append(String.format("%08X:                     %8d %32s [%s]", preamble.length, 4, "", "DICM"));
        System.out.println(line);
    }

    public void dump(DicomInputStream dis) throws IOException {
        dis.setDicomInputHandler(this);
        dis.readDataset(-1, -1);
    }

    public static void dump(Path dcmFile) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(dcmFile)))) {
            new DicomDump().dump(dis);
        }
    }

}
