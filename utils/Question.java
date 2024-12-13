package utils;

public class Question {
    public String question;
    protected String[] options = new String[4];

    public void setOptions(int i, String s) {
        this.options[i] = s;
    }

    public void setOptions(String[] s) {
        for (int i = 0; i < this.options.length; i++) {
            this.options[i] = s[i];
        }
    }

    public String getOption(int i) {
        return options[i];
    }

    public int getOptionLength() {
        return this.options.length;
    }
}