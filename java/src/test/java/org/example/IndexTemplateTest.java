package org.example;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexTemplateTest {
    @Test
    void indexIsSoundHtml() {
        var template = Mustache.compiler().compile(
                new InputStreamReader(
                        getClass().getResourceAsStream("/index.tmpl")));
        var model = new TodoList();

        var html = template.execute(model);

        var parser = Parser.htmlParser()
                .setTrackErrors(10);
        Jsoup.parse(html, "", parser);
        assertThat(parser.getErrors()).isEmpty();
    }

    @Test
    void failOnBrokenHtml() throws IOException {
        String htmlString = "<p>foo</div>";

        assertThatThrownBy(() -> assertWellFormedHtml(htmlString))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Unexpected EndTag token [</div>]");
    }

    @Test
    void html5AttributesAreOk() throws IOException {
        String htmlString = "<p hidden>foo</p>";

        assertWellFormedHtml(htmlString);
    }

    @Test
    void unclosedParasAreOk() throws IOException {
        String htmlString = "<section><p>first<p>second</section>";

        assertWellFormedHtml(htmlString);
    }

    @Test
    void unclosedDivsAreInvalid() throws IOException {
        String htmlString = "<section><div>first<div>second</section>";

        assertThatThrownBy(() -> assertWellFormedHtml(htmlString))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Unexpected EndTag token [</section>]");
    }

    // thanks https://stackoverflow.com/a/64465867/164802
    private static void assertWellFormedHtml(String htmlString) {
        var parser = Parser.htmlParser()
                .setTrackErrors(10);
        Jsoup.parse(htmlString, "", parser);
        assertThat(parser.getErrors()).isEmpty();
    }
}
