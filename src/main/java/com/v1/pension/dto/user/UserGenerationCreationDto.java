package com.v1.pension.dto.user;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserGenerationCreationDto {
  private List<UserRequestDto> users;
}
