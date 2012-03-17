import processing.core.*; 
import processing.xml.*; 

import promidi.*; 
import guicomponents.*; 
import promidi.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class velay extends PApplet {




//  constants
float kDecayRate = 0.005f;
float kGrowthRate = 5;
float kSpeedScalar = 5;
int kPingSizeLimit = 75;
int kPingThickness = 10;
int kHandleSize = 10;
int kSketchSize = 500;

//  Global
VBox container;
MidiIO midiIO;
MidiOut midiOut;
PVector lastClickPoint;

//  Classes
class VNote{
	Note note;
	float life;
	PVector pos;
	PVector vel;

	VNote(Note _note,VBox inBox){
		note = _note;
		life = 1;
		float column = note.getPitch()%4 ;
		float row = note.getPitch()%3;
		float cellW = inBox.w/4;
		float cellH = inBox.h/3;
		PVector boxCenter = new PVector(inBox.w/2,inBox.h/2);
		pos = new PVector((column*cellW)+cellW/2,(row*cellH)+cellH/2); //&&&
		vel = PVector.sub(pos,boxCenter); //&&&
		vel.normalize();
		if(vel.x == 0)
		vel.x = random(-1,1);
		if(vel.y == 0)
		vel.y = random(-1,1);
		vel.mult(kSpeedScalar);
	}

	public void update(){
		if(life > 0)
		  life -= kDecayRate;

                vel.normalize();
                vel.mult(kSpeedScalar);
		note = new Note(note.getPitch(),floor(127*life),note.getLength());
		pos.add(vel);
	}

	public void display(){
		stroke(0,0,0,0);
		fill(255,180,50,255*life);
		ellipse(pos.x,pos.y,5,5);
	}
}

class VPing{
	float life;
	PVector pos;
	int size;
	float growthRate;

	VPing(VNote note){
		pos = new PVector(note.pos.x,note.pos.y);
		life = note.life;
		growthRate = kGrowthRate;
		midiOut.sendNote(note.note);
	} 

	public void update(){
		if(size < kPingSizeLimit)
		size += growthRate;
		life -= growthRate/100;
		if(size > kPingSizeLimit/2)
		growthRate = kGrowthRate/2;
	}

	public void display(){
		strokeWeight(kPingThickness);
		stroke(255,180,50,255*life);
		fill(0,0,0,0);
		ellipse(pos.x,pos.y,size,size);
		strokeWeight(1);
	}
}

class VBox{
	float x,y;
	float w,h;
	ArrayList<VNote> notes;
	ArrayList<VPing> pings;
	VHandle handle;

	class VHandle{
		float hx,hy;
		float hw,hh;

		VHandle(){
			hx = x+w;
			hy = y+h;
			hw = kHandleSize;
			hh = kHandleSize;
		}

		public void update(){
			hx = x+w;
			hy = y+h;
		}  

		public void display(){
                        stroke(0,0,0,0);
			fill(255,180,50);
			rect(hx,hy,hw,hh);
		}
	}

	VBox(float _x,float _y,float _w,float _h){
		notes = new ArrayList<VNote>();
		pings = new ArrayList<VPing>();
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		handle = new VHandle();
	}

	public void update(){
		handle.update();
                
                //dumb messy note updating
		for(int i=0;i<notes.size();i++){
			VNote note = notes.get(i);
			if(note.pos.x <= x){
                                note.pos.x = note.pos.x-(note.pos.x-x);
				note.vel.x *= -1;
				pings.add(new VPing(note));
			}else if(note.pos.x >= x+w){
                                note.pos.x = note.pos.x+(w-note.pos.x);
				note.vel.x *= -1;
				pings.add(new VPing(note));
                        }
			if(note.pos.y <= y){
                                note.pos.y = note.pos.y-(note.pos.y-y);
				note.vel.y *= -1;
				pings.add(new VPing(note));
			}else if(note.pos.y >= y+h){
                                note.pos.y = note.pos.y+(h-note.pos.y);
				note.vel.y *= -1;
				pings.add(new VPing(note));
                        }

			//remove notes
			if(note.life <= 0.25f)
			notes.remove(i);

			note.update();
		}

		for(int i=0;i<pings.size();i++){
			VPing ping = pings.get(i);
			if(ping.life <= 0)
			pings.remove(i);

			ping.update(); 
		}
	}

	public void display(){
		strokeWeight(2);
		stroke(255,180,50);
		fill(80,80,80);
		rect(x,y,w,h);
		handle.display();
		for(int i=0;i<notes.size();i++){
			notes.get(i).display(); 
		}
		for(int i=0;i<pings.size();i++){
			pings.get(i).display(); 
		}
		strokeWeight(1);
	}

	public void addNote(VNote note){
		notes.add(note);
		note.pos.x += x;
		note.pos.y += y;
	}
}

//setup+draw
public void setup(){
	size(kSketchSize,kSketchSize);
        frame.setResizable(true);
	container = new VBox(20,20,kSketchSize-40,kSketchSize-40);

	createGUI();

        //proMidi
	midiIO = MidiIO.getInstance(this);
	midiOut = midiIO.getMidiOut(0,0);
        midiIO.openInput(0,0);
}

public void draw(){
	background(100,100,100);
	container.update();
	noSmooth();
	container.display();
	smooth();
}

//MIDI Handling
public void noteOff(Note note,int deviceNumber,int midiChannel){
		Note _note = new Note(note.getPitch(),100,10);
		container.addNote(new VNote(_note,container));
}

// Mouse events
public void mousePressed(){
		if(mouseX > container.handle.hx && mouseX < container.handle.hx+container.handle.hw && mouseY > container.handle.hy && mouseY < container.handle.hy+container.handle.hh){
			lastClickPoint = new PVector(mouseX,mouseY); 
			}else{
				lastClickPoint = null; 
		}
}

public void mouseReleased(){
			lastClickPoint = null; 
}

public void mouseDragged(){
		if(lastClickPoint != null){
                          if(mouseX > 30 && mouseX < width-20)
			    container.w += mouseX-container.x-container.w;
                          if(mouseY > 30 && mouseY < height-20)
			    container.h += mouseY-container.y-container.h; 
		}
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */
 


int previouslySelectedIn;
int previouslySelectedOut;

//midi in
public void combo1_Click1(GCombo combo) { //_CODE_:GUIMidiOutCombo:511767:
  midiOut = midiIO.getMidiOut(combo.selectedIndex(),0);
} //_CODE_:GUIMidiOutCombo:511767:

//midi out
public void combo2_Click1(GCombo combo) { //_CODE_:GUIMidiInCombo:941021:
  midiIO.closeInput(previouslySelectedOut);
  midiIO.openInput(combo.selectedIndex(),0);
  previouslySelectedOut = combo.selectedIndex();
} //_CODE_:GUIMidiInCombo:941021:

//speed
public void slider1_Change1(GHorzSlider horzslider) { //_CODE_:GUISpeedSlider:887037:
  kSpeedScalar = horzslider.getValue();
} //_CODE_:GUISpeedSlider:887037:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.setColorScheme(this, GCScheme.GREY_SCHEME);
  G4P.messagesEnabled(false);
  G4P.cursor(ARROW, CROSS);
  G4P.setMouseOverEnabled(true);
  Settings = new GWindow(this, "Settings", 0, 0, 240, 120, false, JAVA2D);
  Settings.setBackground(color(150,150,150));
  GUISpeedLabel = new GLabel(this, "Speed", 0, 0, 80, 20);
  GUIMidiInLabel = new GLabel(this, "MIDI In", 0, 20, 80, 20);
  GUIMidiOutLabel = new GLabel(this, "Midi Out", 0, 40, 80, 20);
  
  //get midi outs
  previouslySelectedOut = 0;
  midiIO = MidiIO.getInstance(this);
  int numberOfOuts = midiIO.numberOfOutputDevices();
  String[] midiOuts = new String[numberOfOuts];
  for (int i = 0; i < numberOfOuts; i++){
			midiOuts[i] = midiIO.getOutputDeviceName(i);
		}
  
  GUIMidiOutCombo = new GCombo(this, midiOuts, 5, 80, 40, 160);
  GUIMidiOutCombo.setSelected(0);
  GUIMidiOutCombo.addEventHandler(this, "combo1_Click1");
  
  //get midi ins
  previouslySelectedIn = 0;
  int numberOfIns = midiIO.numberOfInputDevices();
  String[] midiIns = new String[numberOfIns];
  for (int i = 0; i < numberOfIns; i++){
			midiIns[i] = midiIO.getInputDeviceName(i);
		}
  
  GUIMidiInCombo = new GCombo(this, midiIns, 5, 80, 20, 160);
  GUIMidiInCombo.setSelected(0);
  GUIMidiInCombo.addEventHandler(this, "combo2_Click1");
  GUISpeedSlider = new GHorzSlider(this, 80, 0, 160, 20);
  GUISpeedSlider.setLimits(50, 1, 10);
  GUISpeedSlider.addEventHandler(this, "slider1_Change1");
  Settings.add(GUISpeedLabel);
  Settings.add(GUIMidiInLabel);
  Settings.add(GUIMidiOutLabel);
  Settings.add(GUIMidiOutCombo);
  Settings.add(GUIMidiInCombo);
  Settings.add(GUISpeedSlider);
}

// Variable declarations 
// autogenerated do not edit
GWindow Settings;
GLabel GUISpeedLabel; 
GLabel GUIMidiInLabel; 
GLabel GUIMidiOutLabel; 
GCombo GUIMidiOutCombo; 
GCombo GUIMidiInCombo; 
GHorzSlider GUISpeedSlider; 

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "velay" });
  }
}
