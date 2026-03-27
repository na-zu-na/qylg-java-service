package com.cc.qylgjavaservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderCreateDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderQueryDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.OrderItems;
import com.cc.qylgjavaservice.entity.Orders;
import com.cc.qylgjavaservice.mapper.OrderItemsMapper;
import com.cc.qylgjavaservice.mapper.OrdersMapper;
import com.cc.qylgjavaservice.service.OrdersService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Transactional(rollbackFor = Exception.class)
    public Result<Long> createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();

        // 计算总金额和总数量 (后端必须重新计算，不能完全信任前端)
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;

        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            BigDecimal price = new BigDecimal(item.getPrice());
            int count = item.getCount();

            totalAmount = totalAmount.add(price.multiply(new BigDecimal(count)));
            totalCount += count;
        }


        // 构建订单主表实体
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(1); // 1-待收货 (假设支付成功直接变待收货，或者设为 0-待支付，根据业务调整)
        order.setTotalPrice(totalAmount);
        order.setTotalCount(totalCount);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setPayTime(LocalDateTime.now()); // 模拟立即支付
        order.setCreatedAt(LocalDateTime.now());

        // 插入订单主表
        this.save(order);

        // 插入订单明细表
        List<OrderItems> itemsEntities = dto.getItems().stream().map(item -> {
            OrderItems oi = new OrderItems();
            oi.setOrderId(order.getId());
            oi.setGoodsId(item.getId());
            oi.setCount(item.getCount());
            oi.setPrice(new BigDecimal(item.getPrice())); // 记录下单时的价格快照
            return oi;
        }).toList();

        orderItemsMapper.insert(itemsEntities);

        Long id = order.getId();

        return Result.success(id);
    }

    @Override
    public Result<Page<OrderListDTO>> getOrderList(OrderQueryDTO queryDTO) {
//        Long currentUserId = UserContext.getCurrentUserId();
//
//        Page<Orders> ordersPage=new Page<>(orderQueryDTO.getPage(),orderQueryDTO.getPageSize());
//        Page<Orders> orders = ordersMapper.selectPage(ordersPage, new LambdaQueryWrapper<Orders>()
//                .eq(Orders::getUserId,currentUserId)
//                .and(w->w
//                .eq(Orders::getStatus, orderQueryDTO.getStatus())
//                .or()
//                .like(Orders::getOrderNo,orderQueryDTO.getKeyword()
//                ))
//        );
//
//        List<Orders> records = orders.getRecords();
//        List<Long> ids=records.stream().map(Orders::getId).toList();
//        Map<Long,List<OrderItems>> map=new HashMap<>();
//
//        ids.forEach(i->{
//            map.put(i,orderItemsMapper.selectList(new LambdaQueryWrapper<OrderItems>().eq(OrderItems::getOrderId,i)));
//        });
//
//        Page<OrderListDTO> orderListDTOPage=new Page<>(ordersPage.getCurrent(),ordersPage.getPages());
//
//        List<OrderListDTO> orderListDTOList=records.stream().map(i->{
//            OrderListDTO orderListDTO=new OrderListDTO();
//            BeanUtils.copyProperties(i,orderListDTO);
//            orderListDTO.setGoods(map.get(i.getOrderNo()));
//            return orderListDTO;
//        }).toList();
//
//        orderListDTOPage.setRecords(orderListDTOList);
//
//        return Result.success(orderListDTOPage);

        Long userId = UserContext.getCurrentUserId();

        Page<OrderListDTO> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        Page<OrderListDTO> result =
                ordersMapper.selectOrderList(
                        page,
                        userId,
                        queryDTO.getStatus(),
                        queryDTO.getKeyword()
                );

        return Result.success(result);
    }

    @Override
    public Result<Long> receiveOrder(Long id) {
        Long userId = UserContext.getCurrentUserId();

        boolean update = this.lambdaUpdate()
                .eq(Orders::getId, id)
                .eq(Orders::getUserId, userId) // 防止越权
                .eq(Orders::getStatus, 1)      // 确保只有“待收货”状态才能转为“已完成”
                .set(Orders::getStatus, 2)
                .set(Orders::getClosed,LocalDateTime.now())
                .update();
        if(update){
            return Result.success(id);
        }
        return Result.fail(500,"更新错误");
    }

    @Override
    public Result<OrderListDTO> orderDetail(Long id) {
        OrderListDTO orderListDTO=ordersMapper.selectOrderDetail(id);
        if (orderListDTO!=null){
            LocalDateTime createTime = orderListDTO.getCreateTime();
            long epochMilli = createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            orderListDTO.setFormattedCreateTime(epochMilli);

            return Result.success(orderListDTO);
        }
        else {
            return Result.fail(404,"没找到");
        }
    }

    /**
     * 生成唯一订单号：年月日时分秒 + 随机数
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() +
                String.format("%04d", (int)(Math.random() * 10000));
    }
}
