package com.v1.pension.repository.auth;

import com.v1.pension.model.auth.UserDevice;
import com.v1.pension.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
//  @Query(nativeQuery = true, value = """
//      select *
//      from user_devices
//      where user_id = :userId
//      and device_fingerprint = :fingerprint
//      and is_trusted = true
//      """)
//  Optional<UserDevice> findTrustedUserDeviceByFingerprint(
//      @Param("userId") long userId, @Param("fingerprint") String fingerprint);

  @Query(nativeQuery = true, value = """
      select *
      from user_devices
      where user_id = :userId
      and device_fingerprint = :fingerprint
      """)
  Optional<UserDevice> findUserDeviceByFingerprint(
      @Param("userId") long userId, @Param("fingerprint") String fingerprint);

  Optional<UserDevice> findByUserAndDeviceFingerprint(User user, String deviceFingerprint);
}
