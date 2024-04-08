package org.example;

public record TodoItem(String title, boolean isCompleted) {

    public static TodoItem active(String title) {
        return new TodoItem(title, false);
    }

    public static TodoItem completed(String title) {
        return new TodoItem(title, true);
    }
}
