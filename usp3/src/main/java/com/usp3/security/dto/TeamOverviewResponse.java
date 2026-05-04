package com.usp3.security.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamOverviewResponse {
    List<TeamInvitationResponse> invitations;
    List<TeamUserResponse> users;
}
