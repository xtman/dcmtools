package dcmtools.network.storescp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.util.AttributesFormat;

import dcmtools.util.DicomFileUtils;

public class StoreSCP {

	public static final String DEFAULT_DEVICE_NAME = "STORESCP";

	public static final String DEFAULT_AE_TITLE = "STORESCP";

	public static final String DEFAULT_SOP_CLASSES_PROPERTIES_FILE = "storescp/sop-classes.properties";

	public static final String PART_EXT = ".part.tmp";

	public static final String DEFAULT_KEY_STORE_URL = "resource:storescp/key-store.jks";

	public static final String DEFAULT_KEY_STORE_TYPE = "JKS";

	public static final String DEFAULT_KEY_STORE_PASS = "storescp";

	public static final String DEFAULT_KEY_PASS = DEFAULT_KEY_STORE_PASS;

	public static final String DEFAULT_TRUST_STORE_URL = "resource:storescp/trust-store.jks";

	public static final String DEFAULT_TRUST_STORE_TYPE = "JKS";

	public static final String DEFAULT_TRUST_STORE_PASS = "storescp";

	public static final String DEFAULT_PATH_PATTERN = String.format("{%08X}/{%08X}/{%08X}/{%08X}/{%08X}.dcm",
			Tag.SourceApplicationEntityTitle, Tag.PatientID, Tag.StudyInstanceUID, Tag.SeriesInstanceUID,
			Tag.SOPInstanceUID);

	private static final Logger logger = LogManager.getLogger(StoreSCP.class);

	public static final int DEFAULT_PORT = 11112;

	private final Options _options;
	private AttributesFormat _filePathFormat;
	private final Device _device;
	private final ApplicationEntity _ae;
	private final Connection _connection;
	private final BasicCStoreSCP _cstoreSCP;

	private ExecutorService _executor;
	private ScheduledExecutorService _scheduledExecutor;

	public StoreSCP(Options options) throws IOException {
		_options = options;
		_filePathFormat = new AttributesFormat(
				_options.pathPattern() == null ? DEFAULT_PATH_PATTERN : _options.pathPattern());
		_device = new Device(DEFAULT_DEVICE_NAME);
		_ae = new ApplicationEntity(_options.applicationEntity().title);
		_connection = new Connection();
		if (_options.applicationEntity().host != null) {
			_connection.setHostname(_options.applicationEntity().host);
		}
		_connection.setPort(_options.applicationEntity().port);

		// cstore scp
		_cstoreSCP = new BasicCStoreSCP("*") {

			@Override
			protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data,
					Attributes rsp) throws IOException {
				try {
					rsp.setInt(Tag.Status, VR.US, _options.status());
					if (_options.directory() == null) {
						// TODO warn
						return;
					}

					String sopClassUID = rq.getString(Tag.AffectedSOPClassUID);
					String sopInstanceUID = rq.getString(Tag.AffectedSOPInstanceUID);
					String transferSyntaxUID = pc.getTransferSyntax();

					Path tmpFile = Paths.get(_options.directory().toString(), sopInstanceUID + PART_EXT);
					try {
						storeTo(as, as.createFileMetaInformation(sopInstanceUID, sopClassUID, transferSyntaxUID), data,
								tmpFile);
						Attributes attrs = DicomFileUtils.getDicomAttributes(tmpFile);
						Path dstFile = Paths.get(_options.directory().toString(),
								_filePathFormat == null ? sopInstanceUID : _filePathFormat.format(attrs));
						moveTo(as, tmpFile, dstFile);
					} catch (Exception e) {
						if (Files.deleteIfExists(tmpFile)) {
							logger.info("{}: M-DELETE {}", as, tmpFile);
						} else {
							logger.warn("{}: M-DELETE {} failed!", as, tmpFile);
						}
						throw new DicomServiceException(Status.ProcessingFailure, e);
					}
				} finally {
					if (_options.responseDelay() > 0) {
						try {
							Thread.sleep(_options.responseDelay());
						} catch (InterruptedException ie) {

						}
					}
				}
			}

		};

		// service registry
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(_cstoreSCP);

		// device
		_device.setDimseRQHandler(serviceRegistry);
		_device.addConnection(_connection);
		_device.addApplicationEntity(_ae);
		_ae.setAssociationAcceptor(true);
		_ae.addConnection(_connection);

		// connection
		_connection.setReceivePDULength(_options.maxRcvPDULength());
		_connection.setSendPDULength(_options.maxSndPDULength());
		_connection.setPackPDV(_options.packPDV());
//      _connection.setAcceptTimeout(0);
//      _connection.setConnectTimeout(0);
//      _connection.setResponseTimeout(0);       
		_connection.setReleaseTimeout(_options.releaseTimeout());
		_connection.setRequestTimeout(_options.requestTimeout());
		_connection.setIdleTimeout(_options.idleTimeout());
		_connection.setSocketCloseDelay(_options.socketCloseDelay());
		_connection.setSendBufferSize(_options.socketSndBufferSize());
		_connection.setReceiveBufferSize(_options.socketRcvBufferSize());
		_connection.setTcpNoDelay(_options.tcpNoDelay());

		// tls
		if (_options.tlsCiphers() != null && _options.tlsCiphers().length > 0) {
			_connection.setTlsCipherSuites(_options.tlsCiphers());
			if (_connection.isTls()) {
				if (_options.tlsProtocols() != null) {
					_connection.setTlsProtocols(_options.tlsProtocols());
				}
			}
		}
		_connection.setTlsNeedClientAuth(!_options.tlsNoAuth());

		// transfer capabilities
		if (_options.acceptUnknown()) {
			_ae.addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));
		} else {
			Map<String, String> sopClasses = _options.sopClasses();
			if (sopClasses != null) {
				for (String cuid : sopClasses.keySet()) {
					String ts = sopClasses.get(cuid);
					TransferCapability tc = new TransferCapability(null, toUID(cuid), TransferCapability.Role.SCP,
							toUIDs(ts));
					_ae.addTransferCapability(tc);
				}
			}
		}
	}

	private static String toUID(String uid) {
		uid = uid.trim();
		return (uid.equals("*") || Character.isDigit(uid.charAt(0))) ? uid : UID.forName(uid);
	}

	private static String[] toUIDs(String s) {
		s.trim();
		if (s.equals("*")) {
			return new String[] { "*" };
		}
		String[] uids = s.split("\\ *,\\ *");
		for (int i = 0; i < uids.length; i++) {
			uids[i] = toUID(uids[i]);
		}
		return uids;
	}

	private void storeTo(Association as, Attributes fmi, PDVInputStream data, Path file) throws IOException {
		logger.info("{}: M-WRITE {}", as, file);
		Path dir = file.getParent();
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}
		try (DicomOutputStream out = new DicomOutputStream(Files.newOutputStream(file), UID.ExplicitVRLittleEndian)) {
			out.writeFileMetaInformation(fmi);
			data.copyTo(out);
		} finally {
		}
	}

	private static void moveTo(Association as, Path fromFile, Path toFile) throws IOException {
		logger.info("{}: M-RENAME {} to {}", as, fromFile, toFile);
		Path toDir = toFile.getParent();
		if (!Files.exists(toDir)) {
			Files.createDirectories(toDir);
		}
		Files.move(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public void start() {
		if (_executor != null && !_executor.isShutdown()) {
			throw new IllegalStateException(this.getClass().getSimpleName() + ": executor already started.");
		}
		if (_scheduledExecutor != null && !_scheduledExecutor.isShutdown()) {
			throw new IllegalStateException(this.getClass().getSimpleName() + ": scheduled executor already started.");
		}
		try {
			_executor = Executors.newCachedThreadPool();
			_scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			_device.setScheduledExecutor(_scheduledExecutor);
			_device.setExecutor(_executor);
			_device.bindConnections();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			System.exit(2);
		}
	}

}
