package com.dut.erp;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DbConnectionTests extends AbstractIntegrationTest {

  @Autowired private DataSource dataSource;

  @Test
  void shouldObtainConnectionFromTestDataSource() throws SQLException {
    assertThat(dataSource).as("DataSource should be configured for test profile").isNotNull();

    try (Connection connection = dataSource.getConnection()) {
      assertThat(connection)
          .as("Connection should not be null")
          .isNotNull()
          .satisfies(conn -> assertThat(conn.isValid(2)).as("Connection should be valid").isTrue());
    }
  }
}
