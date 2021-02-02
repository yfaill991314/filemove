package com.funi.filemove.controller;

import com.funi.filemove.po.ResultVO;
import com.funi.filemove.service.FastFileService;
import com.funi.filemove.service.FileMoveRecordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    @RequestMapping("fileMoveRecordList")
    public Map<String,Object> fileMoveRecordList(@RequestParam Map<String,Object> queryParams) {
        return fileMoveRecordService.fileMoveRecordList(queryParams);
    }

    @RequestMapping("remigrate")
    public ResultVO remigrate(@RequestParam Map<String,Object> queryParams) {
        fileMoveRecordService.remigrate(queryParams);
        ResultVO resultVO =new ResultVO(200,"手动迁移成功");
        return resultVO;
    }

    @RequestMapping("importTaskTable")
    public ResultVO importTaskTable(@RequestParam Map<String,Object> queryParams) {
        fileMoveRecordService.importTaskTable(queryParams);
        ResultVO resultVO =new ResultVO(200,"导入成功");
        return resultVO;
    }

    @RequestMapping("clearData")
    public ResultVO clearData(@RequestParam Map<String,Object> queryParams) {
        fileMoveRecordService.clearData(queryParams);
        ResultVO resultVO =new ResultVO(200,"导入成功");
        return resultVO;
    }
}
