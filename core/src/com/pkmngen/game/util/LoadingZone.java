package com.pkmngen.game.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Rectangle except provides Vector2 for bottom-left and top-right (for convenience)
 */
public class LoadingZone extends Rectangle {

    public LoadingZone() {
        super();
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
    }
    
    // Idea: void diff(b), get area from b that isn't in this.
    // didn't do because how do you get diff when b is larger than this in both x and y?

    /**
     * Idea: return an ArrayList<Vector2> of all tile positions start BL and ending TR.
     */
    public ArrayList<Vector2> allPositions() {
        ArrayList<Vector2> allPositions = new ArrayList<Vector2>();
        for (Vector2 position = this.bottomLeft(); position.y < this.topRight().y; position.add(16, 0)) {
            allPositions.add(position);
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
