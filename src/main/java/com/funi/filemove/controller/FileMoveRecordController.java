package com.funi.filemove.controller;

import com.funi.filemove.Constants;
import com.funi.filemove.po.ResultVO;
import com.funi.filemove.service.FastFileService;
import com.funi.filemove.service.FileMoveRecordService;
import com.funi.filemove.service.FileMoveService;
import com.funi.filemove.timer.FileMoveTask;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName FileListController
 * @Description 文件迁移 任务 管理
 * @Author Feng.Yang
 * @Date 2020/6/28 16:08
 * @Version 1.0
 */
@RestController
@RequestMapping("/fileMoveRecord")
public class FileMoveRecordController {
    @Resource
    private FileMoveRecordService fileMoveRecordService;
    @Resource
    private FileMoveService fileMoveService;

    @RequestMapping("fileMoveRecordList")
    public Map<String,Object> fileMoveRecordList(@RequestParam Map<String,Object> queryParams) {
        return fileMoveRecordService.fileMoveRecordList(queryParams);
    }

    @RequestMapping("remigrate")
    public ResultVO remigrate(@RequestParam Map<String,Object> queryParams) {
        ResultVO resultVO =new ResultVO(200,"手动迁移成功");
        if (Constants.MG_MAP_FIGURE.equals(queryParams.get("tableName"))){
            fileMoveRecordService.remigrateMgMapFigure(queryParams);
        }else if (Constants.MG_DOOR_IMG.equals(queryParams.get("tableName"))){
            fileMoveRecordService.remigrateMgDoorImg(queryParams);
        }else if(Constants.IMG_IMAGES.equals(queryParams.get("tableName"))){
            fileMoveRecordService.remigrateImgimages(queryParams);
        }else {
            resultVO =new ResultVO(-1,"手动迁移失败未找到对应的表名");
        }

        return resultVO;
    }

    @RequestMapping("importTaskTable")
    public ResultVO importTaskTable(@RequestParam Map<String,Object> queryParams) {
        fileMoveRecordService.importTaskTable(queryParams);
        ResultVO resultVO =new ResultVO(200,"导入成功");
        return resultVO;
    }

    @RequestMapping("cfFileDescClear")
    public ResultVO cfFileDescClear() {
        fileMoveRecordService.cfFileDescClear();
        ResultVO resultVO =new ResultVO(200,"清理成功");
        return resultVO;
    }

    @RequestMapping("selectFileMoveTaskStatus")
    public ResultVO selectFileMoveTaskStatus() {
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("taskStatus", FileMoveTask.startFileMoveTask);
        ResultVO resultVO =new ResultVO(200,"查询成功");
        resultVO.setData(resultMap);
        return resultVO;
    }
    @RequestMapping("updateFileMoveTaskStatus")
    public ResultVO updateFileMoveTaskStatus(@RequestParam Map<String,Object> params) {
        String taskStatus=(String) params.get("taskStatus");
        if (taskStatus!=null){
            FileMoveTask.startFileMoveTask=taskStatus;
        }
        ResultVO resultVO =new ResultVO(200," 修改定时任务状态成功");
        return resultVO;
    }


    @RequestMapping("stopMove")
    public ResultVO stopMove() {
        fileMoveService.stopMove();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis())+"---迁移结束");
        ResultVO resultVO =new ResultVO(200,"终止迁移进程成功");
        return resultVO;
    }

}
