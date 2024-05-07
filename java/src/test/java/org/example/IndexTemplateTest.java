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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTemplateTest {
    record TestCase(String name,
                    TodoList model,
                    String path,
                    String selector,
                    List<String> matches) {
        @Override
        public String toString() {
            return name;
        }

        public static final class Builder {
            String name;
            TodoList model = new TodoList();
            String path = "/";
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

            public Builder path(String path) {
                this.path = path;
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
                return new TestCase(name, model, path, selector, matches);
            }
        }
    }

    public static TestCase[] indexTestCases() {
        return new TestCase[]{
                new TestCase.Builder()
                        .name("all todo items are shown")
                        .model(new TodoList()
                                .add("Foo")
                                .add("Bar"))
                        .selector("ul.todo-list li")
                        .matches("Foo", "Bar")
                        .build(),
                new TestCase.Builder()
                        .name("completed items get the 'completed' class")
                        .model(new TodoList()
                                .add("Foo")
                                .addCompleted("Bar"))
                        .selector("ul.todo-list li.completed")
                        .matches("Bar")
                        .build(),
                new TestCase.Builder()
                        .name("items left")
                        .model(new TodoList()
                                .add("One")
                                .add("Two")
                                .addCompleted("Three"))
                        .selector("span.todo-count")
                        .matches("2 items left")
                        .build(),
                new TestCase.Builder()
                        .name("highlighted navigation link: All")
                        .path("/")
                        .selector("ul.filters a.selected")
                        .matches("All")
                        .build(),
                new TestCase.Builder()
                        .name("highlighted navigation link: Active")
                        .path("/active")
                        .selector("ul.filters a.selected")
                        .matches("Active")
                        .build(),
                new TestCase.Builder()
                        .name("highlighted navigation link: Completed")
                        .path("/completed")
                        .selector("ul.filters a.selected")
                        .matches("Completed")
                        .build(),
        };
    }

    @ParameterizedTest
    @MethodSource("indexTestCases")
    void testIndexTemplate(TestCase test) {
        var html = renderTemplate("/index.tmpl", test.model, test.path);

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
    public static String renderTemplate(String templateName, TodoList model, String path) {
        var template = Mustache.compiler().compile(
                new InputStreamReader(
                        IndexTemplateTest.class.getResourceAsStream(templateName)));
        var data = Map.of(
                "model", model,
                "pathRoot", path.equals("/"),
                "pathActive", path.equals("/active"),
                "pathCompleted", path.equals("/completed")
        );
        return template.execute(data);
    }
}
