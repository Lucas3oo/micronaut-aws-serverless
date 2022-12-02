package se.solrike.demo.factory;

import javax.sql.DataSource;

import com.amazonaws.xray.sql.TracingDataSource;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;

@Factory
@Replaces(factory = DatasourceFactory.class)
public class XrayDataSourceFactory extends DatasourceFactory {

  public XrayDataSourceFactory(ApplicationContext applicationContext) {
    super(applicationContext);
  }

  @Override
  @Context()
  @EachBean(DatasourceConfiguration.class)
  public DataSource dataSource(DatasourceConfiguration datasourceConfiguration) {
    DataSource ds = super.dataSource(datasourceConfiguration);
    boolean enableXray = (boolean) datasourceConfiguration.getDataSourceProperties().getOrDefault("x-ray", false);
    if (enableXray) {
      return new TracingDataSource(ds);
    }
    else {
      return ds;
    }
  }

}
