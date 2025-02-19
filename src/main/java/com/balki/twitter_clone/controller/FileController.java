package com.balki.twitter_clone.controller;

import com.balki.twitter_clone.dto.FileAttachmentDTO;
import com.balki.twitter_clone.model.FileAttachment;
import com.balki.twitter_clone.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/1.0/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileAttachmentDTO> saveTwitterAttachment(MultipartFile file){
        return ResponseEntity.ok(fileService.saveTwitterAttachment(file));
    }
}
