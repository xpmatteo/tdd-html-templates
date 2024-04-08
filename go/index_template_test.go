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
	model := todo.NewList()

	buf := renderTemplate("index.tmpl", model)

	assertWellFormedHtml(t, buf)
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
