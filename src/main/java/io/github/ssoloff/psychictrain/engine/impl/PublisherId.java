package io.github.ssoloff.psychictrain.engine.impl;

import javax.annotation.concurrent.Immutable;

@Immutable
final class PublisherId {
  private PublisherId() {
  }

  static PublisherId newInstance() {
    return new PublisherId();
  }
}
