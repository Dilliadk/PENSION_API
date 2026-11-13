package com.v1.pension.controller.other;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v1.pension.dto.other.ReportDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.IncomeCertificateEntity;
import com.v1.pension.repository.pensioner.IncomeCertificateRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.service.other.ReportService;
import com.v1.pension.service.pensioner.PensionerRegistrationService;
import com.v1.pension.utils.JasperReportExporter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
  private final ReportService reportService;
  private final PensionerRegistrationService pensionerRegistrationService;
  private final VoucherTransactionMasterRepository voucherTransactionMasterRepository;
  private final IncomeCertificateRepository incomeCertificateRepository;

  @PostMapping("/report")
  public void getReport(HttpServletResponse response, @RequestBody ReportDto dto) throws JRException, IOException {
    var jasperPrint = reportService.getReport(dto);
    sendReport(jasperPrint, response, dto);
  }

  @PostMapping("/income-certificate")
  public ResponseEntity<Map<String, Object>> getIncomeCertificate(@RequestBody ReportDto dto) throws JRException {
    Map<String, Object> output = new HashMap<>();
    var version = switch (dto.getReportName()) {
      case "income-certificate-nepali" -> "Nepali";
      case "income-certificate-english" -> "English";
      default -> "None";
    };
    var entityOpt = incomeCertificateRepository.findCertificate(dto.getPensionSubPNo(), dto.getFy(), version,
        dto.getCurrencyCode());
    if (entityOpt.isPresent() && entityOpt.get().getData() != null) {
      output.put("base64String", entityOpt.get().getData());
      output.put("type", "old");
      output.put("id", entityOpt.get().getId());
      return ResponseEntity.ok(output);
    }

    var entity = new IncomeCertificateEntity();
    entity.setPensionSubPNo(dto.getPensionSubPNo());
    entity.setFiscalYear(dto.getFy());
    entity.setVersion(version);
    entity.setCurrencyCode(dto.getCurrencyCode());
    incomeCertificateRepository.save(entity);

    dto.setRecordId(entity.getId());
    var jasperPrint = reportService.getReport(dto);
    var base64String = JasperReportExporter.generateBase64Pdf(jasperPrint);
    var coordinates = JasperReportExporter.getCoordinatesForDigitalSignature(jasperPrint);

    entity.setData(base64String);
    incomeCertificateRepository.save(entity);

    output.put("base64String", base64String);
    output.put("type", "new");
    output.put("id", entity.getId());
    output.put("coordinates", coordinates);

    return ResponseEntity.ok(output);
  }

  @PostMapping("/pdf-base64-generation")
  public ResponseEntity<Map<String, Object>> generatePdfBase64String(@RequestBody ReportDto dto) throws JRException {
    var jasperPrint = reportService.getReport(dto);
    var base64String = JasperReportExporter.generateBase64Pdf(jasperPrint);
    var coordinates = JasperReportExporter.getCoordinatesForDigitalSignature(jasperPrint);

    var output = Map.of("base64String", base64String, "coordinates", coordinates);
    return ResponseEntity.ok(output);
  }

  @PostMapping("/image-base64-generation")
  public ResponseEntity<Map<String, String>> generateImageBase64String(@RequestBody ReportDto dto) throws JRException {
    var jasperPrint = reportService.getReport(dto);
    var base64String = JasperReportExporter.generateBase64Image(jasperPrint, 0);

    var output = Map.of("base64String", base64String);
    return ResponseEntity.ok(output);
  }

  @PostMapping("/letter-print/base64")
  public ResponseEntity<Map<String, Object>> getBase64LetterPrint(@RequestBody ReportDto dto) throws JRException {
    pensionerRegistrationService.hasPermissionOverPensioner(dto.getPensionSubPNo());

    // if recordId is not present, get the latest one
    if (dto.getRecordId() == null) {
      var voucherIdOpt = voucherTransactionMasterRepository
          .getLatestAdjustmentVoucherIdForPensioner(dto.getPensionSubPNo());
      if (voucherIdOpt.isEmpty()) {
        throw new BadRequestException("Adjustment Voucher Not Found");
      }
      dto.setRecordId(voucherIdOpt.get());
    }

    var vtOpt = voucherTransactionMasterRepository.findById(dto.getRecordId());
    if (vtOpt.isEmpty()) {
      throw new ResourceNotFoundException("Voucher Transaction Not Found");
    }

    // setting reportName
    var calculationTypeCode = vtOpt.get().getCalculationTypeCode();
    var reportName = switch (calculationTypeCode) {
      case "1", "4", "5" -> "account-open-letter";
      case "2" -> "account-adjustment-letter";
      case "8", "9" -> "account-close-letter";
      default -> "none";
    };

    if ("none".equals(reportName)) {
      throw new BadRequestException("Letter Print Not Available");
    } else {
      dto.setReportName(reportName);
      dto.setCalculationTypeCode(calculationTypeCode);
      var jasperPrint = reportService.getReport(dto);
      var base64String = JasperReportExporter.generateBase64Pdf(jasperPrint);
      var coordinates = JasperReportExporter.getCoordinatesForDigitalSignature(jasperPrint);

      var output = Map.of("base64String", base64String, "coordinates", coordinates);
      return ResponseEntity.ok(output);
    }
  }

  @PostMapping("/letter-print")
  public void getLetterPrintReport(HttpServletResponse response, @RequestBody ReportDto dto)
      throws JRException, IOException {
    pensionerRegistrationService.hasPermissionOverPensioner(dto.getPensionSubPNo());

    // if recordId is not present, get the latest one
    if (dto.getRecordId() == null) {
      var voucherIdOpt = voucherTransactionMasterRepository
          .getLatestAdjustmentVoucherIdForPensioner(dto.getPensionSubPNo());
      if (voucherIdOpt.isEmpty()) {
        sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Adjustment Voucher Not Found");
        return;
      }
      dto.setRecordId(voucherIdOpt.get());
    }

    var vtOpt = voucherTransactionMasterRepository.findById(dto.getRecordId());
    if (vtOpt.isEmpty()) {
      sendErrorResponse(response, HttpStatus.NOT_FOUND, "Voucher Transaction Not Found");
      return;
    }

    // setting reportName
    var calculationTypeCode = vtOpt.get().getCalculationTypeCode();
    var reportName = switch (calculationTypeCode) {
      case "1", "4", "5" -> "account-open-letter";
      case "2" -> "account-adjustment-letter";
      case "8", "9" -> "account-close-letter";
      default -> "none";
    };

    if ("none".equals(reportName)) {
      sendErrorResponse(response, HttpStatus.BAD_REQUEST, "Letter Print Not Available");
    } else {
      dto.setReportName(reportName);
      dto.setCalculationTypeCode(calculationTypeCode);
      var jasperPrint = reportService.getReport(dto);
      sendReport(jasperPrint, response, dto);
    }
  }

  private void sendReport(JasperPrint jasperPrint, HttpServletResponse response, ReportDto dto)
      throws JRException, IOException {
    if (Objects.equals(dto.getReportFormat(), "docx")) {
      JasperReportExporter.exportToDocx(jasperPrint, response.getOutputStream());
    } else if (Objects.equals(dto.getReportFormat(), "xlsx")) {
      JasperReportExporter.exportToExcel(jasperPrint, response.getOutputStream());
    } else {
      JasperReportExporter.exportToPdf(jasperPrint, response.getOutputStream());
    }
  }

  private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    var objectMapper = new ObjectMapper();

    ObjectNode errorResponse = objectMapper.createObjectNode();
    errorResponse.put("timestamp", LocalDateTime.now().toString());
    errorResponse.put("status", status.value());
    errorResponse.put("error", status.getReasonPhrase());
    errorResponse.put("message", message);

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
