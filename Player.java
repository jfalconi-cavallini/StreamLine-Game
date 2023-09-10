/*
 Author: Jose Falconi
 Email: jfalconi@ucsd.edu
 CS8B Login: cs8bwaaw
 Date: 3/6/19
 File: Player.java
 Sources of Help: piazza, tutors, PSA6 writeup
 
 This file is meant to create method a Player object for the game
 Streamline.
 It is the solution to CSE8B PSA6.
 */

//Keep these line. Tells java what classes it needs.
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;

/**
 * A class that has a constructor method for a Player object, where
 * the color is defined, the stroke, and stroke type; It also has a
 * method to set the size of the object.
 */
public class Player extends RoundedSquare {
    // Size of the stroke when Player is drawn
    final static double STROKE_FRACTION = 0.1;
    
    /**
     * Constructor method that creates a Player object that has a set color,
     * a set stroke, and stroke type.
     * @param none
     * @return none, constructor method
     */
    public Player() {
        //set a fill color, a stroke color, and set the stroke type to
        //centered
        setFill(Color.RED);
        setStroke(Color.ORANGE);
        setStrokeType(StrokeType.CENTERED);
    }
    
    /**
     * Instance method that set's the size of a player object.
     * @param size type double
     * @return none
     * @SpecialInstances changes the size of a Player object and
     * set's the width of the stroke.
     */
    @Override
    public void setSize(double size) {
        //1. update the stroke width based on the size and 
        //STROKE_FRACTION
        //2. call super setSize(), bearing in mind that the size
        //parameter we are passed here includes stroke but the
        //superclass's setSize() does not include the stroke
        setStrokeWidth(size * STROKE_FRACTION);
        super.setSize(size);
    }
}
