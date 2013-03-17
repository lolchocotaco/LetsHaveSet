package gameLogic;

import java.util.Random;

/**
 * @author Sameer
 * Holds the properties of each card
 *
 */
public class SetCard implements Comparable<SetCard>{
	
	/*
	 * Card Properties
	 * cardLock is associated with the posititioning In the deck
	 * all private
	 */
	private int color, number, shade, shape, cardLoc;
	private boolean selected;
	private Random random = new Random();

	/*
	 * Mapping the integer values to actual strings.
	 * Probably can be moved outside. 
	 */
    public static final String colorNames [] = {"red", "green", "blue"};
    public static final String numberNames [] = {"one", "two", "three"};
    public static final String shapeNames [] = {"diamond", "oval", "squiggle"};
    public static final String shadeNames [] = {"hollow", "shaded", "solid"};
    public static final String attributeNames [][] = {colorNames, numberNames, shapeNames, shadeNames};
    public static final String attributes [] = {"color", "number", "shape", "shade"};

    /*
     * Constructor
     * Sets cardLoc to a random float value
     */
    public SetCard (int c, int n, int sp, int sd) {
		color = c;
		number = n;
		shape = sp;
		shade = sd;
		selected = false;
		cardLoc = (int)( Math.random() *81);
    }
    
    /*
     * Allows us to use the Collections.sort method. 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SetCard otherCard) {
    	 if (this.cardLoc == ( otherCard).cardLoc)
             return 0;
         else if ((this.cardLoc) > (otherCard).cardLoc)
             return 1;
         else
             return -1;
    	 }
    

    /*      getFunctions     */
    public String getColor(){
    	return colorNames[color];
    }
    public String getNumber(){
    	return numberNames[number];
    }
    public String getShape(){
    	return shapeNames[shape];
    }
    public String getShade(){
    	return shadeNames[shade];
    }
    public String toString(){
    	return "SetCard (" + color + ", " + number + ", " + shape + ", " + shade + ")";
    }
    /*Allows for easy checking of attributes*/
    public int attr (int i) {
    	switch (i) {
    	case 0:
    	    return color;
    	case 1:
    	    return number;
    	case 2:
    	    return shape;
    	case 3:
    	    return shade;
    	default:
    	    return -1;
    	}
    }
}
