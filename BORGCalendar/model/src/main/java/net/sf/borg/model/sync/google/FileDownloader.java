package net.sf.borg.model.sync.google;
import com.google.api.services.drive.Drive;
import com.google.api.client.http.HttpResponseException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/*
 * written by google gemini
 */

public class FileDownloader {
	
	

    /**
     * Downloads a Google Drive file given its ID.
     *
     * @param driveService The authenticated Drive service object.
     * @param fileId The ID of the file to download.
     * @param localFilePath The path to save the downloaded file locally.
     * @throws IOException If an error occurs during API call or file writing.
     */
    public void downloadFile(Drive driveService, String fileId, Path localFilePath) throws IOException {
        
        System.out.println("Attempting to download file ID: " + fileId + " to path: " + localFilePath);

        // Handle Google Docs, Sheets, Slides, etc. (Google native formats)
        // These files cannot be downloaded directly via 'files().get()'. 
        // They must be exported using 'files().export()'.
        // For simplicity, this example assumes a non-native file (e.g., PDF, image, text).
        // If you need to handle Google native files, you'll need a separate export function.
        // The Drive API documentation can tell you the appropriate MIME type for the export.
        
        try (OutputStream outputStream = new FileOutputStream(localFilePath.toFile())) {
            
            // 1. Execute the download request using files().get()
            driveService.files().get(fileId)
                // 2. Download the file content and write it directly to the output stream
                .executeMediaAndDownloadTo(outputStream); 

            System.out.println("Successfully downloaded file to: " + localFilePath);

        } catch (HttpResponseException e) {
            System.err.println("Error downloading file. Status Code: " + e.getStatusCode());
            System.err.println("Reason: " + e.getStatusMessage());
            throw e; // Re-throw the exception for proper handling
        }
    }
}