package se.solrike.demo.book;

import java.util.Optional;

import javax.validation.Valid;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;

@Validated
@Controller("/api/v1/books")
public class BookController {

  @Inject
  private BookRepository mRepository;

  @Post()
  public BookEntity add(@Body @Valid BookEntity book) {
    return mRepository.save(book);
  }

  @Get()
  public Iterable<BookEntity> findAll() {
    return mRepository.findAll();
  }

  @Get("/{id}")
  public Optional<BookEntity> getById(Integer id) {
    return mRepository.findById(id);
  }

}
