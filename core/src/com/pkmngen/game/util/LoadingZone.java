package com.pkmngen.game.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Rectangle except provides Vector2 for bottom-left and top-right (for convenience).
 * Also includes some utilities that Rectangle doesn't have (getAll(), translate(),
 * diff(), allPositions()).
 */
public class LoadingZone extends Rectangle {
    
    public LoadingZone inner;

    public LoadingZone() {
        super();
//        this.inner.setSize(this.height-offset, this.width-offset);
//        Vector2 center = new Vector2();
//        this.getCenter(center);
//        this.inner.setCenter(center);
    }

    public LoadingZone(LoadingZone loadingZone) {
        super(loadingZone);
    }

    public Vector2 bottomLeft() {
        return new Vector2(this.x, this.y);
    }

    public Vector2 topRight() {
        return new Vector2(this.x+this.height, this.y+this.width);
    }

    public void translate(float x, float y) {
        Vector2 center = new Vector2();
        this.getCenter(center);
        center.add(x, y);
        this.setCenter(center);
        this.inner.setCenter(center);
    }
    
    // Idea: void diff(b), get area from b that isn't in this.
    // Merge them and then always check if in inner.
    public ArrayList<Vector2> diff(LoadingZone rect) {
//        LoadingZone merged = new LoadingZone(this);
//        merged.merge(rect);
        ArrayList<Vector2> allPositions = this.allPositions();
        for (Vector2 pos : new ArrayList<Vector2>(allPositions)) {
            if (rect.contains(pos.cpy().add(8, 8))) {
                allPositions.remove(pos);
            }
        }
        return allPositions;
    }

    /**
     * Idea: return an ArrayList<Vector2> of all tile positions start BL and ending TR.
     * TODO: having out of heap space errors here.
     */
    public ArrayList<Vector2> allPositions() {
//        System.out.println(this.bottomLeft());
//        System.out.println(this.topRight());
        ArrayList<Vector2> allPositions = new ArrayList<Vector2>();
        for (Vector2 position = this.bottomLeft(); position.y < this.topRight().y; position.add(16, 0)) {
            if (position.x > this.topRight().x) {
                position.add(0, 16);
                position.x = this.x-16;
                continue;
            }
            allPositions.add(new Vector2(position));
        }
        return allPositions;
    }

    /**
     * Idea: return an ArrayList<Object> of all Objects in a HashMap<Vector2, Object> contained in this loading zone.
     */
    public ArrayList<Object> getAll(HashMap<Vector2, Object> hashMap) {
        ArrayList<Object> objects = new ArrayList<Object>();
        for (Vector2 pos : hashMap.keySet()) {
            if (!this.contains(pos)) {
                continue;
            }
            objects.add(hashMap.get(pos));
        }
        return objects;
    }
}
