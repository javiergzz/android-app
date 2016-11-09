package com.grahm.livepost.util;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.InputStream;

/**
 * Created by Vyz on 2016-10-12.
 */

public class AzureUtils {
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "AccountName=" + GV.AZURE_NAME + ";" +
                    "AccountKey=" + GV.AZURE_KEY;

    public static String uploadBlob(final String filename, final InputStream source, final String containerName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            // Define the path to a local file.

            // Create or overwrite the "myimage.jpg" blob with contents from a local file.
            CloudBlockBlob blob = container.getBlockBlobReference(filename);

            //File source = new File(filePath);
            blob.upload(source, source.available());
            return blob.getUri().toString();
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteBlob(final String filename, final String containerName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Retrieve reference to a previously created container.
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            // Retrieve reference to a blob named "myimage.jpg".
            CloudBlockBlob blob = container.getBlockBlobReference(filename);

            // Delete the blob.
            blob.deleteIfExists();
        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
        }
    }
}
