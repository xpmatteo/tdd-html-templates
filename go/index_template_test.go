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

	document := parseHtml(t, buf)
	selection := document.Find("ul.todo-list li")
	assert.Equal(t, 2, selection.Length())
	assert.Equal(t, "Foo", text(selection.Nodes[0]))
	assert.Equal(t, "Bar", text(selection.Nodes[1]))
}

func Test_completedItemsGetCompletedClass(t *testing.T) {
	model := todo.NewList()
	model.Add("Foo")
	model.AddCompleted("Bar")

	buf := renderTemplate("index.tmpl", model)

	document := parseHtml(t, buf)
	selection := document.Find("ul.todo-list li.completed")
	assert.Equal(t, 1, selection.Size())
	assert.Equal(t, "Bar", text(selection.Nodes[0]))
}

func parseHtml(t *testing.T, buf bytes.Buffer) *goquery.Document {
	assertWellFormedHtml(t, buf)
	document, err := goquery.NewDocumentFromReader(bytes.NewReader(buf.Bytes()))
	if err != nil {
		// if parsing fails, we stop the test here with t.FatalF
		t.Fatalf("Error rendering template %s", err)
	}
	return document
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
