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
    public void sendOrderNotification(User buyer, User seller, Order order) {
        // 구매자 알림
        Notification.NotificationChannel buyerChannel =
                buyer.getAuthProvider() == User.AuthProvider.KAKAO
                        ? Notification.NotificationChannel.KAKAO
                        : Notification.NotificationChannel.SMS;

        String buyerMessage = String.format("[우리마켓] %s에서 주문이 완료되었습니다. 주문번호: #%d, 금액: %s원",
                order.getStore().getStoreName(), order.getId(), order.getTotalAmount().toPlainString());

        sendNotification(buyer, Notification.NotificationType.ORDER_CONFIRMED, buyerChannel, buyerMessage);

        // 판매자 알림
        Notification.NotificationChannel sellerChannel =
                seller.getAuthProvider() == User.AuthProvider.KAKAO
                        ? Notification.NotificationChannel.KAKAO
                        : Notification.NotificationChannel.SMS;

        String sellerMessage = String.format("[우리마켓] 새 주문이 들어왔습니다! 주문번호: #%d, 주문자: %s, 금액: %s원",
                order.getId(), buyer.getName(), order.getTotalAmount().toPlainString());

        sendNotification(seller, Notification.NotificationType.NEW_ORDER, sellerChannel, sellerMessage);
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
