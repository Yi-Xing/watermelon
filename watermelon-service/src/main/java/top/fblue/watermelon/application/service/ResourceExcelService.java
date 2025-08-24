package top.fblue.watermelon.application.service;

import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.ResourceTreeExcelDTO;
import top.fblue.watermelon.application.dto.ResourceRelationImportDTO;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.vo.ExcelImportResultVO;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;

import java.util.List;
import java.util.Map;

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
    ExcelImportResultVO batchImportExcelData(List<ResourceExcelVO> excelDataList);

    /**
     * 批量导入Excel数据（带事务）
     * 支持增删改操作，确保数据一致性
     */
    ExcelImportResultVO batchImportResources(List<ResourceImportDTO> importDTOs);

    /**
     * 根据动态列生成Excel
     */
    byte[] generateDynamicColumnExcel(List<ResourceTreeExcelDTO> excelData);

    /**
     * 读取资源关联关系Excel文件
     * @param file Excel文件
     * @return 资源关联导入数据列表
     */
    List<ResourceRelationImportDTO> readResourceRelationExcel(MultipartFile file);

    /**
     * 校验资源关联Excel数据
     * @param dataList 资源关联数据列表
     * @param codeToIdMap 所有资源的code映射
     * @return 错误信息列表
     */
    List<String> validateResourceRelationExcelData(List<ResourceRelationImportDTO> dataList, Map<String, Long> codeToIdMap);

    /**
     * 批量处理资源关联关系（全量替换）
     * @param resourceRelationList 新的资源关联关系列表
     * @return 导入结果
     */
    ExcelImportResultVO batchProcessResourceRelations(List<ResourceRelation> resourceRelationList);
}