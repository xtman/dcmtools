package dcmtools.network.storescu;

public class Progress {

    public final int totalFiles;

    public final int transferredFiles;

    public final long transferredBytes;

    public Progress(long transferredBytes, int transferredFiles, int totalFiles) {
        this.transferredBytes = transferredBytes;
        this.transferredFiles = transferredFiles;
        this.totalFiles = totalFiles;
    }

    public String toString() {
        return String.format("%d/%d files (%d bytes)", this.transferredFiles, this.totalFiles, this.transferredBytes);
    }

}
