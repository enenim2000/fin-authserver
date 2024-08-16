package com.elara.authorizationservice.dto.response;

import com.elara.authorizationservice.domain.User;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockedUsers extends BaseResponse{
    private List<User> blockedUsers;
}
