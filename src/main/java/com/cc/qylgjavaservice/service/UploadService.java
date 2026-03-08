package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.articleDTO.FileDTO;
import com.cc.qylgjavaservice.dto.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    Result<FileDTO> upLoadAvatar(MultipartFile file, String type);
}
