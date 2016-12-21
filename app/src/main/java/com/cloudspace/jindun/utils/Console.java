package com.cloudspace.jindun.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Console {

    public interface ResponseListener {

        void onResponse(String data);

    }

    private static final long CONSOLE_STREAM_READER_TIMEOUT = 10000L;

    private static ResponseListener listener = null;

    private static State mState = State.idle;

    public static String execute(String command, long timeout) {
        return execute(command, timeout, CONSOLE_STREAM_READER_TIMEOUT);
    }

    public synchronized static String execute(String command, long timeout, long readerTimeout) {
        try {
            if (mState == State.executing) {
                try {
                    Console.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String data = null;

            ConsoleThread consoleThread = new ConsoleThread(command, readerTimeout);
            consoleThread.start();

            try {
                consoleThread.join(timeout);
            } catch (InterruptedException e) {
            }

            if (consoleThread.isAlive()) {
                consoleThread.interrupt();
            }

            data = consoleThread.getOutputData();
            return data;
        } finally {
            mState = State.idle;
            Console.class.notifyAll();
        }
    }

    public static void execute(String command, long timeout, ResponseListener listener) {
        execute(command, timeout, CONSOLE_STREAM_READER_TIMEOUT, listener);
    }

    public synchronized static void execute(String command, long timeout, long readerTimeout, ResponseListener listener) {
        try {
            if (mState == State.executing) {
                try {
                    Console.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setListener(listener);

            ConsoleThread consoleThread = new ConsoleThread(command, readerTimeout);
            consoleThread.start();

            try {
                consoleThread.join(timeout);
            } catch (InterruptedException e) {
            }

            if (consoleThread.isAlive()) {
                consoleThread.interrupt();
            }

            removeListener();
        } finally {
            mState = State.idle;
            Console.class.notifyAll();
        }
    }

    public static class ConsoleReader extends Thread {

        private InputStream inputStream;

        private String data;

        public ConsoleReader(InputStream istream) {
            this.inputStream = istream;
        }

        @Override
        public void run() {

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder sb = new StringBuilder();

            String resp = null;

            try {
                while ((resp = reader.readLine()) != null) {

                    if (listener != null) {
                        listener.onResponse(resp);
                    } else {
                        sb.append(resp);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
            setData(sb.toString());
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getData() {
            return this.data;
        }

    }

    public static class ConsoleThread extends Thread {

        String data = null;

        String command = null;

        long readerTimeout = 0L;

        public ConsoleThread(String cmd, long readerTimeout) {
            this.command = cmd;
            this.readerTimeout = readerTimeout;
        }

        @Override
        public void run() {
            Runtime runtime = Runtime.getRuntime();

            Process process = null;

            try {
                process = runtime.exec(command);
                if (listener == null) {
                    process.waitFor();
                }
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }

            if (process != null) {

                ConsoleReader consoleReader = new ConsoleReader(process.getInputStream());
                consoleReader.start();

                try {
                    consoleReader.join(readerTimeout);
                } catch (InterruptedException e) {

                }
                if (consoleReader.isAlive()) {
                    consoleReader.interrupt();
                }

                setOutputData(consoleReader.getData());

                process.destroy();
            }
        }

        public String getOutputData() {
            return data;
        }

        public void setOutputData(String data) {
            this.data = data;
        }

    }

    private static void removeListener() {
        listener = null;
    }

    private static void setListener(ResponseListener listener) {
        Console.listener = listener;
    }

    private enum State {
        executing, idle
    }


}
