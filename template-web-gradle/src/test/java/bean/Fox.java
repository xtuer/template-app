package bean;

import java.util.Objects;

public class Fox {
    private int id;
    private String name;

    public Fox(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fox fox = (Fox) o;
        return id == fox.id &&
                Objects.equals(name, fox.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
