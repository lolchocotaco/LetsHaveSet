package examples;

/*
  A Free Software implementation of the Set card game
  Copyright 1998, 1999, 2002, 2003, 2004
  David M. Turner <novalis atsign gnu.org>
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA 

  Thanks to Gary Wong <gtw atsign gnu.org> for curves and Regyt <regyt
  atsign omniheurist.net> and many more for playtesting.  Cassia
  <cimartin at fas.harvard.edu> suggested single player mode.  Janet
  Casey <jcasey atsign gnu.org> suggested the configurator. I forget
  who suggested other things, but I thank them anyway.

  Version: 3

  Todo: 
  Computer finds easier sets faster?
  Karl showset mode
  Fix layout and sizing bugs

*/

import java.lang.System;
import java.util.Vector;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import java.net.*;

public class SetApplet extends Applet implements MouseListener, KeyListener {
    private Panel center;

    private Button players [];
    private Button cheat, help, restart, config;

    private Label scoreLabel [];
    private Label timeLabel [];

    public static final int NUM_PLAYERS = 5;

    private int activePlayer = -1; //index of last player to click a 

    private Vector deck;
    private Vector onTable;

    private int score [];
    private long times [];

    private SetCard card1, card2, card3;

    public static final SetCard nullCard = new SetCard (-100, -100, -100, -100);

    private long turnStart, pauseStart;
    private Configurator configurator = null;

    private Label status = new Label ();
    private Label cardsLeft;

    private DeselectTimer deselectTimer;
    private ComputerPlayer computerPlayer;
    public Disposer disposer;


    private boolean computerSelecting = false;
    private boolean gameover = false;
    private boolean buzzed = false;

    private Statistics stats;

    public void init () {

	score = new int [NUM_PLAYERS];
	scoreLabel = new Label [NUM_PLAYERS];
	timeLabel = new Label [NUM_PLAYERS];
	times = new long [NUM_PLAYERS];
	players = new Button [NUM_PLAYERS];

	stats = new Statistics ();

	initBoard ();
   
	addKeyListener (this);
	requestFocus ();

	String arg, base = getDocumentBase ().toString ();
	int qidx;
	if ((qidx = base.indexOf ('?')) != -1)
	    arg = base.substring (qidx + 1);
	else 
	    arg = "";
	try {
	    Config.main = new Config (arg);
	} catch (RuntimeException e) {
	    Config.main = new Config ();
	}

	deselectTimer = new DeselectTimer (this);
	deselectTimer.start ();

	computerPlayer = new ComputerPlayer (this);
	computerPlayer.start ();

	disposer = new Disposer ();
	disposer.start ();

	startGame ();
	
    }

    public void destroy () {
	SetHelpFrame.instance (this).dispose ();
    }

    //this class implements a computer player that selects and removes a set
    //at configurable intervals
    private class ComputerPlayer extends Thread {
	private long nextComputerSetTime;
	private long deselectTime;
	private int state;
	private SetApplet game;
	private boolean paused = false;

	private final int STATE_LOOKING_FOR_SET = 0;
	private final int STATE_SHOWING_SET = 1;

	public ComputerPlayer (SetApplet g) {
	    game = g;
	}

	public void pause () {
	    paused = true;
	}

	public void unpause () {
	    startTurn ();
	    paused = false;
	}

	public void humanGotSet () {
	    nextComputerSetTime += (long) (Config.main.getFloat ("humanSetPenalty") * 1000.0);
	}

	public void startTurn () {
	    long time = System.currentTimeMillis ();
	    float minSpeed = Config.main.getFloat ("minSpeed");
	    float maxSpeed =  Config.main.getFloat ("maxSpeed");
	    float speedRange = Math.abs (maxSpeed - minSpeed);
	    nextComputerSetTime = (long) (time + (Math.random () * speedRange + minSpeed) * 1000.0);
	    state = STATE_LOOKING_FOR_SET;
	    game.computerSelecting = false;
	}

	private synchronized void doShowSet (long time) {
	    //fixme: this depends on internals of cheat and removeSet
	    //being the same

	    cheat ();
	    game.computerSelecting = true;
	    deselectTime = time + (long) (Config.main.getFloat ("showSetTime") * 1000);
	    state = STATE_SHOWING_SET;
	}

	public void run () {
	    while (true) {
		yield ();
		long time = System.currentTimeMillis ();
		if (paused || !Config.main.getBoolean ("singlePlayerMode")) continue; 
		
		//check if it's time for the computer to play
		if (state == STATE_LOOKING_FOR_SET && 
		    time > nextComputerSetTime) {

		    if (game.card1 == nullCard && game.card2 == nullCard && 
			game.card3 == nullCard && !buzzed) {
			//player has no cards selected

			doShowSet (time);
		    }
		}
		if (state == STATE_SHOWING_SET && time > deselectTime) {
		    game.activePlayer = 1;
		    //find and remove a set
		    removeSet ();
		    game.activePlayer = 0;
		    startTurn ();
		}
	    }
	}
    }


    //Sun's JRE 1.4.2 seems to be quite slow at disposing some
    //windows.  I can't see a good solution for this -- it's clear
    //that the slowdown is in Sun's code, and I've got no interest
    //in debugging Sun's code -- I just want Kaffe and this applet to work.
    //so, we'll do the disposal from a separate thread;
    //(we may later want to move other housekeeping into this thread)
    class Disposer extends Thread {

	LinkedList queue;
	public Disposer () {
	    queue = new LinkedList ();
	}
	public synchronized void dispose (Window w) {
	    queue.addLast (w);
	}

	public synchronized Window getWindow () {
	    if (queue.isEmpty ()) 
		return null;
	    return (Window) queue.getFirst ();
	}
	public void run () {
	    while (true) {
		yield ();
		Window w = getWindow ();
		if (w != null) 
		    w.dispose ();
	    }
	}

    }

    private class DeselectTimer extends Thread {
	private long startTime;
	private SetApplet game;

	//this feature protects against slow delayed class loading
	//causing timeouts between the final card click and the stat gathering 
	//and card removal
	private boolean paused = false;

	public DeselectTimer (SetApplet g) {
	    click ();
	    game = g;
	}

	public void pause () {
	    paused = true;
	}

	public void unpause () {
	    paused = false;
	}

	public void click () {
	    startTime = System.currentTimeMillis ();
	}

	public void run () {
	    while (true) {
		yield ();
		long time = System.currentTimeMillis ();
		if (paused) continue; //unless we're paused

		//deselect cards after five seconds
		if (time - startTime > 5000) {
		    click ();
		    for (int i = 0; i < center.getComponentCount (); i++) {
			SetCard card = (SetCard) center.getComponent (i);
			card.setSelected (false);
		    }
		    game.card1 = nullCard;
		    game.card2 = nullCard;
		    game.card3 = nullCard;
		    unbuzz ();
		}
	    }	  

	}
    }

    private void unbuzz () {
	status.setText ("");
	buzzed = false;
    }

    private void initBoard () {

	setLayout (new BorderLayout ());

	Panel top = new Panel (new GridLayout (0, NUM_PLAYERS));
	center = new Panel (new GridLayout (0, 6));
	GridBagLayout gridbag = new GridBagLayout ();
	Panel bottom = new Panel (gridbag);
	add ("North", top);
	add ("Center", center);
	add ("South", bottom);

	//player scores and buttons

	for (int i = 0; i < NUM_PLAYERS; i++) {
	    players [i] = new Button ("Player " + (i + 1));
	    top.add (players [i]);
	    players [i].addMouseListener (this);
	}

	//why yes, this does have to be two separate loops, b/c there's no
	//good way to place a component at a specific place in a
	//gridlayout.  I hate AWT.  I hate AWT.  I hate AWT.
	for (int i = 0; i < NUM_PLAYERS; i++) {
	    scoreLabel [i] = new Label ("0 sets");
	    top.add (scoreLabel [i]);    
	}
	for (int i = 0; i < NUM_PLAYERS; i++) {
	    timeLabel [i] = new Label ("Average Time: 0");
	    top.add (timeLabel [i]);
	}

	//bottom buttons

	GridBagConstraints gbc = new GridBagConstraints ();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1;
	bottom.add (status, gbc);

	cheat = makeButton (bottom, "Cheat!");
	help = makeButton (bottom, "Help");
	restart = makeButton (bottom, "Restart");
	config = makeButton (bottom, "Configure");

	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;    

	cardsLeft = new Label ("   27 sets left");
	bottom.add (cardsLeft, gbc);
    }

    Button makeButton (Container c, String text) {
	GridBagConstraints gbc = new GridBagConstraints ();
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0;

	Button b = new Button (text);
	c.add (b, gbc);
	b.addMouseListener (this);

	return b;
    }

    //deals a new card off the deck and puts it on the table
    private void dealCard () {

	SetCard deal;
	deal = (SetCard) deck.elementAt (0);
	deck.removeElementAt (0);
	onTable.addElement (deal);
	center.add (deal);
	deal.addMouseListener (this);

    }

    //same, but puts it at a specific place on the table
    private void dealCard (int pos) {

	SetCard deal;
	deal = (SetCard) deck.elementAt (0);
	deck.removeElementAt (0);
	onTable.addElement (deal);
	center.add (deal, pos);
	deal.addMouseListener (this);

    }

    private void shuffle (Vector d) {

	int len = d.size ();
	for (int i = 0; i < len; i++) {
	    Object temp;
	    int j = (int) (Math.random () * len);
	    temp = d.elementAt (j);
	    d.setElementAt (d.elementAt (i), j);
	    d.setElementAt (temp, i);
	}
    } 

    //do the three cards passed in constitute a set?
    public static boolean isSet (SetCard a, SetCard b, SetCard c) {
	int cl, nb, sd, sp;

	if (a == b || a == c || b == c || a == nullCard || b == nullCard || c
	    == nullCard)
	    return false;

	cl = (a.color + b.color + c.color) % 3;
	nb = (a.number + b.number + c.number) % 3;
	sd = (a.shade + b.shade + c.shade) % 3;
	sp = (a.shape + b.shape + c.shape) % 3;
	if (cl == 0 && nb == 0 && sd == 0 && sp == 0)
	    return true;
	else
	    return false;
    }

    //generate a human-readable description of why the three cards passed in are not a set.

    public static String whyNotSet (SetCard a, SetCard b, SetCard c) {
	String out = "there are ";
	boolean and = false;

	if ((a.color + b.color + c.color) % 3 != 0) {
	    and = true;
	    out += 	"two ";
	    if (a.color == b.color || a.color == c.color) {
		out += a.colorName ();
	    } else {
		out += b.colorName ();
	    }
	    out += "s";
	}

	if ((a.number + b.number + c.number) % 3 != 0) {
	    if (and) {
		out += " and ";
	    }
	    and = true;
	    out += 	"two ";
	    if (a.number == b.number || a.number == c.number) {
		out += a.numberName ();
	    } else {
		out += b.numberName ();
	    }
	    out += "s";
	}

	if ((a.shape + b.shape + c.shape) % 3 != 0) {
	    if (and) {
		out += " and ";
	    }
	    and = true;
	    out += 	"two ";
	    if (a.shape == b.shape || a.shape == c.shape) {
		out += a.shapeName ();
	    } else {
		out += b.shapeName ();
	    }
	    out += "s";
	}

	if ((a.shade + b.shade + c.shade) % 3 != 0) {
	    if (and) {
		out += " and ";
	    }
	    and = true;
	    out += 	"two ";
	    if (a.shade == b.shade || a.shade == c.shade) {
		out += a.shadeName ();
	    } else {
		out += b.shadeName ();
	    }
	}
	return out;
    }


    //is there a set on the table at all, or do we need to add more
    //cards or end the game?

    boolean isSetOnTable () {
	int i, j, k;
	for (i = 0; i < onTable.size () - 2; i++) {
	    for (j = i + 1; j < onTable.size () - 1; j++) {
		for (k = j + 1; k < onTable.size (); k++) {
		    if (isSet ((SetCard) onTable.elementAt (i), (SetCard)
			       onTable.elementAt (j), (SetCard)
			       onTable.elementAt (k))) {
			return true;
		    }
		}
	    }
	} 
	return false;
    }

    //Returns true if the board has at least one set on it and 12 or
    //more cards, or the deck is empty.
    public boolean liveboard () {

	if (deck.size () == 0)
	    return true;

	if (onTable.size () < 12)
	    return false;

	return isSetOnTable ();
    
    }

    private void clearcards (SetCard carda, SetCard cardb, SetCard cardc) {

	carda.setSelected (false);
	cardb.setSelected (false);
	cardc.setSelected (false);
    } 

    public void startGame () {

	cardsLeft.setText ("   27 sets left");
	if (Config.main.getBoolean ("singlePlayerMode"))
	    status.setText ("Select three cards");
	else
	    status.setText ("Select a player.");

	for (int i = 0; i < NUM_PLAYERS; i++) {
	    score [i] = 0;
	    times [i] = 0;
	    timeLabel [i].setText ("Average Time: 0.0");
	} 
    
	deck = new Vector ();
	//create the deck
	for (int c = 0; c < 3; c++) {
	    for (int n = 0; n < 3; n++) {
			for (int sp = 0; sp < 3; sp++) {
			    for (int sd = 0; sd < 3; sd++) {
				deck.addElement (new SetCard (c, n, sp, sd));
			    }
			}
	    }
	} 

	center.removeAll ();

	onTable = new Vector ();
	shuffle (deck);

	for (int i = 0; i < 12; i++) {
	    dealCard ();
	}

	while (!liveboard ()) {
	    dealCard ();
	    dealCard ();
	    dealCard ();
	}

	for (int i = 0; i < NUM_PLAYERS; i++) {
	    scoreLabel [i].setText ("0 sets");
	    timeLabel [i].setText ("Average Time: 0");
	}

	card1 = nullCard;
	card2 = nullCard;
	card3 = nullCard;

	validate ();
	gameover = false;
	turnStart = System.currentTimeMillis ();
	computerPlayer.startTurn ();

    }

    private void cheat () {
	SetCard carda, cardb, cardc;

	clearcards (card1, card2, card3);
	card1 = nullCard;
	card2 = nullCard;
	card3 = nullCard;

	for (int i = 0; i < onTable.size () - 2; i++) {
	    for (int j = i + 1; j < onTable.size () - 1; j++) {
		for (int k = j + 1; k < onTable.size (); k++) {

		    carda = (SetCard) onTable.elementAt (i);
		    cardb = (SetCard) onTable.elementAt (j);
		    cardc = (SetCard) onTable.elementAt (k);

		    if (isSet (carda, cardb, cardc)) {
			carda.setSelected (true);
			cardb.setSelected (true);
			cardc.setSelected (true);

			return;
		    }
		}
	    }
	}
    }

    //for debugging and computer player
    private void removeSet () {
	SetCard carda, cardb, cardc;

	clearcards (card1, card2, card3);
	card1 = nullCard;
	card2 = nullCard;
	card3 = nullCard;

	for (int i = 0; i < onTable.size () - 2; i++) {
	    for (int j = i + 1; j < onTable.size () - 1; j++) {
		for (int k = j + 1; k < onTable.size (); k++) {

		    card1 = (SetCard) onTable.elementAt (i);
		    card2 = (SetCard) onTable.elementAt (j);
		    card3 = (SetCard) onTable.elementAt (k);

		    if (isSet (card1, card2, card3)) {
			if (activePlayer == -1) 
			    activePlayer = 0;
			gotSet ();
			return;
		    }
		}
	    }
	}
    }

    private void selectPlayer (int i) {
	activePlayer = i;
	clearcards (card1, card2, card3);
	card1 = nullCard;
	card2 = nullCard;
	card3 = nullCard;
	status.setText ("Player " + (i + 1) + ", select three cards.");
    }

    public void doneConfig () {

	disposer.dispose (configurator);

	configurator = null;
	if (Config.main.getBoolean ("singlePlayerMode") && activePlayer == -1)
	    activePlayer = 0;
	//unblank cards
	for (int i = 0; i < onTable.size (); i++) {
	    SetCard card = (SetCard) onTable.elementAt (i);
	    card.isBlank = false;
	    card.repaint ();
	}

	//force a repaint (works around bug in kaffe)
	validate ();

	unpause ();
	long time = System.currentTimeMillis ();
	turnStart += time - pauseStart;
	computerPlayer.startTurn ();

    }

    public void keyPressed (KeyEvent e) {}
    public void keyReleased (KeyEvent e) {}
    public void keyTyped (KeyEvent e) {

	if (configurator != null) return;

	char key = e.getKeyChar ();

	if (key > '0' && key < '9' && !gameover) {
	    selectPlayer (key - '1');
	}
	if (key == 'c')
	    cheat ();
	if (key == 'r')
	    removeSet ();

	if (buzzed) return;

	switch (key) {
	case '`':
	    selectPlayer (0);
	    break;
	case '=':
	    selectPlayer (1);
	    break;
	case 'z':
	    selectPlayer (2);
	    break;
	case '/':
	    selectPlayer (3);
	    break;
	case 'b':
	    selectPlayer (4);
	    break;
	default:
	    return;
	}
	deselectTimer.click ();
	buzzed = true;
    }

    public void pause () {
	deselectTimer.pause ();
	computerPlayer.pause ();
    }

    public void unpause () {
	deselectTimer.unpause ();
	computerPlayer.unpause ();
    }

    public void mouseClicked (MouseEvent e) {};
    public void mouseReleased (MouseEvent e) {};
    public void mouseExited (MouseEvent e) {};
    public void mouseEntered (MouseEvent e) {};

    public void mousePressed (MouseEvent e) {
	doMousePressed (e);
    }

    private synchronized void doMousePressed (MouseEvent e) {

	if (configurator != null) return;
	deselectTimer.click ();
	requestFocus ();

	Object source = e.getSource ();

	if (source == config) {
	    //pause timer
	    pauseStart = System.currentTimeMillis ();

	    //blank cards
	    for (int i = 0; i < onTable.size (); i++) {
		SetCard card = (SetCard) onTable.elementAt (i);
		card.isBlank = true;
		card.repaint ();
	    }

	    //show configurator
	    configurator = new Configurator (this);
	    configurator.show ();
	    pause ();
	    return;
	} else if (source == restart) {
	    startGame ();
	    return;
	} else if (source == help) {
	    pause ();
	    SetHelpFrame.instance (this).show ();
	    return;
	}

	if (gameover) return; //all of the other buttons are disabled
			      //when the game is over.

	if (computerSelecting) return; // the computer has selected some cards
	// to show to us, so we may not intercede.

	//Was the click on one of the player buttons?
	for (int i = 0; i < NUM_PLAYERS; i++) {
	    if (source == players [i]) {
		selectPlayer (i);
		return;
	    }
	}

	//Was the click on the cheat button?
	if (source == cheat) {
	    cheat ();
	    return;
	}


	//None of the above: It must have been a card click

	if ((e.getModifiers () & MouseEvent.BUTTON3_MASK) != 0) {
	    //right-click deselects all
	    card1.setSelected (false);
	    card2.setSelected (false);
	    card3.setSelected (false);
	    card1 = card2 = card3 = nullCard;
	    unbuzz ();

	} else if (activePlayer != -1) {  
	    if (source == card1 || source == card2 || source == card3) return;
	    pause ();

	    //cycle the selected cards
	    card3.setSelected (false);
	    card3 = card2;
	    card2 = card1;
	    card1 = (SetCard) source;
	    card1.setSelected (true);

	    //do we have a set?
	    if (isSet (card1, card2, card3)) {
		if (Config.main.getBoolean ("singlePlayerMode"))
		    status.setText ("Select three cards");
		else
		    status.setText ("Select a player.");
		gotSet ();
	    } else {
		if (card1 != nullCard && card2 != nullCard && card3 != nullCard) 
		    status.setText ("Not a set because " + 
				    SetApplet.whyNotSet (card1, card2, card3) + ".");
	    }
	    unpause ();
	}
    }

    private void gotSet () {

	long now = System.currentTimeMillis ();

	times [activePlayer] += now - turnStart;

	//record the stats
	stats.gotSet (card1, card2, activePlayer, (int) (now - turnStart));
       
	//Now, penalize the computer player
	computerPlayer.humanGotSet ();

	score [activePlayer]++;
	    
	int sc = score [activePlayer];
	scoreLabel [activePlayer].setText (sc + pluralize (sc, " set"));
	    
	//round to the nearest 1/10th
	int secs = Math.round ((times [activePlayer] / sc) / 100.0f);
	int tenths = secs % 10;
	timeLabel [activePlayer].setText ("Average Time: " + secs / 10 + "." + tenths);

	turnStart = now;

	onTable.removeElement (card1);
	onTable.removeElement (card2);
	onTable.removeElement (card3);

	unbuzz ();

	if (liveboard () && onTable.size () >= 12) {

	    //there were > 12, and the board is still live (most likely,
	    //there are now 12).  Move cards from end into positions of
	    //removed cards (like real players do).

	    for (int i = center.getComponentCount () - 1; i >= 0; i--) {
		Component card = center.getComponent (i);
		if (card == card1 || card == card2 || card == card3) {
		    if (i < 12) {
			//we need to move in a card from the end, #12
			Component moved = center.getComponent (12);
			//remove card from end
			center.remove (12);
			//and put it in the current pos
			center.add (moved, i);
		    }
		    //and remove the current card
		    center.remove (card);
		} 
	    }
	} else {
	    if (deck.size () > 0) {

		//this whole song and dance has the goal of replacing the
		//removed cards, rather than deleting them and adding the new
		//cards at the end.
		for (int i = 0; i < center.getComponentCount (); i++) {
		    Component card = center.getComponent (i);
		    if (card == card1 || card == card2 || card == card3) {
			dealCard (i);
			center.remove (card);
		    }
		}
		//of course, extra cards *should* go at the end
		while (!liveboard ()) {
		    dealCard ();
		    dealCard ();
		    dealCard ();
		}
	    } else {
		center.remove (card1);
		center.remove (card2);
		center.remove (card3);
	    }
	}

	if (deck.size () == 0 && !isSetOnTable ()) {
	    //calculate winner
	    int winner = -1;
	    int best = 0;
	    for (int player = 0; player < NUM_PLAYERS; player ++) {
		if (score [player] > best) {
		    best = score [player];
		    winner = player;
		}
	    }

	    gameover = true;
	    String text;
	    if (Config.main.getBoolean ("singlePlayerMode")) {
		text = "Game over.  Press restart to play again.";
	    } else {
		text = "Game over.  Player " + (winner + 1) + " wins. Press restart to play again.";
	    }
	    GameOver go = new GameOver (this, text, stats);
	    go.show ();
	    go = null;
	    status.setText (text);
	}

	//deselect all cards
	for (int i = 0; i < center.getComponentCount (); i++) {
	    SetCard card = (SetCard) center.getComponent (i);
	    card.setSelected (false);
	}

	int left = deck.size () / 3;
	cardsLeft.setText ("   " + left + pluralize (left, " set") + " left");

	//force a repaint (works around bug in kaffe)
	validate ();

	card1 = nullCard;
	card2 = nullCard;
	card3 = nullCard;
	if (!Config.main.getBoolean ("singlePlayerMode"))
	    activePlayer = -1;
    }

    //pluralise an English word (nowhere near complete)
    public static String pluralize (int i, String str) {
	if (i == 1)
	    return str;
	else
	    if (str.endsWith ("s") || str.endsWith ("x"))
		return str + "es";
	    else
		return str + "s";	
    }

}



/*
This collects per-player stats on types of sets collected
stats are: how many and avg time by: differences, similarities & num diffs
So, one can see what the average time for cards differing in color is
*/

class Statistics extends Panel {

    //build and show the statistics panel
    public void display () {

	removeAll ();
	int i, j;
	//figure out how many players actually played
	//Assume that if player n did not play, player n + 1 also did not
	for (i = 0; i < SetApplet.NUM_PLAYERS; i++) {
	    for (j = 0; j < 4; j ++) {
		if (countByNDiff [i][j] != 0) {
		    //this player has sets
		    break;
		}
	    }
	    if (j == 4) {
		//no sets for this player
		break;
	    }
	}
	int numPlayers = i;

	setLayout (new GridLayout (0, numPlayers * 2 + 1));

	addLabel (" ");
	for (i = 0; i < numPlayers; i++) {
	    addLabel ("Player " + (i + 1) + " Ct");
	    addLabel ("Average time");
	}

	for (int attr = 0; attr < 4; attr ++) {
	    addLabel ("Differing in " + SetCard.attributes [attr]);
	    for (i = 0; i < numPlayers; i++) {
		int count = countByDiff [i][attr];
		addLabel ("" + count, true);
		if (count == 0) {
		    addLabel ("N/A", false);
		} else {
		    int ms = totalTimeByDiff [i][attr] / count;
		    addLabel ("" + ms, false);
		}
	    }
	}

	addLabel ("--");
	for (i = 0; i < numPlayers; i++) {
	    addLabel ("--");
	    addLabel ("--");
	}
	for (int attr = 0; attr < 4; attr ++) {
	    addLabel ("Same in " + SetCard.attributes [attr]);
	    for (i = 0; i < numPlayers; i++) {
		int count = countBySim [i][attr];
		addLabel ("" + count, true);
		if (count == 0) {
		    addLabel ("N/A", false);
		} else {
		    int ms = totalTimeBySim [i][attr] / count;
		    addLabel ("" + ms, false);
		}
	    }
	}
	addLabel ("--");
	for (i = 0; i < numPlayers; i++) {
	    addLabel ("--");
	    addLabel ("--");
	}
	for (int attr = 0; attr < 4; attr ++) {
	    addLabel ((attr + 1) + SetApplet.pluralize (attr + 1, " difference"));
	    for (i = 0; i < numPlayers; i++) {
		int count = countByNDiff [i][attr];
		addLabel ("" + count, true);
		if (count == 0) {
		    addLabel ("N/A", false);
		} else {
		    int ms = totalTimeByNDiff [i][attr] / count;
		    addLabel ("" + ms, false);
		}
	    }
	}
    }

    void addLabel (String s) {
	add (new Label (s));
    }

    void addLabel (String s, boolean b) {
	Label label = new Label (s);
	Color fore = b ? Color.white : Color.black;
	Color back = !b ? Color.white : Color.black;

	label.setForeground (fore);
	label.setBackground (back);
	add (label);
    }

    public void gotSet (SetCard card1, SetCard card2, int player, int msecs) {

	//find differences
	int diffCount = -1; //note that this array is indexed 0-3 for vals 1-4
	for (int i = 0; i < 4; i++) {
	    if (card1.attr (i) != card2.attr (i)) {
		diffCount ++;
		countByDiff [player][i] ++;
		totalTimeByDiff [player][i] += msecs;
	    } else {
		countBySim [player][i] ++;
		totalTimeBySim [player][i] += msecs;
	    }
	}

	countByNDiff [player][diffCount] ++;
	totalTimeByNDiff [player][diffCount] += msecs;
    }

    int totalTimeByDiff [][] = new int [SetApplet.NUM_PLAYERS][4];
    int totalTimeBySim [][] = new int [SetApplet.NUM_PLAYERS][4];
    int totalTimeByNDiff [][] = new int [SetApplet.NUM_PLAYERS][4];
    
    int countByDiff [][] = new int [SetApplet.NUM_PLAYERS][4];
    int countBySim [][] = new int [SetApplet.NUM_PLAYERS][4];
    int countByNDiff [][] = new int [SetApplet.NUM_PLAYERS][4];

    public Statistics () {

	for (int i = 0; i < SetApplet.NUM_PLAYERS; i ++) {
	    for (int j = 0; j < 4; j ++) {
		
		totalTimeByDiff [i][j] = 0;
		totalTimeBySim [i][j] = 0;
		totalTimeByNDiff [i][j] = 0;
		countByDiff [i][j] = 0;
		countBySim [i][j] = 0;
		countByNDiff [i][j] = 0;
	    }
	}
    }
}

class SetCard extends Canvas {

    int color, number, shade, shape;

    public static final String colorNames [] = {"red", "green", "blue"};
    public static final String numberNames [] = {"one", "two", "three"};
    public static final String shapeNames [] = {"diamond", "oval", "squiggle"};
    public static final String shadeNames [] = {"hollow", "shaded", "solid"};
    public static final String attributeNames [][] = {colorNames, numberNames, 
						      shapeNames, shadeNames};

    public static final String attributes [] = {"color", "number", 
						"shape", "shade"};

    boolean selected;

    public SetCard (int c, int n, int sp, int sd) {
	color = c;
	number = n;
	shape = sp;
	shade = sd;
	selected = false;
    }

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
    
    public void setAttributes (int c, int n, int sp, int sd) {
	color = c;
	number = n;
	shape = sp;
	shade = sd;
	repaint ();
    }
    
    public void set (SetCard a) {
	color = a.color;
	number = a.number;
	shape = a.shape;
	shade = a.shade;
	repaint ();
    }
    
    public void setSelected (boolean sel) {
	if (selected != sel) {
	    selected = sel;
	    repaint ();
	}
    }
    
    public String colorName () {
	return colorNames [color];
    }
    public String numberName () {
	return numberNames [number];
    }
    public String shapeName () {
	return shapeNames [shape];
    }
    public String shadeName () {
	return shadeNames [shade];
    }

    public static SetCard third (SetCard a, SetCard b) {
	return new SetCard (2 - (a.color + b.color + 2) % 3, 
			    2 - (a.number + b.number + 2) % 3, 
			    2 - (a.shape + b.shape + 2) % 3, 
			    2 - (a.shade + b.shade + 2) % 3);

    }
    
    final double squiggle_x [] = { 0.6, 0.62, 0.52, 0.27,
				   0.7, 0.7 };
    final double squiggle_y [] = { 0.43431457505076198048, 0.4, 0.42,
				   0.18, 
				   -0.28284271247461900977,
				   -0.65147186257614297071 };
    final double squiggle_r [] = { 0.56568542494923801952, 0.6, 0.5,
				   0.15857864376269049511,
				   0.46568542494923801954,
				   0.83431457505076198048 };
    
    final int squiggle_start [] = { 60, 90, 180, 220, 85, 60 };
    final int squiggle_end [] = { 90, 180, 225, 315, 136, 90 };


    public boolean isBlank = false;

    
    public void paint (Graphics g) {
	
	int xpoints [] = new int [4];
	int ypoints [] = new int [4];
	
	Color bordercolor, fillcolor, backcolor;
	
	Dimension d = getSize ();
	int w = d.width - 1;
	int h = d.height - 1;

	if (isBlank) {
	    //fill
	    g.setColor (Config.main.getColor ("background"));
	    g.fillRoundRect (2, 2, w - 4, h - 4, w / 10, h / 10);
	
	    //border
	    g.setColor (Color.black);
	    g.drawRoundRect (1, 1, w - 2, h - 2, w / 10, h / 10);
	    g.drawRoundRect (1, 1, w - 3, h - 2, w / 10, h / 10);
	    g.drawRoundRect (1, 1, w - 2, h - 3, w / 10, h / 10);

	    return;
	}
	
	fillcolor = Config.main.getColor ("cardcolor" + color);
	bordercolor = fillcolor;

	if (selected)
	    backcolor = Config.main.getColor ("selected");
	else
	    backcolor = Config.main.getColor ("background");
	
	if (shade == 0) {
	    fillcolor = backcolor;
	}
	
	g.setColor (backcolor);
	g.fillRoundRect (2, 2, w - 4, h - 4, w / 10, h / 10);
	
	//card border
	g.setColor (Color.black);
	g.drawRoundRect (1, 1, w - 2, h - 2, w / 10, h / 10);
	g.drawRoundRect (1, 1, w - 3, h - 2, w / 10, h / 10);
	g.drawRoundRect (1, 1, w - 2, h - 3, w / 10, h / 10);
	

	int count = number + 1;
	int sw = Config.main.getInt ("stripeWidth");
	
	for (int i = 0; i < count; i++) {
	    switch (shape) {
	    case 0:
		//diamond
		xpoints[0] = (int) (w * 0.2);
		xpoints[1] = (int) (w * 0.5);
		xpoints[2] = (int) (w * 0.8);
		xpoints[3] = (int) (w * 0.5);
		
		ypoints[0] = (int) (h * (i + .5) / count);
		ypoints[1] = (int) (h * (i + .5) / count - h / 10);
		ypoints[2] = (int) (h * (i + .5) / count);
		ypoints[3] = (int) (h * (i + .5) / count + h / 10);
		g.setColor (fillcolor);
		g.fillPolygon (xpoints, ypoints, 4);

		//shaded
		if (shade == 1) {
		    //Erase:
		    g.setColor (backcolor);
		    for (int x = (int) (w * 0.2); x < w * 0.8; x += sw * 2) {
			for (int j = 0; j < sw; j ++)
			    g.drawLine (x + j, 
					(int) (h * (i + .5) / count - h / 10), 
					x + j, 
					(int) (h * (i + .5) / count + h / 10));
		    }
		}

		//border
		g.setColor (bordercolor);
		for (int k = 0; k < 3; k++) {
		    g.drawPolygon (xpoints, ypoints, 4);
		    for (int j = 0; j < 4; j++) {
			ypoints[j] ++;
		    }
		}
		
		break;
		
	    case 1:
		g.setColor (fillcolor);
		g.fillOval ((int) (w * .2), (int) (h * (i + .5) / count - h / 9),
			    (int) (w * .6), (int) (h / 6));

		//shaded
		if (shade == 1) {
		    //Erase:
		    g.setColor (backcolor);
		    for (int x = (int) (w * 0.19); x < w * 0.8; x += sw * 2) {
			for (int j = 0; j < sw; j ++) 
			    g.drawLine (x + j, 
					(int) (h * (i + .5) / count - h / 10), 
					x + j, 
					(int) (h * (i + .5) / count + h / 10));
		    }
		}

		//border
		g.setColor (bordercolor);
		for (int k = 0; k < 3; k++) {
		    g.drawOval ((int) (w * .2), 
				(int) (h * (i + .5) / count - h / 9) + k,
				(int) (w * .6), (int) (h / 6));
		}
		break;
	    case 2:
		//squiggle
		//Gary Wong <gtw at gnu.org> wrote most of the code in this case
		int base_x = (int) (w * .17);
		int base_y = (int) (h * (i + .5) / count + h / 10);

		g.clipRect (3, 3, w - 6, h - 6);

		g.setColor (fillcolor);

		///fill in the center of the squiggle
		g.fillRect ((int) (base_x + w * .07), (int) (base_y - h * .12),
			    (int) (w * .45), (int) (h * .11));
	
		g.fillRect ((int) (base_x + w * .16), (int) (base_y - h * .185),
			    (int) (w * .43), (int) (h * .08));

		//convex arcs
		for( int j = 0; j < 4; j++) {
		    g.fillArc( (int) (base_x + w * ( squiggle_x [j] -
						     squiggle_r [j]) / 3),
			       (int) (base_y - h * ( squiggle_y [j] + 
						     squiggle_r [j]) / 5),
			       (int) (squiggle_r [j] * w * 2 / 3),
			       (int) (squiggle_r [j] * h * 2 / 5),
			       squiggle_start [j], 
			       squiggle_end [j]  - squiggle_start [j]);
		    g.fillArc( (int) (base_x + w * ( 2 - squiggle_x [j] - 
						     squiggle_r [j]) / 3),
			       (int) (base_y - h * ( 1 - squiggle_y [j] + 
						     squiggle_r [j]) / 5),
			       (int) (squiggle_r [j] * w * 2 / 3),
			       (int) (squiggle_r [j] * h * 2 / 5),
			       180 + squiggle_start [j], 
			       squiggle_end [j]  - squiggle_start [j]);
		}      

		//Concave arcs
		g.setColor( backcolor);
		for( int j = 4; j < 6; j++) {
		    //bottom
		    g.fillArc( (int) (base_x + w * ( squiggle_x [j] - 
						     squiggle_r [j]) / 3),
			       (int) (base_y - h * ( squiggle_y [j] + 
						     squiggle_r [j]) / 5),
			       (int) (squiggle_r [j] * w * 2 / 3),
			       (int) (squiggle_r [j] * h * 2 / 5),
			       squiggle_start [j], 
			       squiggle_end [j]  - squiggle_start [j]);

		    //top
		    g.fillArc( (int) (base_x + w * ( 2 - squiggle_x [j] - 
						     squiggle_r [j]) / 3),
			       (int) (base_y - h * ( 1 - squiggle_y [j] + 
						     squiggle_r [j]) / 5),
			       (int) (squiggle_r [j] * w * 2 / 3),
			       (int) (squiggle_r [j] * h * 2 / 5),
			       180 + squiggle_start [j], 
			       squiggle_end [j]  - squiggle_start [j]);
		}      

		//shaded
		if (shade == 1) {
		    //Erase:
		    g.setColor (backcolor);
		    for (int x = (int) (w * 0.15); x < w * 0.85; x += sw * 2) {
			for (int j = 0; j < sw; j ++) 
			    g.drawLine (x + j, 
					(int) (h * (i + .5) / count - h / 10), 
					x + j, 
					(int) (h * (i + .5) / count + h / 10));

		    }
		}

		//outline
		g.setColor (bordercolor);
		for( int j = 0; j < 6; j++) 
		    for( int k = 0; k < 2; k++) {
			g.drawArc( (int) (base_x + w * ( squiggle_x [j] - 
							 squiggle_r [j]) / 3),
				   (int) (base_y - h * ( squiggle_y [j] + 
							 squiggle_r [j]) / 5) + k,
				   (int) (squiggle_r [j] * w * 2 / 3),
				   (int) (squiggle_r [j] * h * 2 / 5),
				   squiggle_start [j], 
				   squiggle_end [j]  - squiggle_start [j]);
			g.drawArc( (int) (base_x + w * ( 2 - squiggle_x [j] - 
							 squiggle_r [j]) / 3),
				   (int) (base_y - h * ( 1 - squiggle_y [j] + 
							 squiggle_r [j]) / 5) + k,
				   (int) (squiggle_r [j] * w * 2 / 3),
				   (int) (squiggle_r [j] * h * 2 / 5),
				   180 + squiggle_start [j], 
				   squiggle_end [j]  - squiggle_start [j]);
		    }

		break;
	    }
	}
    }
    public Dimension getPreferredSize() {
	return new Dimension (100, 200);
    }
    
    public Dimension getMinimumSize() {
	return new Dimension (50, 100);
    }

    public Dimension getMaximumSize() {
	return new Dimension (150, 200);
    }


    public String toString () {
	return "SetCard (" + color + ", " + number + ", " + shape + ", " + shade + ")";
    }

 
}

class GameOver extends Frame implements ActionListener {

    SetApplet game;

    public GameOver (SetApplet g, String text, Statistics stats) {
	super ();
	setTitle ("Game Over");
	game = g;

	BorderLayout bl = new BorderLayout ();
	setLayout (bl);

	add (new Label (text), "North");
	Button b = new Button ("Restart");
	b.addActionListener (this);
	add (b, "South");
	stats.display ();
	add (stats, "Center");
	pack ();

    }
    public void actionPerformed (ActionEvent e) {
	game.startGame ();
	hide ();
	game.disposer.dispose (this);
    }

}

class SetHelpFrame extends Frame implements WindowListener, ActionListener {
    Vector helpBits;

    static SetHelpFrame _instance = null;
    static SetApplet game = null;

    public static SetHelpFrame instance (SetApplet g) {
	game = g;
	if (_instance == null)
	    _instance = new SetHelpFrame ();
	return _instance;
    }

    private SetHelpFrame () {
	super ();
	setTitle ("Set Help");

	helpBits = new Vector ();

	BorderLayout bl = new BorderLayout ();
	setLayout (bl);

	GridBagLayout gridbag = new GridBagLayout ();
	Panel center = new Panel (gridbag);

	initHelpBits (gridbag);

	for (ListIterator i = helpBits.listIterator (); i.hasNext ();) {
	    center.add ((Component) i.next ());
	}

	ScrollPane sp = new ScrollPane ();
	sp.add (center);
	sp.setSize (new Dimension (320, 500));
	add (sp, "Center");

	addWindowListener (this);

	pack ();
    }

    public void windowActivated (WindowEvent e) {}
    public void windowClosed (WindowEvent e) {}
    public void windowOpened (WindowEvent e) {}
    public void windowDeactivated (WindowEvent e) {}
    public void windowDeiconified (WindowEvent e) {}
    public void windowIconified (WindowEvent e) {}

    public void windowClosing (WindowEvent e) {
	game.unpause ();
	dispose ();
    }

    void initHelpBits (GridBagLayout gridbag) {
	GridBagConstraints gbc = new GridBagConstraints ();
	gbc.gridx = 0;
	gbc.gridy = GridBagConstraints.RELATIVE;
	gbc.weightx = 1;
	gbc.weighty = 0;

	//good cards
	Panel demoPanel1 = new Panel ();
	demoPanel1.setLayout (new GridLayout (1, 3));
	demoPanel1.add (new SetCard (0, 2, 0, 2));
	demoPanel1.add (new SetCard (0, 2, 1, 1));
	demoPanel1.add (new SetCard (0, 2, 2, 0));
	gbc.fill = GridBagConstraints.NONE;
	gridbag.setConstraints (demoPanel1, gbc);
	helpBits.add (demoPanel1);

	//inital explanation
	gbc.fill = GridBagConstraints.BOTH;
	WrappedLabel wl = new WrappedLabel (help [0]);
	gridbag.setConstraints (wl, gbc);
	helpBits.add (wl);

	//bad cards
	Panel demoPanel2 = new Panel ();
	demoPanel2.setLayout (new GridLayout (1, 3));
	demoPanel2.add (new SetCard (1, 2, 0, 0));
	demoPanel2.add (new SetCard (2, 2, 1, 0));
	demoPanel2.add (new SetCard (2, 2, 2, 0));
	gridbag.setConstraints (demoPanel2, gbc);
	gbc.fill = GridBagConstraints.NONE;
	helpBits.add (demoPanel2);

	gbc.fill = GridBagConstraints.BOTH;
	wl = new WrappedLabel (help [1]);
	gridbag.setConstraints (wl, gbc);
	helpBits.add (wl);

	//user cards
	Component stp = new SetTwiddlingPanel ();
	gridbag.setConstraints (stp, gbc);
	helpBits.add (stp);

	//how to use this applet
	for (int i = 2; i < 10; i++) {
	    wl = new WrappedLabel (help [i]);
	    gridbag.setConstraints (wl, gbc);
	    helpBits.add (wl);
	}

	//close button
	Button button = new Button ("Close window");
	gridbag.setConstraints (button, gbc);
	helpBits.add (button);
	button.addActionListener (this);

    }

    public void actionPerformed (ActionEvent e) {
	game.unpause ();
	hide ();
    }

    String help [] = {
//string0:
"A Set deck contains 81 Set cards.  Each card has four attributes: " +
"color, number, shape, and shading.  Each attribute has three possible " +
"settings.  Three attributes match if they're all the same or all " +
"different.  A set consists of three Set cards for which all four " +
"attributes match.  For example, the above set is the same in " +
"number and color, but different in shape and shading.",

//string1:
"The cards above are not a set, because their colors are neither " +
"all the same nor all different.  When two cards have the same " +
"attribute, but one has a different attribute, the cards do not " +
"make a set.",

//string2:
"You can use these selectors to change the cards displayed above. " +
"The text below the cards will tell you if they are a set, and if not, " +
"why not.",

//string3:
"To play Set with this applet, assign one of the numbered player " +
"buttons to each player.  When a player calls a set, he or she " + 
"should click on their player button (or press the corresponding " +
"number key on the keyboard).  Alternately, the keys `, =, z, /, and b " +
"can be used like buzzers on a game show -- once a player buzzes in, " +
"the other buzzers are deactivated (but the number keys and buttons " +
"still work).  After a player is selected, he or she should click the " +
"three cards that make up his or her set.  If the cards are actually " +
"a set, they will be removed.  Otherwise, the bottom of the window " +
"will explain why they are not a set.  To select three different " +
"cards, just click them.",

//string4:
"The 'Configure' button brings up a configuration window.  Java applets " +
"can't save data on your computer, so in order to save your " +
"preferences, click the 'Generate bookmark' button.  Then copy the link " +
"from the 'Link' field at the top of the window.  When you visit that " +
"link, it will start the applet with your configuration.",

//string5:
"You can also play Set against a computer player.  In the configuration " +
"dialog, check the 'Single player mode' box.  The computer will find " +
"sets and remove them at a configurable speed.  The computer will not " +
"remove a set while you have cards selected (or you are buzzed in).  " +
"If you select some cards mistakenly, right-click to deselect them. As " +

"you improve, gradually increase the computer's speed.  The time " +
"penalty is intended to mimic human play.  Humans tend to get slightly " +
"distracted when an opponent finds a set.  Also, if a human is looking " +
"at a subset of the board, an opponent removing cards from that subset " +
"can ruin their calculations.  If you don't want the computer to remove " +
"sets, just set the minimum time to a big number like 1000000.",

//string6:

"When you finish a game of Set, a window will display statistics.  Pay " +
"attention here: if you notice that a certain type of set takes you a " +
"long time, try to look specifically for that type of set in the next " +
"few games.",

//string7:
"Copyright 1998, 1999, 2002, 2003, 2004 David M. Turner <novalis atsign " +
"gnu.org> This program is free software; you can redistribute it and/or " +
"modify it under the terms of the GNU General Public License as " +
"published by the Free Software Foundation; either version 2 of the " +
"License, or (at your option) any later version.",

//string8:
"This program is distributed in the hope that it will be useful, but" +
"WITHOUT ANY WARRANTY; without even the implied warranty of " +
"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU " +
"General Public License for more details.",

//string9:
"You should have received a copy of the GNU General Public License " +
"along with this program; if not, write to the Free Software " +
"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 " +
"USA",


};

}

class SetTwiddlingPanel extends Panel implements ItemListener, ActionListener {

    Choice controllers [][];
    SetCard cards [];
    Button third;
    WrappedLabel status;

    public void itemStateChanged (ItemEvent e) {
	Object source = e.getSource ();

	for (int card = 0; card < 3; card++) { 
	    for (int ctrl = 0; ctrl < 4; ctrl++) {
		Choice [] controller = controllers [card];
		if (source == controller [ctrl]) {
		    //do something
		    cards [card].setAttributes 
			(controller [0].getSelectedIndex (),
			 controller [1].getSelectedIndex (),
			 controller [2].getSelectedIndex (),
			 controller [3].getSelectedIndex ());
		}
	    }
	}
	if (SetApplet.isSet (cards [0], cards [1], cards [2]))
	    status.setText ("These cards are a set");	
	else 
	    status.setText ("Not a set because " + 
			    SetApplet.whyNotSet (cards [0], cards [1], cards [2]));
	validate ();
    }

    public void actionPerformed (ActionEvent e) {
	//must be third button
	status.setText ("These cards are a set");
	validate ();
	cards [2].set (SetCard.third (cards [0], cards [1]));
	controllers[2][0].select (cards [2].color);
	controllers[2][1].select (cards [2].number);
	controllers[2][2].select (cards [2].shape);
	controllers[2][3].select (cards [2].shade);

    }

    public SetTwiddlingPanel () {

	GridBagLayout gridBag = new GridBagLayout ();
	setLayout (gridBag);
	GridBagConstraints gbc = new GridBagConstraints ();

	controllers = new Choice [3][];

	cards = new SetCard [3];

	//set up 3 cards and 4 controllers per card
	for (int card = 0; card < 3; card++) { 

	    cards [card] = new SetCard (card, 0, 0, 0);
	    gbc.gridx = card;
	    gbc.gridy = 0;
	    add (cards [card], gbc);

	    controllers [card] = new Choice [4];
	    for (int ctrl = 0; ctrl < 4; ctrl++) {
		gbc.gridx = card;
		gbc.gridy = ctrl + 1;
		Choice list = new Choice ();
		for (int i = 0; i < 3; i++) {
		    list.add (SetCard.attributeNames [ctrl][i]);
		}
		add (list, gbc);
		controllers [card][ctrl] = list;
		list.addItemListener (this);
	    }
	    //make the selection match the displayed card
	    controllers [card][0].select (card);
	}

	gbc.gridx = 1;
	gbc.gridy = 5;
	third = new Button ("Show third");
	add (third, gbc);
	third.addActionListener (this);

	gbc.gridx = 0;
	gbc.gridy = 6;
	gbc.gridwidth = 3;
	status = new WrappedLabel ("These cards are a set");
	status.setSize (getSize ().width, status.lineHeight * 3);
	add (status, gbc);
    }
}

//a non-editable text area with wrapping.
class WrappedLabel extends Canvas {

    String [] contents;
    Vector wrappedContents = new Vector ();

    static final int leftMargin = 5;
    int lineHeight;

    public WrappedLabel (String c) {
	this (new String [] {c});
    }
    public WrappedLabel (String [] c) {
	super ();
	contents = c;
	Font f = getFont ();  
	if (f == null) {
	    f = new Font ("Times", Font.PLAIN, 12);
	    setFont (f);
	}
	lineHeight = getFontMetrics (f).getHeight ();
	setSize (300, 100);
	wrap ();
    }


    public void setText (String c) {
	contents = new String [] {c};
	forceWrap ();
	repaint ();
    }

    Dimension saved_size = null;

    private void forceWrap () {
	Dimension d = getSize ();

	wrappedContents.clear ();

	FontMetrics fm = getFontMetrics (getFont ());
	//for each "paragraph"
	for (int para = 0; para < contents.length; para++) {
	    int i = 0;
	    contents [para] += ' ';
	    char [] chars = contents [para].toCharArray ();
	    //add characters until we overflow.
	    do {
		int start = i;
		int lastspace = i;
		int width = leftMargin;
		while (width <= d.width && i < chars.length) {
		    if (chars [i] == ' ')
			lastspace = i;
		    width += fm.charWidth (chars [i++]);
		}

		String row = new String (chars, start, lastspace - start);
		wrappedContents.add (row);
		i = lastspace + 1; //start over at last space

	    } while (i != chars.length);
	    wrappedContents.add ("");
	    wrappedContents.add ("");
	}
    }

    private void wrap () {
	Dimension d = getSize ();
	if (! d.equals (saved_size)) {
	    forceWrap ();
	    saved_size = d;
	}
    }

    public Dimension getPreferredSize() {
	int w = saved_size.width;
	if (w < 200) w = 200;
	if (w > 400) w = 400;
	FontMetrics fm = getFontMetrics (getFont ());
	return new Dimension (w, wrappedContents.size () * fm.getHeight ());
    }

    public Dimension getMinimumSize() {
	return new Dimension (200, 10);
    }

    public void paint (Graphics g) {

	int y = 10;
	FontMetrics fm = getFontMetrics (getFont ());

	wrap ();

	for (ListIterator i = wrappedContents.listIterator (); i.hasNext ();) {
	    g.drawString ((String) i.next (), leftMargin, y);
	    y += lineHeight;
	}
    }
}


class Var implements Cloneable {
    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_COLOR = 2;
    public static final int TYPE_BOOLEAN = 3;
    public static final String [] typeNames = 
    { "int", "float", "color", "boolean" };

    int type;

    int ivalue;
    float fvalue;
    Color cvalue;
    boolean bvalue;
    String label;
    String name;

    public Object clone () {
	try {
	    Var myClone = (Var) super.clone ();
	    if (cvalue != null)
		myClone.cvalue = new Color (cvalue.getRGB ());
	    myClone.ivalue = ivalue;
	    myClone.fvalue = fvalue;
	    myClone.bvalue = bvalue;
	    myClone.label = label;
	    myClone.name = name;
	    return myClone;
	} catch (CloneNotSupportedException e) {
	    //can't get here
	    return null;
	}
    }

    Var (String nm, String value, String tyStr, String l) {
	label = l;
	name = nm;
	for (int i = 0; i < typeNames.length; i++) {
	    if (tyStr == typeNames [i]) {
		type = i;
		break;
	    }
	}
	switch (type) {
	case TYPE_INT:
	    ivalue = Integer.parseInt (value);
	    break;
	case TYPE_FLOAT:
	    fvalue = Float.parseFloat (value);
	    break;
	case TYPE_COLOR:
	    cvalue = Color.decode ("0x" + value);
	    break;
	case TYPE_BOOLEAN:
	    bvalue = value.equals ("Y");
	    break;
	default:
	    break;
	}
    }

    public int getInt () {
	if (type == TYPE_INT) {
	    return ivalue;
	} else {
	    throw new WrongTypeException (TYPE_INT, type);
	}
    }

    public float getFloat () {
	if (type == TYPE_FLOAT) {
	    return fvalue;
	} else {
	    throw new WrongTypeException (TYPE_FLOAT, type);
	}
    }

    public Color getColor () {
	if (type == TYPE_COLOR) {
	    return cvalue;
	} else {
	    throw new WrongTypeException (TYPE_COLOR, type);
	}
    }

    public boolean getBoolean () {
	if (type == TYPE_BOOLEAN) {
	    return bvalue;
	} else {
	    throw new WrongTypeException (TYPE_BOOLEAN, type);
	}
    }


    public String toString () {
	switch (type) {
	case TYPE_INT:
	    return "" + ivalue;
	case TYPE_FLOAT:
	    return "" + fvalue;
	case TYPE_COLOR:
	    return colorToHex (cvalue);
	case TYPE_BOOLEAN:
	    return bvalue ? "Y" : "N";
	default:
	    return "bug";
	}
    }

    public static String colorToHex (Color c) {
	int i = c.getRGB () & 0xffffff; //chop off alpha
	String s = Integer.toHexString (i);
	while (s.length () < 6) {
	    s = "0" + s;
	}
	return s;
    }
    class WrongTypeException extends RuntimeException {
	int expected, got;
	WrongTypeException (int e, int g) {
	    expected = e;
	    got = g;
	}
	public String toString () {
	    return "You asked for " + typeNames [expected] + ", but this variable was " + typeNames [got];
	}
    }

}

class Config {
    TreeMap vars;
    TreeMap varsByNumber;
    static Config main = new Config ();

    String [][] defaults = {
	{"singlePlayerMode", "boolean", "N", "Single player mode"},

	{"minSpeed", "float", "10.0", "Minimum time for computer player to find a set"},
	{"maxSpeed", "float", "20.0", "Maximum time for computer player to find a set"},
	{"humanSetPenalty", "float", "3.0", "Time penalty for computer player when human gets a set"},
	{"showSetTime", "float", "0.3", "Time computer's set is selected for before it's removed"},

	{"background", "color", "ffffff", "Background color"},
	{"selected", "color", "c0c0c0", "Background color for selected cards"},
	{"cardcolor0", "color", "ff0000", "Color 1"},
	{"cardcolor1", "color", "00bf00", "Color 2"},
	{"cardcolor2", "color", "0000ff", "Color 3"},

	{"stripeWidth", "int", "3", "Stripe width"},

    };

    public Config () {
	this ("");
    }

    public Config (String args) {
	vars = new TreeMap ();
	varsByNumber = new TreeMap ();
	ArgParser parser = new ArgParser (args);
	for (int i = 0; i < defaults.length; i ++) {
	    String name = defaults [i][0];
	    String type = defaults [i][1];

	    String value = parser.get (name);
	    if (value == null) {
		//nothing passed for this arg, use default
		value = defaults [i][2];
	    }

	    Var var = new Var (name, value, type, defaults [i][3]);
	    vars.put (name, var);
	    varsByNumber.put (new Integer (i), var);
	}
    }

    public String toString () {

	java.util.Set entries = vars.entrySet ();
	String amp = "";
	String out = "";
	for (Iterator i = entries.iterator (); i.hasNext (); ) {
	    Map.Entry entry = (Map.Entry) i.next ();
	    out += amp + entry.getKey () + "=" + ((Var) entry.getValue ());
	    amp = "&";
	}

	return out;
    }

    public Config (Config c) {
	vars = new TreeMap ();
	varsByNumber = new TreeMap ();

	Set entries = c.varsByNumber.entrySet ();
	for (Iterator i = entries.iterator (); i.hasNext (); ) {
	    Map.Entry entry = (Map.Entry) i.next ();
	    Var var = (Var) entry.getValue ();
	    Integer number = (Integer) entry.getKey ();

	    Var newVar = (Var) var.clone ();
	    vars.put (var.name, newVar);
	    varsByNumber.put (number, newVar);
	}
    }
    public void put (String name, int number, Var var) {
	vars.put (name, var);
	varsByNumber.put (new Integer (number), var);
    }
    public Var getVar (String name) {
	return (Var) vars.get (name);
    }
    public int getInt (String name) {
	return getVar (name).getInt ();
    }
    public float getFloat (String name) {
	return getVar (name).getFloat ();
    }
    public Color getColor (String name) {
	return getVar (name).getColor ();
    }
    public boolean getBoolean (String name) {
	return getVar (name).getBoolean ();
    }

}

interface VarWidget {

    public void init (Var v);
    public Var getValue ();
    public String getName ();
}

class TextVarWidget extends TextField implements VarWidget {
    Var var;

    public void init (Var v) {
	setText (v.toString ());
	var = v;
    }
    public Var getValue () {
	return new Var (var.name, getText (), Var.typeNames [var.type], var.label);
    }    
    public String getName () {
	return var.name;
    }
}
class BooleanVarWidget extends Checkbox implements VarWidget {
    Var var;

    public void init (Var v) {
	if (v.bvalue) 
	    setState (true);
	var = v;
    }
    public Var getValue () {
	return new Var (var.name, getState () ? "Y" : "N", "boolean", var.label);
    }
    public String getName () {
	return var.name;
    }
}

class Configurator extends Frame implements ActionListener, WindowListener {
    Config origConfig;

    Button okButton, cancelButton, resetButton, defaultsButton, 
	genBookmarkButton;

    TextField linkField;
    Vector widgets;

    Checkbox checkBoxes;
    String base;
    SetApplet game;

    Panel configPanel;

    public Configurator (SetApplet g) {
	super ("Configurator");
	game = g;
	base = game.getDocumentBase ().toString ();
	int qidx;
	if ((qidx = base.indexOf ('?')) != -1)
	    base = base.substring (0, qidx);
	origConfig = new Config (Config.main);
	addWindowListener (this);

	widgets = new Vector ();
	setup ();
	init (origConfig);
    }
    void setColor (TextComponent f, Color c) {
	f.setText (Var.colorToHex (c));
    }

    void init (Config config) {

	configPanel.removeAll ();
	widgets.clear ();

	Set entries = config.varsByNumber.entrySet ();
	for (Iterator i = entries.iterator (); i.hasNext (); ) {
	    Map.Entry entry = (Map.Entry) i.next ();
	    Var var = (Var) entry.getValue ();

	    configPanel.add (new Label (var.label));
	    Component widget = null;
	    switch (var.type) {
	    case Var.TYPE_INT:
	    case Var.TYPE_FLOAT:
	    case Var.TYPE_COLOR:
		widget = new TextVarWidget ();
		break;
	    case Var.TYPE_BOOLEAN:
		widget = new BooleanVarWidget ();
		break;
	    }
	    ((VarWidget) widget).init (var);
	    configPanel.add (widget);
	    widgets.add (widget);
	}
	pack ();
    }

    void setup () {
	setLayout (new BorderLayout ());

	Panel linkPanel = new Panel ();
	linkPanel.setLayout (new GridLayout (0, 2));

	//for displaying the link
	linkPanel.add (new Label ("Link"));
	linkField = new TextField ();
	linkField.setEditable (false);
	linkPanel.add (linkField);

	add (linkPanel, "North");

	configPanel = new Panel ();
	configPanel.setLayout (new GridLayout (0, 2));

	add (configPanel, "Center");

	Panel buttonPanel = new Panel ();
	buttonPanel.setLayout (new FlowLayout ());
	
	genBookmarkButton = makeButton (buttonPanel, "Generate bookmark");
	resetButton = makeButton (buttonPanel, "Reset");
	defaultsButton = makeButton (buttonPanel, "Defaults");
	cancelButton = makeButton (buttonPanel, "Cancel");
	okButton = makeButton (buttonPanel, "OK");

	add (buttonPanel, "South");
	pack ();
    }

    Button makeButton (Container c, String text) {
	Button b = new Button (text);
	c.add (b);
	b.addActionListener (this);

	return b;
    }
    public void actionPerformed (ActionEvent e) {
	Object source = e.getSource ();
	if (source == okButton) {
	    Config.main = makeConfig ();
	    close ();
	} else if (source == resetButton) {
	    init (origConfig);
	} else if (source == defaultsButton) {
	    init (new Config ());
	} else if (source == cancelButton) {
	    close ();
	} else if (source == genBookmarkButton) {
	    Config c = makeConfig ();
	    String link = base + "?" + c.toString ();
	    linkField.setText (link);
	}
    }
    public void windowActivated (WindowEvent e) {}
    public void windowClosed (WindowEvent e) {}
    public void windowOpened (WindowEvent e) {}
    public void windowDeactivated (WindowEvent e) {}
    public void windowDeiconified (WindowEvent e) {}
    public void windowIconified (WindowEvent e) {}

    public void windowClosing (WindowEvent e) {
	close ();
    }

    void close () {
	hide ();
	game.doneConfig ();
    }

    Config makeConfig () {

	//constraints

	//If the min time is greater than the max time, set the max
	//time to the min time.
	//fixme: should probably have a treemap of widgets to make this lookup quicker

	float min = 0, max = 0;
	TextField maxWidget = null;
	for (ListIterator i = widgets.listIterator (); i.hasNext ();) {
	    VarWidget widget = (VarWidget) i.next ();
	    if (widget.getName ().equals ("minSpeed")) {
		min = widget.getValue ().getFloat ();
	    }
	    if (widget.getName ().equals ("maxSpeed")) {
		max = widget.getValue ().getFloat ();
		maxWidget = (TextField) widget;
	    }
	}

	if (min > max) {
	    maxWidget.setText ("" + min);
	}

	//make config
	Config c = new Config ();
	int idx = 0;
	for (ListIterator i = widgets.listIterator (); i.hasNext ();) {
	    VarWidget widget = (VarWidget) i.next ();
	    Var var = widget.getValue ();
	    c.put (widget.getName (), idx, var);
	    idx ++;
	}
	return c;
    }
}


class ArgParser {

    TreeMap args;

    ArgParser (String str) {
	args = new TreeMap ();
	int start = 0;
	while (true) {
	    int eidx = str.indexOf ('=', start);
	    if (eidx == -1) break;
	    String var = str.substring (start, eidx);
	    int aidx = str.indexOf ('&', eidx);
	    if (aidx == -1) aidx = str.length ();
	    String val = str.substring (eidx + 1, aidx);
	    args.put (var, val);
	    start = aidx + 1;
	}
    }

    String get (String var) {
	return (String) args.get (var);
    }
    String get (String var, String def) {
	Object o = args.get (var);
	if (o == null) 
	    return def;
	else 
	    return (String) o;
    }

}
