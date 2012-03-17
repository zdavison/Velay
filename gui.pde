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
 
import promidi.*;

int previouslySelectedIn;
int previouslySelectedOut;

//midi in
void combo1_Click1(GCombo combo) { //_CODE_:GUIMidiOutCombo:511767:
  midiOut = midiIO.getMidiOut(combo.selectedIndex(),0);
} //_CODE_:GUIMidiOutCombo:511767:

//midi out
void combo2_Click1(GCombo combo) { //_CODE_:GUIMidiInCombo:941021:
  midiIO.closeInput(previouslySelectedOut);
  midiIO.openInput(combo.selectedIndex(),0);
  previouslySelectedOut = combo.selectedIndex();
} //_CODE_:GUIMidiInCombo:941021:

//speed
void slider1_Change1(GHorzSlider horzslider) { //_CODE_:GUISpeedSlider:887037:
  kSpeedScalar = horzslider.getValue();
} //_CODE_:GUISpeedSlider:887037:



// Create all the GUI controls. 
// autogenerated do not edit
void createGUI(){
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

