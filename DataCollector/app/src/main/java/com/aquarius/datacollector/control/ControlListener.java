package com.aquarius.datacollector.control;

import java.io.File;

public abstract interface ControlListener{
    public abstract void processCommand(String command);
    public abstract void fileTransfered(File fileTransferStorage);
}
