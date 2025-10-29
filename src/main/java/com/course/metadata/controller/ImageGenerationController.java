package com.course.metadata.controller;

import com.course.metadata.dto.ImageResponseDto;
import com.course.metadata.service.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    @Autowired
    private ImageGenerationService imageService;

    @PostMapping("/generate/{courseId}")
    public ImageResponseDto generateCourseImage(@PathVariable Long courseId) throws Exception {
        return imageService.generateImage(courseId);
    }
}
