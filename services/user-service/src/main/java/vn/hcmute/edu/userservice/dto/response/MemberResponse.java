package vn.hcmute.edu.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.hcmute.edu.userservice.enums.ESkillLevel;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MemberResponse {
    private ESkillLevel level;
    private Instant addedAt;
}
