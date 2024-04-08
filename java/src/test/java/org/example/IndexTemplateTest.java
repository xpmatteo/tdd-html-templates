package org.example;

import com.samskivert.mustache.Mustache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexTemplateTest {
    @Test
    void indexIsSoundHtml() {
        var model = new TodoList();

        var html = renderTemplate("/index.tmpl", model);

        assertSoundHtml(html);
    }

    @Test
    void todoItemsAreShown() throws IOException {
        var model = new TodoList();
        model.add("Foo");
        model.add("Bar");

        var html = renderTemplate("/index.tmpl", model);

        Document document = Jsoup.parse(html, "");
        var selection = document.select("ul.todo-list li");
        assertThat(selection).hasSize(2);
        assertThat(selection.get(0).text()).isEqualTo("Foo");
        assertThat(selection.get(1).text()).isEqualTo("Bar");
    }

    @Test
    void failOnBrokenHtml() {
        String htmlString = "<p>foo</div>";

        assertThatThrownBy(() -> assertSoundHtml(htmlString))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Unexpected EndTag token [</div>]");
    }

    @Test
    void html5AttributesAreOk() {
        String htmlString = "<p hidden>foo</p>";

        assertSoundHtml(htmlString);
    }

    @Test
    void unclosedParasAreOk() throws IOException {
        String htmlString = "<section><p>first<p>second</section>";

        assertSoundHtml(htmlString);
    }

    @Test
    void unclosedDivsAreInvalid() throws IOException {
        String htmlString = "<section><div>first<div>second</section>";

        assertThatThrownBy(() -> assertSoundHtml(htmlString))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Unexpected EndTag token [</section>]");
    }

    // thanks https://stackoverflow.com/a/64465867/164802
    private static void assertSoundHtml(String html) {
        var parser = Parser.htmlParser().setTrackErrors(10);
        Jsoup.parse(html, "", parser);
        assertThat(parser.getErrors()).isEmpty();
    }

    @SuppressWarnings("DataFlowIssue")
    private String renderTemplate(String templateName, Object model) {
        var template = Mustache.compiler().compile(
                new InputStreamReader(
                        getClass().getResourceAsStream(templateName)));
        return template.execute(model);
    }
}
