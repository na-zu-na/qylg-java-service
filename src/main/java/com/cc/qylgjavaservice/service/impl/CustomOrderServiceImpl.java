package com.cc.qylgjavaservice.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderCreateDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListParamDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.CustomOrder;
import com.cc.qylgjavaservice.entity.Orders;
import com.cc.qylgjavaservice.mapper.CustomOrderMapper;
import com.cc.qylgjavaservice.service.CustomOrderService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomOrderServiceImpl extends ServiceImpl<CustomOrderMapper,CustomOrder> implements CustomOrderService {
    @Autowired
    private CustomOrderMapper customOrderMapper;

    // 预算正则匹配：数字-数字
    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)-(\\d+)");
    // 预算正则匹配：数字以上
    private static final Pattern ABOVE_PATTERN = Pattern.compile("(\\d+)以上");
    // 预算正则匹配：数字以内
    private static final Pattern WITHIN_PATTERN = Pattern.compile("(\\d+)以内");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createCustomOrder(CustomOrderCreateDTO dto, Long userId) {

        // 1. 生成唯一订单号 (格式：CO + YYYYMMDD + 随机数/序列)
        String orderNo = generateOrderNo();

        // 2. 解析预算区间
        BigDecimal[] budgets = parseBudget(dto.getBudgetRange());

        // 3. 构建实体对象
        CustomOrder order = new CustomOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(dto.getProductId());
        order.setPurpose(dto.getPurpose());
        order.setStyle(dto.getStyle());
        order.setMaterial(dto.getMaterial());
        order.setMinBudget(budgets[0]);
        order.setMaxBudget(budgets[1]);
        order.setColors(dto.getColors()); // MP 自动转 JSONB
        order.setPatterns(dto.getPatterns()); // MP 自动转 JSONB
        order.setSize(dto.getSize());
        order.setRemark(dto.getRemark());
        order.setImages(dto.getImages()); // MP 自动转 JSONB
        order.setStatus(0); // 初始状态：制作中
        order.setCreatedAt(dto.getCreatedAt());

        // 4. 插入数据库
        boolean saved = customOrderMapper.insert(order) > 0;
        if (!saved) {
            throw new RuntimeException("订单创建失败，数据库写入错误");
        }

        return Result.success(order.getId());
    }

    @Override
    public Result<Page<CustomOrderListDTO>> customOrderList(CustomOrderListParamDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();

        Integer page = dto.getPage();
        Integer size = dto.getSize();
        Integer status = dto.getStatus();
        String key = dto.getKey();

        Page<CustomOrderListDTO> customOrderListDTOPage=new Page<>(page,size);
        Page<CustomOrderListDTO> customOrderListDTO = customOrderMapper.selectCustomOrderList(customOrderListDTOPage,status,currentUserId,key);
        List<CustomOrderListDTO> records = customOrderListDTO.getRecords();

        //设置状态
        records.forEach(i->{
            Integer currentStatus = i.getStatus();
            if (currentStatus==0){
                i.setStatusText("制作中");
            } else if (currentStatus==1)
                i.setStatusText("运输中");
            else i.setStatusText("已完成");
        });

        //设置预算
        records.forEach(i -> {
            BigDecimal minBudget = i.getMinBudget();
            BigDecimal maxBudget = i.getMaxBudget();

            String budgetText;

            // 情况 1: 只有上限 (min 为空或0, max 有值)
            if ((minBudget == null || minBudget.compareTo(BigDecimal.ZERO) == 0) && maxBudget != null) {
                budgetText = maxBudget + "以下";
            }
            // 情况 2: 只有下限 (max 为空, min 有值且>0)
            else if (maxBudget == null && minBudget != null && minBudget.compareTo(BigDecimal.ZERO) > 0) {
                budgetText = minBudget + "以上";
            }
            // 情况 3: 区间都有值 (min > 0 且 max > 0)
            else if (minBudget != null && maxBudget != null
                    && minBudget.compareTo(BigDecimal.ZERO) > 0
                    && maxBudget.compareTo(BigDecimal.ZERO) > 0) {
                // 额外检查：防止 min > max 的脏数据
                if (minBudget.compareTo(maxBudget) > 0) {
                    budgetText = maxBudget + "以下"; // 或者标记为数据错误
                } else {
                    budgetText = minBudget + "-" + maxBudget;
                }
            }
            // 情况 4: 其他所有情况 (都为 null, 或都为 0, 或数据不完整)
            else {
                budgetText = "暂不确定"; // 或者 "面议"
            }

            i.setBudget(budgetText);
        });

        customOrderListDTO.setRecords(records);

        return Result.success(customOrderListDTO);
    }

    @Override
    public Result<Long> receiveCustomOrder(Long id) {
        Long userId = UserContext.getCurrentUserId();

        boolean update = this.lambdaUpdate()
                .eq(CustomOrder::getId, id)
                .eq(CustomOrder::getUserId, userId) // 防止越权
                .eq(CustomOrder::getStatus, 1)      // 确保只有“待收货”状态才能转为“已完成”
                .set(CustomOrder::getStatus, 2)
                .update();
        if(update){
            return Result.success(id);
        }
        return Result.fail(500,"更新错误");
    }



    /**
     * 生成订单号: CO + 日期 + 随机后缀
     */
    private String generateOrderNo() {
        return "CON" + System.currentTimeMillis() +
                String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 解析预算字符串为 [min, max]
     * 支持："500以内", "500-1000", "8000以上", "暂不确定"
     */
    private BigDecimal[] parseBudget(String budgetStr) {
        BigDecimal min = null;
        BigDecimal max = null;

        if (budgetStr == null || "暂不确定".equals(budgetStr)) {
            return new BigDecimal[]{null, null};
        }

        Matcher rangeMatcher = RANGE_PATTERN.matcher(budgetStr);
        if (rangeMatcher.matches()) {
            min = new BigDecimal(rangeMatcher.group(1));
            max = new BigDecimal(rangeMatcher.group(2));
            return new BigDecimal[]{min, max};
        }

        Matcher withinMatcher = WITHIN_PATTERN.matcher(budgetStr);
        if (withinMatcher.matches()) {
            min = BigDecimal.ZERO;
            max = new BigDecimal(withinMatcher.group(1));
            return new BigDecimal[]{min, max};
        }

        Matcher aboveMatcher = ABOVE_PATTERN.matcher(budgetStr);
        if (aboveMatcher.matches()) {
            min = new BigDecimal(aboveMatcher.group(1));
            max = null; // 或者设为一个极大的值，视业务需求而定，这里设为 null 代表无上限
            return new BigDecimal[]{min, max};
        }

        // 如果格式都不匹配，抛出异常或按默认处理
        throw new IllegalArgumentException("预算格式不正确: " + budgetStr);
    }
}
