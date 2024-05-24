package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.HtmUtil.normalizeWhitespace;
import static org.example.HtmUtil.visualizeHtml;
import static org.example.IndexTemplateTest.renderTemplate;

class IndexTemplateAlternativeTest {
    @Test
    void visualize_html_example() {
        var model = new TodoList()
                .add("One")
                .add("Two")
                .addCompleted("Three");

        var html = renderTemplate("/todo-list.tmpl", model, "/");

        assertThat(visualizeHtml(html)).isEqualTo(normalizeWhitespace("""
                ☐ One 🗑️
                ☐ Two 🗑️
                ☑ Three 🗑️
                """));
    }
}
