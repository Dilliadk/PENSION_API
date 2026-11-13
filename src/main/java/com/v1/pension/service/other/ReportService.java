package com.v1.pension.service.other;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.other.ReportDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.GeneralServerException;
import com.v1.pension.repository.report.AdjustmentVoucherReportRepository;
import com.v1.pension.repository.report.AnusuchiReportRepository;
import com.v1.pension.repository.report.LegacyDataRepository;
import com.v1.pension.repository.report.LiabilityStatementReportRepository;
import com.v1.pension.repository.report.MonthlyExpenseSummaryReportRepository;
import com.v1.pension.repository.report.MonthlyVoucherReportRepository;
import com.v1.pension.repository.report.OtherReportRepository;
import com.v1.pension.repository.report.PaymentVoucherReportRepository;
import com.v1.pension.repository.report.PensionStatementReportRepository;
import com.v1.pension.repository.report.PensionerReportRepository;
import com.v1.pension.repository.report.ReportHeaderInfoRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class ReportService {
  private final ReportHeaderInfoRepository reportHeaderInfoRepository;
  private final PensionStatementReportRepository pensionStatementReportRepository;
  private final LiabilityStatementReportRepository liabilityStatementReportRepository;
  private final AnusuchiReportRepository anusuchiReportRepository;
  private final AdjustmentVoucherReportRepository adjustmentVoucherReportRepository;
  private final PensionerReportRepository pensionerReportRepository;
  private final MonthlyVoucherReportRepository monthlyVoucherReportRepository;
  private final PaymentVoucherReportRepository paymentVoucherReportRepository;
  private final OtherReportRepository otherReportRepository;
  private final LegacyDataRepository legacyDataRepository;
  private final MonthlyExpenseSummaryReportRepository monthlyExpenseSummaryReportRepository;

  public JasperPrint getReport(ReportDto dto) throws JRException {
    JasperReport jasperReport;
    var reportName = dto.getReportName();

    try {
      var jasperRes = new ClassPathResource("pension-reports/" + reportName + ".jasper");

      try (InputStream is = jasperRes.getInputStream()) {
        jasperReport = (JasperReport) JRLoader.loadObject(is);
      }
    } catch (IOException | JRException e) {
      throw new BadRequestException("Failed to load Jasper report: " + reportName);
    }

    var headerData = reportHeaderInfoRepository.getReportHeaderInfo();
    String reportTitle = "";
    Map<String, Object> masterData = new HashMap<>();
    Map<String, Object> footerData = new HashMap<>();
    Map<String, Object> parameters = new HashMap<>();
    Collection<Map<String, Object>> detailsData = new ArrayList<>();

    switch (reportName) {
      case "pension-statement-report" -> {
        reportTitle = "Pensioner Statement Report";
        var eligibilityData = pensionStatementReportRepository.getEligibilityData(dto.getPensionSubPNo(),
            CurrentUser.getFiscalYear(), dto.getDateNep());
        masterData = pensionStatementReportRepository.getMasterData(dto.getPensionSubPNo());
        detailsData = pensionStatementReportRepository.getDetailsData(dto.getPensionSubPNo(), dto.getDateFrom(),
            dto.getDateTo());
        parameters.putAll(eligibilityData);
      }

      case "liability-statement-report" -> {
        reportTitle = "Liability Statement Report";
        detailsData = liabilityStatementReportRepository.getDetailsData(dto.getLiabilityType());
      }

      case "adjustment-voucher-preview" -> {
        reportTitle = "";
        masterData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewMasterData(dto.getRecordId());
        detailsData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewDetailData(dto.getRecordId());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
      }

      case "monthly-voucher-preview" -> {
        masterData = monthlyVoucherReportRepository.getMasterData(dto.getRecordId());
        detailsData = monthlyVoucherReportRepository.getDetailsData(dto.getRecordId());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
      }

      case "payment-voucher-preview" -> {
        masterData = paymentVoucherReportRepository.getMasterData(dto.getRecordId());
        detailsData = paymentVoucherReportRepository.getDetailsData(dto.getRecordId());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
      }

      case "account-open-letter" -> {
        reportTitle = "अनुसूची -१";
        masterData = anusuchiReportRepository.getAnusuchi1MasterData(dto.getRecordId(), dto.getCalculationTypeCode());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
        parameters.put("bodartha", dto.getBodartha());
        parameters.put("note", dto.getNote());
      }

      case "account-close-letter" -> {
        reportTitle = "खाता बन्द";
        masterData = anusuchiReportRepository.getAccountCloseMasterData(dto.getRecordId());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
        parameters.put("bodartha", dto.getBodartha());
        parameters.put("note", dto.getNote());
      }

      case "account-adjustment-letter" -> {
        reportTitle = "अनुसूची -४";
        masterData = anusuchiReportRepository.getAnusuchi4MasterData(dto.getRecordId());
        footerData = adjustmentVoucherReportRepository.getAdjustmentVoucherPreviewFooterData(dto.getRecordId());
        parameters.put("bodartha", dto.getBodartha());
        parameters.put("note", dto.getNote());
      }

      case "category-wise-pensioners" -> {
        reportTitle = "Category Wise Pensioners";
        masterData = pensionerReportRepository.getCategoryName(dto.getCategoryCode());
        detailsData = pensionerReportRepository.getPensionersByCategory(dto.getCategoryCode());
      }

      case "bank-branch-wise-pensioner-report" -> {
        reportTitle = "निवृत्तभरकको संख्या बैंक शाखा अनुसार";
        detailsData = pensionerReportRepository.getBankBranchWisePensionerReport(dto.getMainBankId(),
            dto.getBankBranchCode());
      }

      case "pensioners-count-summary-by-type" -> {
        reportTitle = "निवृत्तभरकको संख्या";
        detailsData = pensionerReportRepository.getTypeWiseCountSummary(dto.getProvinceCode(), dto.getDistrictCode());
      }

      case "expense-assumption-report" -> {
        reportTitle = "अनुमानित खर्च";
        detailsData = pensionerReportRepository.getExpenseAssumptionDetails();
      }

      case "daily-task-summary" -> {
        reportTitle = "निवृत्तभरणको दैनिक कार्य विवरण";
        parameters.put("dateFrom", dto.getDateFrom());
        parameters.put("dateTo", dto.getDateTo());
        detailsData = otherReportRepository.getDailyTaskSummary(CurrentUser.getOfficeCode(), dto);
      }

      case "daily-task-details" -> {
        reportTitle = "कामको बिस्तृत विवरण";
        parameters.put("dateFrom", dto.getDateFrom());
        parameters.put("dateTo", dto.getDateTo());
        detailsData = otherReportRepository.getDailyTaskDetails(CurrentUser.getOfficeCode(), dto);
      }

      case "income-certificate-nepali" -> {
        parameters.put("id", dto.getRecordId());
        parameters.put("fy", dto.getFy());
        parameters.put("is_current_fy", dto.getFy().equals(CurrentUser.getFiscalYear()));
        masterData = pensionerReportRepository.getNepaliIncomeCertificateData(dto);
      }

      case "income-certificate-english" -> {
        parameters.put("id", dto.getRecordId());
        parameters.put("fy", dto.getFy());
        parameters.put("is_current_fy", dto.getFy().equals(CurrentUser.getFiscalYear()));
        masterData = pensionerReportRepository.getEnglishIncomeCertificateData(dto);
      }

      case "tameli-count-summary" -> {
        reportTitle = "लगत कट्टा(तामेली)को विवरण";
        detailsData = pensionerReportRepository.getTameliCountSummaryDetails(CurrentUser.getFiscalYear());
      }

      case "pensioner-search-report" -> {
        if (dto.getSearchLabel1().equals("name"))
          dto.setSearchValue1("%" + dto.getSearchValue1() + "%");

        reportTitle = "Pensioner Search";
        detailsData = pensionerReportRepository.getPensionerSearch(
            dto.getSearchLabel1(), dto.getSearchValue1(), dto.getSearchLabel2(), dto.getSearchValue2());
      }

      case "manachamal-details" -> {
        reportTitle = "मानाचामल खर्चको विस्तृत विवरण";
        parameters.put("fy", dto.getFy());
        detailsData = otherReportRepository.getManachamalDetails(dto.getFy());
      }

      case "manachamal-summary" -> {
        reportTitle = "मानाचामल खर्चको विवरण खर्च समेत";
        detailsData = otherReportRepository.getManachamalSummary();
      }

      case "digital-signature" -> {
        reportTitle = "";
        masterData = reportHeaderInfoRepository.getDigitalSignatureInfoNepali();
      }

      case "legacy-bank-upload-report" -> {
        reportTitle = "पुरानो‍ भुक्तानी प्रतिवेदन";
        parameters.put("pension_subpno", dto.getPensionSubPNo());
        detailsData = legacyDataRepository.getBankUploadData(dto.getPensionSubPNo());
      }

      case "legacy-pentrans-report" -> {
        reportTitle = "पुरानो‍ स्टेटमेन्ट";
        parameters.put("pension_subpno", dto.getPensionSubPNo());
        detailsData = legacyDataRepository.getLegacyStatementData(dto.getPensionSubPNo());
      }

      case "monthly-expense-summary" -> {
        parameters.put("fiscal_year", dto.getFy());
        detailsData = monthlyExpenseSummaryReportRepository.getSummaryData(dto.getFy());
      }

      default -> throw new BadRequestException("Report with name " + reportName + " doesn't exist");
    }

    addReportHeadingParameters(parameters, headerData);
    parameters.put("reportName", reportTitle);
    parameters.putAll(masterData);
    if (!footerData.isEmpty())
      parameters.putAll(footerData);

    try {
      var detailsDataSource = new JRBeanCollectionDataSource(detailsData);
      return JasperFillManager.fillReport(jasperReport, parameters, detailsDataSource);
    } catch (JRException e) {
      throw new GeneralServerException(e.getMessage());
    }
  }

  private void addReportHeadingParameters(Map<String, Object> parameters, Map<String, Object> headingInfo) {
    var govLogoResource = new ClassPathResource("pension-reports/govlogo.png");
    var govLogo = govLogoResource.getPath();

    parameters.put("govLogo", govLogo);
    parameters.putAll(headingInfo);
  }
}
