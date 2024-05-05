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

const stubbedHtml = `
<section class="todoapp">
	<p>Stubbed html</p>
</section>
`

func Test_toggleTodoItem(t *testing.T) {
	// render the initial HTML
	model := todo.NewList().
		AddWithId(101, "One").
		AddWithId(102, "Two")
	initialHtml := renderTemplate("index.tmpl", model, "/")

	// open the browser page with Playwright
	page := openPage()
	defer page.Close()
	logActivity(page)

	// stub network calls
	err := page.Route("**", func(route playwright.Route) {
		if route.Request().URL() == "http://localhost:4567/index.html" {
			// serve the initial HTML
			stubResponse(route, initialHtml.String(), "text/html")
		} else if route.Request().URL() == "http://localhost:4567/toggle/101" && route.Request().Method() == "POST" {
			// we expect that a POST /toggle/101 request is made when we click on the "One" checkbox
			stubResponse(route, stubbedHtml, "text/html")
		} else if route.Request().URL() == "https://unpkg.com/htmx.org@1.9.12" {
			// serve the htmx library
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

	// click on the "One" checkbox
	checkbox := page.GetByRole(*playwright.AriaRoleCheckbox, playwright.PageGetByRoleOptions{Name: "One"})
	if err := checkbox.Click(); err != nil {
		t.Fatal(err)
	}

	// check that the page has been updated
	document := parseHtml(t, content(t, page))
	elements := document.Find("body > section.todoapp > p")
	assert.Equal(t, "Stubbed html", elements.Text(), must(page.Content()))
}

// readFile reads a file from the filesystem
func readFile(fileName string) string {
	data, err := os.ReadFile(fileName)
	if err != nil {
		panic(err)
	}
	return string(data)
}

func must(s string, err error) string {
	if err != nil {
		panic(err)
	}
	return s
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
