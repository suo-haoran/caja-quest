package sg.edu.smu.cs205g2t7;

import java.util.Random;

public class Circle {

    private int x;

    private int y;

    private final int r;

    private final int g;

    private final int b;

    private int radius;

    public int getX() {
        return x;
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public int getY() {
        return y;
    }

    public void setY(int newY) {
        this.y = newY;
    }
    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getRadius() {
        return radius;
    }

    public void moveX(int delta) {
        this.x += delta;
    }

    public void moveY(int delta) {
        this.y += delta;
    }

    public Circle(int x, int y) {
        this.x = x;
        this.y = y;
        radius = 30;
        r = 255;
        g = 255;
        b = 255;

        // Random dice = new Random();
        // x = dice.nextInt(width);
        // y = dice.nextInt(height);
        // r = dice.nextInt(256);
        // g = dice.nextInt(256);
        // b = dice.nextInt(256);
    }
}
