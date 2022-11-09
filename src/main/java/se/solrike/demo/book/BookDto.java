package se.solrike.demo.book;

import io.micronaut.core.annotation.Introspected;

// add @Introspected to avoid use of reflection
@Introspected
public class BookDto {

  private Integer mId;
  private String mDescription;

  public BookDto() {
  }

  public BookDto(String description) {
    super();
    mDescription = description;
  }

  public BookDto(Integer id, String description) {
    super();
    mId = id;
    mDescription = description;
  }

  public void setId(Integer id) {
    mId = id;
  }

  public Integer getId() {
    return mId;
  }

  public void setDescription(String description) {
    mDescription = description;
  }

  public String getDescription() {
    return mDescription;
  }

}
