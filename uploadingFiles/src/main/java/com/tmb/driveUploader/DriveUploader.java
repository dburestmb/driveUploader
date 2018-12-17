package com.tmb.driveUploader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveUploader {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveUploader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
    	String nombre = "file";
    	System.out.println("Parametros: " + args.length);
    	if (args.length>0) {
    		nombre = args[0];       		
    	}
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        // Upload
        uploadFile(service,nombre);
        
        //printFilesName(service);
    }
    
    private static void uploadFile(Drive service, String nombre) throws IOException {    
    	
    	String fileId = getExistingFileId(service, nombre);
    	
    	File fileMetadata = new File();
        fileMetadata.setName(nombre);
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        java.io.File filePath = new java.io.File(nombre+".csv");
        FileContent mediaContent = new FileContent("text/csv", filePath);
        
        File file = null;
        if (fileId == null) {
	        file = service.files().create(fileMetadata, mediaContent)
	            .setFields("id")
	            .execute();
        } else {
        	file = service.files().update(fileId, fileMetadata, mediaContent).execute();
        }
        System.out.println("File ID: " + file.getId());
    }
    
    private static String getExistingFileId(Drive service, String nombre) throws IOException {    	
    	
    	FileList result = service.files().list()
                .setFields("files(id, name)")
                .execute();
    	
    	List<File> files = result.getFiles();
    	if (files != null && !files.isEmpty()) {
    		for (File file : files) {
    			if (nombre.equals(file.getName())) {
    				return file.getId();
    			}
            }
    	}    	
    	return null;
    }
    
    /**
     * Print the names and IDs for up to 10 files. Set DriveScopes.DRIVE_METADATA_READONLY
     */
    public static void printFilesName(Drive service) throws IOException {
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

}
