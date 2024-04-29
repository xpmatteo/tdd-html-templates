package org.example;

import com.microsoft.playwright.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.Render.parseHtml;
import static org.example.Render.renderTemplate;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexBehaviourTest {
    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();

        page.onRequest(request -> System.out.printf(">> %s %s%n", request.method(), request.url()));
        page.onResponse(response -> System.out.printf("<< %s %s%n", response.status(), response.url()));
        page.onLoad(page1 -> System.out.println("Loaded: " + page1.url()));
        page.onConsoleMessage(consoleMessage -> System.out.println("!  " + consoleMessage.text()));
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    String activeHtml = """
            <!doctype html>
            <html>
               <body>
                   <h1>Should not be rendered</h1>
                   <section class="todoapp">
                       <section class="main">
                           <ul class="todo-list">
                               <li>
                                   <div class="view">
                                       <input class="toggle" type="checkbox">
                                       <label>One</label>
                                       <button class="destroy"></button>
                                   </div>
                               </li>
                           </ul>
                       </section>
                   </section>
                </body>
            </html>
            """;

    @Test
    void clickOnActiveLink() {
        // Render our initial html
        TodoList model = new TodoList()
                .add("One")
                .addCompleted("Two");
        String initialHtml = renderTemplate("/index.tmpl", model, "/");

        // mock network calls
        page.route("**", route -> {
            if (route.request().url().equals("http://localhost:4567/index.html")) {
                // this loads our initial html
                route.fulfill(new Route.FulfillOptions()
                        .setContentType("text/html")
                        .setBody(initialHtml));
            } else if (route.request().url().equals("http://localhost:4567/active")) {
                // this loads the html for the active link
                route.fulfill(new Route.FulfillOptions()
                        .setContentType("text/html")
                        .setBody(activeHtml));
            } else {
                // we don't want unexpected calls
                fail(String.format("Unexpected request: %s %s", route.request().method(), route.request().url()));
            }
        });

        // load initial html
        page.navigate("http://localhost:4567/index.html");

        // click on "Active"
        Locator active = page.getByText("Active");
        active.click();

        // now we assert that in the new doc we only have one item
        Document newDocument = parseHtml(page.content());
        Elements newListItems = newDocument.select("ul.todo-list li");
        assertThat(newListItems).hasSize(1);
        assertThat(newListItems.getFirst().text()).isEqualTo("One");
    }
}
