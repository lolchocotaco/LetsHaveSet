package gameLogic;

import gameLogic.SetCard;

import java.util.Collections;
import java.util.Vector;

/**
 * @author Sameer
 * Creates the deck and allows for shuffling
 * Probably useless to have an entire class for the deck. Probably can be combined with the setCard class, but left separate for clarity. 
 */
public class SetDeck {

	public Vector<SetCard> deck = new Vector<SetCard>(81);
	public Vector<SetCard> onTable = new Vector<SetCard>();
	
	public SetDeck() {
		Vector<SetCard> cardList = new Vector<SetCard>(81);
        for (int number=0; number<3; number++) {
            for (int symbol=0; symbol<3; symbol++) {
                for (int shading=0; shading<3; shading++) {
                    for (int color=0; color<3; color++) {
                    	cardList.add(new SetCard(number, symbol, shading, color));
                    }
                }
            }
        }
        Collections.sort(cardList);
        deck = cardList;
	}
}
