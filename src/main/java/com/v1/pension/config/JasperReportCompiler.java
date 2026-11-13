package com.v1.pension.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class JasperReportCompiler implements ApplicationRunner {
  private static final Logger logger = LoggerFactory.getLogger(JasperReportCompiler.class);
  private static final String SOURCE_REPORTS_PATH = "src/main/resources/pension-reports/";
  private static final String BUILD_REPORTS_PATH = "build/resources/main/pension-reports/";

  @Override
  public void run(ApplicationArguments args) throws Exception {
    Path projectRoot = Paths.get(".").toAbsolutePath().normalize();
    Path sourceReportsAbsolutePath = projectRoot.resolve(SOURCE_REPORTS_PATH);
    Path buildReportsAbsolutePath = projectRoot.resolve(BUILD_REPORTS_PATH);

    try {
      Files.createDirectories(sourceReportsAbsolutePath);
      Files.createDirectories(buildReportsAbsolutePath);
    } catch (IOException e) {
      logger.error("Failed to create report directories. Skipping compilation.", e);
      return;
    }

    var resolver = new PathMatchingResourcePatternResolver();

    try {
      Resource[] resources = resolver.getResources("file:" + sourceReportsAbsolutePath.toString() + "/*.jrxml");

      for (Resource resource : resources) {
        File jrxmlFile = resource.getFile();
        String jrxmlFileName = jrxmlFile.getName();

        Path jasperSourcePath = sourceReportsAbsolutePath.resolve(jrxmlFileName.replace(".jrxml", ".jasper"));
        File jasperSourceFile = jasperSourcePath.toFile();

        boolean needsCompilation = false;
        if (!jasperSourceFile.exists()) {
          logger.info("-> Compiling '{}' because its .jasper file does not exist in source.", jrxmlFileName);
          needsCompilation = true;
        } else if (jrxmlFile.lastModified() > jasperSourceFile.lastModified()) {
          logger.info("-> Re-compiling '{}' because the JRXML source is newer.", jrxmlFileName);
          needsCompilation = true;
        }

        if (needsCompilation) {
          compileAndSaveJasperReport(jrxmlFile, jrxmlFileName, jasperSourcePath, buildReportsAbsolutePath,
              jasperSourceFile);
        }
      }
    } catch (IOException e) {
      logger.error("Error accessing Jasper Reports directory: {}", e.getMessage(), e);
    }
  }

  private void compileAndSaveJasperReport(File jrxmlFile, String jrxmlFileName, Path jasperSourcePath,
      Path buildReportsAbsolutePath, File jasperSourceFile) {
    try {
      // 1. save to SRC directory
      JasperCompileManager.compileReportToFile(jrxmlFile.getAbsolutePath(), jasperSourcePath.toString());
      logger.info("   Successfully compiled '{}' to source directory.", jrxmlFileName);

      // 2. Also copy the new .jasper file to the BUILD directory
      Path jasperBuildPath = buildReportsAbsolutePath.resolve(jasperSourceFile.getName());
      Files.copy(jasperSourcePath, jasperBuildPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (JRException | IOException e) {
      logger.error("Failed to compile or copy report: " + jrxmlFileName, e);
    }
  }
}
