package web

import (
	"github.com/stretchr/testify/assert"
	"tdd-html-templates/todo"
	"testing"
)

/*
This test demonstrates Esko Luontola's technique of
asserting against a visual representation of the HTML content.

See https://x.com/EskoLuontola/status/1793950013816713691
*/
func Test_visualize_html_example(t *testing.T) {
	model := todo.NewList().
		Add("One").
		Add("Two").
		AddCompleted("Three")

	buf := renderTemplate("todo-list.tmpl", model, "/")

	expected := `
		⬜ One ❌️
		⬜ Two ❌️
		✅ Three ❌️
		`
	assert.Equal(t, normalizeWhitespace(expected), visualizeHtml(buf.String()))
}
