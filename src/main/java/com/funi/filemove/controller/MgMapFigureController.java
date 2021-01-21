package com.funi.filemove.controller;

import com.funi.filemove.po.CfFileDescPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.service.FastFileService;
import com.funi.filemove.service.MgMapFigureService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName FileListController
 * @Description 文件管理系统 管理页面 列表 后端接口
 * @Author Feng.Yang
 * @Date 2020/6/28 16:08
 * @Version 1.0
 */
@RestController
@RequestMapping("/MgMapFigure")
public class MgMapFigureController {
    @Resource
    private MgMapFigureService mgMapFigureService;

    @RequestMapping("MgMapFigureList")
    public Map<String,Object> MgMapFigureList(@RequestParam Map<String,Object> queryParams) {
       return mgMapFigureService.MgMapFigureList(queryParams);
    }


    @ResponseBody
    @RequestMapping("/download")
    public void  download(@RequestParam("id")String id,@RequestParam("dataSource")String dataSource, HttpServletResponse response) throws Exception {
        String fileName=null;
        OutputStream os = null;

        Map<String,Object> queryParams=new HashMap<>();
        queryParams.put("id",id);
        queryParams.put("dataSource",dataSource);
        MgMapFigurePo mgMapFigurePo = mgMapFigureService.findMgMapFigureByQueryMap(queryParams);
        if (mgMapFigurePo==null){
            throw new Exception("未找到该业务件");
        }
        try {
            fileName = URLEncoder.encode(mgMapFigurePo.getImagename(), "UTF-8");
            fileName=fileName.replaceAll("\\+","%20");
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + fileName);
            os = response.getOutputStream();
            os.write(mgMapFigurePo.getImage());
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
