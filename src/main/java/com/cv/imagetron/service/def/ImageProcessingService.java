package com.cv.imagetron.service.def;

import java.io.File;
import java.io.IOException;

public interface ImageProcessingService {
    File blurImage(File image) throws IOException;

    File removeNoiseFrom(File image) throws IOException;

    File convertToFishEye(File image) throws IOException;
}
