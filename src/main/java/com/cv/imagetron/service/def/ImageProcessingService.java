package com.cv.imagetron.service.def;

import org.apache.commons.lang3.tuple.Pair;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ImageProcessingService {
    BufferedImage blurImage(File image) throws IOException;

    BufferedImage removeNoiseFrom(File image) throws IOException;

    Pair<BufferedImage, BufferedImage> convertToFishEyeToPineHole(File image) throws IOException;

    BufferedImage bilinearInterpolationOf(File image) throws IOException;

    BufferedImage sharpenImage(File image) throws IOException;
}
