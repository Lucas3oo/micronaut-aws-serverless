package se.solrike.demo.book;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class BookFilters {

  private String mDescription;

  public String getDescription() {
    return mDescription;
  }

  public void setDescription(String description) {
    mDescription = description;
  }

}
