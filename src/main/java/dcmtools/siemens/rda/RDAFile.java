package dcmtools.siemens.rda;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class RDAFile {

    /**
     * Prevent the class to be instantiated.
     */
    private RDAFile() {
    };

    /**
     * RDA file is always little endian.
     */
    public static final boolean LittleEndian = true;

    /**
     * Creates an RDA file using specified header, data, and save to the specified
     * Java File object.
     * 
     * @param header RDA file header
     * @param data   RDA data
     * @param f      The output file.
     * @throws IOException
     */
    public static void create(RDAFileHeader header, double[] data, Path f) throws IOException {

        if (header == null) {
            throw new IllegalArgumentException("RDA file header is not set.");
        }

        if (data == null) {
            throw new IllegalArgumentException("RDA file data is not set.");
        }

        try (OutputStream os = Files.newOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos)) {

            dos.write(header.toString().getBytes());
            for (int i = 0; i < data.length; i++) {
                if (LittleEndian) {
                    dos.writeLong(Long.reverseBytes((Double.doubleToLongBits(data[i]))));
                } else {
                    dos.writeDouble(data[i]);
                }
            }

        }

    }

    /**
     * Creates an RDA file using specified header, data, and save to the specified
     * Java File object.
     * 
     * @param header RDA file header
     * @param data   RDA data
     * @param f      The output file.
     * @throws IOException
     */
    public static void create(RDAFileHeader header, double[] data, File f) throws IOException {
        create(header, data, f.toPath());
    }

}
