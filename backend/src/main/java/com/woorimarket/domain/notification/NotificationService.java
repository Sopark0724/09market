package com.woorimarket.domain.notification;

import com.woorimarket.domain.order.Order;
import com.woorimarket.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 주문 알림 발송 (Mock)
     * - 구매자에게 주문 확인 알림
     * - 판매자에게 새 주문 알림
     */
    public String sendOrderNotification(User buyer, User seller, Order order) {
        String storeName = order.getStore().getStoreName();
        String storePhone = order.getStore().getPhone();
        String storeAddress = order.getStore().getAddress();
        String storeAddressDetail = order.getStore().getAddressDetail() != null ? order.getStore().getAddressDetail() : "";
        String businessHours = order.getStore().getBusinessHours() != null ? order.getStore().getBusinessHours() : "";

        // 주문 상품 목록 문자열
        StringBuilder itemsText = new StringBuilder();
        for (var item : order.getItems()) {
            String productName = item.getProductInventory().getProduct().getName();
            String unit = item.getProductInventory().getProduct().getUnit();
            int qty = item.getQuantity();
            itemsText.append(String.format("- %s(%s), %d개\n", productName, unit, qty));
        }

        // 구매자 알림 (카카오톡 스타일)
        Notification.NotificationChannel buyerChannel =
                buyer.getAuthProvider() == User.AuthProvider.KAKAO
                        ? Notification.NotificationChannel.KAKAO
                        : Notification.NotificationChannel.SMS;

        String buyerMessage = String.format(
                "안녕하세요. %s 고객님.\n" +
                "%s 입니다.\n\n" +
                "주문서를 전달드립니다.\n\n" +
                "※ 주문서 정보\n" +
                "1. 수령일 : %s\n" +
                "2. 픽업 시간 : %s\n" +
                "3. 픽업 주소지 : %s %s\n" +
                "4. 주문 상품 : %s원\n" +
                "%s\n" +
                "※ 문의처 : %s, %s\n" +
                "주문 관련 문의는 담당자에게 연락 주세요.",
                buyer.getName(),
                storeName,
                order.getOrderDate().toString(),
                businessHours,
                storeAddress, storeAddressDetail,
                order.getTotalAmount().toPlainString(),
                itemsText.toString().trim(),
                storeName, storePhone
        );

        sendNotification(buyer, Notification.NotificationType.ORDER_CONFIRMED, buyerChannel, buyerMessage);

        // 판매자 알림
        Notification.NotificationChannel sellerChannel =
                seller.getAuthProvider() == User.AuthProvider.KAKAO
                        ? Notification.NotificationChannel.KAKAO
                        : Notification.NotificationChannel.SMS;

        String sellerMessage = String.format(
                "[우리마켓] 새 주문이 들어왔습니다!\n" +
                "주문번호: #%d\n" +
                "주문자: %s (%s)\n" +
                "수령일: %s\n" +
                "금액: %s원\n" +
                "%s",
                order.getId(), buyer.getName(), buyer.getPhone() != null ? buyer.getPhone() : "-",
                order.getOrderDate().toString(),
                order.getTotalAmount().toPlainString(),
                itemsText.toString().trim()
        );

        sendNotification(seller, Notification.NotificationType.NEW_ORDER, sellerChannel, sellerMessage);

        return buyerMessage;
    }

    private void sendNotification(User user, Notification.NotificationType type,
                                   Notification.NotificationChannel channel, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .message(message)
                .status(Notification.NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Mock: 실제 발송 대신 로그 출력
        if (channel == Notification.NotificationChannel.KAKAO) {
            log.info("[Mock Kakao] {} 에게 알림 발송: {}", user.getName(), message);
        } else {
            log.info("[Mock SMS] {} ({}) 에게 문자 발송: {}", user.getName(), user.getPhone(), message);
        }
    }
}
