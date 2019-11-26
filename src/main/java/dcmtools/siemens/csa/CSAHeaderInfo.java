package dcmtools.siemens.csa;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.io.DicomInputStream;

import dcmtools.io.BinaryInputStream;

/**
 * 
 * https://nipy.org/nibabel/dicom/siemens_csa.html
 * 
 * @author wliu5
 */
public class CSAHeaderInfo implements Iterable<CSAHeaderInfo.Element> {

    public static class Element {

        public final String name;
        public final int vm;
        public final String vr;
        public final int syngoDT;
        public final int nItems;
        public final String[] items;

        protected Element(String name, int vm, String vr, int syngoDT, int nItems, String[] items) {
            this.name = name;
            this.vm = vm;
            this.vr = vr;
            this.syngoDT = syngoDT;
            this.nItems = nItems;
            this.items = items;
        }

        public String item(int idx, String defaultValue) {
            if (idx >= this.nItems) {
                return defaultValue;
            }
            return this.items[idx] == null ? defaultValue : this.items[idx];
        }

        public String item(int idx) {
            return item(idx, "");
        }

        public String commaSeparatedItems() {
            if (this.nItems <= 0) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < this.items.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(this.items[i] == null ? "" : this.items[i]);
                }
                return sb.toString();
            }
        }

        public String toString() {
            return String.format("<element name=\"%s\" vm=\"%d\" vr=\"%s\" syngoDT=\"%d\" nItems=\"%d\" items=\"%s\"/>",
                    this.name, this.vm, this.vr, this.syngoDT, this.nItems, this.commaSeparatedItems());
        }

        public static Element read(BinaryInputStream bis) throws IOException {

            byte[] buffer = new byte[64];

            // name: 64 bytes string
            bis.readFully(buffer, 0, 64);
            String name = bytesToString(buffer, 0, 64, true);

            // vm: 4bytes int
            int vm = bis.readInt();

            // vr: 4bytes string the 3rd and 4th char are unused 0x00.
            bis.readFully(buffer, 0, 4);
            String vr = new String(buffer, 0, 2);

            // syngoDT: 4bytes int
            int syngoDT = bis.readInt();

            // nItems: 4bytes int
            int nItems = bis.readInt();

            // padding x: 4bytes: cd 00 00 00 / 4d 00 00 00
            int x = bis.readInt();
            if (!(x == 0xcd || x == 0x4d)) {
                throw new IOException("Failed to parse CSA attribute. Could not found  0x4d or 0xcd.");
            }
            String[] items = new String[nItems];
            for (int i = 0; i < nItems; i++) {
                int[] xx = new int[4];
                // length: int same as xx[1] xx[3]
                xx[0] = bis.readInt();
                // length: int same as xx[0] xx[1]
                xx[1] = bis.readInt();
                // padding x: int 0x4d or 0xcd
                xx[2] = bis.readInt();
                // length: int same as xx[0] xx[1]
                xx[3] = bis.readInt();
                if (!(xx[2] == 0x4d || xx[2] == 0xcd)) {
                    throw new IOException(
                            "Failed to parse CSA element item. Padding 0x4d or 0xcd for item length is not found.");
                }
                if (!(xx[0] == xx[1] && xx[1] == xx[3])) {
                    throw new IOException("Failed to parse CSA element item length.");
                }
                int length = xx[0];
                buffer = new byte[length];
                bis.readFully(buffer, 0, length);
                items[i] = bytesToString(buffer, 0, length, true);
                // skip the padding 00
                bis.skipFully((4 - length % 4) % 4);
            }
            return new Element(name, vm, vr, syngoDT, nItems, items);
        }
    }

    private Map<String, Element> elements;

    protected CSAHeaderInfo(List<Element> elements) {
        this.elements = new LinkedHashMap<String, Element>();
        if (elements != null) {
            for (Element e : elements) {
                this.elements.put(e.name, e);
            }
        }
    }

    @Override
    public Iterator<Element> iterator() {
        return elements.values().iterator();
    }

    public Collection<Element> elements() {
        return this.elements.values();
    }

    public int size() {
        return this.elements.size();
    }

    public Element element(String name) {
        return this.elements.get(name);
    }

    public static CSAHeaderInfo read(DicomInputStream in) throws IOException {
        try (BinaryInputStream bis = new BinaryInputStream(in, ByteOrder.LITTLE_ENDIAN, false)) {

            byte[] b = new byte[4];

            // "SV10"
            bis.readFully(b, 0, 4);
            if (!"SV10".equals(new String(b, 0, 4))) {
                throw new IOException("Failed to parse CSA attributes. Could not found 'SV10'");
            }
            // "\4\3\2\1"
            bis.readFully(b, 0, 4);
            if (!(b[0] == 4 && b[1] == 3 && b[2] == 2 && b[3] == 1)) {
                throw new IOException("Failed to parse CSA attributes. Could not found '\4\3\2\1' or 04 03 02 01.");
            }

            // number of elements: int/4bytes
            int numOfElements = bis.readInt();

            // unused 0x0000004d (M)
            if (bis.readInt() != 0x4d) {
                throw new IOException("Failed to parse CSA attributes. Could not found 4d 00 00 00");
            }
            List<Element> elements = new ArrayList<Element>(numOfElements);
            for (int i = 0; i < numOfElements; i++) {
                elements.add(Element.read(bis));
            }

            // 4 bytes boundary: 0x00000000
            bis.skipFully(4);
            return new CSAHeaderInfo(elements);
        }
    }

    private static String bytesToString(byte[] b, int off, int len, boolean trim) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = off; i < off + len; i++) {
            if (trim) {
                if (b[i] == 0) {
                    break;
                }
            }
            sb.append((char) b[i]);
        }
        return trim ? sb.toString().trim() : sb.toString();
    }
}
