package com.jqp.admin.common.service;

import com.jqp.admin.common.data.SysFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hyz
 * @date 2021/6/17 11:18
 */
public interface SysFileService {
    SysFile upload(MultipartFile file);
    void download(Long id, HttpServletRequest request, HttpServletResponse response);
    void deleteFile(SysFile sysFile);
}
