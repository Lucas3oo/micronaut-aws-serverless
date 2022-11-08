package se.solrike.demo.book;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.micronaut.core.annotation.Introspected;

/**
 *
 */
@Entity
@Introspected
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotEmpty(message = "can not be empty")
  @Size(min = 1, max = 255)
  @Column(nullable = false, length = 255)
  private String description;

  public Book() {
  }

  public Book(String description) {
    this.description = description;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
