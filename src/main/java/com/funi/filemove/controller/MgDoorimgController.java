package com.funi.filemove.controller;

import com.funi.filemove.po.MgDoorImgPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.service.MgDoorimgService;
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
 * @ClassName MgDoorimgController
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/2/22 15:15
 * @Version 1.0
 */
@RestController
@RequestMapping("/MgDoorimg")
public class MgDoorimgController {
    @Resource
    private MgDoorimgService mgDoorimgService;

    @RequestMapping("MgDoorimgList")
    public Map<String,Object> MgDoorimgList(@RequestParam Map<String,Object> queryParams) {
        return mgDoorimgService.MgDoorimgList(queryParams);
    }


    @ResponseBody
    @RequestMapping("/download")
    public void  download(@RequestParam("id")String id,@RequestParam("dataSource")String dataSource, HttpServletResponse response) throws Exception {
        String fileName=null;
        OutputStream os = null;

        Map<String,Object> queryParams=new HashMap<>();
        queryParams.put("id",id);
        queryParams.put("dataSource",dataSource);
        MgDoorImgPo mgDoorImgPo = mgDoorimgService.findMgDoorimgByQueryMap(queryParams);
        if (mgDoorImgPo==null){
            throw new Exception("未找到该业务件");
        }
        try {
            fileName = URLEncoder.encode(mgDoorImgPo.getImgfilename(), "UTF-8");
            fileName=fileName.replaceAll("\\+","%20");
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName="
                    + fileName);
            os = response.getOutputStream();
            os.write(mgDoorImgPo.getImage());
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
