package com.company.mathapp_backend_03.model.response;

import java.util.List;

public class PracticeStatsGroupResponse {
    private List<PracticeStatsResponse> stats;

    public PracticeStatsGroupResponse(List<PracticeStatsResponse> stats) {
        this.stats = stats;
    }

    public List<PracticeStatsResponse> getStats() {
        return stats;
    }
}