package dcmtools.util;

public class FileNameUtils {

    public static String tidySafeFileName(String name, String specialCharReplacement, int maxLength) {
        name = name.trim().replaceAll("\\ {2,}+", " ");
        name = name.replaceAll("[\\/:*?\"<>|]", specialCharReplacement);
        name = name.replaceAll("\\ {2,}+", " ").trim();
        if (maxLength > 0 && name.length() > maxLength) {
            return name.substring(0, maxLength).trim();
        } else {
            return name;
        }
    }

    public static String tidySafeFileName(String name, int maxLength) {
        return tidySafeFileName(name, "", maxLength);
    }

    public static String tidySafeFileName(String name) {
        return tidySafeFileName(name, "", -1);
    }

}
