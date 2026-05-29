package vn.hcmute.edu.materialsservice.services;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadAudio(MultipartFile file) throws IOException {
        // Cloudinary nhận raw bytes, không cần lưu file tạm xuống disk
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of(
                        "resource_type", "video",   // Cloudinary dùng "video" cho cả audio lẫn video
                        "folder",        "treedu/audio",
                        "public_id",     UUID.randomUUID().toString(),
                        "overwrite",     false
                )
        );

        String url = (String) result.get("secure_url");
        log.info("Upload Cloudinary thành công: {}", url);
        return url;
    }
}
