package setServer;

public class Card implements Comparable<Card>{
	public double index;
	public int cardNum;
	public Card(int cardNum) {
		this.index = Math.random();
		this.cardNum = cardNum;
	}
	
	@Override
	public int compareTo(Card otherCard) {
		if (this.index == otherCard.index)
	        return 0;
	    else if (this.index > otherCard.index)
	        return 1;
	    else
	        return -1;
	}
}