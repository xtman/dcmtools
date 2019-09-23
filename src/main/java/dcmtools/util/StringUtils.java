package dcmtools.util;

public class StringUtils {

    public static String repeat(char c, int n) {
        return new String(new char[n]).replace("\0", Character.toString(c));
    }

}
