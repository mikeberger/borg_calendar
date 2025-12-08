package net.sf.borg.model.sync.google;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/*
 * written by google gemini
 */

public class DriveFileManager {

    // The Google Drive service object, assumed to be properly initialized and authenticated.
    private final Drive driveService;

    public DriveFileManager(Drive driveService) {
        this.driveService = driveService;
    }
    
    // --- Core Public Method ---

    /**
     * Gets the file ID of a file by its full Drive path (e.g., "FolderA/SubFolderB/myFile.txt").
     * If the file or any folder in the path does not exist, it is created.
     *
     * @param fullPath The full path of the file, starting from My Drive (root).
     * @param mimeType The MIME type for the file if it needs to be created (e.g., "text/plain").
     * @return The ID of the existing or newly created file.
     * @throws IOException if any API call fails.
     */
    public String findOrCreateFile(String fullPath, String mimeType) throws IOException {
        if (fullPath == null || fullPath.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty.");
        }

        // 1. Separate file name from the path to its parent folder
        String[] pathParts = fullPath.split("/");
        String fileName = pathParts[pathParts.length - 1];
        
        // 2. Find or create all parent folders in the path
        String currentParentId = "root"; // Start from My Drive
        for (int i = 0; i < pathParts.length - 1; i++) {
            currentParentId = findOrCreateFolder(currentParentId, pathParts[i]);
        }
        
        // 3. Search for the file in the final parent folder
        File targetFile = searchFileInFolder(currentParentId, fileName);

        // 4. Create the file if it doesn't exist
        if (targetFile == null) {
            System.out.println("File not found: " + fullPath + ". Creating new file.");
            targetFile = createFileInFolder(currentParentId, fileName, mimeType);
        } else {
            System.out.println("File found: " + fullPath + " (ID: " + targetFile.getId() + ")");
        }

        return targetFile.getId();
    }
    
    // --- Helper Methods ---
    
    /**
     * Finds a file by name within a specific parent folder ID.
     *
     * @param parentId The ID of the folder to search in. Use "root" for My Drive.
     * @param fileName The name of the file to search for.
     * @return The File object if found, or null otherwise.
     * @throws IOException if the API call fails.
     */
    private File searchFileInFolder(String parentId, String fileName) throws IOException {
        String query = String.format(
            "name = '%s' and '%s' in parents and trashed = false",
            fileName.replace("'", "\\'"), // Escape single quotes in the name
            parentId
        );
        
        FileList result = driveService.files().list()
            .setQ(query)
            .setFields("files(id, name)") // Only request the ID and name
            .execute();
            
        List<File> files = result.getFiles();
        
        // Return the first match (Drive allows multiple files with the same name, 
        // but this logic assumes you want the first one found).
        return files.isEmpty() ? null : files.get(0);
    }
    
    /**
     * Creates a new file in the specified parent folder.
     *
     * @param parentId The ID of the folder where the file should be created.
     * @param fileName The name of the new file.
     * @param mimeType The MIME type of the new file.
     * @return The newly created File object.
     * @throws IOException if the API call fails.
     */
    private File createFileInFolder(String parentId, String fileName, String mimeType) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setMimeType(mimeType);
        fileMetadata.setParents(Collections.singletonList(parentId));
        
        // Note: This creates an empty file. For a file with content, you'd use 
        // .setMediaContent() or .upload() depending on the method.
        File newFile = driveService.files().create(fileMetadata)
            .setFields("id, name")
            .execute();
            
        return newFile;
    }
    
    /**
     * Finds a folder by name within a parent ID, or creates it if it does not exist.
     *
     * @param parentId The ID of the parent folder. Use "root" for My Drive.
     * @param folderName The name of the folder to find/create.
     * @return The ID of the existing or newly created folder.
     * @throws IOException if the API call fails.
     */
    private String findOrCreateFolder(String parentId, String folderName) throws IOException {
        // 1. Search for the folder
        String folderMimeType = "application/vnd.google-apps.folder";
        String query = String.format(
            "name = '%s' and mimeType = '%s' and '%s' in parents and trashed = false",
            folderName.replace("'", "\\'"),
            folderMimeType,
            parentId
        );
        
        FileList result = driveService.files().list()
            .setQ(query)
            .setFields("files(id)") // Only request the ID
            .execute();

        List<File> folders = result.getFiles();

        if (!folders.isEmpty()) {
            // Folder found
            System.out.println("Folder found: " + folderName + " (ID: " + folders.get(0).getId() + ")");
            return folders.get(0).getId();
        }

        // 2. Folder not found, create it
        System.out.println("Folder not found: " + folderName + ". Creating new folder.");
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(folderMimeType);
        fileMetadata.setParents(Collections.singletonList(parentId));

        File newFolder = driveService.files().create(fileMetadata)
            .setFields("id")
            .execute();

        return newFolder.getId();
    }
}