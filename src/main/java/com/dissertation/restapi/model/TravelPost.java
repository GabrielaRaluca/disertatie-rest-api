package com.dissertation.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TravelPost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "userId")
    private User uploader;

    @Column
    private String title;

    @Column(nullable = false, name = "date_created")
    Instant creationDate;

    @Column
    private String location;

    @Column
    private String description;

    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImagesContent> images;
}
