package org.example;

public class HtmUtil {

    public static String visualizeHtml(String html) {
        if (html == null) {
            html = "";
        }
        //  custom visualization using data-test-icon attribute
        html = html.replaceAll("<[^<>]+\\bdata-test-icon=\"(.*?)\".*?>", " $1 ");
        // strip all HTML tags
        html = html.replaceAll("</?(a|abbr|b|big|cite|code|em|i|small|span|strong|tt)\\b.*?>", "") // inline elements
                .replaceAll("<[^>]*>", " ");  // block elements
        // replace HTML character entities
        html = html.replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<") // must be after stripping HTML tags, to avoid creating accidental elements
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&amp;", "&"); // must be last, to avoid creating accidental character entities
        return normalizeWhitespace(html);
    }

    public static String normalizeWhitespace(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }
}
