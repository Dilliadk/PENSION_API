package com.v1.pension.dto.forward_data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForwardDataDto {
  private Long forwardId;
  private int formTypeId;
  private long masterId;
  private int sourceStage;
  private int receiveStage;
  private long receiveUserId;
  private String note;
  private String actionDate;
}
