package com.dissertation.restapi.service;

import com.dissertation.restapi.model.ImagesContent;
import com.dissertation.restapi.model.Label;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisionApiService {

    public List<Label> getLabels(ImagesContent imagesContent) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()){
            ByteString imgBytes = ByteString.copyFrom(imagesContent.getContent());

            // Builds the image annotation request
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            // Performs label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            List<Label> labels = new ArrayList<>();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    throw new RuntimeException(res.getError().getMessage());
                }

                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
//                    annotation
//                            .getAllFields()
//                            .forEach((k, v) ->
//                            {
//                                System.out.format("%s : %s%n", k, v.toString());
//                            });
                    labels.add(new Label(annotation.getDescription(), annotation.getScore()));
                }
            }

            return labels;
        } catch (IOException e) {
            throw e;
        }
    }
}
