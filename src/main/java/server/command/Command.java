package server.command;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Command interface for all Redis commands.
 */
public interface Command {
    /**
     * Execute the command using the provided arguments and write the response to outputStream.
     *
     * @param args Arguments (excluding the command name itself)
     * @param outputStream OutputStream to write RESP response
     * @throws IOException if writing fails
     */
    void execute(List<String> args, OutputStream outputStream) throws IOException;
}
