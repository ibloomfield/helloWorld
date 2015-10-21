package readSerial;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.comm.*;
import javax.swing.*;

public class readSerial implements /*Runnable,/**/ SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;

	InputStream inputStream;
	OutputStream outputStream;

	SerialPort serialPort;
	Thread readThread;

	public static void main(String[] args) {
		String newLine = System.getProperty("line.separator"); //system independent newline

		portList = CommPortIdentifier.getPortIdentifiers();

		int i=0;
		System.out.println("Devices Available:");

		//Display all serial com devices available
		while (portList.hasMoreElements()){
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {        		
				System.out.println(i+1 + " - " + portId.getName());
			}
			i++;
			if(i == 20){
				break;
			}
		}

		//prompt for device selection
		Scanner s = new Scanner(System.in);
		System.out.println("Chose Device:");
		int chosenPort = s.nextInt();
		
		//write before reading
		Scanner writeInput = new Scanner(System.in);
		System.out.println("Write to device before reading:");
		String stringToWrite = s.next();
		System.out.println("Writing: " + stringToWrite);
		
		//take input and set com device
		portList = CommPortIdentifier.getPortIdentifiers();
		i=0;
		while (portList.hasMoreElements()){
			i++;
			portId = (CommPortIdentifier) portList.nextElement();
			if (chosenPort == i) {
				readSerial reader = new readSerial();
				System.out.println(portId.getName() + " chosen.");
			}
		}
	}

	public void writeSerial(String toWrite)
    {
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {System.out.println(e);}
        try
        {
            outputStream.write(toWrite);
            outputStream.flush();
            //this is a delimiter for the data
            outputStream.write(DASH_ASCII);
            outputStream.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "n");
        }
    }
	
	public readSerial() {
		try {
			serialPort = (SerialPort) portId.open("readSerial", 2000);
		} catch (PortInUseException e) {System.out.println(e);}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {System.out.println(e);}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {System.out.println(e);}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(115200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {System.out.println(e);}

		//following is needed for runnable implementation... not sure why runnable is desire...
		//readThread = new Thread(this);
		//readThread.start();
	}

	/*public void run() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {System.out.println(e);}
	}/*for thread control?? timing/memory use thing?*/

	public void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[38];
			int bytesRead;
			try {
				int availableBytes =  inputStream.available();
				System.out.println(availableBytes+" bytes are available to read");
				while ((bytesRead = inputStream.read(readBuffer)) != -1) {
					System.out.print(new String(readBuffer,0,bytesRead,Charset.forName("UTF-8")));
					
					/* slow down for testing purposes
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {System.out.println(e);}
					/**/
				}				
			} catch (IOException e) {System.out.println(e);}
			break;
		}
	}
}
