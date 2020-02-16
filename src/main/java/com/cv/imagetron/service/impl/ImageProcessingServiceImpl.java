package com.cv.imagetron.service.impl;

import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import com.cv.imagetron.service.def.ImageProcessingService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessingServiceImpl implements ImageProcessingService {
    @Override
    public File blurImage(File image) throws IOException {
        BufferedImage buffered = UtilImageIO.loadImage(UtilIO.pathExample("standard/kodim17.jpg"));
        Planar<GrayU8> input = ConvertBufferedImage.convertFrom(buffered, true, ImageType.pl(3, GrayU8.class));
        Planar<GrayU8> blurred = input.createSameShape();
        // size of the blur kernel. square region with a width of radius*2 + 1
        int radius = 8;
        // Apply gaussian blur using a procedural interface
        GBlurImageOps.gaussian(input, blurred, -1, radius, null);
        BufferedImage blurredImage = ConvertBufferedImage.convertTo(blurred, null, true);

        File outputFile = new File("blurredImage.png");
        ImageIO.write(blurredImage, "png", outputFile);
        return outputFile;
    }

    @Override
    public File removeNoiseFrom(File image) {
        return null;
    }

    @Override
    public File convertToFishEye(File image) {
        return null;
    }
}
