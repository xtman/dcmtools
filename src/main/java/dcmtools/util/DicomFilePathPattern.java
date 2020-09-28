package dcmtools.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

public class DicomFilePathPattern {

    // @formatter:off
    public static final String DEFAULT_PATTERN = "{0020000D}/{0020000E}/{00080018}.dcm";
    public static final DicomFilePathPattern DEFAULT = new DicomFilePathPattern(DEFAULT_PATTERN);
    // @formatter:on

    static String normalize(String path) {
        if (path == null) {
            return path;
        }
        // @formatter:off
        return path.trim()
                .replaceAll("\\ */\\ *", "/")
                .replaceAll("\\ *\\\\\\ *", "\\\\")
                .replaceAll("/{2,}", "/")
                .replaceAll("\\\\{2,}", "\\\\");
        // @formatter:on
    }

    private final String _pattern;

    public DicomFilePathPattern(String pattern) {
        _pattern = normalize(pattern);
    }

    public String pattern() {
        return _pattern;
    }

    public String compile(Attributes attrs) throws ParseException {
        if (_pattern != null && !_pattern.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int cbs = -1;
            int cbe = -1;
            int from = 0;
            while (from < _pattern.length()) {
                cbs = _pattern.indexOf('{', from);
                if (cbs >= 0) {
                    cbe = _pattern.indexOf('}', cbs);
                    if (cbe > 0) {
                        String tagStr = _pattern.substring(cbs + 1, cbe);
                        tagStr = tagStr == null ? null : tagStr.trim();
                        if (tagStr.matches("^[0-9abcedfABCDEF]{8}$")) {
                            int tag = Integer.parseUnsignedInt(tagStr, 16);
                            System.out.println(String.format("%08X", tag));
                            String value = attrs.getString(tag);
                            sb.append(_pattern.subSequence(from, cbs));
                            if (value != null) {
                                sb.append(FileNameUtils.tidySafeFileName(value));
                            }
                            from = cbe + 1;
                            cbs = -1;
                            cbe = -1;
                        } else {
                            throw new ParseException("Failed to parse pattern: " + _pattern, cbs);
                        }
                    } else {
                        throw new ParseException("Failed to parse pattern: " + _pattern, cbs);
                    }
                } else {
                    sb.append(_pattern.subSequence(from, _pattern.length()));
                    from = _pattern.length();
                }
            }
            if (sb.length() > 0) {
                return normalize(sb.toString());
            }
        }
        throw new ParseException("Failed to parse pattern: " + _pattern, 0);
    }

    public String compile(DicomInputStream dis) throws IOException, ParseException {
        return compile(DicomFileUtils.getDicomAttributes(dis, false, true));
    }

    public String compile(Path dicomFile) throws IOException, ParseException {
        try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(Files.newInputStream(dicomFile)))) {
            return compile(dis);
        }
    }

    public String compile(File dicomFile) throws IOException, ParseException {
        try (DicomInputStream dis = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomFile)))) {
            return compile(dis);
        }
    }

}
