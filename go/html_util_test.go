package web

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_visualizeHtml(t *testing.T) {
	tests := []struct {
		name     string
		html     string
		expected string
	}{
		{
			name:     "empty string",
			html:     "",
			expected: "",
		},
		{
			name:     "nomalizes whitespace",
			html:     " a\n\t\rb    c ",
			expected: "a b c",
		},
		{
			name:     "replaces_html_tags_with_whitespace",
			html:     "<p>one</p><p>two</p>",
			expected: "one two",
		},
		{
			name:     "inline_elements_will_not_add_spacing_to_text",
			html:     "x<a>y</a>z",
			expected: "xyz",
		},
		{
			name:     "inline_elements_will_not_add_spacing_to_text",
			html:     "x<a><abbr><b><big><cite><code><em><i><small><span><strong><tt>y</tt></strong></span></small></i></em></code></cite></big></b></abbr></a>z",
			expected: "xyz",
		},
		{
			html:     "1&nbsp;000",
			expected: "1 000",
		},
		{
			html:     "&lt;",
			expected: "<",
		},
		{
			html:     "&gt;",
			expected: ">",
		},
		{
			html:     "&amp;",
			expected: "&",
		},
		{
			html:     "&quot;",
			expected: "\"",
		},
		{
			html:     "&apos;",
			expected: "'",
		},
		{
			name:     "elements_with_the_data_test_icon_attribute_are_replaced_with_its_value",
			html:     "<input type=\"checkbox\" data-test-icon=\"☑️\" checked value=\"true\">",
			expected: "☑️",
		},
		{
			name:     "spacing before, inside and after element",
			html:     "x<div data-test-icon=\"🟢\">y</div>z",
			expected: "x 🟢 y z",
		},
	}
	for _, test := range tests {
		if test.name == "" {
			test.name = test.html
		}
		t.Run(test.name, func(t *testing.T) {
			assert.Equal(t, test.expected, visualizeHtml(test.html))
		})
	}
}
