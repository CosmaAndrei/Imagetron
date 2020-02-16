package com.cv.imagetron.service.impl;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
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
        BufferedImage buffered = ImageIO.read(image);
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

    /**
     * Example of how to "remove" noise from images using wavelet based algorithms.  A simplified interface is used
     * which hides most of the complexity.  Wavelet image processing is still under development and only floating point
     * images are currently supported.  Which is why the image  type is hard coded.
     */
    @Override
    public File removeNoiseFrom(File image) throws IOException {
        // load the input image, declare data structures
        GrayF32 input = UtilImageIO.loadImage(image.getAbsolutePath(), GrayF32.class);

        GrayF32 denoised = input.createSameShape();

        // How many levels in wavelet transform
        int numLevels = 4;
        // Create the noise removal algorithm
        WaveletDenoiseFilter<GrayF32> denoiser =
                FactoryImageDenoise.waveletBayes(GrayF32.class, numLevels, 0, 255);

        // remove noise from the image
        denoiser.process(input, denoised);

        // display the results
        BufferedImage denoisedImage = ConvertBufferedImage.convertTo(denoised, null);

        File outputFile = new File("denoisedImage.png");
        ImageIO.write(denoisedImage, "png", outputFile);

        return outputFile;
    }

    @Override
    public File convertToFishEye(File image) {
        return null;
    }
}
