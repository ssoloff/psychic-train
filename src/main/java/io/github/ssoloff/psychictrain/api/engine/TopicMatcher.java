package io.github.ssoloff.psychictrain.api.engine;

import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

@Immutable
public abstract class TopicMatcher {
  TopicMatcher() {
  }

  public static TopicMatcher forTopic(final Topic<?> topic) {
    return forTopics(topic);
  }

  public static TopicMatcher forTopics(final Topic<?> topic1, final @NonNull Topic<?>... otherTopics) {
    return new MultipleTopicMatcher(ImmutableSet.<Topic<?>>builderWithExpectedSize(1 + otherTopics.length)
        .add(topic1)
        .add(otherTopics)
        .build());
  }

  public static TopicMatcher forTopicsMatchingPattern(final Pattern namePattern, final TypeToken<?> typeToken) {
    return new NamePatternTopicMatcher(namePattern, typeToken);
  }

  public abstract boolean matches(Topic<?> topic);

  @Immutable
  private static final class MultipleTopicMatcher extends TopicMatcher {
    private final Set<Topic<?>> topics;

    MultipleTopicMatcher(final Set<Topic<?>> topics) {
      this.topics = topics;
    }

    @Override
    public boolean matches(final Topic<?> topic) {
      return topics.contains(topic);
    }
  }

  @Immutable
  private static final class NamePatternTopicMatcher extends TopicMatcher {
    private final Pattern namePattern;
    private final TypeToken<?> typeToken;

    NamePatternTopicMatcher(final Pattern namePattern, final TypeToken<?> typeToken) {
      this.namePattern = namePattern;
      this.typeToken = typeToken;
    }

    @Override
    public boolean matches(final Topic<?> topic) {
      return namePattern.matcher(topic.getName()).matches() && typeToken.equals(topic.getTypeToken());
    }
  }
}
