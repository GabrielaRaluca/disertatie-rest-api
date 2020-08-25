package com.dissertation.restapi.controller;

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
import java.util.Optional;

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
        Optional<TravelPost> optionalTravelPost = travelPostRepository.findById(id);
        ObjectNode response = objectMapper.createObjectNode();

        if(!optionalTravelPost.isPresent()) {
            response.put("success", false);
            response.put("message", "No travel post exists with id " + id);
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);

        } else {
            TravelPost travelPost = optionalTravelPost.get();
            ArrayNode imagesIds = objectMapper.createArrayNode();
            travelPost.getImages().forEach(imagesContent -> imagesIds.add(imagesContent.getId()));

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());
            userData.set("images", imagesIds);

            response.put("success", true);
            response.set("data", userData);

            return ResponseEntity.ok(response);
        }
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
        Optional<List<TravelPost>> optionalTravelPosts = travelPostRepository.findByUploaderId(userId);
        ObjectNode response = objectMapper.createObjectNode();
        ArrayNode responseArray = objectMapper.createArrayNode();

        if(optionalTravelPosts.isPresent()) {
            List<TravelPost> travelPosts = optionalTravelPosts.get();

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
        }

        response.set("data", responseArray);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    ResponseEntity addTravelPost(@PathVariable Long userId, @RequestBody TravelPost travelPostBody) {
        ObjectNode response = objectMapper.createObjectNode();

        if(travelPostRepository.findByTitleAndUploaderId(travelPostBody.getTitle(), userId).isPresent()) {
            response.put("success", false);
            response.put("message", "This user already has a post with the same title!");
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        }
        Optional<User> optionalUploader = userRepository.findById(userId);
        if(!optionalUploader.isPresent()) {
            response.put("success", false);
            response.put("message", "No user exists with id " + userId);
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            User uploader = optionalUploader.get();

            travelPostBody.setUploader(uploader);
            TravelPost travelPost = travelPostRepository.save(travelPostBody);

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());

            response.put("success", true);
            response.set("data", userData);

            return ResponseEntity.ok(response);
        }
    }
}
