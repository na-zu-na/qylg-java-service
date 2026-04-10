package com.cc.qylgjavaservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.AiDTO.*;
import com.cc.qylgjavaservice.dto.OrderDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsCustomDetailDTO;
import com.cc.qylgjavaservice.entity.Conversation;
import com.cc.qylgjavaservice.entity.CustomOrder;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.mapper.ConversationMapper;
import com.cc.qylgjavaservice.mapper.CustomOrderMapper;
import com.cc.qylgjavaservice.mapper.UserMapper;
import com.cc.qylgjavaservice.service.CustomOrderService;
import com.cc.qylgjavaservice.service.ProductsService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomOrderServiceImpl extends ServiceImpl<CustomOrderMapper,CustomOrder> implements CustomOrderService {
    @Autowired
    private CustomOrderMapper customOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ProductsService productsService;

    @Autowired
    private WebClient webClient;

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

    @Override
    public Result<Void> customDesignConfirm(Long id) {
        CustomOrder customOrder = customOrderMapper.selectById(id);
        if (customOrder==null){
            return Result.fail(404,"没找到");
        }

        customOrder.setIsConfirmed(1);

        int i = customOrderMapper.updateById(customOrder);
        if (i<1){
            return Result.fail(500,"更新失败");
        }

        return Result.success();
    }

    @Override
    public Result<AdminCustomOrderListVO> getAdminCustomOrderList(int page, int pageSize, Integer status, String keyword) {
        Page<CustomOrderListDTO> dtoPage=new Page<>(page,pageSize);
        boolean isPending=false;

        //查列表
        if (status!=null){
            if (status == 4){
                status=null;
                isPending=true;
            }
        }
        Page<CustomOrderListDTO> orderListDTOPage=customOrderMapper.selectAdminOrderList(dtoPage,status,keyword,isPending);
        List<CustomOrderListDTO> orderListDTO=orderListDTOPage.getRecords();

        AdminCustomOrderListVO adminOrderListVO= new AdminCustomOrderListVO();
        adminOrderListVO.setOrderList(orderListDTO);
        //设置分页
        adminOrderListVO.setSize(orderListDTOPage.getSize());
        adminOrderListVO.setTotal(orderListDTOPage.getTotal());
        adminOrderListVO.setCurrent(orderListDTOPage.getCurrent());

        //查统计
        AdminCustomOrderListVO.Stats stats=customOrderMapper.selectStats();
        adminOrderListVO.setStats(stats);

        return Result.success(adminOrderListVO);
    }

    @Override
    public Result<String> orderAssign(Long workerId, Long customOrderId) {
        CustomOrder customOrder = customOrderMapper.selectById(customOrderId);
        if (customOrder==null){
            return Result.fail(404,"订单不存在");
        }

        Users users = userMapper.selectById(workerId);
        if (users==null){
            return Result.fail(404,"用户不存在");
        }

        customOrder.setWorkerId(workerId);
        int i = customOrderMapper.updateById(customOrder);
        if (i<1){
            return Result.fail(500,"更新失败");
        }
        return Result.success("负责人已分配");
    }

    @Override
    public Result<AdminCustomOrderDetailVO> getAdminCustomOrderDetail(Long customOrderId) {
        CustomOrderListDTO dto=customOrderMapper.selectAdminCustomOrderDetail(customOrderId);
        Conversation conversation = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getOrderId, customOrderId));

        AdminCustomOrderDetailVO adminCustomOrderDetailVO=new AdminCustomOrderDetailVO();
        adminCustomOrderDetailVO.setOrderList(dto);
        adminCustomOrderDetailVO.setConversation(conversation);
        return Result.success(adminCustomOrderDetailVO);
    }

    @Override
    public Result<Void> updateOrderStatus(Long customOrderId, Integer status) {
        CustomOrder customOrder = customOrderMapper.selectById(customOrderId);
        if (customOrder==null){
            return Result.fail(404,"没找到订单");
        }

        customOrder.setStatus(status);

        int i = customOrderMapper.updateById(customOrder);
        if (i<1){
            return Result.fail(500,"更新失败");
        }

        return Result.success();
    }

    @Override
    public Result<Void> updateCustomPic(Long customOrderId, CustomPicDTO url) {
        CustomOrder customOrder = customOrderMapper.selectById(customOrderId);
        if (customOrder==null){
            return Result.fail(404,"没找到订单");
        }

        customOrder.setProposalImage(url.getUrl());
        customOrder.setProposalTime(LocalDateTime.now());

        int i = customOrderMapper.updateById(customOrder);
        if (i<1){
            return Result.fail(500,"更新失败");
        }

        return Result.success();
    }

    @Override
    public Result<Void> updateCustomQuote(Long customOrderId, BigDecimal quote) {
        CustomOrder customOrder = customOrderMapper.selectById(customOrderId);
        if (customOrder==null){
            return Result.fail(404,"没找到订单");
        }

        customOrder.setQuote(quote);
        customOrder.setQuoteTime(LocalDateTime.now());

        int i = customOrderMapper.updateById(customOrder);
        if (i<1){
            return Result.fail(500,"更新失败");
        }

        return Result.success();
    }

    @Override
    public Result<AiCustomProductVO> aiCustomProduct(AiSearchRequestDTO dto) {
        Long productId = dto.getProductId();
        if (productId == null) {
            return Result.fail(400, "productId不能为空");
        }

        if (dto.getText() == null || dto.getText().trim().isEmpty()) {
            return Result.fail(400, "需求描述不能为空");
        }

        // 查询商品信息
        Result<ProductsCustomDetailDTO> productCustomsDetail = productsService.getProductCustomsDetail(productId);
        ProductsCustomDetailDTO data = productCustomsDetail.getData();
        if (data == null) {
            return Result.fail(404, "没找到商品");
        }

        // 用途候选
        List<String> purposeList = Arrays.asList(
                "定制礼品", "家居装饰", "艺术陈设", "商务赠礼", "其他"
        );

        // 风格候选
        List<String> styleList = data.getStyleList();
        if (styleList == null || styleList.isEmpty()) {
            styleList = Arrays.asList(
                    "传统国风", "现代简约", "复古雅致", "混搭创意", "参考案例定制"
            );
        }

        // 材质候选
        List<String> materialList = data.getMaterialList();
        if (materialList == null || materialList.isEmpty()) {
            materialList = Arrays.asList(
                    "木胎 + 髹漆 + 嵌银", "金属嵌银", "漆艺为主", "根据建议选择"
            );
        }

        // 预算候选
        List<String> budgetRangeList = Arrays.asList(
                "500以内", "500-1000", "1000-3000", "3000-8000", "8000以上", "暂不确定"
        );

        // 颜色候选
        List<String> colorList = Arrays.asList(
                "朱红", "雅黑", "鎏金", "银白", "木色", "青绿"
        );

        // 纹样候选
        List<String> patternList = Arrays.asList(
                "祥云纹", "水波纹", "花卉纹", "龙纹", "凤纹", "自定义纹样"
        );

        // 组装 AI 请求参数
        AiCustomProductDTO aiCustomProductDTO = new AiCustomProductDTO();
        aiCustomProductDTO.setText(dto.getText().trim());
        aiCustomProductDTO.setPurposeList(purposeList);
        aiCustomProductDTO.setStyleList(styleList);
        aiCustomProductDTO.setMaterialList(materialList);
        aiCustomProductDTO.setBudgetRangeList(budgetRangeList);
        aiCustomProductDTO.setColorList(colorList);
        aiCustomProductDTO.setPatternList(patternList);

        try {
            ApiResponse<AiCustomProductVO> response = webClient.post()
                    .uri("/api/ai/custom-product")
                    .bodyValue(aiCustomProductDTO)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("AI服务4xx错误: " + body)))
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("AI服务5xx错误: " + body)))
                    )
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<AiCustomProductVO>>() {})
                    .block();

            if (response == null || response.getData() == null) {
                return Result.fail(500, "AI返回结果为空");
            }

            AiCustomProductVO vo = response.getData();

            // 校验 AI 返回结果，只保留候选范围内的数据
            vo.setPurpose(pickValidValue(vo.getPurpose(), purposeList));
            vo.setStyle(pickValidValue(vo.getStyle(), styleList));
            vo.setMaterial(pickValidValue(vo.getMaterial(), materialList));
            vo.setBudgetRange(pickValidValue(vo.getBudgetRange(), budgetRangeList));
            vo.setColors(pickValidList(vo.getColors(), colorList));
            vo.setPatterns(pickValidList(vo.getPatterns(), patternList));

            // size / remark 一般是自由文本，不强校验
            if (vo.getSize() != null) {
                vo.setSize(vo.getSize().trim());
            }
            if (vo.getRemark() != null) {
                vo.setRemark(vo.getRemark().trim());
            }

            return Result.success(vo);

        } catch (Exception e) {
            log.error("AI智能填写定制订单失败, productId="+productId+", text="+dto.getText()+e);
            return Result.fail(500, "AI服务调用失败");
        }
    }

    //单值校验
    private String pickValidValue(String value, List<String> candidates) {
        if (value == null || candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.contains(value) ? value : null;
    }

    //多值校验
    private List<String> pickValidList(List<String> values, List<String> candidates) {
        if (values == null || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(candidates::contains)
                .distinct()
                .toList();
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
