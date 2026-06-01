package com.gabusdev.dev.quizbank.core.drive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;

/**
 * Helper para interactuar con la API de Google Drive.
 * Permite gestionar carpetas y subir archivos.
 */
public class DriveServiceHelper {
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        this.mDriveService = driveService;
    }

    /**
     * Busca o crea la carpeta principal "NotebookLM_Math_Sources" en Drive.
     * @return El ID de la carpeta.
     * @throws IOException Si ocurre un error en la comunicación con Drive.
     */
    public String getOrCreateRootFolder() throws IOException {
        String folderName = "NotebookLM_Math_Sources";
        FileList result = mDriveService.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "' and trashed = false")
                .setSpaces("drive")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        File folderMetadata = new File()
                .setName(folderName)
                .setMimeType("application/vnd.google-apps.folder");

        File folder = mDriveService.files().create(folderMetadata).execute();
        return folder.getId();
    }

    /**
     * Sube un archivo local a una carpeta específica en Drive.
     * @param folderId ID de la carpeta destino.
     * @param name Nombre que tendrá el archivo en Drive.
     * @param mimeType Tipo MIME del archivo.
     * @param filePath Objeto File que apunta al archivo local.
     * @return El ID del archivo creado en Drive.
     * @throws IOException Si ocurre un error durante la subida.
     */
    public String uploadFileToFolder(String folderId, String name, String mimeType, java.io.File filePath) throws IOException {
        File metadata = new File()
                .setParents(Collections.singletonList(folderId))
                .setMimeType(mimeType)
                .setName(name);

        FileContent content = new FileContent(mimeType, filePath);

        File googleFile = mDriveService.files().create(metadata, content).execute();
        if (googleFile == null) {
            throw new IOException("Error al subir el archivo a Google Drive.");
        }

        return googleFile.getId();
    }
}
