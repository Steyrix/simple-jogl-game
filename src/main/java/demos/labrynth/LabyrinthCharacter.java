package demos.labrynth;

import com.jogamp.opengl.GL4;
import engine.animation.BasicAnimation;
import engine.collision.BoundingBox;
import engine.collision.PointF;
import engine.collision.SpeculativeCollider;
import engine.core.ControllableObject;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

//TODO: manage collisions
public class LabyrinthCharacter extends ControllableObject implements SpeculativeCollider {

    private boolean[] keys;
    private ArrayList<PointF> collisionPoints;
    private BoundingBox nextBox;

    private boolean isWalking;
    private boolean canJump;
    private float jumpTime;

    LabyrinthCharacter(int bufferParamsCount, int verticesCount, GL4 gl, Dimension boxDim, int id, float frameSizeX, float frameSizeY, BasicAnimation... animationSet) throws Exception {
        super(bufferParamsCount, verticesCount, gl, boxDim, id, frameSizeX, frameSizeY, animationSet);
        this.keys = new boolean[1000000];

        this.collisionPoints = new ArrayList<>();
        this.collisionPoints.add(new PointF(posX, posY));
        this.collisionPoints.add(new PointF(posX, getBottomY()));
        this.collisionPoints.add(new PointF(getRightX(), getBottomY()));
        this.collisionPoints.add(new PointF(getRightX(), posY));
        this.collisionPoints.add(new PointF(posX + width / 2, posY));
        this.collisionPoints.add(new PointF(posX + width / 2, getBottomY()));
        this.collisionPoints.add(new PointF(posX, posY + height / 2));
        this.collisionPoints.add(new PointF(getRightX(), posY + height / 2));
        this.nextBox = new BoundingBox(posX, posY, width, height);

        this.isWalking = false;
        this.canJump = true;
        this.jumpTime = 0f;
    }

    LabyrinthCharacter(int bufferParamsCount, int verticesCount, GL4 gl, float posX, float posY, Dimension boxDim, int id, float frameSizeX, float frameSizeY, BasicAnimation... animationSet) throws Exception {
        super(bufferParamsCount, verticesCount, gl, posX, posY, boxDim, id, frameSizeX, frameSizeY, animationSet);
        this.keys = new boolean[1000000];

        this.collisionPoints = new ArrayList<>();
        this.collisionPoints.add(new PointF(posX, posY));
        this.collisionPoints.add(new PointF(posX, getBottomY()));
        this.collisionPoints.add(new PointF(getRightX(), getBottomY()));
        this.collisionPoints.add(new PointF(getRightX(), posY));
        this.collisionPoints.add(new PointF(posX + width / 2, posY));
        this.collisionPoints.add(new PointF(posX + width / 2, getBottomY()));
        this.collisionPoints.add(new PointF(posX, posY + height / 2));
        this.collisionPoints.add(new PointF(getRightX(), posY + height / 2));
        this.nextBox = new BoundingBox(posX, posY, width, height);

        this.isWalking = false;
        this.canJump = true;
        this.jumpTime = 0f;
    }

    @Override
    public void reactToCollision(BoundingBox anotherBox) {
        if (detectBottomContact(anotherBox))
            jumpState = false;
        if (anotherBox.containsPoint(this.collisionPoints))
            processCollision(anotherBox);
    }

    private boolean detectBottomContact(BoundingBox anotherBox) {
        return anotherBox.containsPoint(
                this.collisionPoints.get(1),
                this.collisionPoints.get(2),
                this.collisionPoints.get(5));
    }

    @Override
    public void update(float deltaTime) {

        applyVelocityX();
        applyVelocityY();

        processJumpTime(deltaTime);
        processGravityEffect(deltaTime);

        changePosition(deltaTime);
        processAnimation();

        updateCollisionPoints();
        updateNextBox(deltaTime);

        playAnimation(deltaTime);

        //System.out.println("JUMPSTATE:" + jumpState);
        System.out.println(this.posX + "   " + this.posY + "||| " + deltaTime + " |||" + this.velocityX + " " + this.velocityY);

    }

    private void applyVelocityX() {
        if (keys[KeyEvent.VK_D])
            this.velocityX = 3.5f;
        else if (keys[KeyEvent.VK_A])
            this.velocityX = -3.5f;
        else
            this.velocityX = 0f;
    }

    private void applyVelocityY() {
        if (keys[KeyEvent.VK_S]) {
            this.velocityY = 5.0f;
            this.jumpState = true;
            this.canJump = false;
        } else if (keys[KeyEvent.VK_W] && canJump)
            jump();
        else if (!jumpState)
            this.velocityY = 0.0f;
    }

    private void processJumpTime(float deltaTime) {
        if (!canJump) {
            this.jumpTime += deltaTime;
            float jumpTimeLimit = 300f;
            if (jumpTime >= jumpTimeLimit) {
                jumpTime = 0f;
                canJump = true;
            }
        }
    }

    private void processGravityEffect(float deltaTime) {
        float gravity = 1f;
        if (jumpState) {
            System.out.println("Applying gravity");
            this.velocityY += (gravity * deltaTime) / 10;
        }
    }

    private void processAnimation() {
        if (this.velocityX == 0 && this.velocityY == 0) {
            this.setAnimation(this.animations.get(2));
            this.currentAnim.setCurrentFrameX(0);
            this.currentAnim.setCurrentFrameY(2);
            this.isWalking = false;
        } else if (this.velocityY != 0) {
            this.setJumpAnimation();
        }
    }

    private void changePosition(float deltaTime) {
        this.posY += (this.velocityY * deltaTime) / 20;
        this.posX += (this.velocityX * deltaTime) / 20;
    }

    private void updateCollisionPoints() {
        collisionPoints.set(0, new PointF(posX, posY));
        collisionPoints.set(1, new PointF(posX, getBottomY()));
        collisionPoints.set(2, new PointF(getRightX(), getBottomY()));
        collisionPoints.set(3, new PointF(getRightX(), posY));
        collisionPoints.set(4, new PointF(posX + width / 2, posY));
        collisionPoints.set(5, new PointF(posX + width / 2, getBottomY()));
        collisionPoints.set(6, new PointF(posX, posY + height / 2));
        collisionPoints.set(7, new PointF(getRightX(), posY + height / 2));
    }

    private void processCollision(BoundingBox anotherBox) {
        float moveX = this.getIntersectionWidth(anotherBox),
                moveY = this.getIntersectionHeight(anotherBox);

        if (this.velocityX != 0f && this.velocityY != 0f) {
            this.velocityX = 0f;
            if (!jumpState && this.velocityY < 0) {
                this.velocityY = 0f;
                this.posX += moveX;
            }
            this.posY += moveY;

        } else if (this.velocityX != 0f) {
            this.posX += moveX;
            this.velocityX = 0f;

        } else if (this.velocityY != 0f) {
            this.posY += moveY;
            this.velocityY = 0f;
        }
    }

    @Override
    public void preventCollision() {
        this.velocityY = 0.0f;
        this.velocityX = 0.0f;
        keys[KeyEvent.VK_W] = false;
        keys[KeyEvent.VK_S] = false;
        keys[KeyEvent.VK_A] = false;
        keys[KeyEvent.VK_D] = false;
        isWalking = false;
    }

    private void updateNextBox(float deltaTime) {
        nextBox.setPosition(posX + velocityX * deltaTime, posY + velocityY * deltaTime);
    }

    @Override
    public BoundingBox getNextBox() {
        return this.nextBox;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (!this.keys[e.getKeyCode()])
            this.keys[e.getKeyCode()] = true;

        if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_A)
            if (!isWalking)
                setWalkAnim();
            else if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S)
                if (!jumpState)
                    setJumpAnimation();

    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (this.keys[e.getKeyCode()])
            this.keys[e.getKeyCode()] = false;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
                break;
            case KeyEvent.VK_A:
                break;
            case KeyEvent.VK_W:
                break;
            case KeyEvent.VK_S:
                break;
            default:
                break;
        }

    }

    private void jump() {
        velocityY -= 31f;
        setAirFloating();
    }

    private void setAirFloating() {
        jumpState = true;
        canJump = false;
    }

    private void setJumpAnimation() {
        currentAnim = animations.get(1);
        currentAnim.setCurrentFrameY(1);
        currentAnim.setCurrentFrameX(7);
        currentAnim.setFirstPosX(7);
        currentAnim.setLastPosX(10);
    }

    private void setWalkAnim() {
        currentAnim = animations.get(0);
        currentAnim.setCurrentFrameY(2);
        currentAnim.setCurrentFrameX(1);
        currentAnim.setFirstPosX(1);
        currentAnim.setLastPosX(6);
        isWalking = true;
    }

}
