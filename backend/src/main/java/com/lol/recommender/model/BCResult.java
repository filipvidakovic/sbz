package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BCResult {
    private String championName;
    private boolean valid;
    private List<String> reasons;
}