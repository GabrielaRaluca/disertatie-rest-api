package com.dissertation.restapi.controller;

import com.dissertation.restapi.model.ImagesContent;
import com.dissertation.restapi.model.Label;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.repository.ImagesContentRepository;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.dissertation.restapi.service.AnalysisService;
import com.dissertation.restapi.service.VisionApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@Transactional
@RequestMapping("/imagescontent")
@CrossOrigin(origins = "*")
public class ImagesContentController {
    private final ImagesContentRepository imagesContentRepository;
    private final TravelPostRepository travelPostRepository;
    private final VisionApiService visionApiService;
    private final AnalysisService analysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImagesContentController(ImagesContentRepository imagesContentRepository,
                                   TravelPostRepository travelPostRepository,
                                   VisionApiService visionApiService,
                                   AnalysisService analysisService){
        this.imagesContentRepository = imagesContentRepository;
        this.travelPostRepository = travelPostRepository;
        this.visionApiService = visionApiService;
        this.analysisService = analysisService;
    }

    @GetMapping("/{imageId}")
    ResponseEntity getPostImagesContent(@PathVariable Long imageId) {
        Optional<ImagesContent> optionalImageContent = imagesContentRepository.findById(imageId);
        if(!optionalImageContent.isPresent()) {
            return ResponseEntity.badRequest().build();
        } else {
            ImagesContent imageContent = optionalImageContent.get();

            return ResponseEntity.ok()
                    .contentLength(imageContent.getSize())
                    .contentType(MediaType.parseMediaType(imageContent.getMediaType()))
                    .body(imageContent.getContent());
        }
    }

    @PostMapping("/{travelPostId}")
    ResponseEntity addImagesContent(@PathVariable Long travelPostId, @RequestParam("file") MultipartFile[] files)
            throws IOException {
        Optional<TravelPost> optionalTravelPost = travelPostRepository.findById(travelPostId);
        ObjectNode response = objectMapper.createObjectNode();

        if(!optionalTravelPost.isPresent()) {
            response.put("success", false);
            response.put("message", "No post exists with id " + travelPostId );
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            TravelPost travelPost = optionalTravelPost.get();

            for(int i = 0; i < files.length; i++){
                byte[] fileContent = files[i].getBytes();
                long size = files[i].getSize();
                String contentType = files[i].getContentType();

                ImagesContent imagesContent = ImagesContent.builder()
                        .content(fileContent)
                        .size(size)
                        .mediaType(contentType)
                        .build();

                travelPost.getImages().add(imagesContent);

                try {
                    List<Label> imageLabels = visionApiService.getLabels(imagesContent);
                    analysisService.calculatePreferences(travelPost.getUploader(),
                            travelPost.getScore(), imageLabels);

                } catch (IOException e) {
                    e.printStackTrace();
                    response.put("success", false);
                    response.put("statusCode", 500);
                    response.put("message", e.getMessage());

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    response.put("success", false);
                    response.put("statusCode", 500);
                    response.put("message", e.getMessage());

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            travelPostRepository.save(travelPost);

            response.put("success", true);

            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/{travelPostId}")
    ResponseEntity updateImagesContent(@PathVariable Long travelPostId, @RequestParam("file") MultipartFile[] files)
            throws IOException {
        Optional<TravelPost> optionalTravelPost = travelPostRepository.findById(travelPostId);
        ObjectNode response = objectMapper.createObjectNode();

        if(!optionalTravelPost.isPresent()) {
            response.put("success", false);
            response.put("message", "No post exists with id " + travelPostId );
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            TravelPost travelPost = optionalTravelPost.get();

            for(int i = 0; i < files.length; i++){
                byte[] fileContent = files[i].getBytes();
                long size = files[i].getSize();
                String contentType = files[i].getContentType();

                ImagesContent imagesContent = ImagesContent.builder()
                        .content(fileContent)
                        .size(size)
                        .mediaType(contentType)
                        .build();

                travelPost.getImages().add(imagesContent);

                try {
                    List<Label> imageLabels = visionApiService.getLabels(imagesContent);
                } catch (IOException e) {
                    e.printStackTrace();
                    response.put("success", false);
                    response.put("statusCode", 500);
                    response.put("message", e.getMessage());

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    response.put("success", false);
                    response.put("statusCode", 500);
                    response.put("message", e.getMessage());

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            travelPostRepository.save(travelPost);

            response.put("success", true);

            return ResponseEntity.ok(response);
        }
    }
}
