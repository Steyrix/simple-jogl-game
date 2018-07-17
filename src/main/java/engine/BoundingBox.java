package engine;

import java.awt.*;

public class BoundingBox {
    protected float posX;
    protected float posY;
    protected float width;
    protected float height;
    protected boolean undefined;

    public BoundingBox(){
        this.posX = 0.0f;
        this.posY = 0.0f;
        this.width = 0.0f;
        this.height = 0.0f;

        this.undefined = true;
    }

    public BoundingBox(float posX, float posY, float width, float height) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;

        this.undefined = false;
    }

    public BoundingBox(float posX, float posY) {
        this.posX = posX;
        this.posY = posY;
        this.width = 0.0f;
        this.height = 0.0f;

        this.undefined = true;
    }

    public BoundingBox(Dimension dimension) {
        this.posX = 0.0f;
        this.posY = 0.0f;
        this.width = dimension.width;
        this.height = dimension.height;

        this.undefined = true;
    }

    protected float getWidthX(){
        return this.posX + this.width;
    }

    protected float getHeightY(){
        return this.posY + this.height;
    }

    private boolean intersectX(BoundingBox anotherBox){
        return !undefined && ((anotherBox.getWidthX() <= this.getWidthX() && anotherBox.getWidthX() >= this.posX) ||
                (anotherBox.posX >= this.posX && anotherBox.posX <= this.getWidthX()));
    }

    private boolean intersectY(BoundingBox anotherBox){
        return !undefined && ((anotherBox.getHeightY() <= this.getHeightY() && anotherBox.getHeightY() >= this.posY) ||
                (anotherBox.posY >= this.posY && anotherBox.posY <= this.getHeightY()));
    }

    public boolean intersects(BoundingBox anotherBox) {
        return intersectX(anotherBox) && intersectY(anotherBox);
    }

}
