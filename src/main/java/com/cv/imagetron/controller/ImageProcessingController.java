package com.cv.imagetron.controller;

import com.cv.imagetron.service.def.ImageProcessingService;
import com.cv.imagetron.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

@RequestMapping("/image-processing-management")
@RestController
public class ImageProcessingController {

    @Autowired
    ImageProcessingService imageProcessingService;
    @Autowired
    Util util;

    @PostMapping(value = "/blur",
            produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BufferedImage blurImage(@RequestParam("image") MultipartFile file) throws IOException {
        return imageProcessingService.blurImage(util.multipartToFile(file, "convertedFileName"));
    }

    @PostMapping(value = "/remove-noise",
            produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BufferedImage removeNoiseFrom(@RequestParam("image") MultipartFile file) throws IOException {
        return imageProcessingService.removeNoiseFrom(util.multipartToFile(file, "convertedFileName"));
    }

    @PostMapping(value = "/bilinear-interpolation",
            produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BufferedImage bilinearInterpolationOf(@RequestParam("image") MultipartFile file) throws IOException {
        return imageProcessingService.bilinearInterpolationOf(util.multipartToFile(file, "convertedFileName"));
    }

    @PostMapping(value = "/sharpen-image",
            produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BufferedImage sharpenImage(@RequestParam("image") MultipartFile file) throws IOException {
        return imageProcessingService.sharpenImage(util.multipartToFile(file, "convertedFileName"));
    }
}
