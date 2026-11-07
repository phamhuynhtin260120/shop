package com.shop.pattern;

import com.shop.entity.Order;
import com.shop.pattern.creational.factory.CodCreator;
import com.shop.pattern.creational.factory.MomoCreator;
import com.shop.pattern.creational.factory.PaymentCreator;
import com.shop.pattern.creational.factory.VnPayCreator;
import com.shop.pattern.structural.adapter.CodPayment;
import com.shop.pattern.structural.adapter.MomoClient;
import com.shop.pattern.structural.adapter.PaymentResult;
import com.shop.pattern.structural.adapter.RefundablePayment;
import com.shop.pattern.structural.adapter.VnPayAdapter;
import com.shop.pattern.structural.adapter.VnPayClient;
import com.shop.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Factory Method + Adapter: thanh toán")
class FactoryMethodAdapterTest {

    @Test
    @DisplayName("TC01: chọn VNPay -> VnPayCreator tạo VnPayAdapter, gọi VnPayClient, thanh toán thành công")
    void tc01_vnpayCreatorUsesVnPayAdapter() {
        Order order = TestData.order(TestData.product(1, "450000", 10), 1, "VNPAY");
        PaymentCreator creator = new VnPayCreator(new VnPayClient());

        PaymentResult result = creator.processPayment(order);

        assertTrue(result.success());
        assertTrue(result.transactionRef().startsWith("VNP"));
    }

    @Test
    @DisplayName("TC01b: mọi Creator trả về kết quả cùng kiểu PaymentResult (thay thế được cho nhau - LSP)")
    void allCreatorsAreInterchangeable() {
        Order order = TestData.order(TestData.product(1, "200000", 10), 1, "COD");
        List<PaymentCreator> creators = List.of(
                new CodCreator(), new VnPayCreator(new VnPayClient()), new MomoCreator(new MomoClient()));

        for (PaymentCreator creator : creators) {
            assertTrue(creator.processPayment(order).success(), creator.code() + " phải thanh toán thành công");
        }
    }

    @Test
    @DisplayName("Adapter MoMo chuyển mã lỗi riêng của MoMo thành PaymentResult thất bại")
    void momoAdapterMapsFailure() {
        Order order = TestData.order(TestData.product(1, "25000000", 10), 1, "MOMO");

        PaymentResult result = new MomoCreator(new MomoClient()).processPayment(order);

        assertFalse(result.success());
        assertTrue(result.message().contains("hạn mức"));
    }

    @Test
    @DisplayName("ISP: VNPay hoàn tiền được, COD không bị ép cài đặt refund()")
    void ispRefundOnlyForOnlinePayments() {
        assertTrue(new VnPayAdapter(new VnPayClient()) instanceof RefundablePayment);
        assertFalse(RefundablePayment.class.isAssignableFrom(CodPayment.class));
    }
}
