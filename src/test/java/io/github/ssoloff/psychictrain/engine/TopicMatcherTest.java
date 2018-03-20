package io.github.ssoloff.psychictrain.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.reflect.TypeToken;

public final class TopicMatcherTest {
  private final TypeToken<Integer> otherTypeToken = TypeToken.of(Integer.class);
  private final TypeToken<Object> typeToken = TypeToken.of(Object.class);

  @Nested
  public final class MultipleTopicMatcherTest {
    private final TopicMatcher topicMatcher = TopicMatcher.forMultiple(Pattern.compile("name\\..+"), typeToken);

    @Test
    public void shouldMatchTopicWithMatchingNameAndSameTypeToken() {
      assertThat(topicMatcher.matches(Topic.of("name.subname1", typeToken)), is(true));
      assertThat(topicMatcher.matches(Topic.of("name.subname2", typeToken)), is(true));
    }

    @Test
    public void shouldNotMatchTopicWithMatchingNameButDifferentTypeToken() {
      assertThat(topicMatcher.matches(Topic.of("name.subname", otherTypeToken)), is(false));
    }

    @Test
    public void shouldNotMatchTopicWithSameTypeTokenButNonmatchingName() {
      assertThat(topicMatcher.matches(Topic.of("_name.subname", typeToken)), is(false));
    }

    @Test
    public void shouldNotMatchTopicWithNonmatchingNameAndDifferentTypeToken() {
      assertThat(topicMatcher.matches(Topic.of("_name.subname", otherTypeToken)), is(false));
    }
  }

  @Nested
  public final class SingleTopicMatcherTest {
    private final String name = "name";
    private final TopicMatcher topicMatcher = TopicMatcher.forSingle(Topic.of(name, typeToken));

    @Test
    public void shouldMatchTopicWithSameNameAndSameTypeToken() {
      assertThat(topicMatcher.matches(Topic.of(name, typeToken)), is(true));
    }

    @Test
    public void shouldNotMatchTopicWithSameNameButDifferentTypeToken() {
      assertThat(topicMatcher.matches(Topic.of(name, otherTypeToken)), is(false));
    }

    @Test
    public void shouldNotMatchTopicWithSameTypeTokenButDifferentName() {
      assertThat(topicMatcher.matches(Topic.of("otherName", typeToken)), is(false));
    }

    @Test
    public void shouldNotMatchTopicWithDifferentNameAndDifferentTypeToken() {
      assertThat(topicMatcher.matches(Topic.of("otherName", otherTypeToken)), is(false));
    }
  }
}
