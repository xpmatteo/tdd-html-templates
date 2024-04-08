package web

import (
	"html/template"
	"testing"
)

func Test_wellFormedHtml(t *testing.T) {
	templ := template.Must(template.ParseFiles("index.tmpl"))
	_ = templ
}
