package com.example.OnlyBuns.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PostCommentStatsDto {
    private long weeklyPosts;
    private long monthlyPosts;
    private long yearlyPosts;
    private long weeklyComments;
    private long monthlyComments;
    private long yearlyComments;

}