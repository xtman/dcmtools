package dcmtools.network.storescu;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultProgressMonitor implements ProgressMonitor {

    private final AtomicInteger _totalFiles;
    private final AtomicInteger _transferredFiles;
    private final AtomicLong _transferredBytes;

    public DefaultProgressMonitor(int totalFiles) {
        _totalFiles = new AtomicInteger(totalFiles);
        _transferredFiles = new AtomicInteger(0);
        _transferredBytes = new AtomicLong(0);
    }

    public DefaultProgressMonitor() {
        this(0);
    }

    public int totalFiles() {
        return _totalFiles.get();
    }

    public int transferredFiles() {
        return _transferredFiles.get();
    }

    public long transferredBytes() {
        return _transferredBytes.get();
    }

    public void begin(int totalFiles) {
        _transferredBytes.set(0);
        _transferredFiles.set(0);
        _totalFiles.set(totalFiles);
    }

    public void incTransferredBytes(long increment) {
        _transferredBytes.getAndAdd(increment);
    }

    public void incTransferredFiles() {
        _transferredFiles.getAndIncrement();
    }

    public void end() {

    }

    @Override
    public Progress progress() {
        return new Progress(transferredBytes(), transferredFiles(), totalFiles());
    }

}
