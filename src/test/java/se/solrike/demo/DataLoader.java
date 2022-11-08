package se.solrike.demo;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import io.micronaut.context.event.ApplicationEventListener;
//import io.micronaut.context.event.StartupEvent;
//import jakarta.inject.Singleton;
//import se.solrike.demo.book.Book;
//import se.solrike.demo.book.BookRepository;

//@Singleton
// -Dmicronaut.environments=someProfile
//@Requires(env = Environment.TEST)
//public class DataLoader implements ApplicationEventListener<StartupEvent> {
//  private static final Logger sLogger = LoggerFactory.getLogger(DataLoader.class);
//
//  private final BookRepository mRepository;
//
//  public DataLoader(final BookRepository repository) {
//    mRepository = repository;
//  }
//
//  @Override
//  public void onApplicationEvent(StartupEvent event) {
//    sLogger.info("Loading test data");
//    mRepository.save(new Book("Lord of the rings"));
//  }
//
//}
