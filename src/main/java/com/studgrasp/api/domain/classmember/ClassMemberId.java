package com.studgrasp.api.domain.classmember;

import java.io.Serializable;
import java.util.UUID;

public record ClassMemberId(UUID classroom, UUID user) implements Serializable {}