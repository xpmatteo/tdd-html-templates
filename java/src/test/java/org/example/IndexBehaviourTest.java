package org.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

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

    @Test
    void toggleTodoItem() {
        // Render our initial html
        TodoList model = new TodoList()
                .add("One")
                .add("Two");
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
                } else {
                    // we don't want unexpected calls
                    fail(String.format("Unexpected request: %s %s", route.request().method(), route.request().url()));
                }
            });

            // load initial html
            page.navigate("http://localhost:4567/index.html");
        }
    }
}

