package node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface IClient {
    public OutputStream getOutputStream() throws IOException;
    public InputStream getInputStream() throws IOException;
    public int getID();
}
