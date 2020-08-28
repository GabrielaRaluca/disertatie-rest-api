package com.dissertation.restapi.controller;

import com.dissertation.restapi.model.ImagesContent;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.repository.ImagesContentRepository;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

@RestController
@Transactional
@RequestMapping("/imagescontent")
@CrossOrigin(origins = "*")
public class ImagesContentController {
    private final ImagesContentRepository imagesContentRepository;
    private final TravelPostRepository travelPostRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImagesContentController(ImagesContentRepository imagesContentRepository,
                                   TravelPostRepository travelPostRepository){
        this.imagesContentRepository = imagesContentRepository;
        this.travelPostRepository = travelPostRepository;
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
            }

            travelPostRepository.save(travelPost);

            response.put("success", true);

            return ResponseEntity.ok(response);
        }
    }
}
