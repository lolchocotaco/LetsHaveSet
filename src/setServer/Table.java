package setServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Table {
	public String name;
	public int numPlayers;
	public int maxPlayers;
	public Map<Object, Integer> players = null; // userId --> score
	public int numGoPressed;
	public List<Card> deck;
	public List<Card> onTable;
	
	public Table(String name, int numPlayers, int maxPlayers) {
		this.name = name;
		this.numPlayers = numPlayers;
		this.maxPlayers = maxPlayers;
		this.players = new HashMap<Object, Integer>();
		this.numGoPressed = 0;
		this.deck = new ArrayList<Card>();
		this.onTable = new ArrayList<Card>();
	}
	
	public String status() {
		if(numGoPressed == maxPlayers) {
			return "Playing";
		}
		if(numPlayers < maxPlayers) {
			return "Open";
		} else {
			return "Full";
		}
	}
	
	public void addPlayer(int userID) {
		numPlayers++;
		players.put(userID, 0);
	}
	
	public void removePlayer(int userID) {
		numPlayers--;
		players.remove(userID);
	}
	
	public String playerString(Map<Object, User> userMap) {
		String out = "P;" + numPlayers + ";" + maxPlayers;
		Iterator<Entry<Object, Integer> > it = players.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
			int userID = (Integer) entry.getKey();
			String username = userMap.get(userID).username;
			int score = (Integer) entry.getValue();
			out += ";" + username + ";" + score;
		}
		return out;
	}
	
	public void resetScores() {
		Iterator<Entry<Object, Integer> > it = players.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
			entry.setValue(0);
		}
	}
	
	public void initializeDeck() {
		deck.clear();
		for(int i = 0; i<81; i++) {
			deck.add(new Card(i));
		}
		Collections.sort(deck);
		onTable.clear();
		for(int i = 0; i<12; i++) {
			onTable.add(deck.get(0));
			deck.remove(0);
		}
	}
	
	public String tableString() {
		String out = "T;" + onTable.size();
		Iterator<Card> it = onTable.iterator();
		while(it.hasNext()) {
			out += ";" + it.next().cardNum;
		}
		return out;
	}
	
	public boolean setExists(int C1, int C2, int C3) {
		boolean b1 = false;
		boolean b2 = false;
		boolean b3 = false;
		Iterator<Card> it = onTable.iterator();
		while(it.hasNext()) {
			int C = it.next().cardNum;
			if(C == C1) b1 = true;
			if(C == C2) b2 = true;
			if(C == C3) b3 = true;
		}
		return (b1 && b2 && b3);
	}
	
	public int[] removeSet(int C1, int C2, int C3) { // returns {-1} if there is no more cards, {C1, C2, C3} if 3 new cards
		for(int i = 0; i < onTable.size(); i++) {
			int C = onTable.get(i).cardNum;
			if((C == C1)||(C == C2)||(C == C3)) {
				onTable.remove(i);
				i--; // Otherwise it will skip indices
			}
		}
		
		if(onTable.size() < 12) {
			return newCards();
		} else {
			int[] x = {-1, -1, -1};
			return x;
		}
	}
	
	public int[] newCards() {
		if(deck.size() == 0) {
			int[] out = {1, -1, -1};
			return out;
		} else {
			onTable.add(deck.get(0));
			onTable.add(deck.get(1));
			onTable.add(deck.get(2));
			int[] out = {deck.get(0).cardNum, deck.get(1).cardNum, deck.get(2).cardNum};
			deck.remove(0); deck.remove(0); deck.remove(0);
			return out;
		}
	}
	
	public boolean noMoreSets() {
		for(int i = 0; i < onTable.size(); i++) {
			int i4 = onTable.get(i).cardNum;
			int i1 = i4%3; i4/=3;
			int i2 = i4%3; i4/=3;
			int i3 = i4%3; i4/=3;
			for(int j = i+1; j < onTable.size(); j++) {
				int j4 = onTable.get(j).cardNum;
				int j1 = j4%3; j4/=3;
				int j2 = j4%3; j4/=3;
				int j3 = j4%3; j4/=3;
				for(int k = j+1; k < onTable.size(); k++) {
					int k4 = onTable.get(k).cardNum;
					int k1 = k4%3; k4/=3;
					int k2 = k4%3; k4/=3;
					int k3 = k4%3; k4/=3;
					if((((i1+j1+k1) % 3) == 0) && (((i2+j2+k2) % 3) == 0) && (((i3+j3+k3) % 3) == 0) && (((i4+j4+k4) % 3) == 0)) {
						return false;
					}
					
				}
			}
		}
		
		return true;
	}
	
}
