package com.v1.pension.repository.auth;

import com.v1.pension.model.auth.OtpVerification;
import com.v1.pension.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
  @Modifying
  @Query(nativeQuery = true, value = """
      update otp_verifications set status = 0
      where user_id = :userId and status = 1
      """)
  void disableActiveOtp(@Param("userId") long userId);

  @Query(nativeQuery = true, value = """
      select * from otp_verifications
      where user_id = :userId
      	and status = 1
      	and device_fingerprint = :deviceFp
      	and otp_type = :otpType
      order by created_date desc
      limit 1
      """)
  Optional<OtpVerification> getLatestOtpForUser(
      @Param("userId") long userId, @Param("deviceFp") String deviceFp,
      @Param("otpType") String otpType);

  @Query("SELECT o FROM OtpVerification o WHERE o.user = :user AND o.deviceFingerprint = :deviceFingerprint AND o.otpType = :otpType AND o.verified = false AND o.expiryDate > :currentTime ORDER BY o.createdDate DESC")
  List<OtpVerification> findAllValidOtpsForDeviceAndType(@Param("user") User user,
                                                         @Param("deviceFingerprint") String deviceFingerprint,
                                                         @Param("otpType") String otpType,
                                                         @Param("currentTime") LocalDateTime currentTime);

}