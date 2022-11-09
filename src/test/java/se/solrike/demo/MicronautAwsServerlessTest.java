package se.solrike.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
class MicronautAwsServerlessTest {

  @Inject
  EmbeddedApplication<?> mApplication;

  @Test
  void testItWorks() {
    Assertions.assertTrue(mApplication.isRunning());
  }

}
