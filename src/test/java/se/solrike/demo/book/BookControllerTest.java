package se.solrike.demo.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class BookControllerTest {

  private static final Logger sLogger = LoggerFactory.getLogger(BookControllerTest.class);

  private static MicronautLambdaHandler sHandler;
  private static Context sLambdaContext = new MockLambdaContext();

  @Inject
  private BookRepository mRepository;

  @BeforeAll
  public static void setupSpec() throws Exception {
    sHandler = new MicronautLambdaHandler();
  }

  @AfterAll
  public static void cleanupSpec() {
    sHandler.getApplicationContext().close();
  }

  @BeforeEach
  public void setUp() {
    sLogger.info("Loading test data");
    mRepository.save(new Book("Lord of the rings"));
  }

  @Test
  void testHandler() throws JsonProcessingException {
    AwsProxyRequest request = new AwsProxyRequest();
    request.setHttpMethod("GET");
    request.setPath("/api/v1/books");
    AwsProxyResponse response = sHandler.handleRequest(request, sLambdaContext);
    assertEquals(200, response.getStatusCode());
    assertEquals("[{\"id\":1,\"description\":\"Lord of the rings\"}]", response.getBody());
  }
}
