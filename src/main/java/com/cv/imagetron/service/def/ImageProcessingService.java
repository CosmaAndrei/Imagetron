package com.cv.imagetron.service.def;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

public interface ImageProcessingService {
    File blurImage(File image) throws IOException;

    File removeNoiseFrom(File image) throws IOException;

    Pair<File, File> convertToFishEyeToPineHole(File image) throws IOException;
}
