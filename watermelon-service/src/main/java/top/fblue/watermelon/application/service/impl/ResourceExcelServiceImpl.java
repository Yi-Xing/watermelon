package top.fblue.watermelon.application.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.service.ResourceExcelService;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
        } catch (Exception e) {
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> validateExcelData(List<ResourceExcelVO> dataList) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i++) {
            ResourceExcelVO data = dataList.get(i);
            int rowNumber = i + 2; // Excel行号从2开始（第1行是标题）
            
            // 校验必填字段
            if (data.getName() == null || data.getName().trim().isEmpty()) {
                errors.add(String.format("第%d行: 资源名称不能为空", rowNumber));
            }
            
            if (data.getCode() == null || data.getCode().trim().isEmpty()) {
                errors.add(String.format("第%d行: 资源code不能为空", rowNumber));
            }
            
            // 校验类型
            if (data.getType() != null && !isValidType(data.getType())) {
                errors.add(String.format("第%d行: 资源类型无效，只能是'页面'、'按钮'、'接口'", rowNumber));
            }
            
            // 校验状态
            if (data.getState() != null && !isValidState(data.getState())) {
                errors.add(String.format("第%d行: 状态无效，只能是'启用'、'禁用'", rowNumber));
            }
            
            // 校验显示顺序
            if (data.getOrderNum() != null && data.getOrderNum() < 0) {
                errors.add(String.format("第%d行: 显示顺序不能为负数", rowNumber));
            }
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
     * 校验资源类型是否有效
     */
    private boolean isValidType(String type) {
        if (type == null) return true; // 允许为空，使用默认值
        String trimmedType = type.trim();
        return "页面".equals(trimmedType) || "按钮".equals(trimmedType) || "接口".equals(trimmedType);
    }
    
    /**
     * 校验状态是否有效
     */
    private boolean isValidState(String state) {
        if (state == null) return true; // 允许为空，使用默认值
        String trimmedState = state.trim();
        return "启用".equals(trimmedState) || "禁用".equals(trimmedState);
    }
}