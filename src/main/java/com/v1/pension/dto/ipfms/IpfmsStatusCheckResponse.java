package com.v1.pension.dto.ipfms;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpfmsStatusCheckResponse {
  private boolean success;
  private String message;
  private Integer currentPage;
  private Integer totalPages;
  private Integer totalRecords;
  private Integer pageSize;
  private List<IpfmsStatusCheckRow> rows;
}
