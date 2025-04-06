package com.tisd.c4change.Service;

import com.tisd.c4change.CustomException.FileStorageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileStorageService {

    public byte[] storeFile(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }
}
