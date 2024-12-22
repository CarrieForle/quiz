package gui;

import java.net.InetSocketAddress;

abstract class LoginHandler {
    protected LoginHandler next;

    public void setNext(LoginHandler next) {
        this.next = next;
    }

    protected abstract void login(LoginDialog dialog, InetSocketAddress address, String name);
}
