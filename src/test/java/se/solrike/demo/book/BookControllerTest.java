package se.solrike.demo.book;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest
public class BookControllerTest {

  private static final Logger sLogger = LoggerFactory.getLogger(BookControllerTest.class);

  private static MicronautLambdaHandler sHandler;
  private static final Context sLambdaContext = new MockLambdaContext();

  private static final String PRE_LOADED_BOOK_1 = "Lord of the rings";
  private static final String PRE_LOADED_BOOK_2 = "The Hobbit";

  @Inject
  private BookRepository mRepository;

  @Inject
  private ObjectMapper mObjectMapper;

  // pre-loaded book
  private Integer mBookId1;
  private Integer mBookId2;

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
    mBookId1 = mRepository.save(new BookEntity(PRE_LOADED_BOOK_1)).getId();
    mBookId2 = mRepository.save(new BookEntity(PRE_LOADED_BOOK_2)).getId();
  }

  @AfterEach
  public void tearDown() {
    mRepository.deleteAll();
  }

  @Test
  void getBookById() throws Exception {
    AwsProxyResponse response = handleRequest("GET", "/api/v1/books" + "/" + mBookId1);
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).contains(PRE_LOADED_BOOK_1);
  }

  @Test
  void getBookByDescription() throws Exception {
    AwsProxyResponse response = handleRequest("GET", "/api/v1/books",
        queryStringParametersOf("description", PRE_LOADED_BOOK_1));
    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).contains(PRE_LOADED_BOOK_1);
    assertThat(response.getBody()).doesNotContain(PRE_LOADED_BOOK_2);
  }

  @Test
  void getBookNonExistentById() throws Exception {
    AwsProxyResponse response = handleRequest("GET", "/api/v1/books" + "/" + -1);
    assertThat(response.getStatusCode()).isEqualTo(404);
  }

  @Test
  void getBookNonExistentByDescription() throws Exception {
    AwsProxyResponse response = handleRequest("GET", "/api/v1/books",
        queryStringParametersOf("description", "does not exists"));
    assertThat(response.getStatusCode()).isEqualTo(404);
  }

  @Test
  void getAllBooks() throws Exception {
    AwsProxyResponse response = handleRequest("GET", "/api/v1/books");
    assertThat(response.getStatusCode()).isEqualTo(200);
    // then at least two of the books shall be the pre-loaded books
    assertThat(response.getBody()).contains(PRE_LOADED_BOOK_1, PRE_LOADED_BOOK_2);
  }

  @Test
  void addBook() throws Exception {
    // given a book
    String bookDescription = "The Silmarillion";
    // when book is added
    AwsProxyResponse response = handleRequest("POST", "/api/v1/books", new BookDto(bookDescription), null);
    // then response is OK
    assertThat(response.getStatusCode()).isEqualTo(200);
    // then the book shall exists in the DB
    Integer bookId = Integer.valueOf(response.getBody());
    assertThat(mRepository.findById(bookId).get().getDescription()).isEqualTo(bookDescription);
  }

  AwsProxyResponse handleRequest(String method, String path) throws Exception {
    return handleRequest(method, path, null, null);
  }

  AwsProxyResponse handleRequest(String method, String path, MultiValuedTreeMap<String, String> queryStringParameters)
      throws Exception {
    return handleRequest(method, path, null, queryStringParameters);
  }

  AwsProxyResponse handleRequest(String method, String path, Object body,
      MultiValuedTreeMap<String, String> queryStringParameters) throws Exception {
    AwsProxyRequest request = new AwsProxyRequest();
    request.setHttpMethod(method);
    request.setPath(path);
    request.setMultiValueQueryStringParameters(queryStringParameters);
    if (body != null) {
      request.setBody(mObjectMapper.writeValueAsString(body));
    }
    return sHandler.handleRequest(request, sLambdaContext);
  }

  MultiValuedTreeMap<String, String> queryStringParametersOf(String key, String value) {
    MultiValuedTreeMap<String, String> queryStringParameters = new MultiValuedTreeMap<>();
    queryStringParameters.add(key, value);
    return queryStringParameters;
  }
}
