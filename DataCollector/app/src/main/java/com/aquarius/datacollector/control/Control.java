package com.aquarius.datacollector.control;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by matthewxi on 7/13/17.
 */

public class Control {

    public static final int CONTROL_MODE = 1;
    public static final int FILE_TRANSFER_MODE = 2;

    public static final String ACK = ">WT_OK<";

    ControlListener listener = null;
    private String fullCommand;
    private Boolean receivingCommand = false;
    private int mode = CONTROL_MODE;
    private File fileTransferStorage;
    private FileWriter writer;

    private Context context;

    public Control(Context context){
        this.context = context;
    }

    public String getFullCommand() {
        return fullCommand;
    }

    public Boolean getReceivingCommand() {
        return receivingCommand;
    }

    public void setListener(ControlListener listener){
        this.listener = listener;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) throws IOException {
        if( this.mode == CONTROL_MODE && mode == FILE_TRANSFER_MODE){
            initializeFileTransferStorage();
        }
        this.mode = mode;
    }

    private void initializeFileTransferStorage() throws IOException {
        fileTransferStorage = File.createTempFile("aquarius", "", context.getFilesDir());
        writer = new FileWriter(fileTransferStorage);
    }

    public void receivedData(String dataString) throws IOException  {

        if(mode == CONTROL_MODE) {

            if (receivingCommand) {
                String command = dataString;
                if (command.contains("<")) {
                    command = command.substring(0, command.indexOf('<'));
                    fullCommand = fullCommand + command;
                    // Process Command
                    receivingCommand = false;
                    if (listener != null) {
                        listener.processCommand(fullCommand);
                    }
                } else {
                    // Command not completely received
                    fullCommand = fullCommand + command;
                }
            } else if (dataString.contains(">")) {
                String command = dataString.substring(dataString.indexOf('>') + 1);
                if (command.contains("<")) {
                    fullCommand = command.substring(0, command.indexOf('<'));
                    // Process Command
                    receivingCommand = false;
                    if (listener != null) {
                        listener.processCommand(fullCommand);
                    }
                } else {
                    // Command not completely received
                    fullCommand = command;
                    receivingCommand = true;
                }

            }
        } else if (mode == FILE_TRANSFER_MODE){

            // This could get unit tested
            try {

                String controlString = null;
                Boolean transferComplete = false;
                if(dataString.contains(">")) {
                    controlString = dataString;
                    dataString = dataString.substring(0, dataString.indexOf('>'));
                    transferComplete = true;
                }

                writer.append(dataString);

                if(transferComplete){
                    writer.flush();
                    writer.close();
                    setMode(CONTROL_MODE);
                    listener.fileTransfered(fileTransferStorage);
                }

                if(controlString != null){
                    receivedData(controlString);
                }


            } catch (IOException e) {
                writer.close();
                fileTransferStorage.delete();
                setMode(CONTROL_MODE);
                e.printStackTrace();
                throw e;
            }
        }
    }
}
