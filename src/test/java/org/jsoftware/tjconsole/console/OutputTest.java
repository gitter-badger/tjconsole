package org.jsoftware.tjconsole.console;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class OutputTest {

    @Test
    public void testNoColors() throws Exception {
        String text = "@|cyan name|@ = @|yellow value |@";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(new PrintStream(baos), false);
        output.print(text);
        String result = new String(baos.toByteArray());
        assertEquals("name = value", result);
    }
}