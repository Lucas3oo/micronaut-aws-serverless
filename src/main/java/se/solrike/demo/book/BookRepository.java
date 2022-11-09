package se.solrike.demo.book;

import java.util.List;
import java.util.Optional;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BookRepository extends PageableRepository<BookEntity, Integer> {

  // DTO projection so that entities aren't populated
  // see https://micronaut-projects.github.io/micronaut-data/latest/guide/#dto
  List<BookDto> retrieveAll();

  Optional<BookDto> retrieveById(Integer id);

  Optional<BookDto> retrieveByDescription(String description);

}
