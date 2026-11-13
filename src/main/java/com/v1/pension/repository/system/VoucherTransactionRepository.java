package com.v1.pension.repository.system;

import com.v1.pension.model.pensioner.VoucherTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherTransactionRepository extends JpaRepository<VoucherTransactionDetail, Long> {
}