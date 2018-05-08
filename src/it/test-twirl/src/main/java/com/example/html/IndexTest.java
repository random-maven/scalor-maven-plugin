package com.example.html;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexTest {
  @Test public void indexWithTemplate() {
    String actual = Index.render().toString();
    String expected = String.format("%n"
        + "<!DOCTYPE html>%n"
        + "<html>%n"
        + "  <head>%n"
        + "    <title>Index - Example</title>%n"
        + "  </head>%n"
        + "  <body>%n"
        + "%n"
        + "<h1>Hello, world!</h1>%n"
        + "%n"
        + "  </body>%n"
        + "</html>%n"
        + "%n");
    assertThat(actual).isEqualTo(expected);
  }
}
