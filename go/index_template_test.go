package web

import (
	"bytes"
	"encoding/xml"
	"html/template"
	"io"
	"tdd-html-templates/todo"
	"testing"
)

func Test_wellFormedHtml(t *testing.T) {
	templ := template.Must(template.ParseFiles("index.tmpl"))
	model := todo.NewList()

	// render the template into a buffer
	var buf bytes.Buffer
	err := templ.Execute(&buf, model)
	if err != nil {
		panic(err)
	}

	// check that the template can be parsed as (lenient) XML
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
