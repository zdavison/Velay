import promidi.*;
import guicomponents.*;

//  constants
float kDecayRate = 0.005;
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

	void update(){
		if(life > 0)
		  life -= kDecayRate;

                vel.normalize();
                vel.mult(kSpeedScalar);
		note = new Note(note.getPitch(),floor(127*life),note.getLength());
		pos.add(vel);
	}

	void display(){
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

	void update(){
		if(size < kPingSizeLimit)
		size += growthRate;
		life -= growthRate/100;
		if(size > kPingSizeLimit/2)
		growthRate = kGrowthRate/2;
	}

	void display(){
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

		void update(){
			hx = x+w;
			hy = y+h;
		}  

		void display(){
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

	void update(){
		handle.update();
                
                //dumb messy note updating
		for(int i=0;i<notes.size();i++){
			VNote note = notes.get(i);
			if(note.pos.x <= x){
                                note.pos.x = note.pos.x-(note.pos.x-x);
				note.vel.x *= -1;
				pings.add(new VPing(note));
			}else if(note.pos.x >= x+w){
                                note.pos.x = note.pos.x+(x+w-note.pos.x);
				note.vel.x *= -1;
				pings.add(new VPing(note));
                        }
			if(note.pos.y <= y){
                                note.pos.y = note.pos.y-(note.pos.y-y);
				note.vel.y *= -1;
				pings.add(new VPing(note));
			}else if(note.pos.y >= y+h){
                                note.pos.y = note.pos.y+(y+h-note.pos.y);
				note.vel.y *= -1;
				pings.add(new VPing(note));
                        }

			//remove notes
			if(note.life <= 0.25)
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

	void display(){
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

	void addNote(VNote note){
		notes.add(note);
		note.pos.x += x;
		note.pos.y += y;
	}
}

//setup+draw
void setup(){
	size(kSketchSize,kSketchSize);
        frame.setResizable(true);
	container = new VBox(20,20,kSketchSize-40,kSketchSize-40);

	createGUI();

        //proMidi
	midiIO = MidiIO.getInstance(this);
	midiOut = midiIO.getMidiOut(0,0);
        midiIO.openInput(0,0);
}

void draw(){
	background(100,100,100);
	container.update();
	noSmooth();
	container.display();
	smooth();
}

//MIDI Handling
void noteOff(Note note,int deviceNumber,int midiChannel){
		Note _note = new Note(note.getPitch(),100,10);
		container.addNote(new VNote(_note,container));
}

// Mouse events
void mousePressed(){
		if(mouseX > container.handle.hx && mouseX < container.handle.hx+container.handle.hw && mouseY > container.handle.hy && mouseY < container.handle.hy+container.handle.hh){
			lastClickPoint = new PVector(mouseX,mouseY); 
			}else{
				lastClickPoint = null; 
		}
}

void mouseReleased(){
			lastClickPoint = null; 
}

void mouseDragged(){
		if(lastClickPoint != null){
                          if(mouseX > 30 && mouseX < width-20)
			    container.w += mouseX-container.x-container.w;
                          if(mouseY > 30 && mouseY < height-20)
			    container.h += mouseY-container.y-container.h; 
		}
}
