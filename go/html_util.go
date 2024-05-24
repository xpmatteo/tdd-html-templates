package web

import (
	"regexp"
	"strings"
)

func normalizeWhitespace(s string) string {
	return strings.TrimSpace(replaceAll(s, "\\s+", " "))
}

func visualizeHtml(html string) string {
	//  custom visualization using data-test-icon attribute
	html = replaceAll(html, "<[^<>]+\\bdata-test-icon=\"(.*?)\".*?>", " $1 ")
	// strip all HTML tags: inline elements
	html = replaceAll(html, "</?(a|abbr|b|big|cite|code|em|i|small|span|strong|tt)\\b.*?>", "")
	// strip all HTML tags: block elements
	html = replaceAll(html, "<[^>]*>", " ")
	// replace HTML character entities
	html = replaceAll(html, "&nbsp;", " ")
	html = replaceAll(html, "&lt;", "<")
	html = replaceAll(html, "&gt;", ">")
	html = replaceAll(html, "&amp;", "&")
	html = replaceAll(html, "&quot;", "\"")
	html = replaceAll(html, "&apos;", "'")
	return normalizeWhitespace(html)
}

func replaceAll(src, regex, repl string) string {
	re := regexp.MustCompile(regex)
	return re.ReplaceAllString(src, repl)
}
