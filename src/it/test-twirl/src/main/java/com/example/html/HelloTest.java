package com.example.html;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloTest {
  @Test public void helloWorld() {
    String actual = Hello.render("world").toString();
    assertThat(actual).isEqualTo(String.format("%n<h1>Hello, world!</h1>%n"));
  }
}
