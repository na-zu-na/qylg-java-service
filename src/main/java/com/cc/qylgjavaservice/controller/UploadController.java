package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.articleDTO.FileDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.service.UploadService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Resource
    private UploadService uploadService;

    @PostMapping("/avatar")
    public Result<FileDTO> upLoadAvatar(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "type", defaultValue = "avatar") String type){
        return uploadService.upLoadAvatar(file,type);
    }
}
