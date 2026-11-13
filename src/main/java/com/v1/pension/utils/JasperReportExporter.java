package com.v1.pension.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleDocxExporterConfiguration;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import jakarta.servlet.ServletOutputStream;

import com.v1.pension.exception.BadRequestException;

public class JasperReportExporter {
  public static String generateBase64Pdf(JasperPrint jasperPrint) {
    try {
      var bytes = JasperExportManager.exportReportToPdf(jasperPrint);
      return Base64.getEncoder().encodeToString(bytes);
    } catch (JRException e) {
      throw new BadRequestException("Failed to export jasper report to pdf bytes");
    }
  }

  public static String generateBase64Image(JasperPrint jasperPrint, int pageIndex) {
    try {
      // 1. Render the specific page to a BufferedImage. 
      // 2.0f is the zoom factor; higher numbers mean higher resolution/sharpness.
      BufferedImage bufferedImage = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, pageIndex, 2.0f);

      // 2. Convert the BufferedImage into a byte array
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        javax.imageio.ImageIO.write(bufferedImage, "png", baos);
        byte[] bytes = baos.toByteArray();

        // 3. Encode the bytes to a Base64 string
        return Base64.getEncoder().encodeToString(bytes);
      }
    } catch (JRException | IOException e) {
      throw new BadRequestException("Failed to export jasper report to image base64");
    }
  }

  public static List<Integer> getCoordinatesForDigitalSignature(JasperPrint jasperPrint) {
    var lastPage = jasperPrint.getPages().get(jasperPrint.getPages().size() - 1);
    List<JRPrintElement> elements = lastPage.getElements();

    for (JRPrintElement element : elements) {
      if ("digitalSignature".equals(element.getKey())) {
        var coordinates = new ArrayList<Integer>();
        var c1 = element.getX();
        var c4 = jasperPrint.getPageHeight() - element.getY();
        var c2 = c4 - element.getHeight();
        var c3 = c1 + element.getWidth();

        coordinates.add(c1);
        coordinates.add(c2);
        coordinates.add(c3);
        coordinates.add(c4);
        return coordinates;
      }
    }

    return List.of(0, 0, 0, 0);
  }

  public static void exportToPdf(JasperPrint jasperPrint, ServletOutputStream outputStream)
      throws IOException, JRException {
    try {
      JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
    } finally {
      if (outputStream != null) {
        outputStream.flush();
        outputStream.close();
      }
    }
  }

  public static void exportToHtml(JasperPrint jasperPrint, ServletOutputStream outputStream)
      throws IOException, JRException {
    try {
      SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
      SimpleHtmlExporterOutput exporterOutput = new SimpleHtmlExporterOutput(outputStream);

      HtmlExporter htmlExporter = new HtmlExporter();
      htmlExporter.setExporterInput(exporterInput);
      htmlExporter.setExporterOutput(exporterOutput);
      htmlExporter.exportReport();
    } finally {
      if (outputStream != null) {
        outputStream.flush();
        outputStream.close();
      }
    }
  }

  public static void exportToExcel(JasperPrint jasperPrint, ServletOutputStream outputStream)
      throws IOException, JRException {
    SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
    SimpleOutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(outputStream);

    SimpleXlsxReportConfiguration reportConfig = new SimpleXlsxReportConfiguration();
    SimpleXlsxExporterConfiguration exportConfig = new SimpleXlsxExporterConfiguration();

    JRXlsxExporter xlsExporter = new JRXlsxExporter();
    xlsExporter.setExporterInput(exporterInput);
    xlsExporter.setExporterOutput(exporterOutput);
    xlsExporter.setConfiguration(reportConfig);
    xlsExporter.setConfiguration(exportConfig);

    xlsExporter.exportReport();
  }

  public static void exportToDocx(JasperPrint jasperPrint, ServletOutputStream outputStream)
      throws IOException, JRException {
    SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
    SimpleOutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(outputStream);

    SimpleDocxReportConfiguration reportConfig = new SimpleDocxReportConfiguration();
    SimpleDocxExporterConfiguration exportConfig = new SimpleDocxExporterConfiguration();

    JRDocxExporter docxExporter = new JRDocxExporter();
    docxExporter.setExporterInput(exporterInput);
    docxExporter.setExporterOutput(exporterOutput);
    docxExporter.setConfiguration(reportConfig);
    docxExporter.setConfiguration(exportConfig);

    docxExporter.exportReport();
  }
}
