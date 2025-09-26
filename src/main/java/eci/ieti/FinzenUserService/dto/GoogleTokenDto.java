package eci.ieti.FinzenUserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleTokenDto {
    private String idToken;
}