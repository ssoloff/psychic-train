package io.github.ssoloff.psychictrain.api.engine;

import org.junit.jupiter.api.Test;

import com.google.common.reflect.TypeToken;

import nl.jqno.equalsverifier.EqualsVerifier;

public final class TopicTest {
  @SuppressWarnings("static-method")
  @Test
  public void shouldBeEquatableAndHashable() {
    EqualsVerifier.forClass(Topic.class)
        .withPrefabValues(TypeToken.class, TypeToken.of(String.class), TypeToken.of(Integer.class))
        .verify();
  }
}
