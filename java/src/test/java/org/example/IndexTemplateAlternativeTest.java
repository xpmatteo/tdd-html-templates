package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.HtmUtil.normalizeWhitespace;
import static org.example.HtmUtil.visualizeHtml;
import static org.example.IndexTemplateTest.renderTemplate;

/*
    This test demonstrates Esko Luontola's technique of
    asserting against a visual representation of the HTML content.

    See https://x.com/EskoLuontola/status/1793950013816713691
 */
class IndexTemplateAlternativeTest {
    @Test
    void visualize_html_example() {
        var model = new TodoList()
                .add("One")
                .add("Two")
                .addCompleted("Three");

        var html = renderTemplate("/todo-list.tmpl", model, "/");

        assertThat(visualizeHtml(html))
                .isEqualTo(normalizeWhitespace("""
                        ⬜ One ❌️
                        ⬜ Two ❌️
                        ✅ Three ❌️
                        """));
    }
}
