package com.inkit.backend.membership.DTO;
import java.util.*;

import com.inkit.backend.common.enums.Role;
public class CreateMembershipRequest {
    public UUID userId;
    public UUID firmId;
    public Role role;
}
