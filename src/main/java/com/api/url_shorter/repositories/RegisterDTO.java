package com.api.url_shorter.repositories;

import com.api.url_shorter.config.UserRoles;

public record RegisterDTO(String username, String password, UserRoles role) {

}
