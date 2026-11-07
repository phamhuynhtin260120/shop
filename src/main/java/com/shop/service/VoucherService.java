package com.shop.service;

import com.shop.dto.VoucherForm;
import com.shop.entity.Voucher;
import com.shop.exception.BusinessException;
import com.shop.repository.VoucherRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    /** Trả về voucher nếu dùng được cho đơn có tạm tính = subtotal, ngược lại ném lỗi kèm lý do. */
    public Voucher findUsable(String code, BigDecimal subtotal) {
        Voucher v = voucherRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException("Mã giảm giá \"" + code.trim() + "\" không tồn tại"));
        if (v.isExpired(LocalDate.now())) {
            throw new BusinessException("Mã " + v.getCode() + " đã hết hạn");
        }
        if (v.isUsedUp()) {
            throw new BusinessException("Mã " + v.getCode() + " đã hết lượt sử dụng");
        }
        if (subtotal.compareTo(v.getMinOrder()) < 0) {
            throw new BusinessException("Mã " + v.getCode() + " áp dụng cho đơn từ "
                    + String.format("%,d", v.getMinOrder().longValue()).replace(',', '.') + "đ");
        }
        return v;
    }

    // ---------- Quản trị ----------

    public List<Voucher> findAll() {
        return voucherRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Voucher get(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy voucher id = " + id));
    }

    @Transactional
    public Voucher save(VoucherForm f) {
        String code = f.getCode().trim().toUpperCase();
        voucherRepository.findByCodeIgnoreCase(code)
                .filter(other -> !other.getId().equals(f.getId()))
                .ifPresent(other -> { throw new BusinessException("Mã " + code + " đã tồn tại"); });
        if (Voucher.PERCENT.equals(f.getType()) && f.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Voucher phần trăm không được vượt quá 100%");
        }
        if (f.getId() == null) {
            return voucherRepository.save(new Voucher(code, f.getType(), f.getValue(), f.getMinOrder(),
                    f.getExpiresAt(), f.getUsageLimit()));
        }
        Voucher v = get(f.getId());
        v.update(code, f.getType(), f.getValue(), f.getMinOrder(), f.getExpiresAt(), f.getUsageLimit());
        return v;
    }
}
