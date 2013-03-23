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
	private SLConfig mainCfg;
	
	
	
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
	public SetCard getElementAt(int pos){	return deck.elementAt(pos );}
	public SetCard getTableCard(int pos){ 	return onTable.elementAt(pos);}
	public SetCard rmTableCard(int pos){	return onTable.remove(pos);}
	
	public  void setLayout(){
		System.out.println("Setting main Layout");
		mainCfg = new SLConfig(tableView)
		.gap(10, 10)
		.row(150).row(150).row(150).col(100).col(100).col(100).col(100);
		
		for(int r = 0; r<3;r++){
			for(int c = 0;c<4; c++ ){
				SetCard tblCard = onTable.elementAt(r*4+c);
//				tblCard.setAction(p1Action);
				mainCfg.place(r,c,tblCard);
			}
		}
		tableView.initialize(mainCfg);
	}
	
	//TODO need each config
	
//	private final Runnable p1Action = new Runnable() {@Override public void run() {
//		
//		onTable.elementAt(0).disableAction();
//
//		tableView.createTransition()
//			.push(new SLKeyframe(createLayout(0), 0.6f)
//				.setEndSide(SLSide.BOTTOM, onTable.elementAt(1))
//				.setCallback(new SLKeyframe.Callback() {@Override public void done() {
//					onTable.elementAt(0).setAction(p1BackAction);
//					onTable.elementAt(0).enableAction();
//				}}))
//			.play();
//	}};
//	
//	private final Runnable p1BackAction = new Runnable() {@Override public void run() {
//		onTable.elementAt(0).disableAction();
//
//		tableView.createTransition()
//			.push(new SLKeyframe(mainCfg, 0.6f)
//				.setStartSide(SLSide.BOTTOM, onTable.elementAt(1))
//				.setCallback(new SLKeyframe.Callback() {@Override public void done() {
//					onTable.elementAt(0).setAction(p1Action);
//					onTable.elementAt(0).enableAction();
//				}}))
//			.play();
//	}};
//	
//	private SLConfig createLayout(int cardNum){
//		SLConfig tempLayout = new SLConfig(tableView)
//			.gap(10, 10)
//			.row(150).row(150).row(150).col(100).col(100).col(100).col(100)
//			.place(0, 0, onTable.elementAt(1))
//			.place(0, 1, onTable.elementAt(2))
//			.place(0, 2, onTable.elementAt(3))
//			.place(0, 3, onTable.elementAt(4))
//			.place(1, 0, onTable.elementAt(4))
//			.place(1, 1, onTable.elementAt(5))
//			.place(1, 2, onTable.elementAt(6))
//			.place(1, 3, onTable.elementAt(7))
//			.place(2, 0, onTable.elementAt(8))
//			.place(2, 1, onTable.elementAt(9))
//			.place(2, 2, onTable.elementAt(10))
//			.place(2, 3, onTable.elementAt(11));
//		
//		return tempLayout;
//	}

}

