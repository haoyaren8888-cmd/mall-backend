package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.common.BusinessException;
import com.course.mall.common.SessionContext;
import com.course.mall.dto.AddressRequest;
import com.course.mall.entity.Address;
import com.course.mall.mapper.AddressMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<Address> list() {
        Long userId = SessionContext.requireUser().getId();
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getDefaultAddress)
                .orderByDesc(Address::getUpdatedAt));
    }

    public Address create(AddressRequest request) {
        Long userId = SessionContext.requireUser().getId();
        if (Boolean.TRUE.equals(request.getDefaultAddress()) || list().isEmpty()) {
            clearDefault(userId);
            request.setDefaultAddress(true);
        }
        Address address = new Address();
        address.setUserId(userId);
        fill(address, request);
        addressMapper.insert(address);
        return address;
    }

    public Address update(Long id, AddressRequest request) {
        Address address = requireOwnAddress(id);
        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            clearDefault(address.getUserId());
        }
        fill(address, request);
        addressMapper.updateById(address);
        return address;
    }

    public void delete(Long id) {
        requireOwnAddress(id);
        addressMapper.deleteById(id);
    }

    public void setDefault(Long id) {
        Address address = requireOwnAddress(id);
        clearDefault(address.getUserId());
        address.setDefaultAddress(true);
        addressMapper.updateById(address);
    }

    private Address requireOwnAddress(Long id) {
        Long userId = SessionContext.requireUser().getId();
        Address address = addressMapper.selectById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw BusinessException.notFound("收货地址不存在");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getDefaultAddress, true));
        for (Address address : addresses) {
            address.setDefaultAddress(false);
            addressMapper.updateById(address);
        }
    }

    private void fill(Address address, AddressRequest request) {
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setDefaultAddress(Boolean.TRUE.equals(request.getDefaultAddress()));
    }
}
