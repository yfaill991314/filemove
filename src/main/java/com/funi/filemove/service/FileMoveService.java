package com.funi.filemove.service;

/**
 * @ClassName FileMoveService
 * @Description TODO
 * @Author YL
 * @Date 2020/10/29 14:31
 * @Version 1.0
 */
public interface FileMoveService {

    void fileMove() throws Exception;

    boolean stopMove();

    boolean startMove();

}
