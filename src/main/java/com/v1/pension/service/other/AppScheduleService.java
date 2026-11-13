package com.v1.pension.service.other;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;

@Service
@RequiredArgsConstructor
public class AppScheduleService {
  private final VoucherTransactionMasterRepository transactionMasterRepository;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Scheduled(fixedRate = 60000 * 10) // 60000 (1Minute) * 10 = 10 Minutes
  public void updateDashboardPaymentView() {
    transactionMasterRepository.updateDashboardPaymentView();
  }
}
