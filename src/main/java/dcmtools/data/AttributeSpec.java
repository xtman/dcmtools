package dcmtools.data;

import java.text.ParseException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;

import picocli.CommandLine.ITypeConverter;

public class AttributeSpec {

    private int[] _tags;
    private String[] _values;

    public AttributeSpec(int[] tags, String[] values) {
        _tags = tags;
        _values = values;
    }

    public int[] tags() {
        return _tags;
    }

    public String[] values() {
        return _values;
    }

    public void addToAttributes(Attributes attrs) {
        addAttribute(attrs, this);
    }

    public static AttributeSpec parse(String v) throws ParseException {
        int idx = v.indexOf('=');

        String tagsPart = idx == -1 ? v.trim() : v.substring(0, idx).trim();
        String valuePart = idx == -1 ? "" : (idx == v.length() - 1 ? "" : v.substring(idx + 1).trim());

        int[] tags;
        if (tagsPart.indexOf('/') == -1) {
            tags = toTags(new String[] { tagsPart });
        } else {
            tags = toTags(tagsPart.split("\\ */\\ *"));
        }
        String[] values = valuePart.split("\\ *,\\ *");
        return new AttributeSpec(tags, values);
    }

    private static int toTag(String tagOrKeyword) {
        if (tagOrKeyword.trim().matches("\\d{8}")) {
            return Integer.parseUnsignedInt(tagOrKeyword, 16);
        } else {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1) {
                throw new IllegalArgumentException("Invalid element name: " + tagOrKeyword);
            }
            return tag;
        }
    }

    private static int[] toTags(String[] tagOrKeywords) {
        int[] tags = new int[tagOrKeywords.length];
        for (int i = 0; i < tags.length; i++)
            tags[i] = toTag(tagOrKeywords[i]);
        return tags;
    }

    public static void addAttribute(Attributes attrs, AttributeSpec a) {
        addAttribute(attrs, a.tags(), a.values());
    }

    public static void addAttribute(Attributes attrs, int[] tags, String... values) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length - 1; i++) {
            int tag = tags[i];
            Sequence sq = item.getSequence(tag);
            if (sq == null) {
                sq = item.newSequence(tag, 1);
            }
            if (sq.isEmpty()) {
                sq.add(new Attributes());
            }
            item = sq.get(0);
        }
        int tag = tags[tags.length - 1];
        VR vr = ElementDictionary.vrOf(tag, item.getPrivateCreator(tag));
        if (values.length == 0) {
            if (vr == VR.SQ) {
                item.newSequence(tag, 1).add(new Attributes(0));
            } else {
                item.setNull(tag, vr);
            }
        } else {
            item.setString(tag, vr, values);
        }
    }

    public static class Converter implements ITypeConverter<AttributeSpec> {

        @Override
        public AttributeSpec convert(String value) throws Exception {
            return AttributeSpec.parse(value);
        }

    }
}
