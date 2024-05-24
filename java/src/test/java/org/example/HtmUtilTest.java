package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.HtmUtil.visualizeHtml;

class HtmUtilTest {

    @Test
    void empty_input() {
        assertThat(visualizeHtml("")).isEqualTo("");
        assertThat(visualizeHtml(null)).isEqualTo("");
    }

    @Test
    void normalizes_whitespace() {
        assertThat(visualizeHtml(" a\n\t\rb    c ")).isEqualTo("a b c");
    }

    @Test
    void replaces_html_tags_with_whitespace() {
        assertThat(visualizeHtml("<p>one</p><p>two</p>")).isEqualTo("one two");
    }

    @Test
    void inline_elements_will_not_add_spacing_to_text() {
        assertThat(visualizeHtml("x<a>y</a>z"))
                .isEqualTo("xyz");
        assertThat(visualizeHtml("x<a><abbr><b><big><cite><code><em><i><small><span><strong><tt>y</tt></strong></span></small></i></em></code></cite></big></b></abbr></a>z"))
                .isEqualTo("xyz");
    }

    @Test
    void replaces_html_characters_entities() {
        assertThat(visualizeHtml("1&nbsp;000")).isEqualTo("1 000");
        assertThat(visualizeHtml("&lt;")).isEqualTo("<");
        assertThat(visualizeHtml("&gt;")).isEqualTo(">");
        assertThat(visualizeHtml("&amp;")).isEqualTo("&");
        assertThat(visualizeHtml("&quot;")).isEqualTo("\"");
        assertThat(visualizeHtml("&apos;")).isEqualTo("'");
    }

    @Test
    void elements_with_the_data_test_icon_attribute_are_replaced_with_its_value() {
        assertThat(visualizeHtml("<input type=\"checkbox\" data-test-icon=\"☑️\" checked value=\"true\">"))
                .isEqualTo("☑️");
        assertThat(visualizeHtml("x<div data-test-icon=\"🟢\">y</div>z"))
                .describedAs("spacing before, inside and after element")
                .isEqualTo("x 🟢 y z");
    }
}
