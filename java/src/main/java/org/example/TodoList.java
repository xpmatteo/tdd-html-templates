package org.example;

import java.util.ArrayList;
import java.util.List;

public class TodoList {
    private final List<TodoItem> items = new ArrayList<>();
    public TodoList add(String title) {
        items.add(TodoItem.active(title));
        return this;
    }
    public TodoList addCompleted(String title) {
        items.add(TodoItem.completed(title));
        return this;
    }

}
