package io.github.haappi.ducksmputils;

public enum Enums {
    JOIN("join"),
    LEAVE("leave"),
    CHAT("chat"),
    STATUS_UPDATE("update"),
    DEATH("death"),


    REQUEST_ONLINE("request-online");

    private final String name;

    Enums(String name) {
        this.name = name;
    }

    public static Enums getByName(String name) {
        for (Enums e : Enums.values()) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
