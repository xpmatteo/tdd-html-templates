package web

import (
	"github.com/playwright-community/playwright-go"
	"log"
	"tdd-html-templates/todo"
	"testing"
)

// <codeFragment name = "initial-stage">
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

// </codeFragment>

/* <codeFragment name = "initial-stage-output">
=== RUN   Test_clickOnActiveLink
2024/04/30 11:37:38 >> GET http://localhost:4567/index.html
2024/04/30 11:37:38 << 200 http://localhost:4567/index.html
2024/04/30 11:37:38 Loaded: http://localhost:4567/index.html
--- PASS: Test_clickOnActiveLink (0.89s)
PASS
</codeFragment> */
