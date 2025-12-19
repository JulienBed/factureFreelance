package com.facture.dto;

import com.facture.entity.User;

public class UserDto {

    public Long id;
    public String email;
    public String firstName;
    public String lastName;
    public String companyName;
    public String siret;
    public String phone;

    public UserDto() {
    }

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.id = user.id;
        dto.email = user.email;
        dto.firstName = user.firstName;
        dto.lastName = user.lastName;
        dto.companyName = user.companyName;
        dto.siret = user.siret;
        dto.phone = user.phone;
        return dto;
    }
}
