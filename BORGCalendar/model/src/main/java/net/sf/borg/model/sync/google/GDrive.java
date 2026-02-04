package net.sf.borg.model.sync.google;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
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
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDB;

/*
 * Much of this code brought to you by google gemini
 */

public class GDrive {
	private static final String APPLICATION_NAME = "BORG Calendar";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	static private final Logger log = Logger.getLogger("net.sf.borg");
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

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

		String googleFileId = Prefs.getPref(PrefName.GOOGLE_DB_FILE_PATH);
		if (googleFileId == null || googleFileId.isEmpty()) {
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
					.setDataStoreFactory(new FileDataStoreFactory(
							new java.io.File(Prefs.getPref(PrefName.GOOGLE_TOKEN_DIR) + "/drivecred")))
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

		String googleFilePath = Prefs.getPref(PrefName.GOOGLE_DB_FILE_PATH);
		if (googleFilePath == null || googleFilePath.isEmpty()) {
			log.info("GDrive:checkModTimes(): Google File Id is not set");
			return;
		}

		try {
			connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showErrorDialog(false, e.getMessage());
			return;
		}

		DriveFileManager fileManager = new DriveFileManager(service);

		String googleFileId = null;
		try {
			String fileMimeType = "application/octet-stream";

			googleFileId = fileManager.findOrCreateFile(googleFilePath, fileMimeType);

			log.info("Final File ID: " + googleFileId);

		} catch (IOException e) {
			System.err.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
			showErrorDialog(false, "<html>" + e.getMessage() + ":" + e.getClass() + "<br/>Accessing the google file failed, Error: "
					+ e.getMessage() + "<br/>It is recommended that you exit and fix the issue manually</html>");
			return;
		}

		String localDBFile = JdbcDB.getDBFilePath();
		if (localDBFile == null)
			return;

		File fileMeta = service.files().get(googleFileId).setFields("id,name,modifiedTime").execute();

		log.info("Google File Mod: " + fileMeta.getName() + " " + fileMeta.getModifiedTime() + " "
				+ fileMeta.getModifiedTime().getValue());

		java.io.File f = new java.io.File(localDBFile);

		if (f.exists()) {

			long lastModifiedMillis = f.lastModified();
			Date lastModifiedDate = new Date(lastModifiedMillis);
			Date gdate = new Date(fileMeta.getModifiedTime().getValue());

			log.info("Database File Mod: " + localDBFile + " " + lastModifiedMillis + " " + lastModifiedDate);

			if (lastModifiedMillis + 1000 * 60 < fileMeta.getModifiedTime().getValue()) {
				String msg = "<html>Google DB file is newer than local File, sync may be needed<br/>" + "Google file: "
						+ fileMeta.getName() + " " + gdate + "<br/>Local file: " + localDBFile + " " + lastModifiedDate
						+ "<br/>" + "Difference: " + (fileMeta.getModifiedTime().getValue() - lastModifiedMillis) / 1000
						+ " seconds</html>";
				log.info(msg);
				showSyncNeededDialog(googleFileId, localDBFile, msg);
				Prefs.putPref(PrefName.GOOGLE_DB_FORCE_DOWNLOAD, "false");


			}else if( Prefs.getBoolPref(PrefName.GOOGLE_DB_FORCE_DOWNLOAD)) {
				String msg = "<html>You requested to download the google DB file<br/>" + "Google file: "
						+ fileMeta.getName() + " " + gdate + "<br/>Local file: " + localDBFile + " " + lastModifiedDate
						+ "</html>";
				log.info(msg);
				showSyncNeededDialog(googleFileId, localDBFile, msg);
				Prefs.putPref(PrefName.GOOGLE_DB_FORCE_DOWNLOAD, "false");
			}

		} else {
			log.warning(localDBFile + "Not Found");
		}
		
		

	}

	private void downloadFile(String googleFileId, String localPath) {

		FileDownloader downloader = new FileDownloader();

		try {

			// 2. Specify the local file path where you want to save the content
			Path downloadPath = Paths.get(localPath);

			// 3. Execute the download
			downloader.downloadFile(service, googleFileId, downloadPath);

		} catch (IOException e) {
			System.err.println("Download failed: " + e.getMessage());
			showErrorDialog(false, "<html>The download failed. Error: " + e.getMessage()
					+ "<br/>It is recommended that you exit and fix the issue manually</html>");
		}
	}

	private void showSyncNeededDialog(String googleFileId, String localPath, String msg) {

		/*
		 * gemini wrote 99% of the following, including the comments
		 */

		// Create the main frame (or just use null for parent if not needed)
		// We'll use a hidden JFrame as the owner to properly center the dialog.
		JFrame ownerFrame = new JFrame();
		ownerFrame.setSize(0, 0); // Keep it invisible
		ownerFrame.setVisible(false);

		final JDialog dialog = new JDialog(ownerFrame, "DB Startup", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// 2. Create the main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(20, 20)); // Padding between components
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Overall padding

		// --- Text Area ---
		JLabel textLabel = new JLabel(msg);
		// textLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

		// Use a wrapper panel for the text to mimic a JOptionPane-style layout
		// (optional)
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textPanel.add(textLabel);
		contentPanel.add(textPanel, BorderLayout.CENTER);

		// --- Button Panel ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

		// 3. Create "Exit the program" button
		Icon exitIcon = UIManager.getIcon("OptionPane.informationIcon"); // Using a standard information icon
		if (exitIcon == null) {
			log.info("Could not find UIManager information icon, falling back to a dummy icon.");
			exitIcon = new ImageIcon(); // Fallback
		}
		JButton exitButton = new JButton("Exit the program", exitIcon);
		exitButton.addActionListener(e -> {

			log.info("Exiting the program.");
			System.exit(0); // Terminate the application
		});

		// For standard icons, we can try using a built-in one like error/warning icon:
		Icon stopIcon = UIManager.getIcon("OptionPane.errorIcon");
		if (stopIcon == null) {
			// Fallback if the UIManager doesn't provide a suitable icon (less common)
			log.info("Could not find UIManager icon, falling back to a dummy icon.");
			stopIcon = new ImageIcon(); // Empty icon
		}

		JButton proceedButton1 = new JButton("Proceed Anyway", stopIcon);
		// Optional: Set the button to be the default action
		dialog.getRootPane().setDefaultButton(proceedButton1);

		proceedButton1.addActionListener(e -> {
			// Get the checkbox state
			log.info("User chose 'Proceed Anyway'.");

			// In a real app, you would save the 'shouldHide' state here.
			dialog.dispose(); // Close the dialog
		});

		JButton proceedButton2 = new JButton("Download DB and Proceed", exitIcon);

		proceedButton2.addActionListener(e -> {
			// Get the checkbox state
			log.info("User chose 'Download and Proceed'.");

			// In a real app, you would save the 'shouldHide' state here.
			dialog.dispose(); // Close the dialog
			downloadFile(googleFileId, localPath);
		});

		// 5. Add buttons to the button panel
		buttonPanel.add(exitButton);
		buttonPanel.add(proceedButton1);
		buttonPanel.add(proceedButton2);

		// Add all main components to the dialog
		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		// 6. Configure and show the dialog
		dialog.pack(); // Size the dialog based on its contents
		dialog.setLocationRelativeTo(ownerFrame); // Center the dialog on the screen (relative to the invisible owner)
		dialog.setVisible(true);

	}

	public void showUploadDialog() {

		String googleFilePath = Prefs.getPref(PrefName.GOOGLE_DB_FILE_PATH);
		if (googleFilePath == null || googleFilePath.isEmpty()) {
			log.info("GDrive:checkModTimes(): Google File Path is not set");
			return;
		}

		String localDBFile = JdbcDB.getDBFilePath();
		if (localDBFile == null)
			return;

		try {
			connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}

		// Create the main frame (or just use null for parent if not needed)
		// We'll use a hidden JFrame as the owner to properly center the dialog.
		JFrame ownerFrame = new JFrame();
		ownerFrame.setSize(0, 0); // Keep it invisible
		ownerFrame.setVisible(false);

		final JDialog dialog = new JDialog(ownerFrame, "DB Upload", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// 2. Create the main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(20, 20)); // Padding between components
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Overall padding

		// --- Text Area ---
		JLabel textLabel = new JLabel("Should I upload the database to Google Drive?");

		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textPanel.add(textLabel);
		contentPanel.add(textPanel, BorderLayout.CENTER);

		// --- Button Panel ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

		Icon exitIcon = UIManager.getIcon("OptionPane.informationIcon"); // Using a standard information icon
		if (exitIcon == null) {
			log.info("Could not find UIManager information icon, falling back to a dummy icon.");
			exitIcon = new ImageIcon(); // Fallback
		}
		JButton exitButton = new JButton("Yes, Upload", exitIcon);
		exitButton.addActionListener(e -> {
			try {

				JDialog loadingDialog = new JDialog(ownerFrame, "Please Wait", true);
				loadingDialog.add(new JLabel("Uploading... Please wait."), BorderLayout.CENTER);
				loadingDialog.setSize(250, 100);
				loadingDialog.setLocationRelativeTo(ownerFrame);
				loadingDialog.setModal(true);

				// 2. Create the SwingWorker
				SwingWorker<Void, Void> worker = new SwingWorker<>() {
					@Override
					protected Void doInBackground() throws Exception {
						
						GDrive.getReference().uploadFile(googleFilePath, localDBFile);
						return null;
					}

					@Override
					protected void done() {
						try {
							get();
							JOptionPane.showMessageDialog(ownerFrame, "Upload Completed Successfully!");
						} catch (Exception e1) {
							showErrorDialog(true, "<html>The upload failed. Error: " + e1.getMessage()
									+ "<br/>It is recommended that you exit and try again, or fix the issue manually</html>");
						}
						loadingDialog.dispose();


					}
				};

				// 4. Start the worker and show the dialog
				worker.execute();
				loadingDialog.setVisible(true);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				showErrorDialog(true, "<html>The upload failed. Error: " + e1.getMessage()
						+ "<br/>It is recommended that you exit and try again, or fix the issue manually</html>");

			}
			dialog.dispose();

		});

		// For standard icons, we can try using a built-in one like error/warning icon:
		Icon stopIcon = UIManager.getIcon("OptionPane.errorIcon");
		if (stopIcon == null) {
			// Fallback if the UIManager doesn't provide a suitable icon (less common)
			log.info("Could not find UIManager icon, falling back to a dummy icon.");
			stopIcon = new ImageIcon(); // Empty icon
		}

		JButton proceedButton1 = new JButton("Exit without Uploading", stopIcon);
		// Optional: Set the button to be the default action
		dialog.getRootPane().setDefaultButton(proceedButton1);

		proceedButton1.addActionListener(e -> {
			// Get the checkbox state
			log.info("User chose 'exit wihtout uploading'.");

			// In a real app, you would save the 'shouldHide' state here.
			dialog.dispose(); // Close the dialog
		});

		// 5. Add buttons to the button panel
		buttonPanel.add(exitButton);
		buttonPanel.add(proceedButton1);

		// Add all main components to the dialog
		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		// 6. Configure and show the dialog
		dialog.pack(); // Size the dialog based on its contents
		dialog.setLocationRelativeTo(ownerFrame); // Center the dialog on the screen (relative to the invisible owner)
		dialog.setVisible(true);

	}

	private void uploadFile(String googleFilePath, String localPath) throws Exception {
		log.info("Uploading file");
		java.io.File f = new java.io.File(localPath);

		DriveFileManager fileManager = new DriveFileManager(service);

		String googleFileId = null;
		String fileMimeType = "application/octet-stream";

		googleFileId = fileManager.findOrCreateFile(googleFilePath, fileMimeType);

		log.info("Final File ID: " + googleFileId);

		updateFileContent(service, googleFileId, f.toPath(), "application/octet-stream");

	}

	/**
	 * Updates the content of an existing file on Google Drive.
	 *
	 * @param driveService  Authenticated Drive service object.
	 * @param fileId        The ID of the file to update.
	 * @param localFilePath The local path to the new file content.
	 * @param mimeType      The MIME type of the file being uploaded (e.g.,
	 *                      "image/jpeg").
	 * @return The updated File resource with new metadata.
	 * @throws IOException If the API call fails.
	 */
	private static void updateFileContent(Drive driveService, String fileId, Path localFilePath, String mimeType)
			throws IOException {

		// 1. Prepare the new content
		java.io.File fileToUpload = localFilePath.toFile();
		FileContent mediaContent = new FileContent(mimeType, fileToUpload);

		// 2. Prepare the metadata (optional, but good practice)
		// You can set new metadata like a new name or description,
		// but leaving it empty will keep the existing metadata.
		File fileMetadata = new File();
		// Example: Update the file's name (optional)
		// fileMetadata.setName(localFilePath.getFileName().toString());

		try {
			// 3. Execute the update call
			// The .update(fileId, fileMetadata, mediaContent) call uploads
			// the new content and updates the metadata (if provided).
			File updatedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
					.setFields("id, name, mimeType, modifiedTime, size") // Specify fields to return
					.execute();

			System.out.println("File ID: " + updatedFile.getId());
			System.out.println("New File Name: " + updatedFile.getName());
			System.out.println("New File Size: " + updatedFile.getSize());

			// return updatedFile;

		} catch (IOException e) {
			System.err.println("An error occurred during file update: " + e);
			throw e;
		}
	}

	public static void showErrorDialog(boolean exitOnly, String msg) {

		// Create the main frame (or just use null for parent if not needed)
		// We'll use a hidden JFrame as the owner to properly center the dialog.
		JFrame ownerFrame = new JFrame();
		ownerFrame.setSize(0, 0); // Keep it invisible
		ownerFrame.setVisible(false);

		final JDialog dialog = new JDialog(ownerFrame, "Error", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// 2. Create the main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(20, 20)); // Padding between components
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Overall padding

		// --- Text Area ---
		JLabel textLabel = new JLabel(msg);

		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textPanel.add(textLabel);
		contentPanel.add(textPanel, BorderLayout.CENTER);

		// --- Button Panel ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

		Icon exitIcon = UIManager.getIcon("OptionPane.informationIcon"); // Using a standard information icon
		if (exitIcon == null) {
			log.info("Could not find UIManager information icon, falling back to a dummy icon.");
			exitIcon = new ImageIcon(); // Fallback
		}
		JButton exitButton = new JButton("Exit", exitIcon);
		exitButton.addActionListener(e -> {
			dialog.dispose();
			System.exit(0);

		});

		buttonPanel.add(exitButton);

		if (!exitOnly) {

			// For standard icons, we can try using a built-in one like error/warning icon:
			Icon stopIcon = UIManager.getIcon("OptionPane.errorIcon");
			if (stopIcon == null) {
				// Fallback if the UIManager doesn't provide a suitable icon (less common)
				log.info("Could not find UIManager icon, falling back to a dummy icon.");
				stopIcon = new ImageIcon(); // Empty icon
			}

			JButton proceedButton1 = new JButton("Proceed anyway", stopIcon);
			// Optional: Set the button to be the default action
			dialog.getRootPane().setDefaultButton(proceedButton1);

			proceedButton1.addActionListener(e -> {
				// Get the checkbox state
				log.info("user chose to proceed");

				// In a real app, you would save the 'shouldHide' state here.
				dialog.dispose(); // Close the dialog
			});

			// 5. Add buttons to the button panel
			buttonPanel.add(proceedButton1);
		}

		// Add all main components to the dialog
		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		// 6. Configure and show the dialog
		dialog.pack(); // Size the dialog based on its contents
		dialog.setLocationRelativeTo(ownerFrame); // Center the dialog on the screen (relative to the invisible owner)
		dialog.setVisible(true);

	}
	
	/*
	 * upload the db to google drive while the db connection is open
	 * implemented for sqlite only using the VACUUM command
	 */
	public void vacuumAndUpload() {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if( !"sqlite".equals(dbtype)) {
			Errmsg.getErrorHandler().notice("Only Sqlite databases can be uploaded while BORG is running");
			return;
		}
		
		String googleFilePath = Prefs.getPref(PrefName.GOOGLE_DB_FILE_PATH);
		if (googleFilePath == null || googleFilePath.isEmpty()) {
			Errmsg.getErrorHandler().notice("Google File Path is not set");
			return;
		}
		
		String dbfolder = Prefs.getPref(PrefName.SQLITEDIR);
		String newDBFilename = "borg_backup.db";
		String fullpath = dbfolder + "/" + newDBFilename;
		
		try {
			Files.delete(Paths.get(fullpath));
		} catch (IOException e) {
			
		}

		// vacuum the db
		try {
			

			DBHelper.getController().beginTransaction();
			DBHelper.getController().execQuery("VACUUM INTO '" + fullpath + "'");
			DBHelper.getController().commitTransaction();

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
			return;
		}
		
		JFrame ownerFrame = new JFrame();
		ownerFrame.setSize(0, 0); // Keep it invisible
		ownerFrame.setVisible(false);
		JDialog loadingDialog = new JDialog(ownerFrame, "Please Wait", true);
		loadingDialog.add(new JLabel("Uploading... Please wait."), BorderLayout.CENTER);
		loadingDialog.setSize(250, 100);
		loadingDialog.setLocationRelativeTo(ownerFrame);
		loadingDialog.setModal(true);
		
		try {
			

			// 2. Create the SwingWorker
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					
					GDrive.getReference().uploadFile(googleFilePath, fullpath);
					return null;
				}

				@Override
				protected void done() {
					try {
						get();
						JOptionPane.showMessageDialog(ownerFrame, "Upload Completed Successfully!");
					} catch (Exception e1) {
						showErrorDialog(true, "<html>The upload failed. Error: " + e1.getMessage()
								+ "<br/>It is recommended that you exit and try again, or fix the issue manually</html>");
					}
					loadingDialog.dispose();


				}
			};

			// 4. Start the worker and show the dialog
			worker.execute();
			loadingDialog.setVisible(true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			showErrorDialog(true, "<html>The upload failed. Error: " + e1.getMessage()
					+ "<br/>It is recommended that you exit and try again, or fix the issue manually</html>");

		}
		loadingDialog.dispose();
		ownerFrame.dispose();
		
	}

}