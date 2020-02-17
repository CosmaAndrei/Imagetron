package com.cv.imagetron.service.impl;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.alg.distort.*;
import boofcv.alg.distort.pinhole.LensDistortionPinhole;
import boofcv.alg.distort.universal.LensDistortionUniversalOmni;
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.InterpolatePixelS;
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
    public BufferedImage blurImage(File image) throws IOException {
        BufferedImage buffered = ImageIO.read(image);
        Planar<GrayU8> input = ConvertBufferedImage.convertFrom(buffered, true, ImageType.pl(3, GrayU8.class));
        Planar<GrayU8> blurred = input.createSameShape();
        // size of the blur kernel. square region with a width of radius*2 + 1
        int radius = 8;
        // Apply gaussian blur using a procedural interface
        GBlurImageOps.gaussian(input, blurred, -1, radius, null);
        BufferedImage blurredImage = ConvertBufferedImage.convertTo(blurred, null, true);

        return blurredImage;
    }

    /**
     * Example of how to "remove" noise from images using wavelet based algorithms.  A simplified interface is used
     * which hides most of the complexity.  Wavelet image processing is still under development and only floating point
     * images are currently supported.  Which is why the image  type is hard coded.
     */
    @Override
    public BufferedImage removeNoiseFrom(File image) throws IOException {
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


        return denoisedImage;
    }

    @Override
    public Pair<BufferedImage, BufferedImage> convertToFishEyeToPineHole(File image) throws IOException {
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

        return Pair.of(bufferedPinhole0, bufferedPinhole1);
    }

    @Override
    public BufferedImage bilinearInterpolationOf(File image) throws IOException {
        BufferedImage buffered = ImageIO.read(image);

        // For sake of simplicity assume it's a gray scale image.  Interpolation functions exist for planar and
        // interleaved color images too
        GrayF32 input = ConvertBufferedImage.convertFrom(buffered, (GrayF32) null);
        GrayF32 scaled = input.createNew(500, 500 * input.height / input.width);

        // Create the single band (gray scale) interpolation function for the input image
        InterpolatePixelS<GrayF32> interp = FactoryInterpolation.
                createPixelS(0, 255, InterpolationType.BILINEAR, BorderType.EXTENDED, input.getDataType());

        // Tell it which image is being interpolated
        interp.setImage(input);

        // Manually apply scaling to the input image.  See FDistort() for a built in function which does
        // the same thing and is slightly more efficient
        BufferedImage out = ConvertBufferedImage.convertTo(scaled, null, true);

        for (int y = 0; y < scaled.height; y++) {
            // iterate using the 1D index for added performance.  Altertively there is the set(x,y) operator
            int indexScaled = scaled.startIndex + y * scaled.stride;
            float origY = y * input.height / (float) scaled.height;

            for (int x = 0; x < scaled.width; x++) {
                float origX = x * input.width / (float) scaled.width;

                scaled.data[indexScaled++] = interp.get(origX, origY);
            }

            // Add the results to the output
            out = ConvertBufferedImage.convertTo(scaled, null, true);
        }

        return out;
    }

    @Override
    public BufferedImage sharpenImage(File image) throws IOException {
        BufferedImage buffered = ImageIO.read(image);
        GrayU8 gray = ConvertBufferedImage.convertFrom(buffered,(GrayU8)null);
        GrayU8 adjusted = gray.createSameShape();

       //Sharpen 8
        EnhanceImageOps.sharpen8(gray, adjusted);
        return ConvertBufferedImage.convertTo(adjusted,null);
    }
}
