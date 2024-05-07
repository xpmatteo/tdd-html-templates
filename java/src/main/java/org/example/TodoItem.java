package org.example;

import java.util.Random;

public record TodoItem(int id, String title, boolean isCompleted) {

    public static TodoItem active(String title) {
        return new TodoItem(generateRandomId(), title, false);
    }

    public static TodoItem completed(String title) {
        return new TodoItem(generateRandomId(), title, true);
    }

    public boolean isActive() {
        return !isCompleted;
    }

    private static int generateRandomId() {
        return new Random().nextInt(0, Integer.MAX_VALUE);
    }
}
