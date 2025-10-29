package com.course.metadata.service;

import com.course.metadata.dto.ImageResponseDto;
import com.course.metadata.entity.Course;
import com.course.metadata.repository.CourseRepository;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class ImageGenerationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OpenAiImageModel openAiImageModel;

    // ✅ Custom directory to save generated images
    private static final String IMAGE_SAVE_DIR = "D:/CourseImages";

    public ImageResponseDto generateImage(Long courseId) throws IOException {

        // 1️⃣ Fetch course metadata
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        // 2️⃣ Read prompt template
        String promptTemplate = Files.readString(
                new ClassPathResource("prompt.txt").getFile().toPath()
        );

        // 3️⃣ Build final prompt
        String finalPrompt = promptTemplate
                .replace("{{course_name}}", course.getName())
                .replace("{{learning_objective}}", course.getLearningObjective());

        // 4️⃣ Prepare image prompt
        ImagePrompt imagePrompt = new ImagePrompt(
                finalPrompt,
                OpenAiImageOptions.builder()
                        .model("dall-e-3")
                        .width(1024)
                        .height(1024)
                        .quality("hd")
                        .build()
        );

        // 5️⃣ Generate image
        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);

        // 6️⃣ Extract image output
        var output = imageResponse.getResult().getOutput();
        String base64Image = output.getB64Json();
        String imageUrl = output.getUrl();

        // Ensure directory exists
        Path directory = Paths.get(IMAGE_SAVE_DIR);
        if (!Files.exists(directory))
            Files.createDirectories(directory);

        Path imagePath = directory.resolve("course_" + courseId + ".png");

        // ✅ Case 1: Base64 available (rare for DALL-E 3)
        if (base64Image != null) {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            Files.write(imagePath, imageBytes);
        }
        // ✅ Case 2: URL returned (default for DALL-E 3)
        else if (imageUrl != null) {
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, imagePath);
            }
        } else {
            throw new RuntimeException("Image generation failed: no Base64 or URL returned.");
        }

        return new ImageResponseDto(imagePath.toAbsolutePath().toString());
    }
}