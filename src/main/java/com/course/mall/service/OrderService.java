package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.mall.common.BusinessException;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.OrderCreateRequest;
import com.course.mall.entity.Address;
import com.course.mall.entity.CartItem;
import com.course.mall.entity.Order;
import com.course.mall.entity.OrderItem;
import com.course.mall.entity.PaymentRecord;
import com.course.mall.entity.Product;
import com.course.mall.mapper.AddressMapper;
import com.course.mall.mapper.CartItemMapper;
import com.course.mall.mapper.OrderItemMapper;
import com.course.mall.mapper.OrderMapper;
import com.course.mall.mapper.PaymentRecordMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.vo.OrderItemVO;
import com.course.mall.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, CartItemMapper cartItemMapper,
                        ProductMapper productMapper, AddressMapper addressMapper, PaymentRecordMapper paymentRecordMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @Transactional
    public OrderVO create(OrderCreateRequest request) {
        Long userId = SessionContext.requireUser().getId();
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !userId.equals(address.getUserId())) {
            throw BusinessException.badRequest("请选择有效的联系地址");
        }
        List<CartItem> checkedItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getChecked, true));
        if (checkedItems.isEmpty()) {
            throw BusinessException.badRequest("请先勾选要结算的商品");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setReceiverSnapshot(snapshot(address));
        order.setStatus("PENDING_PAY");
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : checkedItems) {
            int quantity = requireCartQuantity(cartItem);
            Product product = productMapper.selectById(cartItem.getProductId());
            ensureProductCanOrder(product, quantity);
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getCoverImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(subtotal);
            orderItemMapper.insert(orderItem);

            product.setStock(stockOf(product) - quantity);
            product.setSales(salesOf(product) + quantity);
            productMapper.updateById(product);
        }
        order.setTotalAmount(total);
        orderMapper.updateById(order);
        cartItemMapper.deleteBatchIds(checkedItems.stream().map(CartItem::getId).toList());
        return detail(order.getOrderNo());
    }

    public Page<OrderVO> list(long page, long size) {
        Long userId = SessionContext.requireUser().getId();
        Page<Order> orderPage = orderMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt));
        Page<OrderVO> voPage = Page.of(page, size, orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    public OrderVO detail(String orderNo) {
        Long userId = SessionContext.requireUser().getId();
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        return toVO(order);
    }

    @Transactional
    public OrderVO cancel(String orderNo) {
        Long userId = SessionContext.requireUser().getId();
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw BusinessException.badRequest("只有待支付订单可以取消");
        }
        restoreStock(order.getId());
        order.setStatus("CANCELED");
        orderMapper.updateById(order);
        return toVO(order);
    }

    @Transactional
    public OrderVO mockPay(String orderNo) {
        Long userId = SessionContext.requireUser().getId();
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        requirePayableOrder(order);
        LocalDateTime now = LocalDateTime.now();
        paymentRecordMapper.insert(buildMockPaymentRecord(order, now));

        order.setStatus("PAID");
        order.setPaidAt(now);
        orderMapper.updateById(order);
        return toVO(order);
    }

    private void requirePayableOrder(Order order) {
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw BusinessException.badRequest("订单当前状态不能支付");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("订单金额异常");
        }
    }

    private PaymentRecord buildMockPaymentRecord(Order order, LocalDateTime paidAt) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setPayNo(generatePayNo());
        record.setAmount(order.getTotalAmount());
        record.setStatus("SUCCESS");
        record.setPayType("MOCK");
        record.setPaidAt(paidAt);
        return record;
    }

    public Page<OrderVO> adminList(long page, long size, String status, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(StringUtils.hasText(status), Order::getStatus, status)
                .like(StringUtils.hasText(orderNo), Order::getOrderNo, orderNo)
                .orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = orderMapper.selectPage(Page.of(page, size), wrapper);
        Page<OrderVO> voPage = Page.of(page, size, orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    public OrderVO adminShip(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw BusinessException.badRequest("只有已支付交易可以交付");
        }
        order.setStatus("SHIPPED");
        orderMapper.updateById(order);
        return toVO(order);
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                product.setStock(stockOf(product) + quantity);
                product.setSales(Math.max(0, salesOf(product) - quantity));
                productMapper.updateById(product);
            }
        }
    }

    private int requireCartQuantity(CartItem cartItem) {
        Integer quantity = cartItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw BusinessException.badRequest("商品数量必须大于 0");
        }
        return quantity;
    }

    private void ensureProductCanOrder(Product product, int quantity) {
        if (product == null || !"ON".equals(product.getStatus())) {
            throw BusinessException.badRequest("商品已下架，无法下单");
        }
        if (quantity > stockOf(product)) {
            throw BusinessException.badRequest(product.getName() + " 库存不足");
        }
    }

    private int stockOf(Product product) {
        return product.getStock() == null ? 0 : product.getStock();
    }

    private int salesOf(Product product) {
        return product.getSales() == null ? 0 : product.getSales();
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = OrderVO.from(order);
        List<OrderItemVO> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId()))
                .stream()
                .map(OrderItemVO::from)
                .toList();
        vo.setItems(items);
        return vo;
    }

    private String snapshot(Address address) {
        return address.getReceiverName() + " " + address.getReceiverPhone() + " " +
                address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();
    }

    private String generateOrderNo() {
        return "M" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private String generatePayNo() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
