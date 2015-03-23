import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ICE_dev4 extends PApplet {

/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */



PFont font;
Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port
String pID = "";

Table theTable; // Where the session data gets stored.
String csvPath = "" ; //The path for the completed csv to be stored.
String csvName = "" ; // The name provided by RA for the csv file

// Variable to store text currently being typed
String typing = "";
// Variable to store saved text when return is hit
String saved = "";
int indent = 25;
boolean  handPresent = false;
boolean setupEnded = true;

String displayData = "ICE Data Acquisition v0.01";

public void setup() 
{
  size(1200, 200); // Draw the UI window
  font = createFont("DINCondensed-Bold", 24);
  textFont(font);

  selectFolder("SelectFolder to save data to", "getSavePath");


  theTable = new Table(); // Create the table
  theTable.addColumn("pID");
  theTable.addColumn("mS");
  theTable.addColumn("S");
  theTable.addColumn("M");

  // I know that the first port in the serial list on my mac
  // is always my d FTDI adaptor, so I open Serial.list()[0]. //<------- LIES!!!!
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  //println("Hey there, Welcome to a bucket of ice water");
  handPresent = false; 
}


public void draw() {
  //Stanard BG
  background(0x44);
  fill(0xFF);
  textAlign(CENTER);
  text(pID + "\n----- READY ----", width/2, height/2);


  //check for a file name
  if (csvName == "") {
      getCsvName(); // If not, then go out and make a name for your self
    //If the name is not empty, then config the serial port
    }  
    else {
    //Show the destination file
    fill(0xFF);
    textAlign(LEFT);
    text(csvPath, 10, height-20);
    //Verify if there is a connection to the rig.
    if (myPort != null) {

      // At this point there is a Serial link, but no data has been TX/RX
      // Request the partcipant ID.
        
        //When there is data on the line do these things
      if(pID != ""){

        if(handPresent == true){
            background(0xff0044);
            textAlign(CENTER);
            text(pID + " \n Hand Detected ", width/2, height/2);
        }

        if (myPort.available() > 0) {  // If data is available,
          val = myPort.read();         // read it and store it in val
          print(PApplet.parseChar(val)); 

          switch(val) {
          case '[':  //Start of setup
            setupEnded = false;
          break;

           case ']':  //end of setup
            setupEnded = true;
          break;

          case '\n': 
            println("------------------------"); 
            break;

          case '\t': // A tab is sent over when the hand is removed 

            long elapsed;
            // pID++;//Increment the pID (For now, eventually will be input)
            println("---- Time Elapsed Data Breakdown ----"); 
            println("Participant ID: " + pID);
            if ( myPort.available() > 0) {  // If data is available,
              String elapsedBuffer = myPort.readString();
              println("I done right stored me this value for elapsed time:" + elapsedBuffer);
              
               handPresent = false;


              elapsed = Long.parseLong(elapsedBuffer); // Store the Long value of the Time elapsed

              println("mS: " + elapsed);
              float elapsedSecs = elapsed*0.001f;
              println("S: " + elapsedSecs);
              float elapsedMins = elapsedSecs/60;
              println("M: "+elapsedMins);
              displayData = str(elapsedMins);
              TableRow newSession = theTable.addRow();
              newSession.setString("pID", pID);
              newSession.setString("mS", str(elapsed));
              newSession.setString("S", str(elapsedSecs));
              newSession.setString("M", str(elapsedMins));
              println("Saving Data to File: " + csvPath); 
              saveTable(theTable, csvPath);
              fill(0xFF);
              myPort.clear();

              pID = ""; // Clear the pID
            } else {
              println("hello? ya' there?");
            }
            break;

          case '\0':
            println("Hey, there's a hand in there");
            handPresent = true;

            break;
          } // switch

          if(setupEnded == false){
            background(255, 75, 0);
            textAlign(CENTER);
            text("Configuring testing rig....",width/2, height/2);
          }
        } 
      } else {
        getPID();
      }
    //otherwise, make a connection to the rig
    } else {
      //but only do so if there is a data path to save
      if (csvPath != ""){
        //Set up the Serial Comm
        //Look for the Arduino on the USB_modem port
        println("Looking for the Arduino....");
        String portName = "0";

        for (int i = 0; i < Serial.list ().length; i++) {
            String thisPort = Serial.list()[i];
            println("[" + i + "]" + thisPort);
        }
      
        portName = Serial.list()[2];
        println("Connecting to rig via " + portName);
        myPort = new Serial(this, portName, 115200);
      }

    }
  }  
} //draw EOF
  


public void getSavePath(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    csvPath = selection.getAbsolutePath();
  }
}

public void getCsvName() {
  textAlign(LEFT);
  background(0x0055AA);
  text("Provide Name for output file. \nHit return when done. ", indent, 40);
  text(typing, indent, 90);
  if (saved != "") {
    csvName = "/"+ saved +".csv";
    println(csvName);
    saved = "";
  }
  //println("------" + csvName);
  csvPath = csvPath + csvName;
  //  text(saved,indent,130);
}

public void getPID() {
  //Clear the typing buffer
  textAlign(LEFT);
  background(0x55AA00);
  text("Type in the Participant ID. \nHit return when done. ", indent, 40);
  text(typing, indent, 90);

  if (saved != "") {
    pID = saved;
    saved = "";
  }
}
//Keyboard event handler 
public void keyPressed() {
  // If the return key is pressed, save the String and clear it
  if (key == '\n' ) {

    saved = typing;
    //csvName = "/"+ saved +".csv";
    //csvPath = csvPath + csvName;
    // A String can be cleared by setting it equal to ""
    typing = "";
  } else {
    // Otherwise, concatenate the String
    // Each character typed by the user is added to the end of the String variable.

  //Trap to prevent coded keys showing up in data or file paths
    if (keyCode == BACKSPACE) {
      //println("typing: "+ typing.length());
      typing = typing.substring(0, typing.length()-1);
    } 

     else {
      typing = typing + key;
    }
  }
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ICE_dev4" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
