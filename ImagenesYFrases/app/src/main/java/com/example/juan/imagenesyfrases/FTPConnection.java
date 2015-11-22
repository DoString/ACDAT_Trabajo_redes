package com.example.juan.imagenesyfrases;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by usuario on 10/11/15.
 */
public class FTPConnection {
    private FTPClient myFTPClient = null;

    public boolean ftpConnect(String host, int port, String username, String password) {
        boolean status = false;
        try {
            myFTPClient = new FTPClient();
            myFTPClient.setConnectTimeout(5000);
            myFTPClient.connect(host, port);
            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(myFTPClient.getReplyCode())) {
                // login using username & password
                status = myFTPClient.login(username, password);
                myFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                myFTPClient.enterLocalPassiveMode();
            }
        } catch (Exception e) {
            Log.d("ERROR", "Error: could not connect/login to host " + host);
        }
        return status;
    }

    public boolean ftpUpload(File srcFile, String desFile, String desDirectory) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFile);
            // change working directory to the destination directory
            if (myFTPClient.changeWorkingDirectory(desDirectory)) {
                status = myFTPClient.storeFile(desFile, srcFileStream);
            }
            srcFileStream.close();
        } catch (Exception e) {
            Log.d("ERROR", "upload failed: " + e.getMessage());
        }
        return status;
    }

    public boolean ftpDownload(String srcFile, String srcDirectory, File desFile) {
        //file server, directorio server, fichero destino.
        //FileOutputStream
        //RetrieveFile

        boolean status = false;
        try {
            FileOutputStream desFileStream = new FileOutputStream(desFile);
            // change working directory to the destination directory
            if (myFTPClient.changeWorkingDirectory(srcDirectory)) {
                status = myFTPClient.retrieveFile(srcFile, desFileStream);
            }
            desFileStream.close();
        } catch (Exception e) {
            Log.d("ERROR", "upload failed: " + e.getMessage());
        }
        return status;
    }

    public boolean ftpDisconnect() {
        boolean status = false;
        try {
            status = myFTPClient.logout();
            myFTPClient.disconnect();
        } catch (Exception e) {
            Log.d("ERROR", "Error occurred while disconnecting from ftp server.");
        }
        return status;
    }
}
