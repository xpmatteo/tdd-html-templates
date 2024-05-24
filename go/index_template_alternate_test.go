package web

import (
	"github.com/stretchr/testify/assert"
	"tdd-html-templates/todo"
	"testing"
)

func Test_visualize_html_example(t *testing.T) {
	model := todo.NewList().
		Add("One").
		Add("Two").
		AddCompleted("Three")

	buf := renderTemplate("todo-list.tmpl", model, "/")

	expected := `
		☐ One 🗑️
		☐ Two 🗑️
		☑ Three 🗑️
		`
	assert.Equal(t, normalizeWhitespace(expected), visualizeHtml(buf.String()))
}
