package dcmtools.network.storescu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;

import dcmtools.util.DicomFileInfo;
import dcmtools.util.DicomFiles;

public class StoreSCU {

	public static final String DEFAULT_DEVICE_NAME = "STORESCU";

	public static final String DEFAULT_AE_TITLE = "STORESCU";

	public static final String DEFAULT_RELATED_SOP_CLASSES_PROPERTIES_FILE = "storescu/related-sop-classes.properties";

	public static final String DEFAULT_KEY_STORE_URL = "resource:storescu/key-store.jks";

	public static final String DEFAULT_KEY_STORE_TYPE = "JKS";

	public static final String DEFAULT_KEY_STORE_PASS = "storescu";

	public static final String DEFAULT_KEY_PASS = DEFAULT_KEY_STORE_PASS;

	public static final String DEFAULT_TRUST_STORE_URL = "resource:storescu/trust-store.jks";

	public static final String DEFAULT_TRUST_STORE_TYPE = "JKS";

	public static final String DEFAULT_TRUST_STORE_PASS = "storescu";

	private static final Logger logger = LogManager.getLogger(StoreSCU.class);

	public static interface DimseRSPHandlerFactory {
		DimseRSPHandler createDimseRSPHandler(Association as, Path f, ProgressMonitor progressMonitor);
	}

	public static class DefaultDimseRSPHandlerFactory implements DimseRSPHandlerFactory {

		@Override
		public DimseRSPHandler createDimseRSPHandler(final Association as, final Path f,
				final ProgressMonitor progressMonitor) {
			return new DimseRSPHandler(as.nextMessageID()) {
				@Override
				public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
					super.onDimseRSP(as, cmd, data);
					int status = cmd.getInt(Tag.Status, -1);
					long fileSize = 0;
					try {
						fileSize = Files.size(f);
					} catch (IOException e) {
						logger.error("Failed to get file size: " + f, e);
						return;
					}
					switch (status) {
					case Status.Success:
						if (progressMonitor != null) {
							progressMonitor.incTransferredBytes(fileSize);
							progressMonitor.incTransferredFiles();
						}
						logger.info(String.format("Received C-STORE-RSP with Status %sH for %s",
								TagUtils.shortToHexString(status), f.toString()));
						break;
					case Status.CoercionOfDataElements:
					case Status.ElementsDiscarded:
					case Status.DataSetDoesNotMatchSOPClassWarning:
						if (progressMonitor != null) {
							progressMonitor.incTransferredBytes(fileSize);
							progressMonitor.incTransferredFiles();
						}
						logger.warn(String.format("Received C-STORE-RSP with Status %sH for %s",
								TagUtils.shortToHexString(status), f.toString()));
						logger.warn(cmd.toString());
						break;
					default:
						logger.error(String.format("Received C-STORE-RSP with Status %sH for %s",
								TagUtils.shortToHexString(status), f.toString()));
						logger.error(cmd.toString());
					}
				}
			};
		}

	}

	private final Options _op;
	private DimseRSPHandlerFactory _rspHandlerFactory;

	private final ApplicationEntity _ae;
	private final Connection _lc;
	private final Connection _rc;
	private final AAssociateRQ _rq;
	private Association _as;

	public StoreSCU(Options options) throws Exception {
		this(options, new DefaultDimseRSPHandlerFactory());
	}

	public StoreSCU(Options options, DimseRSPHandlerFactory rspHandlerFactory) throws Exception {

		_op = options;
		_rspHandlerFactory = rspHandlerFactory == null ? new DefaultDimseRSPHandlerFactory() : rspHandlerFactory;

		// local bind connection
		_lc = new Connection();
		if (_op.applicationEntity().host != null) {
			_lc.setHostname(_op.applicationEntity().host);
		}
		if (_op.applicationEntity().port > 0) {
			_lc.setPort(_op.applicationEntity().port);
		}
		_lc.setSendPDULength(_op.maxSndPDULength() > 0 ? _op.maxSndPDULength() : Connection.DEF_MAX_PDU_LENGTH);
		_lc.setReceivePDULength(_op.maxRcvPDULength() > 0 ? _op.maxRcvPDULength() : Connection.DEF_MAX_PDU_LENGTH);
		_lc.setMaxOpsInvoked(_op.maxOpsInvoked() > 0 ? _op.maxOpsInvoked() : 0); // 0: unlimited
		_lc.setMaxOpsPerformed(_op.maxOpsPerformed() > 0 ? _op.maxOpsPerformed() : 0); // 0: unlimited
		_lc.setPackPDV(_op.packPDV());
		_lc.setConnectTimeout(_op.connectTimeout());
		_lc.setRequestTimeout(_op.requestTimeout());
		_lc.setAcceptTimeout(_op.acceptTimeout());
		_lc.setReleaseTimeout(_op.releaseTimeout());
		_lc.setResponseTimeout(_op.responseTimeout());
		if (_op.retrieveTimeout() > 0) {
			_lc.setRetrieveTimeout(_op.retrieveTimeout());
			_lc.setRetrieveTimeoutTotal(false);
		} else if (_op.retrieveTimeoutTotal() > 0) {
			_lc.setRetrieveTimeout(_op.retrieveTimeoutTotal());
			_lc.setRetrieveTimeoutTotal(true);
		}
		_lc.setIdleTimeout(_op.idleTimeout());
		_lc.setSocketCloseDelay(_op.socketCloseDelay() > 0 ? _op.socketCloseDelay() : Connection.DEF_SOCKETDELAY);
		_lc.setSendBufferSize(_op.socketSndBufferSize());
		_lc.setReceiveBufferSize(_op.socketRcvBufferSize());
		_lc.setTcpNoDelay(_op.tcpNoDelay());

		// tls
		if (_op.tlsCiphers() != null && _op.tlsCiphers().length > 0) {
			_lc.setTlsCipherSuites(_op.tlsCiphers());
			if (_lc.isTls()) {
				if (_op.tlsProtocols() != null) {
					_lc.setTlsProtocols(_op.tlsProtocols());
				}
			}
		}

		_lc.setTlsNeedClientAuth(!_op.tlsNoAuth());

		Device device = new Device(DEFAULT_DEVICE_NAME);
		device.addConnection(_lc);
		_ae = new ApplicationEntity(
				(_op.applicationEntity() == null || _op.applicationEntity().title == null) ? DEFAULT_AE_TITLE
						: _op.applicationEntity().title);
		device.addApplicationEntity(_ae);
		_ae.addConnection(_lc);

		if (_op.keyStore() != null) {
			device.setKeyManager(SSLManagerFactory.createKeyManager(_op.keyStore().type(), _op.keyStore().path(),
					_op.keyStore().password(), _op.keyPass()));
		}
		if (_op.trustStore() != null) {
			device.setTrustManager(SSLManagerFactory.createTrustManager(_op.trustStore().type(),
					_op.trustStore().path(), _op.trustStore().password()));
		}

		_rc = new Connection();
		_rc.setHostname(_op.remoteApplicationEntity().host);
		_rc.setPort(_op.remoteApplicationEntity().port);
		_rc.setTlsCipherSuites(_lc.getTlsCipherSuites());
		_rc.setTlsProtocols(_lc.getTlsProtocols());
		if (_op.httpProxy() != null) {
			_rc.setHttpProxy(_op.httpProxy().toString());
		}

		_rq = new AAssociateRQ();
		_rq.addPresentationContext(new PresentationContext(1, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian));
		_rq.setCalledAET(_op.remoteApplicationEntity().title);
		if (_op.username() != null) {
			_rq.setUserIdentityRQ(
					_op.userPassword() != null ? new UserIdentityRQ(_op.username(), _op.userPassword().toCharArray())
							: new UserIdentityRQ(_op.username(), _op.userRSP()));
		}
	}

	public Progress send(Path... paths) throws Exception {
		ProgressMonitor progressMonitor = new DefaultProgressMonitor();
		send(DicomFiles.scan(paths), progressMonitor);
		return progressMonitor.progress();
	}

	public void send(ProgressMonitor progressMonitor, Path... paths) throws Exception {
		send(DicomFiles.scan(paths), progressMonitor);
	}

	public Progress send(DicomFiles dicomFiles) throws Exception {
		ProgressMonitor progressMonitor = new DefaultProgressMonitor();
		send(dicomFiles, progressMonitor);
		return progressMonitor.progress();
	}

	public void send(DicomFiles dicomFiles, ProgressMonitor progressMonitor) throws Exception {
		if (dicomFiles == null) {
			return;
		}
		Map<String, String> tss = dicomFiles.transferSyntax();
		for (String cuid : tss.keySet()) {
			String ts = tss.get(cuid);
			if (!_rq.containsPresentationContextFor(cuid, ts)) {
				if (!_rq.containsPresentationContextFor(cuid)) {
					Map<String, CommonExtendedNegotiation> relatedSOPClasses = _op.relatedSOPClasses();
					if (relatedSOPClasses != null && relatedSOPClasses.containsKey(cuid)) {
						_rq.addCommonExtendedNegotiation(relatedSOPClasses.get(cuid));
					}
					if (!ts.equals(UID.ExplicitVRLittleEndian)) {
						_rq.addPresentationContext(new PresentationContext(
								_rq.getNumberOfPresentationContexts() * 2 + 1, cuid, UID.ExplicitVRLittleEndian));
					}
					if (!ts.equals(UID.ImplicitVRLittleEndian)) {
						_rq.addPresentationContext(new PresentationContext(
								_rq.getNumberOfPresentationContexts() * 2 + 1, cuid, UID.ImplicitVRLittleEndian));
					}
				}
				_rq.addPresentationContext(
						new PresentationContext(_rq.getNumberOfPresentationContexts() * 2 + 1, cuid, ts));
			}
		}
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		Device device = _lc.getDevice();
		device.setExecutor(executor);
		device.setScheduledExecutor(scheduledExecutor);
		try {
			open();
			try {
				if (dicomFiles.isEmpty()) {
					_as.cecho().next();
				} else {
					if (progressMonitor != null) {
						progressMonitor.begin(dicomFiles.size());
					}
					sendDicomFiles(dicomFiles, progressMonitor);
					if (progressMonitor != null) {
						progressMonitor.end();
					}
				}
			} finally {
				close();
			}
		} finally {
			executor.shutdown();
			scheduledExecutor.shutdown();
		}
	}

	private void sendDicomFiles(DicomFiles dicomFiles, ProgressMonitor progressMonitor) throws Exception {
		for (DicomFileInfo dicomFile : dicomFiles) {
			if (!_as.isReadyForDataTransfer()) {
				break;
			}
			sendDicomFile(dicomFile, progressMonitor);
		}
		try {
			_as.waitForOutstandingRSP();
		} catch (InterruptedException ie) {
			logger.warn(ie.getMessage(), ie);
		}
	}

	private void sendDicomFile(DicomFileInfo dicomFile, ProgressMonitor progressMonitor) throws Exception {

		/*
		 * transfer syntax UID
		 */
		String ts = dicomFile.transferSyntaxUID;
		Set<String> tss = _as.getTransferSyntaxesFor(dicomFile.mediaStorageSOPClassUID);
		if (tss.contains(dicomFile.transferSyntaxUID)) {
			ts = dicomFile.transferSyntaxUID;
		} else if (tss.contains(UID.ExplicitVRLittleEndian)) {
			ts = UID.ExplicitVRLittleEndian;
		} else {
			ts = UID.ImplicitVRLittleEndian;
		}

		if (dicomFile.path.toString().endsWith(".xml")) {
			// parsed dicom file
			try (InputStream in = new BufferedInputStream(Files.newInputStream(dicomFile.path))) {
				Attributes data = SAXReader.parse(in);
				String mediaStorageSOPInstanceUID = dicomFile.mediaStorageSOPInstanceUID;
				if (updateAttributes(data)) {
					mediaStorageSOPInstanceUID = data.getString(Tag.SOPInstanceUID);
				}
				if (!ts.equals(dicomFile.transferSyntaxUID)) {
					Decompressor.decompress(data, dicomFile.transferSyntaxUID);
				}
				_as.cstore(dicomFile.mediaStorageSOPClassUID, mediaStorageSOPInstanceUID, _op.priority(),
						new DataWriterAdapter(data), ts,
						_rspHandlerFactory.createDimseRSPHandler(_as, dicomFile.path, progressMonitor));
			}
		} else {
			if (_op.uidSuffix() == null && !_op.hasAttributes() && ts.equals(dicomFile.transferSyntaxUID)) {
				try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(dicomFile.path))) {
					StreamUtils.skipFully(in, dicomFile.datasetOffset);
					_as.cstore(dicomFile.mediaStorageSOPClassUID, dicomFile.mediaStorageSOPInstanceUID, _op.priority(),
							new InputStreamDataWriter(in), ts,
							_rspHandlerFactory.createDimseRSPHandler(_as, dicomFile.path, progressMonitor));
				}
			} else {
				try (DicomInputStream in = new DicomInputStream(
						new BufferedInputStream(Files.newInputStream(dicomFile.path)))) {
					in.setIncludeBulkData(IncludeBulkData.URI);
					Attributes data = in.readDataset(-1, -1);
					String mediaStorageSOPInstanceUID = dicomFile.mediaStorageSOPInstanceUID;
					if (updateAttributes(data))
						mediaStorageSOPInstanceUID = data.getString(Tag.SOPInstanceUID);
					if (!ts.equals(dicomFile.transferSyntaxUID)) {
						Decompressor.decompress(data, dicomFile.transferSyntaxUID);
					}
					_as.cstore(dicomFile.mediaStorageSOPClassUID, mediaStorageSOPInstanceUID, _op.priority(),
							new DataWriterAdapter(data), ts,
							_rspHandlerFactory.createDimseRSPHandler(_as, dicomFile.path, progressMonitor));
				}
			}
		}
	}

	private boolean updateAttributes(Attributes data) {
		if (_op.uidSuffix() == null && !_op.hasAttributes()) {
			// no change
			return false;
		}
		if (_op.uidSuffix() != null) {
			// update uid with defined suffix
			data.setString(Tag.StudyInstanceUID, VR.UI, data.getString(Tag.StudyInstanceUID) + _op.uidSuffix());
			data.setString(Tag.SeriesInstanceUID, VR.UI, data.getString(Tag.SeriesInstanceUID) + _op.uidSuffix());
			data.setString(Tag.SOPInstanceUID, VR.UI, data.getString(Tag.SOPInstanceUID) + _op.uidSuffix());
		}
		// update with common attributes
		data.update(Attributes.UpdatePolicy.OVERWRITE, _op.attributes(), null);
		return true;
	}

	private void close() throws IOException, InterruptedException {
		if (_as != null) {
			if (_as.isReadyForDataTransfer()) {
				_as.release();
			}
			_as.waitForSocketClose();
			_as = null;
		}
	}

	private void open()
			throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
		if (_as == null) {
			_as = _ae.connect(_rc, _rq);
		}
	}

}
