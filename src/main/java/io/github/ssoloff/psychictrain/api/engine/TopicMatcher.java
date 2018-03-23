package io.github.ssoloff.psychictrain.api.engine;

import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import com.google.common.reflect.TypeToken;

@Immutable
public abstract class TopicMatcher {
  TopicMatcher() {
  }

  public static TopicMatcher forMultiple(final Pattern namePattern, final TypeToken<?> typeToken) {
    return new MultipleTopicMatcher(namePattern, typeToken);
  }

  // TODO: eventually need an override that accepts a varargs array of topics of
  // wildcard type
  public static TopicMatcher forSingle(final Topic<?> topic) {
    return new SingleTopicMatcher(topic);
  }

  public abstract boolean matches(Topic<?> topic);

  @Immutable
  private static final class MultipleTopicMatcher extends TopicMatcher {
    private final Pattern namePattern;
    private final TypeToken<?> typeToken;

    MultipleTopicMatcher(final Pattern namePattern, final TypeToken<?> typeToken) {
      this.namePattern = namePattern;
      this.typeToken = typeToken;
    }

    @Override
    public boolean matches(final Topic<?> topic) {
      return namePattern.matcher(topic.getName()).matches() && typeToken.equals(topic.getTypeToken());
    }
  }

  @Immutable
  private static final class SingleTopicMatcher extends TopicMatcher {
    private final Topic<?> topic;

    SingleTopicMatcher(final Topic<?> topic) {
      this.topic = topic;
    }

    @Override
    public boolean matches(final Topic<?> otherTopic) {
      return topic.equals(otherTopic);
    }
  }
}
