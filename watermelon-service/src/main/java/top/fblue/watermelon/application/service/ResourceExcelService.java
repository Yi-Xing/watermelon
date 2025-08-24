package top.fblue.watermelon.application.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.ResourceTreeExcelDTO;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.vo.ResourceExcelVOTmp;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;

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
    <T> byte[] writeExcel(List<T> data, String sheetName, Class<T> clazz);

    /**
     * 批量导入Excel数据（带事务）
     * 支持增删改操作，确保数据一致性
     */
    ResourceImportResultVO batchImportExcelData(List<ResourceExcelVO> excelDataList);

    /**
     * 批量导入Excel数据（带事务）
     * 支持增删改操作，确保数据一致性
     */
    ResourceImportResultVO batchImportResources(List<ResourceImportDTO> importDTOs);

    /**
     * 根据动态列生成Excel
     */
    byte[] generateDynamicColumnExcel(List<ResourceTreeExcelDTO> excelData);
}