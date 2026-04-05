package com.woorimarket.domain.store;

import com.woorimarket.domain.store.dto.StoreRequest;
import com.woorimarket.domain.store.dto.StoreResponse;
import com.woorimarket.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreResponse createOrUpdate(User user, StoreRequest request) {
        Store store = storeRepository.findByUserId(user.getId())
                .orElse(Store.builder().user(user).active(true).build());

        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setAddressDetail(request.getAddressDetail());
        store.setPhone(request.getPhone());
        store.setBusinessHours(request.getBusinessHours());
        store.setDescription(request.getDescription());
        store.setImageUrl(request.getImageUrl());

        store = storeRepository.save(store);
        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public StoreResponse getMyStore(Long userId) {
        Store store = storeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("매장 정보가 없습니다. 먼저 매장을 등록해주세요."));
        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getActiveStores() {
        return storeRepository.findByActiveTrue().stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
        return StoreResponse.from(store);
    }
}
