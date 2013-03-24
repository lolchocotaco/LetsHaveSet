package gameLogic;

import gameLogic.SetCard;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.slidinglayout.SLConfig;
import aurelienribon.slidinglayout.SLKeyframe;
import aurelienribon.slidinglayout.SLPanel;
import aurelienribon.slidinglayout.SLSide;

/**
 * @author Sameer
 * Creates the deck and allows for shuffling
 * Probably useless to have an entire class for the deck. Probably can be combined with the setCard class, but left separate for clarity. 
 */
public class SetTable{

	/*Animation stuff*/
	
	
	
	
	private Vector<SetCard> deck;
	private Vector<SetCard> onTable = new Vector<SetCard>(15);
	public SLPanel tableView = new SLPanel();
	
	
	public SetTable() {
		newDeck();
		tableView.setTweenManager(SLAnimator.createTweenManager());
	}
	
	/*
	 * Deck Functions:
	 * Creates New deck, clears on table vector
	 * Draw Card Functionality
	 * Get deckSize
	 * getElementAt
	 */
	public void  newDeck(){
		deck = new Vector<SetCard>(81);
		onTable = new Vector<SetCard>();
        for (int number=0; number<3; number++) {
            for (int symbol=0; symbol<3; symbol++) {
                for (int shading=0; shading<3; shading++) {
                    for (int color=0; color<3; color++) {
                    	deck.add(new SetCard(number, symbol, shading, color));
                    }
                }
            }
        }
        Collections.sort(deck);
	}
	
	public SetCard drawCard(){
		SetCard tCard = deck.remove(0);
		onTable.add(tCard);
		return tCard;
	}
	
	public int deckSize(){	return deck.size();	}
	public int tableSize() { return onTable.size();}
	public SetCard getElementAt(int pos){	return deck.elementAt(pos );}
	public SetCard getTableCard(int pos){ 	return onTable.elementAt(pos);	}
	public SetCard rmTableCard(int pos){	return onTable.remove(pos);}
	
	/*
	 * Returns the default layout of 3x4 grid. 
	 * Sets the appropriate action for associated grid location. 
	 * 
	 */
	public SLConfig defaultLayout(){
		SLConfig mainCfg = new SLConfig(tableView)
		.gap(10, 10)
		.row(150).row(150).row(150).col(100).col(100).col(100).col(100);
		
		for(int r = 0; r<3;r++){
			for(int c = 0;c<4; c++ ){
				SetCard tblCard = onTable.elementAt(r*4+c);
				tblCard.setAction(animationCreate(r, c));
				mainCfg.place(r,c,tblCard);
			}
		}
		return mainCfg;
	}
	
	//TODO need each config
	
	/*
	 * Create animation based on location in 3x4 default grid.  
	 * Adds card to onTable vector... 
	 * Transistions into new layout(without clicked card)
	 * Replaces the selected card in vector with newly added card to vector. 
	 */
	private Runnable animationCreate( final int row, final int col){
		Runnable pAction = new Runnable() {
			
			@Override
			public void run() {
				disableActions();
				
				drawCard();
				tableView.createTransition()
					.push(new SLKeyframe(createLayout(row, col), 1.6f)
//						.setEndSide(SLSide.BOTTOM, onTable.elementAt(1))
//						.setCallback(new SLKeyframe.Callback() {@Override public void done() {
//							onTable.elementAt(0).setAction(p1BackAction);
//							onTable.elementAt(0).enableAction();
//						}})
						)
					.play();
				
				tableReplace(row,col);
//				defaultLayout();
				
//				tableView.createTransition()
//				.push(new SLKeyframe(defaultLayout(), 1.6f)
////					.setEndSide(SLSide.BOTTOM, onTable.elementAt(1))
////					.setCallback(new SLKeyframe.Callback() {@Override public void done() {
////						onTable.elementAt(0).setAction(p1BackAction);
////						onTable.elementAt(0).enableAction();
////					}})
//					)
//				.play();
				enableActions();
				
			}
		};
		return pAction;
	}
	
	/*
	 * Adds the last card in onTable vector (index= 12) to the new position. 
	 * 
	 */
	private void tableReplace(int row, int col){
		System.out.println("Old Size of Table: " + Integer.toString(onTable.size()));
		SetCard newCard = onTable.remove(12);
		int vecPos = row*4+3;
		onTable.setElementAt(newCard, vecPos);
		System.out.println("Size of Table: " + Integer.toString(onTable.size()));
		System.out.println("Size of Deck: " + Integer.toString(deck.size()));
	}
	
	
	/*
	 * Generates "new" layout specific to each card clicked. 
	 * Each card in the grid will have a different "final" layout. 
	 */
	private SLConfig createLayout(int row, int col){
		int rowNum = row/3;
		System.out.println(rowNum);
		SLConfig tempLayout = new SLConfig(tableView)
			.gap(10, 10)
			.row(150).row(150).row(150).col(100).col(100).col(100).col(100);
			for(int r = 0; r<3;r++){
				for(int c = 0;c<4; c++ ){
					SetCard tblCard = onTable.elementAt(r*4+c);
					if (rowNum == row){
						if(r == row && c == col){
							tblCard = onTable.elementAt(12);
						}
						else{
							tblCard = onTable.elementAt(r*4+c+1);
						}
					}
					tblCard.setAction(animationCreate(r, c));
					tempLayout.place(r,c,tblCard);
				}
			}
//			.place(0, 0, onTable.elementAt(1))
//			.place(0, 1, onTable.elementAt(2))
//			.place(0, 2, onTable.elementAt(3))
//			.place(0, 3, onTable.elementAt(12))
//			.place(1, 0, onTable.elementAt(4))
//			.place(1, 1, onTable.elementAt(5))
//			.place(1, 2, onTable.elementAt(6))
//			.place(1, 3, onTable.elementAt(7))
//			.place(2, 0, onTable.elementAt(8))
//			.place(2, 1, onTable.elementAt(9))
//			.place(2, 2, onTable.elementAt(10))
//			.place(2, 3, onTable.elementAt(11));
		
		return tempLayout;
	}

	
	/*Disables actions*/
	private void disableActions() {
		for(int i = 0; i<onTable.size(); i++){
			onTable.elementAt(i).disableAction();
		}
	}
	/*Enables Actions*/
	private void enableActions(){
		for(int i = 0; i<onTable.size(); i++){
			onTable.elementAt(i).enableAction();
		}
	}
	
}

