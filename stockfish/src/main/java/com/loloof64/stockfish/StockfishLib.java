package com.loloof64.stockfish;

/**
 * Stockfish UCI process. Starts automatically, then runs infinitely : can only be stopped by sending
 * the command "exit".
 */
public class StockfishLib {

    static {
        System.loadLibrary("stockfish");
    }

    public StockfishLib() {
        new Thread(this::mainLoop).start();
    }

    public native void mainLoop();

    /**
     * Stops the process : sends the command "quit".
     * This is better to do this in the Application subclass.
     */
    public void quit() {
        sendCommand("quit");
    }

    /**
     * Reads next output from the stockfish process.
     * Returns "@@@Empty output@@@" if no output available.
     * @return String - the output if available, otherwise "@@@Empty output@@@".
     */
    public native String readNextOutput();

    /**
     * Sends a command to the stockfish process.
     *
     * If you want to stop the process, send the command "quit".
     * This is better to do this in the Application subclass.
     *
     * @param command - String - the command to send to the stockfish process.
     */
    public native void sendCommand(String command);
}