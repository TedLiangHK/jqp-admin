package com.jqp.admin.common.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.jqp.admin.common.config.FileConfig;
import com.jqp.admin.common.data.SysFile;
import com.jqp.admin.common.service.SysFileService;
import com.jqp.admin.db.service.JdbcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

/**
 * @author hyz
 * @date 2021/6/17 13:55
 */
@Service("sysFileService")
@Slf4j
public class SysFileServiceImpl implements SysFileService {

    @Resource
    private JdbcService jdbcService;
    @Resource
    private FileConfig fileConfig;
    private static final String TYPE_LOCAL = "local";
    private static final String TYPE_OSS = "oss";

    @Override
    @Transactional
    public SysFile upload(MultipartFile file) {
        SysFile sysFile = new SysFile();
        sysFile.setSize(file.getSize());
        sysFile.setFileName(file.getOriginalFilename());
        sysFile.setUploadTime(new Date());
        sysFile.setContentType(file.getContentType());
        String suffix = "";
        if(sysFile.getFileName().contains(".")){
            suffix = sysFile.getFileName().substring(sysFile.getFileName().lastIndexOf(".")+1).toLowerCase();
        }
        sysFile.setSuffix(suffix);

        String f1 = DateUtil.format(new Date(),"yyyy/MM/dd");
        String name = UUID.randomUUID()
                    .toString()
                    .replaceAll("-","")
                    .toLowerCase()
                    +(StringUtils.isBlank(suffix) ? "" : ("."+suffix));
        sysFile.setPath("/"+f1+"/"+name);
        if(TYPE_LOCAL.equals(fileConfig.getType())){
            File folder = new File(fileConfig.getBase()+"/"+f1);
            folder.mkdirs();
            File dest = new File(folder,name);
            try {
                file.transferTo(dest);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败",e);
            }
            jdbcService.saveOrUpdate(sysFile);
            return sysFile;
        }else if(TYPE_OSS.equals(fileConfig.getType())){

            OSS oss = new OSSClientBuilder().build(fileConfig.getOssEndpoint(),fileConfig.getOssKey(),fileConfig.getOssSecret());
            try {
                oss.putObject(fileConfig.getOssBucketName(),fileConfig.getBase()+sysFile.getPath(),file.getInputStream());
                jdbcService.saveOrUpdate(sysFile);
                return sysFile;
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败",e);
            }
            oss.shutdown();
        }
        return null;
    }

    @Override
    public void download(Long id, HttpServletRequest request, HttpServletResponse response) {
        SysFile sysFile = jdbcService.getById(SysFile.class, id);
        boolean download = "true".equals(request.getParameter("download"));
        try {
            response.setContentLength(sysFile.getSize().intValue());
            response.setContentType(sysFile.getContentType());
            if(download){
                String downloadFileName = URLEncoder.encode(sysFile.getFileName(),"UTF-8");
                response.addHeader("Content-Disposition","attachment;fileName="+downloadFileName);
            }
            ServletOutputStream out = response.getOutputStream();
            InputStream in = null;
            OSS oss = null;
            if(TYPE_LOCAL.equals(fileConfig.getType())){
                File file = new File(fileConfig.getBase()+sysFile.getPath());
                in = new FileInputStream(file);
            }else if(TYPE_OSS.equals(fileConfig.getType())){
                oss = new OSSClientBuilder().build(fileConfig.getOssEndpoint(),fileConfig.getOssKey(),fileConfig.getOssSecret());
                OSSObject ossObject = oss.getObject(fileConfig.getOssBucketName(), fileConfig.getBase() + sysFile.getPath());
                in = ossObject.getObjectContent();
            }
            if(oss != null){
                oss.shutdown();
            }
            IoUtil.copy(in,out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(SysFile sysFile) {
        try{
            if(TYPE_LOCAL.equals(fileConfig.getType())){
                File file = new File(fileConfig.getBase()+sysFile.getPath());
                if(file.exists()){
                    file.delete();
                }
            }else if(TYPE_OSS.equals(fileConfig.getType())){
                OSS oss = new OSSClientBuilder().build(fileConfig.getOssEndpoint(),fileConfig.getOssKey(),fileConfig.getOssSecret());
                oss.deleteObject(fileConfig.getOssBucketName(),fileConfig.getBase()+sysFile.getPath());
                oss.shutdown();
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("删除文件失败:",e);
        }
        jdbcService.delete(sysFile);
    }
}