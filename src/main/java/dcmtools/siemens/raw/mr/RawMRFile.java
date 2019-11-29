package dcmtools.siemens.raw.mr;

import java.io.BufferedInputStream;
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
import java.util.LinkedHashSet;
import java.util.Set;

import dcmtools.io.BinaryInputStream;
import dcmtools.io.CountingInputStream;
import dcmtools.io.SizedInputStream;

public class RawMRFile {

    public static void dump(Path f, boolean metadata, String... protocols) throws IOException {
        dump(f, System.out, metadata, protocols);
    }

    public static void dump(Path f, Path of, boolean metadata, String... protocols) throws IOException {
        try (OutputStream os = Files.newOutputStream(of);
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw)) {
            dump(f, bw, metadata, protocols);
        }
    }

    public static void dump(Path f, OutputStream out, boolean metadata, String... protocols) throws IOException {
        long fileSize = Files.size(f);
        try (InputStream in = Files.newInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(in);
                CountingInputStream cis = new CountingInputStream(bis)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(out); BufferedWriter bw = new BufferedWriter(osw)) {
                dump(cis, osw, metadata, protocols);
                osw.write(String.format("[%08XH] data.length=%d", cis.count(), fileSize - cis.count()));
            }
        }
    }

    public static void dump(Path f, Writer w, boolean metadata, String... protocols) throws IOException {
        long fileSize = Files.size(f);
        try (InputStream in = Files.newInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(in);
                CountingInputStream cis = new CountingInputStream(bis)) {
            dump(cis, w, metadata, protocols);
            w.write(String.format("[%08XH] data.length=%d", cis.count(), fileSize - cis.count()));
        }
    }

    public static void dump(InputStream in, Writer w, boolean metadata, String... protocols) throws IOException {

        Set<String> ps = new LinkedHashSet<String>();
        if (protocols != null) {
            for (String p : protocols) {
                ps.add(p.toLowerCase());
            }
        }
        try (BinaryInputStream bis = new BinaryInputStream(in, ByteOrder.LITTLE_ENDIAN, false)) {

            long offset = bis.count();

            int protocolsLength = bis.readInt();

            offset = bis.count();
            int protocolsCount = bis.readInt();

            w.write(String.format("[%08XH] protocols.length=%d, protocols.count=%d", offset, protocolsLength,
                    protocolsCount));
            w.write(System.lineSeparator());
            w.flush();

            for (int i = 0; i < protocolsCount; i++) {
                offset = bis.count();
                String protocolName = RawMRMetaData.readString(bis);

                offset = bis.count();
                int protocolLength = bis.readInt();

                w.write(String.format("[%08XH] protocol.name=%s, protocol.length=%d", offset, protocolName,
                        protocolLength));
                w.write(System.lineSeparator());
                w.flush();

                if (metadata && (ps.isEmpty() || ps.contains(protocolName.toLowerCase()))) {
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
        }
    }

    public static void main(String[] args) throws IOException {
//        Path rf = Paths.get("/Users/wliu5/Downloads/meas_MID36_EdLineFullKlineXSLAC146V_FID33313.dat");
//        dump(rf, true, "Dicom");
    }
}
