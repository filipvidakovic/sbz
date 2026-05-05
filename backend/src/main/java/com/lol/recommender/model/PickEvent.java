package com.lol.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;


@Role(org.kie.api.definition.type.Role.Type.EVENT)
@Timestamp("pickedAt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickEvent {
    private String championName;
    private String team;
    private Champion.Role role;
    private Champion.DamageType damageType;
    private Champion.PlayStyle playStyle;
    private long pickedAt;
    private int pickOrder;
}