package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.articleDTO.FileDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.mapper.UpLoadMapper;
import com.cc.qylgjavaservice.service.UploadService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Service
public class UploadServiceImpl extends ServiceImpl<UpLoadMapper, FileDTO> implements UploadService {
    @Resource
    private S3Client s3Client;

    @Value("${cloudflare.r2.cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.cloudflare.r2.public-url}")
    private String publicUrl;

    @Override
    public Result<FileDTO> upLoadAvatar(MultipartFile file, String type) {
        if (file.isEmpty()){
            throw new IllegalArgumentException("文件不能为空");
        }

        try{
            String ext = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));

            String fileName = type+"/" + UUID.randomUUID() + ext;

            InputStream inputStream = file.getInputStream();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            inputStream,
                            file.getSize()
                    )
            );

        String url = publicUrl + "/" + fileName;
        FileDTO fileDTO=new FileDTO(url,fileName);
        return Result.success(fileDTO);

        }
        catch (Exception e) {
            throw new RuntimeException("上传失败");
        }
    }
}
