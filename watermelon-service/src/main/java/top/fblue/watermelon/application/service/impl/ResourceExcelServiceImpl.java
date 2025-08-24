package top.fblue.watermelon.application.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.converter.ResourceConverter;
import top.fblue.watermelon.application.dto.ResourceTreeExcelDTO;
import top.fblue.watermelon.application.dto.ResourceRelationImportDTO;
import top.fblue.watermelon.application.service.ResourceExcelService;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.vo.ResourceExcelVOTmp;
import top.fblue.watermelon.application.vo.ExcelImportResultVO;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.repository.ResourceRepository;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceRelationDomainService;

import jakarta.annotation.Resource;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源Excel处理服务实现 - 负责Excel的读写操作和数据校验
 */
@Slf4j
@Service
public class ResourceExcelServiceImpl implements ResourceExcelService {

    @Resource
    private ResourceDomainService resourceDomainService;
    @Resource
    private ResourceConverter resourceConverter;
    @Resource
    private ResourceRepository resourceRepository;
    @Resource
    private ResourceRelationDomainService resourceRelationDomainService;

    @Override
    public List<ResourceExcelVO> readExcel(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                throw new BusinessException("只支持Excel文件格式(.xlsx/.xls)");
            }
            List<ResourceExcelVO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(ResourceExcelVO.class)
                    .sheet()
                    .doReadSync();
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new BusinessException("Excel文件为空");
            }
            return excelDataList;
        } catch (Exception e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> validateExcelData(List<ResourceExcelVO> dataList) {
        // 记录错误信息
        List<String> errors = new ArrayList<>();
        // 用于 code 去重校验
        Set<String> codeSet = new HashSet<>();


        for (int i = 0; i < dataList.size(); i++) {
            ResourceExcelVO data = dataList.get(i);
            int rowNumber = i + 2; // Excel行号从2开始（第1行是标题）

            // 校验 name 是否为空 和 是否包含特殊字符
            if (data.getName() == null || data.getName().isEmpty()) {
                errors.add(String.format("第%d行: 资源名称不能为空", rowNumber));
            } else if (data.getName().contains("/")) {
                errors.add(String.format("第%d行: 资源名称不能包含'/'字符", rowNumber));

            }

            // 校验 code 是否为空 和 是否已存在
            if (data.getCode() == null || data.getCode().isEmpty()) {
                errors.add(String.format("第%d行: 资源code不能为空", rowNumber));
            } else if (codeSet.contains(data.getCode())) {
                errors.add(String.format("第%d行: 资源code已存在", rowNumber));
            }

            // 校验类型
            if (!ResourceTypeEnum.isValidDesc(data.getType())) {
                errors.add(String.format("第%d行: 资源类型无效，只能是'页面'、'按钮'、'接口、目录'", rowNumber));
            }

            // 校验状态
            if (!StateEnum.isValidDesc(data.getState())) {
                errors.add(String.format("第%d行: 状态无效，只能是'启用'、'禁用'", rowNumber));
            }

            // 检验备注
            if (data.getRemark() != null && data.getRemark().length() > 500) {
                errors.add(String.format("第%d行: 备注长度不能超过500个字符", rowNumber));
            }

            // 收集数据，用于去重校验
            codeSet.add(data.getCode());
        }

        return errors;
    }

    @Override
    public <T> byte[] writeExcel(List<T> data, String sheetName, Class<T> clazz) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 列宽自适应
                    .sheet(sheetName)
                    .doWrite(data);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Excel导出失败，数据类型: {}, sheet名称: {}, 错误信息: {}",
                    clazz.getSimpleName(), sheetName, e.getMessage(), e);
            throw new BusinessException("导出Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 检测资源树中是否存在环
     * 使用深度优先搜索（DFS）检测环
     *
     * @param dataList          资源数据列表
     * @param codeToResourceMap code到资源的映射
     * @return 存在环，则返回资源的code
     */
    private String hasCycle(List<ResourceExcelVOTmp> dataList, Map<String, ResourceExcelVOTmp> codeToResourceMap) {

        // 记录已访问的节点
        Set<String> visited = new HashSet<>();
        // 记录当前路径中的节点（用于检测环）
        Set<String> path = new HashSet<>();

        // 对每个节点进行DFS检测
        for (ResourceExcelVOTmp resource : dataList) {
            if (!visited.contains(resource.getCode())) {
                if (dfsHasCycle(resource.getCode(), codeToResourceMap, visited, path)) {
                    return resource.getCode();
                }
            }
        }

        return null;
    }

    /**
     * 深度优先搜索检测环
     *
     * @param currentCode       当前节点code
     * @param codeToResourceMap code到资源的映射
     * @param visited           已访问的节点集合
     * @param path              当前路径中的节点集合
     * @return true 如果检测到环，false 如果没有环
     */
    private boolean dfsHasCycle(String currentCode, Map<String, ResourceExcelVOTmp> codeToResourceMap,
                                Set<String> visited, Set<String> path) {
        // 如果当前节点已在当前路径中，说明存在环
        if (path.contains(currentCode)) {
            return true;
        }

        // 如果当前节点已访问过，说明这个分支已经检查过，无环
        if (visited.contains(currentCode)) {
            return false;
        }

        // 将当前节点加入已访问和当前路径
        visited.add(currentCode);
        path.add(currentCode);

        // 获取当前资源
        ResourceExcelVOTmp currentResource = codeToResourceMap.get(currentCode);
        if (currentResource != null && currentResource.getParentCode() != null && !currentResource.getParentCode().isEmpty()) {
            // 检查父节点是否存在
            if (codeToResourceMap.containsKey(currentResource.getParentCode())) {
                // 递归检查父节点
                if (dfsHasCycle(currentResource.getParentCode(), codeToResourceMap, visited, path)) {
                    return true;
                }
            }
        }

        // 从当前路径中移除当前节点（回溯）
        path.remove(currentCode);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResultVO batchImportExcelData(List<ResourceExcelVO> excelDataList) {
        // 1. 转换为导入VO
        List<ResourceImportDTO> importDTOs = convertSortToImportDTOs(excelDataList);

        // 2. 批量导入（带事务）
        return batchImportResources(importDTOs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResultVO batchImportResources(List<ResourceImportDTO> importDTOs) {
        int totalRows = importDTOs.size();
        int insertedRows = 0;
        int updatedRows = 0;
        int deletedRows = 0;
        // 1. 获取Excel中的所有code
        Set<String> excelCodes = importDTOs.stream()
                .map(ResourceImportDTO::getCode)
                .collect(Collectors.toSet());

        // 2. 获取数据库中所有资源
        List<ResourceNode> existingResources = resourceDomainService.findAll();
        Map<String, ResourceNode> existingCodeToResource = existingResources.stream()
                .collect(Collectors.toMap(ResourceNode::getCode, resource -> resource));

        // 3. 批量新增和更新
        for (ResourceImportDTO importDTO : importDTOs) {
            ResourceNode existingResource = existingCodeToResource.get(importDTO.getCode());

            // 转为 ResourceNode
            ResourceNode resourceNode = resourceConverter.toResourceNode(importDTO);
            if (existingResource != null) {
                // 数据库存在，Excel存在 -> 发生变动则更新
                if (!existingResource.equals(resourceNode)) {
                    resourceNode.setId(existingResource.getId());
                    if (!resourceRepository.update(resourceNode)) {
                        throw new BusinessException(String.format("%s 资源更新失败", importDTO.getName()));
                    }
                    updatedRows++;
                }
            } else {
                // 数据库不存在，Excel存在 -> 新增
                resourceRepository.save(resourceNode);
                insertedRows++;
            }
        }


        // 4. 批量删除
        List<Long> deleteIds = new ArrayList<>();
        // 处理需要删除的资源（数据库存在，Excel不存在）
        for (Map.Entry<String, ResourceNode> entry : existingCodeToResource.entrySet()) {
            if (!excelCodes.contains(entry.getKey())) {
                deleteIds.add(entry.getValue().getId());
            }
        }

        if (!deleteIds.isEmpty()) {
            // 批量删除资源
            int deleteCount = resourceRepository.batchDelete(deleteIds);
            if (deleteCount != deleteIds.size()) {
                throw new BusinessException(String.format("资源删除失败，失败 %d 个", deleteIds.size() - deleteCount));
            }
            deletedRows = deleteIds.size();
        }

        return ExcelImportResultVO.builder()
                .success(true)
                .totalCount(totalRows)
                .addedCount(insertedRows)
                .updatedCount(updatedRows)
                .deletedCount(deletedRows)
                .errors(new ArrayList<>())
                .build();
    }

    @Override
    public byte[] generateDynamicColumnExcel(List<ResourceTreeExcelDTO> excelData) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("资源关系树");

            // 计算最大列数
            int maxColumns = calculateMaxColumns(excelData);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < maxColumns; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue("资源树");
            }

            // 添加固定列
            Cell orderCell = headerRow.createCell(maxColumns);
            orderCell.setCellValue("显示顺序");

            Cell stateCell = headerRow.createCell(maxColumns + 1);
            stateCell.setCellValue("状态");

            // 创建数据行
            for (int rowIndex = 0; rowIndex < excelData.size(); rowIndex++) {
                ResourceTreeExcelDTO data = excelData.get(rowIndex);
                Row dataRow = sheet.createRow(rowIndex + 1);

                // 填充资源树列
                for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                    if (colIndex == data.getDepth()) {
                        Cell cell = dataRow.createCell(colIndex);
                        cell.setCellValue(data.getResourceInfo());
                    }
                }

                // 填充固定列
                Cell orderDataCell = dataRow.createCell(maxColumns);
                if (data.getOrderNum() != null) {
                    orderDataCell.setCellValue(data.getOrderNum());
                }

                Cell stateDataCell = dataRow.createCell(maxColumns + 1);
                if (data.getState() != null) {
                    stateDataCell.setCellValue(data.getState());
                }
            }

            // 自动调整列宽
            for (int i = 0; i < maxColumns + 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成动态列Excel失败: {}", e.getMessage(), e);
            throw new BusinessException("生成Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 计算最大列数
     */
    private int calculateMaxColumns(List<ResourceTreeExcelDTO> excelData) {
        if (excelData == null || excelData.isEmpty()) {
            return 0;
        }

        return excelData.stream()
                .mapToInt(ResourceTreeExcelDTO::getDepth)
                .max()
                .orElse(0) + 1; // +1 因为索引从0开始
    }

    /**
     * 转换为导入VO并进行拓扑排序
     */
    private List<ResourceImportDTO> convertSortToImportDTOs(List<ResourceExcelVO> excelDataList) {
        // 1. 构建code到Excel数据的映射
        Map<String, ResourceExcelVO> codeToExcelMap = excelDataList.stream()
                .collect(Collectors.toMap(ResourceExcelVO::getCode, data -> data));

        // 2. 拓扑排序，确保父节点在子节点之前
//        List<ResourceExcelVOTmp> sortedDataList = topologicalSort(excelDataList, codeToExcelMap);

        // 3. 转换为导入VO
//        return sortedDataList.stream()
//                .map(resourceConverter::toImportDTO)
//                .collect(Collectors.toList());
        // todo 暂存
        return null;
    }

    /**
     * 拓扑排序，确保父节点在子节点之前处理
     */
    private List<ResourceExcelVOTmp> topologicalSort(List<ResourceExcelVOTmp> dataList, Map<String, ResourceExcelVOTmp> codeToExcelMap) {
        // 构建邻接表
        Map<String, List<String>> adjacencyList = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // 初始化
        for (ResourceExcelVOTmp data : dataList) {
            adjacencyList.put(data.getCode(), new ArrayList<>());
            inDegree.put(data.getCode(), 0);
        }

        // 构建依赖关系
        for (ResourceExcelVOTmp data : dataList) {
            if (data.getParentCode() != null && !data.getParentCode().isEmpty()
                    && codeToExcelMap.containsKey(data.getParentCode())) {
                // 如果父节点也在Excel中，建立依赖关系
                adjacencyList.get(data.getParentCode()).add(data.getCode());
                inDegree.put(data.getCode(), inDegree.get(data.getCode()) + 1);
            }
        }

        // 拓扑排序
        List<ResourceExcelVOTmp> result = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();

        // 将所有入度为0的节点加入队列
        for (String code : inDegree.keySet()) {
            if (inDegree.get(code) == 0) {
                queue.offer(code);
            }
        }

        // 处理队列中的节点
        while (!queue.isEmpty()) {
            String currentCode = queue.poll();
            ResourceExcelVOTmp currentData = codeToExcelMap.get(currentCode);
            result.add(currentData);

            // 减少所有邻居的入度
            for (String neighbor : adjacencyList.get(currentCode)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 如果结果数量不等于输入数量，说明存在环
        if (result.size() != dataList.size()) {
            throw new BusinessException("资源树中存在循环引用");
        }

        return result;
    }

    @Override
    public List<ResourceRelationImportDTO> readResourceRelationExcel(MultipartFile file) {
        List<ResourceRelationImportDTO> result = new ArrayList<>();

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("只支持Excel文件格式(.xlsx/.xls)");
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel文件为空");
            }

            // 获取表头行，确定资源树列数
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || headerRow.getLastCellNum() < 2) {
                throw new BusinessException("Excel文件格式错误：至少需要一列资源树和一列显示顺序");
            }

            // 查找"显示顺序"列的位置来确定资源树列数
            int resourceTreeColumnCount = 0;
            int orderColumnIndex = 0;

            for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
                Cell headerCell = headerRow.getCell(colIndex);
                if (headerCell != null) {
                    String headerValue = getCellStringValue(headerCell);
                    if ("显示顺序".equals(headerValue)) {
                        orderColumnIndex = colIndex;
                        resourceTreeColumnCount = colIndex - 1; // 显示顺序列之前的都是资源树列
                        break;
                    }
                }
            }

            if (orderColumnIndex == 0) {
                throw new BusinessException("Excel文件格式错误：未找到'显示顺序'列");
            }

            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                List<String> resourcePath = new ArrayList<>();
                // 读取资源树列
                for (int j = 0; j <= resourceTreeColumnCount; j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = "";
                    if (cell != null) {
                        cellValue = getCellStringValue(cell);
                        if (!cellValue.isEmpty()) {
                            resourcePath.add(cellValue);
                            break;
                        }
                    }
                    resourcePath.add(cellValue);
                }

                // 读取显示顺序
                Cell orderCell = row.getCell(orderColumnIndex);
                Integer orderNum = getInteger(orderCell);
                // 添加到结果中
                ResourceRelationImportDTO dto = ResourceRelationImportDTO.builder()
                        .resourcePath(resourcePath)
                        .orderNum(orderNum)
                        .build();
                result.add(dto);
            }

        } catch (Exception e) {
            log.error("读取资源关联Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        if (result.isEmpty()) {
            throw new BusinessException("Excel文件为空");
        }
        return result;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        String cellValue = "";
        switch (cell.getCellType()) {
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                cellValue = String.valueOf((int) cell.getNumericCellValue());
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = cell.getCellFormula();
                break;
            default:
                return "";
        }
        return cellValue.trim();
    }

    private static Integer getInteger(Cell orderCell) {
        if (orderCell == null) {
            return null;
        }
        Integer orderNum = null;
        switch (orderCell.getCellType()) {
            case STRING:
                orderNum = Integer.parseInt(orderCell.getStringCellValue());
                break;
            case NUMERIC:
                orderNum = (int) orderCell.getNumericCellValue();
                break;
            default:
        }
        return orderNum;
    }

    @Override
    public List<String> validateResourceRelationExcelData(List<ResourceRelationImportDTO> dataList, Map<String, Long> existingResources) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            ResourceRelationImportDTO dto = dataList.get(i);
            int rowNum = i + 2; // Excel行号（从第2行开始）

            // 验证资源路径
            List<String> resourcePath = dto.getResourcePath();
            if (resourcePath.isEmpty()) {
                errors.add(String.format("第%d行: 资源路径不能为空", rowNum));
                continue;
            }

            String resourceInfo = resourcePath.getLast();
            String code = resourceInfo.substring(resourceInfo.indexOf('/') + 1);
            if (!existingResources.containsKey(code)) {
                errors.add(String.format("第%d行，第%d列: 资源code不存在，code：%s",
                        rowNum, dto.getResourcePath().size() - 1, code));
            }

            // 验证显示顺序
            if (dto.getOrderNum() == null) {
                errors.add(String.format("第%d行: 显示顺序不能为空", rowNum));
            } else if (dto.getOrderNum() < 0) {
                errors.add(String.format("第%d行: 显示顺序不能小于0", rowNum));
            }
        }
        return errors;
    }

    /**
     * 从资源信息中提取资源code
     * 格式: 资源名称/资源code 或 资源名称/资源code:路径
     */
    private String extractResourceCode(String resourceInfo) {
        if (resourceInfo == null || resourceInfo.trim().isEmpty()) {
            return null;
        }

        String trimmed = resourceInfo.trim();
        int slashIndex = trimmed.indexOf('/');
        if (slashIndex == -1 || slashIndex == trimmed.length() - 1) {
            return null;
        }

        String codePart = trimmed.substring(slashIndex + 1);
        // 如果有冒号，取冒号前面的部分作为code
        int colonIndex = codePart.indexOf(':');
        if (colonIndex != -1) {
            return codePart.substring(0, colonIndex);
        }

        return codePart;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelImportResultVO batchProcessResourceRelations(List<ResourceRelation> resourceRelationList) {
        int totalCount = resourceRelationList.size();
        int addedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        // 1. 构建Excel中的关联关系映射（用parent-child作为唯一键）
        Set<String> excelRelationKeys = resourceRelationList.stream()
                .map(relation -> relation.getParentId() + "-" + relation.getChildId())
                .collect(Collectors.toSet());

        // 2. 获取数据库中所有的资源关联关系
        List<ResourceRelation> existingRelations = resourceRelationDomainService.getAllResourceRelations();
        Map<String, ResourceRelation> existingRelationMap = existingRelations.stream()
                .collect(Collectors.toMap(
                        relation -> relation.getParentId() + "-" + relation.getChildId(),
                        relation -> relation
                ));

        // 3. 批量新增和更新
        for (ResourceRelation newRelation : resourceRelationList) {
            String relationKey = newRelation.getParentId() + "-" + newRelation.getChildId();
            ResourceRelation existingRelation = existingRelationMap.get(relationKey);

            if (existingRelation != null) {
                // 数据库存在，Excel存在 -> 检查是否需要更新
                if (!existingRelation.getOrderNum().equals(newRelation.getOrderNum())) {
                    // 只有显示顺序不同才更新
                    existingRelation.setOrderNum(newRelation.getOrderNum());
                    if (!resourceRelationDomainService.updateResourceRelation(existingRelation)) {
                        throw new BusinessException(String.format("%s 资源关联关系更新失败", relationKey));
                    }
                    updatedCount++;
                }
            } else {
                // 数据库不存在，Excel存在 -> 新增
                resourceRelationDomainService.createResourceRelation(newRelation);
                addedCount++;
            }
        }

        // 4. 批量删除
        List<Long> deleteIds = new ArrayList<>();
        // 处理需要删除的关联关系（数据库存在，Excel不存在）
        for (Map.Entry<String, ResourceRelation> entry : existingRelationMap.entrySet()) {
            if (!excelRelationKeys.contains(entry.getKey())) {
                deleteIds.add(entry.getValue().getId());
            }
        }

        if (!deleteIds.isEmpty()) {
            // 批量删除资源
            int deleteCount = resourceRelationDomainService.batchDelete(deleteIds);
            if (deleteCount != deleteIds.size()) {
                throw new BusinessException(String.format("资源关联关系删除失败，失败 %d 个", deleteIds.size() - deleteCount));
            }
            deletedCount = deleteIds.size();
        }

        return ExcelImportResultVO.builder()
                .success(true)
                .totalCount(totalCount)
                .addedCount(addedCount)
                .updatedCount(updatedCount)
                .deletedCount(deletedCount)
                .errors(new ArrayList<>())
                .build();
    }
}