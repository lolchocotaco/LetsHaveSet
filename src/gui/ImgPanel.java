package gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import aurelienribon.slidinglayout.SLAnimator;

@SuppressWarnings("serial")
public class ImgPanel extends JPanel{
	private static final String checkPath = "images/Check.png";
	private static final String xPath = "images/bigX.png";
	private BufferedImage dispImage = null;
	
	public ImgPanel(int type){
		try {
			if(type ==1){
				dispImage = ImageIO.read(new File(checkPath));
			}else {
				dispImage = ImageIO.read(new File(xPath));
			}
		} catch (IOException e) {
			// Shouldn't have a problem
		}	
	}
	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
//        g.drawImage(dispImage, 0, 0, this.getWidth(), this.getHeight(), null);
        this.add(new JLabel(new ImageIcon(dispImage.getScaledInstance(this.getWidth(), this.getHeight(), 0))));
    }	
    
    
    
	public static class imgAccessor extends SLAnimator.ComponentAccessor {
		public static final int SCALE = 100;
		
		@Override
		public int getValues(Component target, int tweenType, float[] returnValues) {
			int ret = super.getValues(target, tweenType, returnValues);
			if( ret >= 0) return ret ;
			switch( tweenType){
			case SCALE:
				returnValues[0] = target.getX();
				returnValues[1] = target.getY();
				returnValues[2] = target.getWidth();
				returnValues[3] = target.getHeight();
				return 4;
			default: return -1;
			}
			
		}

		@Override
		public void setValues(Component target, int tweenType, float[] newValue) {
			super.setValues(target, tweenType, newValue);
			
		switch(tweenType){
			case SCALE:
			target.setBounds(Math.round(newValue[0]), Math.round(newValue[1]), Math.round(newValue[2]), Math.round(newValue[3]));
			target.validate();
			target.repaint();
			break;
			}
		}
	}

}
