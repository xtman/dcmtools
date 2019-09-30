package dcmtools.picocli.converters;

import java.text.ParseException;

import picocli.CommandLine.ITypeConverter;

public class StringArrayConverter implements ITypeConverter<String[]> {

    private String _separator;
    private String[] _candidates;

    public StringArrayConverter(String separator, String[] candidates) {
        _separator = separator;
        _candidates = candidates;
    }

    @Override
    public String[] convert(String value) throws Exception {
        String[] r = null;
        if (value == null || value.indexOf(_separator) == -1) {
            r = new String[] { value };
        } else {
            r = value.split("\\ *" + _separator + "\\ *");
        }
        if (_candidates != null && _candidates.length > 0) {
            for (String v : r) {
                boolean found = false;
                for (String c : _candidates) {
                    if (c != null && c.equals(v)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ParseException("Failed to parse: '" + value + "'. '" + v + "' is not a valid value.", 0);
                }
            }
        }
        return r;
    }

}
