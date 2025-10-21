package net.sf.borg.model.sync.google;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.gson.stream.MalformedJsonException;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.db.jdbc.JdbcDB;

public class GDrive {
	private static final String APPLICATION_NAME = "BORG Calendar";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	static private final Logger log = Logger.getLogger("net.sf.borg");
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

	private Drive service = null;
	static volatile private GDrive singleton = null;

	static public GDrive getReference() {
		if (singleton == null) {
			GDrive b = new GDrive();
			singleton = b;
		}
		return (singleton);
	}

	public void connect() throws Exception {
		
		String googleFileId = Prefs.getPref(PrefName.GOOGLE_DB_FILE_ID);
		if( googleFileId == null || googleFileId.isEmpty())
		{
			log.info("GDrive:connect(): Google File Id is not set");
			return;
		}

		if (service != null)
			return;

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

	}

	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
		// Load client secrets.
		java.io.File f = new java.io.File(Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		
		try {
			InputStream in = new FileInputStream(f);

			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
					clientSecrets, SCOPES)
					.setDataStoreFactory(
							new FileDataStoreFactory(new java.io.File(Prefs.getPref(PrefName.GOOGLE_TOKEN_DIR)+"/drivecred")))
					.setAccessType("offline").build();
			LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
			Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
			// returns an authorized Credential object.
			return credential;
		} catch (FileNotFoundException fe) {
			throw new Exception("Credentials File not found: " + Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		} catch (MalformedJsonException mj) {
			log.severe(mj.toString());
			throw new Exception("could not parse JSON credentials file: " + Prefs.getPref(PrefName.GOOGLE_CRED_FILE));
		}

	}

	public void checkModTimes() throws IOException {
		
		String googleFileId = Prefs.getPref(PrefName.GOOGLE_DB_FILE_ID);
		if( googleFileId == null || googleFileId.isEmpty())
		{
			log.info("GDrive:checkModTimes(): Google File Id is not set");
			return;
		}
		

		String localDBFile = JdbcDB.getDBFilePath();
		if( localDBFile == null ) return;
		
		File fileMeta = service.files().get(googleFileId).setFields("id,name,modifiedTime").execute();

		log.info("Google File Mod: " + fileMeta.getName() + " " + fileMeta.getModifiedTime() + " "
				+ fileMeta.getModifiedTime().getValue());

		
		java.io.File f = new java.io.File(localDBFile);

		if (f.exists()) {

			long lastModifiedMillis = f.lastModified();
			Date lastModifiedDate = new Date(lastModifiedMillis);
			
			log.info("Database File Mod: " + localDBFile + " " + lastModifiedMillis + " " + lastModifiedDate);
			
			if( lastModifiedMillis < fileMeta.getModifiedTime().getValue()) {
				Errmsg.getErrorHandler().notice("Google DB file is newer than local File, sync needed");
				JOptionPane.showMessageDialog(null, "Google DB file is newer than local File, sync needed");

			}


		} else {
			log.warning(localDBFile + "Not Found");
		}
		
		

	}
}