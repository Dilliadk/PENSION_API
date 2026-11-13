//
//
//
//package com.v1.pension.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
//@Data
//@MappedSuperclass
//@EntityListeners(AuditingEntityListener.class)
//public abstract class BaseModel {
//
//    @CreatedDate
//    @Column(name = "created_date", updatable = false)
//    private LocalDateTime createdDate;
//
//    @LastModifiedDate
//    @Column(name = "modified_date")
//    private LocalDateTime modifiedDate;
//
//    @Column(name = "created_by")
//    private String createdBy;
//
//    @Column(name = "modified_by")
//    private String modifiedBy;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//    @PrePersist
//    protected void onCreate() {
//        if (createdDate == null) {
//            createdDate = LocalDateTime.now();
//        }
//        if (isActive == null) {
//            isActive = true;
//        }
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        modifiedDate = LocalDateTime.now();
//    }
//
//    public Boolean getIsActive() {
//        return isActive != null ? isActive : true;
//    }
//
//    // Your BaseModel should have these methods
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public String getCreatedBy() { return createdBy; }
//    public String getUpdatedBy() { return updatedBy; }
//
//}

package com.v1.pension.model;

import com.v1.pension.utils.CurrentUser;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseModel {
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private LocalDateTime createdDate;

  @Column(name = "modified_by")
  private String modifiedBy;

  @UpdateTimestamp
  @Column(name = "modified_date")
  private LocalDateTime modifiedDate;

  @PrePersist
  protected void onCreate() {
    this.createdBy = CurrentUser.getUserName();
  }

  @PreUpdate
  protected void onUpdate() {
    this.modifiedBy = CurrentUser.getUserName();
  }
}

