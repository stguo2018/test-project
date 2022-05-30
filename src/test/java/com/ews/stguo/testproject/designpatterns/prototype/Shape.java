package com.ews.stguo.testproject.designpatterns.prototype;

import java.util.Objects;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public abstract class Shape implements Cloneable {

    private int x;
    private int y;
    private String color;

    public Shape() {

    }

    public Shape(Shape target) {
        if (target != null) {
            this.x = target.x;
            this.y = target.y;
            this.color = target.color;
        }
    }

    public abstract Shape clone();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Shape)) return false;
        Shape shape = (Shape) o;
        return shape.x == x && shape.y == y && Objects.equals(shape.color, color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, color);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
