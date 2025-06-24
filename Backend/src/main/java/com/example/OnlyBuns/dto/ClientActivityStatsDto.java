package com.example.OnlyBuns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
@NoArgsConstructor
public class ClientActivityStatsDto {
    private List<ActivityCategory> userActivity = new ArrayList<>();

    public void setClientsWithPostsPercentage(double percentage) {
        userActivity.add(new ActivityCategory("Users with posts", percentage));
    }

    public void setClientsWithOnlyCommentsPercentage(double percentage) {
        userActivity.add(new ActivityCategory("Users with only comments", percentage));
    }

    public void setInactiveClientsPercentage(double percentage) {
        userActivity.add(new ActivityCategory("Inactive users", percentage));
    }

    /**
     * Pomoćna inner klasa za predstavljanje pojedinačne kategorije aktivnosti.
     * Njen format (name, value) je idealan za ngx-charts.
     */
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityCategory {
        private String name;
        private double value;
    }
}