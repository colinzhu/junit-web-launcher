package junitweblauncher.web;

import io.vertx.core.Vertx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SysOutToEventBus {

    private static final String EVENT_ADDRESS = "console_messages_created";
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final Vertx vertx;

    public SysOutToEventBus(Vertx vertx) {
        this.vertx = vertx;
        sysOutToMessageQueue();
        messageQueueToEventBus();
    }

    private void messageQueueToEventBus() {
        vertx.setPeriodic(500, id -> {
            List<String> batch = new ArrayList<>();
            messageQueue.drainTo(batch);
            if (!batch.isEmpty()) {
                vertx.eventBus().publish(EVENT_ADDRESS, String.join("\n", batch));
            }
        });
    }

    private void sysOutToMessageQueue() {
        OutputStream messageQueueOutputStream = new OutputStream() {
            private final BufferedOutputStream oriOutStream = new BufferedOutputStream(System.out);
            private final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                if (b == '\n') {
                    // Convert the byte array into a string and queue the message
                    boolean offered = messageQueue.offer(byteArrayStream.toString());
                    if (!offered) {
                        System.err.println("Message queue is full. Dropping message.");
                    }
                    byteArrayStream.reset();  // Clear the buffer
                } else {
                    byteArrayStream.write(b);  // Buffer the log message
                }
                // Write to the original System.out in a buffered manner
                oriOutStream.write(b);
            }

            @Override
            public void flush() throws IOException {
                oriOutStream.flush();  // Ensure the buffered output is flushed
            }
        };

        // Redirect System.out to our custom PrintStream
        System.setOut(new PrintStream(messageQueueOutputStream, true));
    }
}
