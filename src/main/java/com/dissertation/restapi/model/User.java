package com.dissertation.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable =  false)
    String name;

    @Column(nullable = false, name = "picture_url")
    String pictureUrl;

    @Column
    String description;

    @Column
    @ManyToMany
    List<User> unfollowed;

    @Column
    @ManyToMany
    List<User> following;

}
