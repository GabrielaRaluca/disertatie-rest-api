package com.dissertation.restapi.controller;

import com.dissertation.restapi.exception.EntityNotFoundException;
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

@RestController
@Transactional
@RequestMapping("/imagescontent")
@CrossOrigin(origins = "*")
public class ImagesContentController {
    private final ImagesContentRepository imagesContentRepository;
    private final TravelPostRepository travelPostRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImagesContentController(ImagesContentRepository imagesContentRepository, TravelPostRepository travelPostRepository){
        this.imagesContentRepository = imagesContentRepository;
        this.travelPostRepository = travelPostRepository;
    }

    @GetMapping("/{imageId}")
    ResponseEntity getPostImagesContent(@PathVariable Long imageId) {
        ImagesContent imageContent = imagesContentRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("No image found!"));

        return ResponseEntity.ok()
                .contentLength(imageContent.getSize())
                .contentType(MediaType.parseMediaType(imageContent.getMediaType()))
                .body(imageContent.getContent());
    }

    @PostMapping("/{travelPostId}")
    ResponseEntity addImagesContent(@PathVariable Long travelPostId, @RequestParam("file") MultipartFile[] files) throws IOException {
        TravelPost travelPost = travelPostRepository.findById(travelPostId)
                .orElseThrow(() -> new EntityNotFoundException("No post found!"));

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

        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}
