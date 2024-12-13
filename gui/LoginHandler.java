package gui;

abstract class LoginHandler {
    protected LoginHandler next;

    public void setNext(LoginHandler next) {
        this.next = next;
    }

    public abstract void login(String address, String name);
}
