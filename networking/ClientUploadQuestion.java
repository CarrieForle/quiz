package networking;

import java.io.*;
import utils.QuestionSet;

public class ClientUploadQuestion {
    public static void main(String[] args) {

    }

    public static void uploadToServer(QuestionSet questionSet, OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF("#");
        dataOutputStream.writeUTF(questionSet.toString());
    }
}
