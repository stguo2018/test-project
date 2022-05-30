package com.ews.stguo.testproject.designpatterns.prototype;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class Rectangle extends Shape {

    private int width;
    private int height;

    public Rectangle() {

    }

    public Rectangle(Rectangle target) {
        super(target);
        if (target != null) {
            this.width = target.width;
            this.height = target.height;
        }
    }

    @Override
    public Rectangle clone() {
        return new Rectangle(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  Rectangle) || !super.equals(o)) return false;
        Rectangle rectangle = (Rectangle) o;
        return rectangle.width == width && rectangle.height == height;
    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
