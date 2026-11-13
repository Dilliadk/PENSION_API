package com.v1.pension.dto.forward_data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApproveDataDto {
  private long forwardId;
  private String actionDate;
}
