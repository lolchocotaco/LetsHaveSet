package gameLogic;

import gameLogic.SetCard;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.slidinglayout.SLConfig;
import aurelienribon.slidinglayout.SLPanel;

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
		mainCfg = new SLConfig(tableView)
		.gap(10, 10)
		.row(150).row(150).row(150).col(100).col(100).col(100).col(100);
		
		for(int c = 0; c<4;c++){
			for(int r = 0;r<3; r++ ){
				mainCfg.place(r,c,onTable.elementAt(c*3+r));
			}
		}
//		.place(0, 0, onTable.elementAt(0))
//		.place(1, 0, onTable.elementAt(1))
//		.place(2, 0, onTable.elementAt(2))
//		.place(0, 1, onTable.elementAt(3));
		
//		.beginGrid(0, 0)
//			.row(1f).row(1f).row(1f).col(1f)
//			.place(0, 0, onTable.elementAt(0))
//			.place(1, 0, onTable.elementAt(1))
//			.place(2, 0, onTable.elementAt(2))
//		.endGrid();
//		.beginGrid(0, 1)
//			.row(1f).row(2f).col(1f)
//			.place(0, 0, onTable.elementAt(2))
//			.place(1, 0, onTable.elementAt(3))
//		.endGrid()
//		.place(0, 2, onTable.elementAt(4));
		tableView.initialize(mainCfg);
	}
	
	
}

//for(int c = 0; c<4;c++){
//for(int r = 0;r<3; r++ ){
//	mainCfg.place(r,c,onTable.elementAt(c*3+r));
//}
//}
