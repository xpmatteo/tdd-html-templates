package web

import (
	"bytes"
	"github.com/playwright-community/playwright-go"
	"github.com/stretchr/testify/assert"
	"log"
	"os"
	"tdd-html-templates/todo"
	"testing"
)

var activeHtml = `
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
</html>`

func Test_clickOnActiveLink(t *testing.T) {
	// render the initial HTML
	model := todo.NewList().
		Add("One").
		AddCompleted("Two")
	initialHtml := renderTemplate("index.tmpl", model, "/")

	// open the browser page with Playwright
	page := openPage()
	defer page.Close()
	logActivity(page)

	// stub network calls
	err := page.Route("**", func(route playwright.Route) {
		if route.Request().URL() == "http://localhost:4567/index.html" {
			stubResponse(route, initialHtml.String(), "text/html")
		} else if route.Request().URL() == "http://localhost:4567/active" {
			stubResponse(route, activeHtml, "text/html")
		} else if route.Request().URL() == "https://unpkg.com/htmx.org@1.9.12" {
			stubResponse(route, readFile("testdata/htmx.min.js"), "application/javascript")
		} else {
			// avoid unexpected requests
			panic("unexpected request: " + route.Request().URL())
		}
	})
	if err != nil {
		t.Fatal(err)
	}

	// load initial HTML in the page
	response, err := page.Goto("http://localhost:4567/index.html")
	if err != nil {
		t.Fatal(err)
	}
	if response.Status() != 200 {
		t.Fatalf("unexpected status: %d", response.Status())
	}

	// click on "Active"
	active := page.GetByText("Active")
	if err := active.Click(); err != nil {
		t.Fatal(err)
	}

	// now we assert that in the new doc we only have one item
	document := parseHtml(t, content(t, page))
	elements := document.Find("ul.todo-list li")
	assert.Equal(t, 1, len(elements.Nodes), "unexpected # of matches")
	assert.Equal(t, "One", text(elements.Nodes[0]))

	// now we assert that in the new doc only the main section was replaced
	h1 := document.Find("h1")
	assert.Equal(t, "todos", h1.Text())
}

// readFile reads a file from the filesystem
func readFile(fileName string) string {
	data, err := os.ReadFile(fileName)
	if err != nil {
		panic(err)
	}
	return string(data)
}

func content(t *testing.T, page playwright.Page) bytes.Buffer {
	content, err := page.Content()
	if err != nil {
		t.Fatal(err)
	}
	return *bytes.NewBuffer([]byte(content))
}

func stubResponse(route playwright.Route, text string, contentType string) {
	err := route.Fulfill(playwright.RouteFulfillOptions{ContentType: &contentType, Body: text})
	if err != nil {
		panic(err)
	}
}

func logActivity(page playwright.Page) {
	page.OnRequest(func(request playwright.Request) {
		log.Printf(">> %s %s\n", request.Method(), request.URL())
	})
	page.OnResponse(func(response playwright.Response) {
		log.Printf("<< %d %s\n", response.Status(), response.URL())
	})
	page.OnLoad(func(page playwright.Page) {
		log.Println("Loaded: " + page.URL())
	})
	page.OnConsole(func(message playwright.ConsoleMessage) {
		log.Println("!  " + message.Text())
	})
}

func openPage() playwright.Page {
	pw, err := playwright.Run()
	if err != nil {
		log.Fatalf("could not start playwright: %v", err)
	}
	browser, err := pw.Chromium.Launch()
	if err != nil {
		log.Fatalf("could not launch browser: %v", err)
	}
	page, err := browser.NewPage()
	if err != nil {
		log.Fatalf("could not create page: %v", err)
	}
	return page
}
