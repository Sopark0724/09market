package com.woorimarket.domain.payment;

import com.woorimarket.domain.order.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Mock 결제 처리
     * 실제 구현 시 PaymentGateway 인터페이스를 통해 PG사 연동
     */
    public Payment processPayment(Order order, String method) {
        log.info("[Mock Payment] 주문 #{} 결제 처리 중... 금액: {}원, 방식: {}",
                order.getId(), order.getTotalAmount(), method);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(Payment.PaymentMethod.valueOf(method))
                .status(Payment.PaymentStatus.COMPLETED)
                .transactionId("MOCK_TXN_" + UUID.randomUUID().toString().substring(0, 8))
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        log.info("[Mock Payment] 결제 완료! Transaction ID: {}", payment.getTransactionId());
        return payment;
    }
}
