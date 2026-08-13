package com.toy.cnr.port.user.model;

import java.time.LocalDateTime;

public record UserDto(Long id, String email, String name, String nickname, LocalDateTime createdAt) {}
