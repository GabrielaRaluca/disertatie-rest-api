package com.dissertation.restapi.controller;

import com.dissertation.restapi.exception.BadRequestException;
import com.dissertation.restapi.exception.EntityNotFoundException;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.dissertation.restapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@Transactional
@RequestMapping("/travelpost")
@CrossOrigin(origins = "*")
public class TravelPostController {
    private final TravelPostRepository travelPostRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TravelPostController(TravelPostRepository travelPostRepository,
                                UserRepository userRepository){
        this.travelPostRepository = travelPostRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    ResponseEntity getTravelPost(@PathVariable Long id){
        TravelPost travelPost = travelPostRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("No travel post found for id " + id)
        );

        ArrayNode imagesIds = objectMapper.createArrayNode();
        travelPost.getImages().forEach(imagesContent -> imagesIds.add(imagesContent.getId()));

        ObjectNode userData = objectMapper.createObjectNode();
        userData.put("id", travelPost.getId());
        userData.put("description", travelPost.getDescription());
        userData.put("title", travelPost.getTitle());
        userData.put("location", travelPost.getLocation());
        userData.set("images", imagesIds);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.set("data", userData);

        return ResponseEntity.ok(response);
    }


    @GetMapping()
    ResponseEntity getTravelPost(){
        Iterable<TravelPost> travelPosts = travelPostRepository.findAll();
        ArrayNode responseArray = objectMapper.createArrayNode();

        for(TravelPost travelPost : travelPosts) {
            ArrayNode imagesIds = objectMapper.createArrayNode();
            travelPost.getImages().forEach(imagesContent -> imagesIds.add(imagesContent.getId()));

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());
            userData.set("images", imagesIds);

            responseArray.add(userData);
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.set("data", responseArray);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    ResponseEntity getTravelPostsByUserId(@PathVariable Long userId) {
        List<TravelPost> travelPosts = travelPostRepository.findByUploaderId(userId).orElseThrow(
                () -> new EntityNotFoundException("No travel post found for user " + userId)
        );

        ArrayNode responseArray = objectMapper.createArrayNode();

        for(TravelPost travelPost : travelPosts){
            ArrayNode imagesIds = objectMapper.createArrayNode();
            travelPost.getImages().forEach(imagesContent -> imagesIds.add(imagesContent.getId()));

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());
            userData.set("images", imagesIds);

            responseArray.add(userData);
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.set("data", responseArray);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    ResponseEntity addTravelPost(@PathVariable Long userId, @RequestBody TravelPost travelPostBody){
        if(travelPostRepository.findByTitleAndUploaderId(travelPostBody.getTitle(), userId).isPresent()){
            throw new BadRequestException("This user already has a travel post with the same title!");
        }
        User uploader = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("No user found for the id " + userId));

        travelPostBody.setUploader(uploader);
        TravelPost travelPost = travelPostRepository.save(travelPostBody);

        ObjectNode userData = objectMapper.createObjectNode();
        userData.put("id", travelPost.getId());
        userData.put("description", travelPost.getDescription());
        userData.put("title", travelPost.getTitle());
        userData.put("location", travelPost.getLocation());

        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.set("data", userData);

        return ResponseEntity.ok(response);
    }
}
