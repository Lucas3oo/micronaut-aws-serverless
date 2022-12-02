package se.solrike.demo.book;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.validation.Validated;

@Validated
@Controller("/api/v1/books")
public class BookController {

  private static final Logger sLogger = LoggerFactory.getLogger(BookController.class);

  @Inject
  private BookRepository mRepository;

  @Inject
  private BookMapper mMapper;

  @Post()
  @Transactional
  public Integer add(@Body @Valid BookDto book) {
    sLogger.debug("in add method");
    BookEntity entity = mMapper.convertToEntity(book);
    // set id to null to ensure that it will be a new book we are adding.
    entity.setId(null);
    Integer id = mRepository.save(entity).getId();
    return id;
  }

  // Using the syntax {?bookFilters*} we can assign request parameters
  // to a POJO, where the request parameter name matches a property
  // name in the POJO. The name must match the argument
  // name of our method, which is 'bookFilters' in our example.
  // The properties of the POJO can use the Validation API to
  // define constraints and those will be validated if we use
  // @Valid for the method argument and @Validated at the class level.
  // see https://docs.micronaut.io/latest/guide/#routing
  @Get("{?bookFilters*}")
  @ReadOnly
  public Optional<List<BookDto>> getByQueryParams(@Valid @Nullable BookFilters bookFilters) {
    // even if bookFilters is indeed optional the instance is created but with its properties set to null.
    if (bookFilters.getDescription() != null) {
      Optional<BookDto> book = mRepository.retrieveByDescription(bookFilters.getDescription());
      Optional<List<BookDto>> list = book.map(List::of);
      // empty Optional will be translated to 404
      return list;
    }
    else {
      return Optional.of(mRepository.retrieveAll());
    }
  }

  @Get("/{id}")
  @ReadOnly
  public Optional<BookDto> getById(@PathVariable Integer id) {
    return mRepository.retrieveById(id);
  }

}
