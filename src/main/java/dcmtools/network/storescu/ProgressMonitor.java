package dcmtools.network.storescu;

public interface ProgressMonitor {

    void begin(int totalFiles);

    void incTransferredBytes(long increment);

    void incTransferredFiles();

    void end();

    Progress progress();
}
