package com.ews.stguo.testproject.designpatterns.prototype;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Circle extends Shape {

    private int radius;

    public Circle() {

    }

    public Circle(Circle target) {
        super(target);
        if (target != null) {
            this.radius = target.radius;
        }
    }

    @Override
    public Circle clone() {
        return new Circle(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  Circle) || !super.equals(o)) return false;
        Circle circle = (Circle) o;
        return circle.radius == radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
