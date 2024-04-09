package org.example;

import com.samskivert.mustache.Mustache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStreamReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTemplateTest {
    record TestCase(String name,
                    TodoList model,
                    String selector,
                    List<String> matches) {
        @Override
        public String toString() {
            return name;
        }
    }

    public static TestCase[] indexTestCases() {
        return new TestCase[]{
                new TestCase(
                        "all todo items are shown",
                        new TodoList()
                                .add("Foo")
                                .add("Bar"),
                        "ul.todo-list li",
                        List.of("Foo", "Bar")),
                new TestCase(
                        "completed items get the 'completed' class",
                        new TodoList()
                                .add("Foo")
                                .addCompleted("Bar"),
                        "ul.todo-list li.completed",
                        List.of("Bar")),
                new TestCase(
                        "items left",
                        new TodoList()
                                .add("One")
                                .add("Two")
                                .addCompleted("Three"),
                        "span.todo-count",
                        List.of("2 items left")),
        };
    }

    @ParameterizedTest
    @MethodSource("indexTestCases")
    void testIndexTemplate(TestCase test) {
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
