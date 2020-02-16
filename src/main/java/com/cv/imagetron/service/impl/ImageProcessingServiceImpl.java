package com.cv.imagetron.service.impl;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.alg.distort.*;
import boofcv.alg.distort.pinhole.LensDistortionPinhole;
import boofcv.alg.distort.universal.LensDistortionUniversalOmni;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.factory.distort.FactoryDistort;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraUniversalOmni;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import com.cv.imagetron.service.def.ImageProcessingService;
import georegression.geometry.ConvertRotation3D_F32;
import georegression.struct.EulerType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
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
    public Pair<File, File> convertToFishEyeToPineHole(File image) throws IOException {
        // Path to image data and calibration data
        String fisheyePath = UtilIO.pathExample("fisheye/theta/");

        // load the fisheye camera parameters
        CameraUniversalOmni fisheyeModel = CalibrationIO.load(new File(fisheyePath, "front.yaml"));

        // Specify what the pinhole camera should look like
        CameraPinhole pinholeModel = new CameraPinhole(400, 400, 0, 300, 300, 600, 600);

        // Create the transform from pinhole to fisheye views
        LensDistortionNarrowFOV pinholeDistort = new LensDistortionPinhole(pinholeModel);
        LensDistortionWideFOV fisheyeDistort = new LensDistortionUniversalOmni(fisheyeModel);
        NarrowToWidePtoP_F32 transform = new NarrowToWidePtoP_F32(pinholeDistort, fisheyeDistort);

        // Load fisheye RGB image
        BufferedImage bufferedFisheye = ImageIO.read(image);
        Planar<GrayU8> fisheyeImage = ConvertBufferedImage.convertFrom(
                bufferedFisheye, true, ImageType.pl(3, GrayU8.class));

        // Create the image distorter which will render the image
        InterpolatePixel<Planar<GrayU8>> interp = FactoryInterpolation.
                createPixel(0, 255, InterpolationType.BILINEAR, BorderType.ZERO, fisheyeImage.getImageType());
        ImageDistort<Planar<GrayU8>, Planar<GrayU8>> distorter =
                FactoryDistort.distort(false, interp, fisheyeImage.getImageType());

        // Pass in the transform created above
        distorter.setModel(new PointToPixelTransform_F32(transform));

        // Render the image.  The camera will have a rotation of 0 and will thus be looking straight forward
        Planar<GrayU8> pinholeImage = fisheyeImage.createNew(pinholeModel.width, pinholeModel.height);

        distorter.apply(fisheyeImage, pinholeImage);
        BufferedImage bufferedPinhole0 = ConvertBufferedImage.convertTo(pinholeImage, null, true);

        // rotate the virtual pinhole camera to the right
        transform.setRotationWideToNarrow(ConvertRotation3D_F32.eulerToMatrix(EulerType.YXZ, 0.8f, 0, 0, null));

        distorter.apply(fisheyeImage, pinholeImage);
        BufferedImage bufferedPinhole1 = ConvertBufferedImage.convertTo(pinholeImage, null, true);


        File left = new File("fishEyeImage.png");
        ImageIO.write(bufferedPinhole0, "png", left);

        File right = new File("fishEyeImage.png");
        ImageIO.write(bufferedPinhole1, "png", right);

        return Pair.of(left, right);
    }
}
