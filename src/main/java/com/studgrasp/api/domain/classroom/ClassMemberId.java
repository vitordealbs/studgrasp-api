package com.studgrasp.api.domain.classroom;

import java.io.Serializable;
import java.util.UUID;

public record ClassMemberId(UUID classroom, UUID user) implements Serializable {}