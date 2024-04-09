package web

import (
	"bytes"
	"encoding/xml"
	"github.com/PuerkitoBio/goquery"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"golang.org/x/net/html"
	"html/template"
	"io"
	"strings"
	"tdd-html-templates/todo"
	"testing"
)

var testCases = []struct {
	name     string
	model    *todo.List
	path     string
	selector string
	matches  []string
}{
	{
		name: "all todo items are shown",
		model: todo.NewList().
			Add("Foo").
			Add("Bar"),
		selector: "ul.todo-list li",
		matches:  []string{"Foo", "Bar"},
	},
	{
		name: "completed items get the 'completed' class",
		model: todo.NewList().
			Add("Foo").
			AddCompleted("Bar"),
		selector: "ul.todo-list li.completed",
		matches:  []string{"Bar"},
	},
}

func Test_indexTemplate(t *testing.T) {
	for _, test := range testCases {
		t.Run(test.name, func(t *testing.T) {
			buf := renderTemplate("index.tmpl", test.model)

			assertWellFormedHtml(t, buf)
			document := parseHtml(t, buf)
			selection := document.Find(test.selector)
			require.Equal(t, len(test.matches), len(selection.Nodes), "unexpected # of matches")
			for i, node := range selection.Nodes {
				assert.Equal(t, test.matches[i], text(node))
			}
		})
	}
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
