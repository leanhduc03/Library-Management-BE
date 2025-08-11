package com.spring.LibraryManagement.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/image")
    @PreAuthorize("hasAuthority('PERMISSION_BOOK_CREATE') or hasAuthority('PERMISSION_BOOK_UPDATE')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Chuyển đổi MultipartFile sang File tạm thời
            File uploadedFile = convertMultiPartToFile(file);
            
            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.emptyMap());
            
            // Xóa file tạm
            uploadedFile.delete();
            
            // Lấy URL từ kết quả upload
            String imageUrl = (String) uploadResult.get("secure_url");
            
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Không thể tải lên ảnh: " + e.getMessage());
        }
    }
    
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename != null ? originalFilename : UUID.randomUUID().toString();
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}