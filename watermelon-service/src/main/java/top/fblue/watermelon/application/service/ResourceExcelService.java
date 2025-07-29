package top.fblue.watermelon.application.service;

import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.vo.ResourceExcelVO;

import java.util.List;

/**
 * 资源Excel处理服务 - 负责Excel的读写操作和数据校验
 */
public interface ResourceExcelService {
    
    /**
     * 读取Excel文件
     */
    List<ResourceExcelVO> readExcel(MultipartFile file);
    
    /**
     * 校验Excel数据
     */
    List<String> validateExcelData(List<ResourceExcelVO> dataList);
    
    /**
     * 写入Excel文件
     */
    byte[] writeExcel(List<ResourceExcelVO> data);
}