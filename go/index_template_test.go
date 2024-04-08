package web

import (
	"bytes"
	"encoding/xml"
	"github.com/PuerkitoBio/goquery"
	"github.com/stretchr/testify/assert"
	"golang.org/x/net/html"
	"html/template"
	"io"
	"strings"
	"tdd-html-templates/todo"
	"testing"
)

func Test_wellFormedHtml(t *testing.T) {
	model := todo.NewList()

	buf := renderTemplate("index.tmpl", model)

	assertWellFormedHtml(t, buf)
}

func Test_todoItemsAreShown(t *testing.T) {
	model := todo.NewList()
	model.Add("Foo")
	model.Add("Bar")

	buf := renderTemplate("index.tmpl", model)

	// parse the HTML with goquery
	document, err := goquery.NewDocumentFromReader(bytes.NewReader(buf.Bytes()))
	if err != nil {
		// if parsing fails, we stop the test here with t.FatalF
		t.Fatalf("Error rendering template %s", err)
	}

	// assert there are two <li> elements inside the <ul class="todo-list">
	selection := document.Find("ul.todo-list li")
	assert.Equal(t, 2, selection.Length())

	// assert the first <li> text is "Foo"
	assert.Equal(t, "Foo", text(selection.Nodes[0]))

	// assert the second <li> text is "Bar"
	assert.Equal(t, "Bar", text(selection.Nodes[1]))
}

func text(node *html.Node) string {
	// A little mess due to the fact that goquery has
	// a .Text() method on Selection but not on html.Node
	sel := goquery.Selection{Nodes: []*html.Node{node}}
	return strings.TrimSpace(sel.Text())
}

func assertWellFormedHtml(t *testing.T, buf bytes.Buffer) {
	decoder := xml.NewDecoder(bytes.NewReader(buf.Bytes()))
	decoder.Strict = false
	decoder.AutoClose = xml.HTMLAutoClose
	decoder.Entity = xml.HTMLEntity
	for {
		_, err := decoder.Token()
		switch err {
		case io.EOF:
			return // We're done, it's valid!
		case nil:
			// do nothing
		default:
			t.Fatalf("Error parsing html: %s", err)
		}
	}
}

func renderTemplate(templateName string, model any) bytes.Buffer {
	templ := template.Must(template.ParseFiles(templateName))
	var buf bytes.Buffer
	err := templ.Execute(&buf, model)
	if err != nil {
		panic(err)
	}
	return buf
}
