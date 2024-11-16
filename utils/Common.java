package utils;

public class Common {
    public static void main(String[] args) {
        String[] test_cases = {
            "123",
            "",
            "What do you mean",
            "      \n\t",
            null,
            "1234567890abcdefg"
        };

        for (String username : test_cases) {
            System.out.println(validate_username(username));
        }
    }

    /* 
     * Validate the username.
     * 
     * It returns null if username is valid; otherwise it returns the reason it's not valid.
     */
    public static String validate_username(String username) {
        if (username == null) {
            return "Username must not be null";
        }

        if (username.isBlank()) {
            return "Username must not be blank or contain only whitespace";
        }

        username = username.trim();

        if (username.length() > 16) {
            return "Username must not exceed 16 letters";
        }

        return null;
    }
}