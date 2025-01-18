package com.pugwoo.for_test.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DownloadController {

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(String content) {
        byte[] contentBytes = content.getBytes();

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "hello.txt");
        headers.setContentLength(contentBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(contentBytes);
    }

}
