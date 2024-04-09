package org.example;

import com.samskivert.mustache.Mustache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTemplateTest {
    record TestCase(String name,
                    TodoList model,
                    String url,
                    String selector,
                    List<String> matches) {
        @Override
        public String toString() {
            return name;
        }

        public static final class Builder {
            String name;
            TodoList model;
            String url;
            String selector;
            List<String> matches;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder model(TodoList model) {
                this.model = model;
                return this;
            }

            public Builder url(String url) {
                this.url = url;
                return this;
            }

            public Builder selector(String selector) {
                this.selector = selector;
                return this;
            }

            public Builder matches(String ... matches) {
                this.matches = Arrays.asList(matches);
                return this;
            }

            public TestCase build() {
                return new TestCase(name, model, url, selector, matches);
            }
        }
    }

    public static TestCase.Builder[] indexTestCases() {
        return new TestCase.Builder[]{
                new TestCase.Builder()
                        .name("all todo items are shown")
                        .model(new TodoList()
                                .add("Foo")
                                .add("Bar"))
                        .selector("ul.todo-list li")
                        .matches("Foo", "Bar"),
                new TestCase.Builder()
                        .name("completed items get the 'completed' class")
                        .model(new TodoList()
                                .add("Foo")
                                .addCompleted("Bar"))
                        .selector("ul.todo-list li.completed")
                        .matches("Bar"),
                new TestCase.Builder()
                        .name("items left")
                        .model(new TodoList()
                                .add("One")
                                .add("Two")
                                .addCompleted("Three"))
                        .selector("span.todo-count")
                        .matches("2 items left"),
        };
    }

    @ParameterizedTest
    @MethodSource("indexTestCases")
    void testIndexTemplate(TestCase.Builder testBulder) {
        var test = testBulder.build();
        var html = renderTemplate("/index.tmpl", test.model);

        var document = parseHtml(html);
        var selection = document.select(test.selector);
        assertThat(selection).hasSize(test.matches.size());
        for (int i = 0; i < test.matches.size(); i++) {
            assertThat(selection.get(i).text()).isEqualTo(test.matches.get(i));
        }
    }

    // thanks https://stackoverflow.com/a/64465867/164802
    private static Document parseHtml(String html) {
        var parser = Parser.htmlParser().setTrackErrors(10);
        var document = Jsoup.parse(html, "", parser);
        assertThat(parser.getErrors()).isEmpty();
        return document;
    }

    @SuppressWarnings({"DataFlowIssue", "SameParameterValue"})
    private String renderTemplate(String templateName, Object model) {
        var template = Mustache.compiler().compile(
                new InputStreamReader(
                        getClass().getResourceAsStream(templateName)));
        return template.execute(model);
    }
}
