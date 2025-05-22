package com.example.jammoney.S3;

import com.example.jammoney.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

    private final S3Uploader s3Uploader;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("image") MultipartFile image) {
        String url;
        try {
            url = s3Uploader.upload(image);
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", url));
    }
}