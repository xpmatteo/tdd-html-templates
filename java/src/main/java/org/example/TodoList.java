package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoList {
    private final List<TodoItem> items = new ArrayList<>();
    public TodoList add(String title) {
        items.add(TodoItem.active(title));
        return this;
    }

    public TodoList add(int id, String title) {
        items.add(new TodoItem(id, title, false));
        return this;
    }

    public TodoList addCompleted(String title) {
        items.add(TodoItem.completed(title));
        return this;
    }

    public List<TodoItem> allItems() {
        return Collections.unmodifiableList(items);
    }

    public long activeItemsCount() {
        return items.stream().filter(TodoItem::isActive).count();
    }
}
