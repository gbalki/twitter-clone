package com.balki.twitter_clone.service;

import com.balki.twitter_clone.configuration.FileConfiguration;
import com.balki.twitter_clone.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class FileService {

    private final FileConfiguration fileConfiguration;

    Tika tika = new Tika();

    public String writeBase64EncodedStringToFile(String image) throws IOException {
        String fileName = generateRandomName();
        File target = new File(fileConfiguration.getProfileStoragePath() + "/" + fileName);
        OutputStream outputStream = new FileOutputStream(target);
        byte[] base64encoded = Base64.getDecoder().decode(image.split(",")[1]);

        outputStream.write(base64encoded);
        outputStream.close();
        return fileName;
    }

    public String generateRandomName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public void deleteProfileImage(String oldImageName) {
        if (oldImageName == null) {
            return;
        }
        deleteFile(Paths.get(fileConfiguration.getProfileStoragePath(), oldImageName));
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("File path doesn't exist");
        }
    }

    public String detectType(String base64) {
        byte[] base64encoded = Base64.getDecoder().decode(base64.split(",")[1]);
        return detectType(base64encoded);
    }

    public String detectType(byte[] arr) {
        return tika.detect(arr);
    }

    public void deleteAllStoredFilesForUser(User user) {
        deleteProfileImage(user.getImage());
      /*  List<FileAttachment> filesToBeRemoved = fileAttachmentRepository.findByTwitterUser(user);
        for (FileAttachment file : filesToBeRemoved) {
            deleteAttachmentFile(file.getName());
        }*/
    }
}
