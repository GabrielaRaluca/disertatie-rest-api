package com.dissertation.restapi.controller;

import com.dissertation.restapi.model.ImagesContent;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.model.UserLabelScore;
import com.dissertation.restapi.repository.ImagesContentRepository;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.dissertation.restapi.repository.UserLabelScoreRepository;
import com.dissertation.restapi.repository.UserRepository;
import com.dissertation.restapi.service.AnalysisService;
import com.dissertation.restapi.service.SentimentAnalysis;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@Transactional
@RequestMapping("/travelpost")
@CrossOrigin(origins = "*")
public class TravelPostController {
    private final TravelPostRepository travelPostRepository;
    private final UserRepository userRepository;
    private final ImagesContentRepository imagesContentRepository;
    private final SentimentAnalysis sentimentAnalysis;
    private final UserLabelScoreRepository userLabelScoreRepository;
    private final AnalysisService analysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TravelPostController(TravelPostRepository travelPostRepository,
                                UserRepository userRepository,
                                ImagesContentRepository imagesContentRepository,
                                SentimentAnalysis sentimentAnalysis,
                                UserLabelScoreRepository userLabelScoreRepository,
                                AnalysisService analysisService){
        this.travelPostRepository = travelPostRepository;
        this.userRepository = userRepository;
        this.imagesContentRepository = imagesContentRepository;
        this.sentimentAnalysis = sentimentAnalysis;
        this.userLabelScoreRepository = userLabelScoreRepository;
        this.analysisService = analysisService;
    }

//    @GetMapping("/userlabels")
//    ResponseEntity getUserLabelScores() throws IOException {
//        ObjectNode response = objectMapper.createObjectNode();
//        ArrayNode userLabelScores = objectMapper.createArrayNode();
//        Iterator<UserLabelScore> iterator = userLabelScoreRepository.findAll().iterator();
//        while(iterator.hasNext()) {
//            UserLabelScore userLabelScore = iterator.next();
//            ObjectNode userLabelScoreObject = objectMapper.createObjectNode();
//            userLabelScoreObject.put("userId", userLabelScore.getUserId().getId());
//            userLabelScoreObject.put("label", userLabelScore.getLabel());
//            userLabelScoreObject.put("score", userLabelScore.getScore());
//
//            userLabelScores.add(userLabelScoreObject);
//        }
//
//        analysisService.getSimilarities(userLabelScores);
//
//        response.put("success", true);
//        return ResponseEntity.ok().build();
//    }

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
            userData.put("creationDate", travelPost.getCreationDate().toString());
            userData.put("uploaderId", travelPost.getUploader().getId());

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
            userData.put("creationDate", travelPost.getCreationDate().toString());
            userData.put("uploaderId", travelPost.getUploader().getId());

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
        Optional<List<TravelPost>> optionalTravelPosts = travelPostRepository
                .findAllByUploaderIdOrderByCreationDateDesc(userId);
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
                userData.put("creationDate", travelPost.getCreationDate().toString());
                userData.put("uploaderId", travelPost.getUploader().getId());
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
            travelPostBody.setCreationDate(Instant.now());

            try {
                float score = sentimentAnalysis.analyze(travelPostBody.getDescription());
                travelPostBody.setScore(score);
            } catch (IOException e) {
                e.printStackTrace();
            }

            TravelPost travelPost = travelPostRepository.save(travelPostBody);

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());
            userData.put("creationDate", travelPost.getCreationDate().toString());
            userData.put("uploaderId", travelPost.getUploader().getId());

            response.put("success", true);
            response.set("data", userData);

            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/{userId}/{postId}")
    public ResponseEntity updatePost(@PathVariable Long userId,
                                     @PathVariable Long postId,
                                     @RequestBody ObjectNode travelPostBody) {
        ObjectNode response = objectMapper.createObjectNode();

        Optional<TravelPost> optionalTravelPost = travelPostRepository.findById(postId);
        if (!optionalTravelPost.isPresent()) {
            response.put("success", false);
            response.put("message", "No post exists with id " + postId);
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            Optional<TravelPost> optionalExistingTravelPost =
                    travelPostRepository.findByTitleAndUploaderId(travelPostBody.get("title").asText(), userId);
            if(optionalExistingTravelPost.isPresent() && optionalExistingTravelPost.get().getId() != postId) {
                response.put("success", false);
                response.put("message", "You already have a post with the same title!");
                response.put("statusCode", 400);

                return ResponseEntity.badRequest().body(response);
            }

            List<ImagesContent> currentImages = new ArrayList<>();
            ArrayNode arrayNode = (ArrayNode) travelPostBody.get("images");

            for(int i = 0; i < arrayNode.size(); i++) {
                long imageId = arrayNode.get(i).asLong();
                Optional<ImagesContent> optionalExistingImage = imagesContentRepository.findById(imageId);
                if (!optionalExistingImage.isPresent()) {
                    response.put("success", false);
                    response.put("message", "Some of the provided img ids do not exist");
                    response.put("statusCode", 400);

                    return ResponseEntity.badRequest().body(response);
                } else {
                    currentImages.add(optionalExistingImage.get());
                }
            }

            TravelPost travelPost = optionalTravelPost.get();
            travelPost.setDescription(travelPostBody.get("description").asText());
            travelPost.setTitle(travelPostBody.get("title").asText());
            travelPost.setLocation(travelPostBody.get("location").asText());
            travelPost.setImages(currentImages);

            travelPostRepository.save(travelPost);

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", travelPost.getId());
            userData.put("description", travelPost.getDescription());
            userData.put("title", travelPost.getTitle());
            userData.put("location", travelPost.getLocation());
            userData.put("creationDate", travelPost.getCreationDate().toString());
            userData.put("uploaderId", travelPost.getUploader().getId());

            response.put("success", true);
            response.set("data", userData);

            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity deletePost(@PathVariable Long postId) {
        ObjectNode response = objectMapper.createObjectNode();

        Optional<TravelPost> optionalTravelPost = travelPostRepository.findById(postId);
        if (!optionalTravelPost.isPresent()) {
            response.put("success", false);
            response.put("message", "No post exists with id " + postId);
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            TravelPost travelPost = optionalTravelPost.get();
            travelPostRepository.delete(travelPost);

            response.put("success", true);

            return ResponseEntity.ok(response);
        }
    }

    @GetMapping(value = "/user/following/{userId}")
    ResponseEntity getFollowingPosts(@PathVariable Long userId) {
        ObjectNode response = objectMapper.createObjectNode();

        Optional<User> optionalUser = userRepository.findById(userId);
        if(!optionalUser.isPresent()) {
            response.put("success", false);
            response.put("message", "No user exists with id " + userId);
            response.put("statusCode", 400);

            return ResponseEntity.badRequest().body(response);
        } else {
            User user = optionalUser.get();
            ArrayNode responseArray = objectMapper.createArrayNode();

            for(User following: user.getFollowing()) {
                Optional<List<TravelPost>> optionalTravelPosts = travelPostRepository
                        .findAllByUploaderIdOrderByCreationDateDesc(following.getId());

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
                        userData.put("creationDate", travelPost.getCreationDate().toString());
                        userData.put("uploaderId", travelPost.getUploader().getId());
                        userData.put("uploaderImage", travelPost.getUploader().getPictureUrl());
                        userData.put("uploaderName", travelPost.getUploader().getName());
                        userData.set("images", imagesIds);

                        responseArray.add(userData);
                    }
                }
            }
            response.set("data", responseArray);
            response.put("success", true);
            return ResponseEntity.ok(response);
        }
    }
}
