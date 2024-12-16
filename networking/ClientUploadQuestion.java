package networking;

import java.io.*;
import utils.QuestionSet;

public class ClientUploadQuestion {
    // private void showInitialDialog() {
    //     String[] options = {"Create New Question Bank", "Select Local Question Bank"};
    //     int choice = JOptionPane.showOptionDialog(
    //             this,
    //             "Choose an option to start:",
    //             "Question Bank",
    //             JOptionPane.DEFAULT_OPTION,
    //             JOptionPane.INFORMATION_MESSAGE,
    //             null,
    //             options,
    //             options[0]
    //     );

    //     if (choice == 0) {
    //         questions.clear();
    //         buttonPanel.removeAll();
    //         updateButtons();
    //     } else if (choice == 1) {
    //         loadQuestionBank();
    //     } else {
    //         System.exit(0);
    //     }
    // }

    public static void main(String[] args) {

    }

    public static void uploadToServer(QuestionSet questionSet, OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF("#");
        dataOutputStream.writeUTF(questionSet.toString());
    }
}
