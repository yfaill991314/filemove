package com.funi.filemove.controller;

import com.funi.filemove.po.CfFileDescPo;
import com.funi.filemove.service.FastFileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @ClassName FileListController
 * @Description 文件管理系统 管理页面 列表 后端接口
 * @Author Feng.Yang
 * @Date 2020/6/28 16:08
 * @Version 1.0
 */
@RestController
@RequestMapping("/fastFile")
public class FastFileController {
    @Resource
    private FastFileService fastFileService;

    @RequestMapping("findFileList")
    public Map<String,Object> findFileList(@RequestParam Map<String,Object> queryParams) {
       return fastFileService.findFileList(queryParams);
    }


    @ResponseBody
    @RequestMapping("/download")
    public void  download(String fileUuid, HttpServletResponse response){
        String fileName=null;
        OutputStream os = null;
        CfFileDescPo cfFileDescPo = fastFileService.findFileInfoByUuid(fileUuid);
        try {
            fileName = URLEncoder.encode(cfFileDescPo.getFileName(), "UTF-8");
            fileName=fileName.replaceAll("\\+","%20");
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + fileName);
            os = response.getOutputStream();
            fastFileService.findDownLoad(fileUuid,os);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (os != null){
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
