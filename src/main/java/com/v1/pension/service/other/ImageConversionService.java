package com.v1.pension.service.other;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageConversionService {
  public byte[] convertJp2ToJpeg(byte[] jp2Data) {
    try {
      // 1. Read the byte array into a BufferedImage
      // Thanks to the TwelveMonkeys dependency, ImageIO can now understand JP2
      ByteArrayInputStream inputStream = new ByteArrayInputStream(jp2Data);
      BufferedImage inputImage = ImageIO.read(inputStream);

      if (inputImage == null) {
        throw new IOException("Unable to decode JP2 image data. The format might be unsupported or corrupted.");
      }

      // 2. Handle Transparency (Alpha Channel)
      // JP2 supports transparency, but JPEG does not. If we don't handle this,
      // the transparent parts might turn black or look corrupted.
      // We create a new image with a standard RGB structure and a white background.
      BufferedImage outputImage = new BufferedImage(
          inputImage.getWidth(),
          inputImage.getHeight(),
          BufferedImage.TYPE_INT_RGB
      );

      // Paint the original image onto the new white background
      outputImage.createGraphics().drawImage(inputImage, 0, 0, Color.WHITE, null);

      // 3. Write the image back to a byte array as JPEG
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // "jpg" is the format name used by ImageIO
      boolean canWrite = ImageIO.write(outputImage, "jpg", outputStream);

      if (!canWrite) {
        throw new IOException("No appropriate writer found for JPEG format.");
      }

      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Cannot convert image data");
    }
  }
}
