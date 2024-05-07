package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.IndexTemplateTest.parseHtml;
import static org.example.IndexTemplateTest.renderTemplate;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexBehaviourTest {
    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    private void logActivity(Page page) {
        page.onRequest(request -> System.out.printf(">> %s %s%n", request.method(), request.url()));
        page.onResponse(response -> System.out.printf("<< %s %s%n", response.status(), response.url()));
        page.onLoad(page1 -> System.out.println("Loaded: " + page1.url()));
        page.onConsoleMessage(consoleMessage -> System.out.println("!  " + consoleMessage.text()));
    }

    String stubbedHtml = """
            <section class="todoapp">
                <p>Stubbed html</p>
            </section>
            """;

    @Test
    void toggleTodoItem() {
        // Render our initial html
        TodoList model = new TodoList()
                .add(101, "One")
                .add(102, "Two");
        String initialHtml = renderTemplate("/index.tmpl", model, "/");

        try (Page page = browser.newPage()) {
            logActivity(page);

            // stub network calls
            page.route("**", route -> {
                if (route.request().url().equals("http://localhost:4567/index.html")) {
                    // serve the initial HTML
                    route.fulfill(new Route.FulfillOptions()
                            .setContentType("text/html")
                            .setBody(initialHtml));
                } else if (route.request().url().equals("http://localhost:4567/toggle/101") && route.request().method().equals("POST")) {
                    // we expect that a POST /toggle/101 request is made when we click on the "One" checkbox
                    route.fulfill(new Route.FulfillOptions()
                            .setContentType("text/html")
                            .setBody(stubbedHtml));
                } else if (route.request().url().equals("https://unpkg.com/htmx.org@1.9.12")) {
                    // serve the htmx library
                    route.fulfill(new Route.FulfillOptions()
                            .setContentType("text/html")
                            .setBody(readFile("/htmx.min.js")));
                } else {
                    // we don't want unexpected calls
                    fail(String.format("Unexpected request: %s %s", route.request().method(), route.request().url()));
                }
            });

            // load initial html
            page.navigate("http://localhost:4567/index.html");

            // click on the "One" checkbox
            var checkbox = page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("One"));
            checkbox.click();

            // check that the page has been updated
            var document = parseHtml(page.content());
            var elements = document.select("body > section.todoapp > p");
            assertThat(elements.text())
                    .describedAs(page.content())
                    .isEqualTo("Stubbed html");
        }
    }

    private String readFile(String fileName) {
        try {
            return Files.readString(Paths.get("src/test/resources", fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
