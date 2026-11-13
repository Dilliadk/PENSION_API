package com.v1.pension.dto.pis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PisPensionerPersonInfo {
  private String full_name_np;
  private String full_name_en;
  private String district_code;
  private String vdc_code;
  private String district;
  private String vdc;
  private String fed_permanent_ward_no;
  private String pis_number;
  private String dob_bs;
  private String citizenship_number;
  private String awakash_hudako_age;
  private String grandfathername;
  private String fathername;
  private String position_sub_class_name_np;
  private String position_name;
  private String post_level_name;
  private String office_name;
  private String retired_date_bs;
  private String service_period;
  private Double basic_salary;
  private Integer grade_count;
  private Double grade_rate;
  private Double grade_amount_tech;
  private Double total_payable_amount;
  private String description;
  private String niyukti_reporting_date_bs;
  private String pension_number;
  private String p_g_s_all_name_np;
  private String post_group_name;
  private String bank_branch;
  private String book_no;
  private String pnsn_bank_id;
  private String pnsn_bank_code;
  private String pnsn_bank_name_en;
  private String pnsn_bank_name_np;
  private Long pnsn_bank_branch_id;
  private String pnsn_bank_branch_code;
  private String pnsn_bank_branch_name_en;
  private String pnsn_bank_branch_name_np;
  private String mobiles;
  private String email;
}
