package gameLogic;

import gameLogic.SetCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sameer
 *
 */
public class SetDeck {

	/**
	 * Its all gone. 
	 */
	ArrayList<SetCard> cards = new ArrayList<SetCard>(81);
	
	
	public SetDeck() {
	ArrayList<SetCard> cardList = new ArrayList<SetCard>(81);
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
        cards = cardList;
	}
}
