package top.fblue.watermelon.application.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.service.ResourceExcelService;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;

import javax.naming.Name;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * 资源Excel处理服务实现 - 负责Excel的读写操作和数据校验
 */
@Service
public class ResourceExcelServiceImpl implements ResourceExcelService {

    @Override
    public List<ResourceExcelVO> readExcel(MultipartFile file) {
        try {
            List<ResourceExcelVO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(ResourceExcelVO.class)
                    .sheet()
                    .doReadSync();
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new RuntimeException("Excel文件为空");
            }
            return excelDataList;
        } catch (Exception e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> validateExcelData(List<ResourceExcelVO> dataList) {
        List<String> errors = new ArrayList<>();
        Map<String,Set<String>> nameMap = new HashMap<>();
        Set<String> codeSet = new HashSet<>();
        // 构建 code 到 ResourceExcelVO 的映射
        Map<String, ResourceExcelVO> codeToResourceMap = new HashMap<>();
        Set<String> allCdeSet = new HashSet<>();

        for (ResourceExcelVO data : dataList) {
            allCdeSet.add(data.getCode());
            codeToResourceMap.put(data.getCode(), data);
        }

        for (int i = 0; i < dataList.size(); i++) {
            ResourceExcelVO data = dataList.get(i);
            int rowNumber = i + 2; // Excel行号从2开始（第1行是标题）

            // 校验父级 code 是否存在
            if (!data.getParentCode().isEmpty() && !allCdeSet.contains(data.getParentCode())) {
                errors.add(String.format("第%d行: 上级资源code不存在", rowNumber));
            }

            // 校验 name 是否为空 和 同级是否已存在
            if (data.getName() == null || data.getName().isEmpty()) {
                errors.add(String.format("第%d行: 资源名称不能为空", rowNumber));
            } else if (nameMap.get(data.getParentCode()).contains(data.getName())) {
                errors.add(String.format("第%d行: 同级中资源名称已存在", rowNumber));
            }

            // 校验 code 是否为空 和 是否已存在
            if (data.getCode() == null || data.getCode().isEmpty()) {
                errors.add(String.format("第%d行: 资源code不能为空", rowNumber));
            } else if (codeSet.contains(data.getCode())) {
                errors.add(String.format("第%d行: 资源code已存在", rowNumber));
            }

            // 校验类型
            if (!ResourceTypeEnum.isValidDesc(data.getType())) {
                errors.add(String.format("第%d行: 资源类型无效，只能是'页面'、'按钮'、'接口'", rowNumber));
            }

            // 校验显示顺序
            if (data.getOrderNum() == null || data.getOrderNum() < 0) {
                errors.add(String.format("第%d行: 显示顺序不合法", rowNumber));
            }

            // 校验状态
            if (!StateEnum.isValidDesc(data.getType())) {
                errors.add(String.format("第%d行: 状态无效，只能是'启用'、'禁用'", rowNumber));
            }

            // 检验备注
            if (data.getRemark() != null && data.getRemark().length() > 500) {
                errors.add(String.format("第%d行: 备注长度不能超过500个字符", rowNumber));
            }

            // 收集数据，用于去重校验
            nameMap.computeIfAbsent(data.getParentCode(), k -> new HashSet<>()).add(data.getName());
            codeSet.add(data.getCode());
        }

        // 检测是否存在环
        String resourceCode = hasCycle(dataList, codeToResourceMap);
        if (resourceCode != null) {
            errors.add(String.format("资源树中存在循环引用，请检查上级资源设置，资源code:%s", resourceCode));
        }

        return errors;
    }

    @Override
    public byte[] writeExcel(List<ResourceExcelVO> data) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, ResourceExcelVO.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 列宽自适应
                    .sheet("资源列表")
                    .doWrite(data);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("写入Excel文件失败: " + e.getMessage());
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
    private String hasCycle(List<ResourceExcelVO> dataList, Map<String, ResourceExcelVO> codeToResourceMap) {

        // 记录已访问的节点
        Set<String> visited = new HashSet<>();
        // 记录当前路径中的节点（用于检测环）
        Set<String> path = new HashSet<>();

        // 对每个节点进行DFS检测
        for (ResourceExcelVO resource : dataList) {
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
    private boolean dfsHasCycle(String currentCode, Map<String, ResourceExcelVO> codeToResourceMap,
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
        ResourceExcelVO currentResource = codeToResourceMap.get(currentCode);
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
}