package io.github.ssoloff.psychictrain.api.engine;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;

@Immutable
public final class Topic<@NonNull T> {
  private final String name;
  private final TypeToken<T> typeToken;

  private Topic(final String name, final TypeToken<T> typeToken) {
    this.name = name;
    this.typeToken = typeToken;
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Topic)) {
      return false;
    }

    final Topic<?> other = (Topic<?>) obj;
    return Objects.equals(name, other.name) && Objects.equals(typeToken, other.typeToken);
  }

  public String getName() {
    return name;
  }

  public TypeToken<T> getTypeToken() {
    return typeToken;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, typeToken);
  }

  public static <@NonNull T> Topic<T> of(final String name, final TypeToken<T> typeToken) {
    return new Topic<>(name, typeToken);
  }

  public static <@NonNull T> Topic<T> of(final String name, final Class<T> type) {
    return of(name, TypeToken.of(type));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("typeToken", typeToken)
        .toString();
  }
}
