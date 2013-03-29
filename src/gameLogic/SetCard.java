package gameLogic;

import java.util.Random;

import javax.print.attribute.AttributeSetUtilities;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Quad;

/**
 * @author Sameer
 * Holds the properties of each card
 *
 */
public class SetCard extends JPanel implements Comparable<SetCard>{
	
	/*
	 * Panel Properties
	 */
	private static final Color FG_COLOR = new Color(0xFFFFFF);
	private static final Color BG_COLOR = new Color(0x3B5998);
	private static final Color BORDER_COLOR = new Color(0x000000);
	private static final float baseScale = 1f;
	private static final float bigScale = 1.1f;
	private int borderThickness = 2;
	private int height = 150;
	private int width = 100;
	private float scaleXY = baseScale;
	private float opacity = 100;
	private boolean hover = false;
	private boolean actionEnabled = true;
	private Runnable clickAction, unClickAction;
	private final JTextArea cardInfo = new JTextArea();
	private static final TweenManager tweenManager = SLAnimator.createTweenManager();
	
	
	
	/*
	 * Card Properties
	 * cardLock is associated with the positioning In the deck
	 * all private
	 */
	private int color, number, shade, shape, cardLoc;
	private boolean selected = false;
	private Random random = new Random();

	/*
	 * Mapping the integer values to actual strings.
	 * Probably can be moved outside.  
	 * cannot be changed. 
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
		cardLoc = (int)( random.nextFloat() * 81);
		setBackground(BG_COLOR);
//		setLayout(new BorderLayout());
		
		clickAction = new Runnable() {
			@Override
			public void run() {
				disableAction();
				grow();
				enableAction();
			}
		};
		unClickAction = new Runnable() {
			@Override
			public void run() {
				disableAction();
				shrink();
				enableAction();
			}
		};
				
				
				
				
		setPreferredSize(new Dimension(width,height));
		setSize(width, height);
		
		
		String cardString = "Color: " + colorNames[c] + "\nNumber: " + numberNames[n] + "\nShape: " + shapeNames[sp]+ "\nShade: " +shadeNames[sd];
		cardInfo.setText(cardString);
		cardInfo.setEditable(false);
		cardInfo.setEnabled(false);
		cardInfo.setBackground(null);
		cardInfo.setForeground(FG_COLOR);
		cardInfo.setVisible(false);
		add(cardInfo);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				if (actionEnabled) showBorder();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hover = false;
				if(!selected)
					hideBorder();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (actionEnabled) {
					if(selected)
						unClickAction.run();
					else
						clickAction.run();
				}
			}
			
		});
		
    }//End Constructor
    
    
	public void setAction(Runnable action) {this.clickAction = action;}
	public void enableAction() {actionEnabled = true; if (hover) showBorder();}
	public void disableAction() {actionEnabled = false;}

   
	private void showBorder() {
		tweenManager.killTarget(borderThickness);
		Tween.to(SetCard.this, Accessor.BORDER_THICKNESS, 0.4f)
			.target(10)
			.start(tweenManager);
	}

	private void hideBorder() {
		tweenManager.killTarget(borderThickness);
		Tween.to(SetCard.this, Accessor.BORDER_THICKNESS, 0.4f)
			.target(2)
			.start(tweenManager);
	}
	
	private void grow(){
		selected = true;
		scaleXY = bigScale;
		Timeline.createSequence()
			.beginParallel()
			.push(	Tween.to(SetCard.this, Accessor.XYWH, 0.5f)
					.targetRelative((1-scaleXY)*width/2, (1-scaleXY)*height/2, (scaleXY-1)*width, (scaleXY-1)*height)
					.ease(Quad.OUT) )
			.end()
			.start(tweenManager);
	}
	
	private void shrink(){
		selected = false;
		Tween.to(SetCard.this, Accessor.XYWH, 0.5f)
			.targetRelative(-(1-scaleXY)*width/2, -(1-scaleXY)*height/2, -(scaleXY-1)*width, -(scaleXY-1)*height)
			.ease(Quad.OUT)
			.start(tweenManager);
		scaleXY = baseScale;
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
    
    /*
     * Border Animation
     */
    @Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gg = (Graphics2D) g;

		int w = getWidth();
		int h = getHeight();
		
		int t = borderThickness;
		gg.setColor(BORDER_COLOR);
		gg.fillRect(0, 0, t, h-1);
		gg.fillRect(0, 0, w-1, t);
		gg.fillRect(0, h-1-t, w-1, t);
		gg.fillRect(w-1-t, 0, t, h-1);
	}
    
    private float getOpacity(){
    	return opacity;
    }
    private void setOpacity(float newVal){
    	opacity = newVal;
    }
    
    public void unSelect(){
    	unClickAction.run();
    	hideBorder();
    }
     
    /*Animation 2*/
   
	public static class Accessor extends SLAnimator.ComponentAccessor {
		public static final int BORDER_THICKNESS = 100;
		public static final int OPACITY = 101;

		@Override
		public int getValues(Component target, int tweenType, float[] returnValues) {
			SetCard tp = (SetCard) target;

			int ret = super.getValues(target, tweenType, returnValues);
			if (ret >= 0) return ret;

			switch (tweenType) {
				case BORDER_THICKNESS: 
					returnValues[0] = tp.borderThickness; 
					return 1;
				case OPACITY:
					returnValues[0] = tp.getOpacity();
					return 1;					
				default: return -1;
			}
		}

		@Override
		public void setValues(Component target, int tweenType, float[] newValues) {
			SetCard tp = (SetCard) target;

			super.setValues(target, tweenType, newValues);

			switch (tweenType) {
				case BORDER_THICKNESS:
					tp.borderThickness = Math.round(newValues[0]);
					tp.repaint();
					break;
				case OPACITY:
					tp.setOpacity(newValues[0]);
				default: assert false;
					
			}
		}
	}   
    
}
