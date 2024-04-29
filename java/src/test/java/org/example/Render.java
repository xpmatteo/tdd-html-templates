package org.example;

import com.samskivert.mustache.Mustache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.InputStreamReader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class Render {
    @SuppressWarnings({"DataFlowIssue", "SameParameterValue"})
    public static String renderTemplate(String templateName, TodoList model, String path) {
        var template = Mustache.compiler().compile(
                new InputStreamReader(
                        Render.class.getResourceAsStream(templateName)));
        var data = Map.of(
                "model", model,
                "pathRoot", path.equals("/"),
                "pathActive", path.equals("/active"),
                "pathCompleted", path.equals("/completed")
        );
        return template.execute(data);
    }

    // thanks https://stackoverflow.com/a/64465867/164802
    public static Document parseHtml(String html) {
        var parser = Parser.htmlParser().setTrackErrors(10);
        var document = Jsoup.parse(html, "", parser);
        assertThat(parser.getErrors()).isEmpty();
        return document;
    }
}
