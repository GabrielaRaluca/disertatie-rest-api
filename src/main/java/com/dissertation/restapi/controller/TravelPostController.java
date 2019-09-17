package com.dissertation.restapi.controller;

import com.dissertation.restapi.exception.HttpException;
import com.dissertation.restapi.model.TravelPost;
import com.dissertation.restapi.repository.TravelPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@Transactional
@RequestMapping("/travelpost")
@CrossOrigin(origins = "*")
public class TravelPostController {
    private final TravelPostRepository travelPostRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TravelPostController(TravelPostRepository travelPostRepository){
        this.travelPostRepository = travelPostRepository;
    }

    @PostMapping("/{userId}")
    ResponseEntity addTravelPost(@PathVariable Long userId, @RequestBody TravelPost travelPostBody){
        if(travelPostRepository.findByTitleAndUploaderId(travelPostBody.getTitle(), userId).isPresent()){
            throw new HttpException("This user already has a travel post with the same title!");
        }
        TravelPost travelPost = travelPostRepository.save(travelPostBody);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("id", travelPost.getId());
        response.put("description", travelPost.getDescription());
        response.put("title", travelPost.getTitle());
        response.put("location", travelPost.getLocation());

        return ResponseEntity.ok(response);
    }
}
