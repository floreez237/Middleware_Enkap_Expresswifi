package com.maviance.middleware_enkap_expresswifi.model.request;

import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@With
@AllArgsConstructor
@Table(name = "requests")
public class MiddleWareRequestEntity {
    @Id
    @Column(name = "request_payment_id", nullable = false)
    private String requestPaymentId;
    @Column(name = "callback_url", nullable = false)
    private String expressWifiCallBackUrl;
    @Column(nullable = false)
    private String currency;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "order_date")
    private Date orderDate;
    @Column(nullable = false)
    private Double amount;
    @Enumerated(EnumType.STRING)
    private ExpressWifiStatus status;

    public void configureWithEWPaymentRequest(ExpressWifiPaymentRequest paymentRequest) {
        requestPaymentId = paymentRequest.getPaymentId();
        expressWifiCallBackUrl = paymentRequest.getCallbackUrl();
        currency = paymentRequest.getCurrency();
        phoneNumber = paymentRequest.getPhoneNumber();
        orderDate = new Date(paymentRequest.getTimestamp());
        amount = paymentRequest.getAmount();
        //by default
        status = ExpressWifiStatus.PENDING;
    }

}
