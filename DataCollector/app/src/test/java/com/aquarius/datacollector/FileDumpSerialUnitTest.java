package com.aquarius.datacollector;

import com.aquarius.datacollector.control.Control;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthewxi on 7/13/17.
 */

public class FileDumpSerialUnitTest {


    @Test
    public void recognizedStartCommand() throws Exception {
        Control control = new Control();
        control.receivedData(new String(">AQ_TRANSFER_READY<").getBytes());
        assertEquals("AQ_TRANSFER_READY", control.getFullCommand());
    }

    @Test
    public void recognizedStartCommandMultipleTransmission() throws Exception {
        Control control = new Control();
        control.receivedData(new String(">AQ_TRAN").getBytes());
        control.receivedData(new String("SFER_REA").getBytes());
        control.receivedData(new String("DY<").getBytes());
        assertEquals("AQ_TRANSFER_READY", control.getFullCommand());
    }

    @Test
    public void separatesCommands() throws Exception {
        Control control = new Control();
        control.receivedData(new String(">AQ_TRANSFER_READY<").getBytes());
        control.receivedData(new String(">SOMETHING_ELSE<").getBytes());
        assertEquals("SOMETHING_ELSE", control.getFullCommand());
    }

    @Test
    public void separatesCommandsMultipleTransmission() throws Exception {
        Control control = new Control();
        control.receivedData(new String(">AQ_TRAN").getBytes());
        control.receivedData(new String("SFER_REA").getBytes());
        control.receivedData(new String("DY<").getBytes());
        control.receivedData(new String(">SOMETH").getBytes());
        control.receivedData(new String("ING_ELSE<").getBytes());
        assertEquals("SOMETHING_ELSE", control.getFullCommand());
    }

}
