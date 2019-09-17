package com.dissertation.restapi.controller;

import com.dissertation.restapi.exception.EntityNotFoundException;
import com.dissertation.restapi.model.ImagesContent;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.repository.ImagesContentRepository;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.awt.*;
import java.io.IOException;

@RestController
@Transactional
@RequestMapping("/iamgescontent")
@CrossOrigin(origins = "*")
public class ImagesContentController {
    private final ImagesContentRepository imagesContentRepository;
    private final TravelPostRepository travelPostRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImagesContentController(ImagesContentRepository imagesContentRepository, TravelPostRepository travelPostRepository){
        this.imagesContentRepository = imagesContentRepository;
        this.travelPostRepository = travelPostRepository;
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

        return ResponseEntity.ok().build();
    }
}
