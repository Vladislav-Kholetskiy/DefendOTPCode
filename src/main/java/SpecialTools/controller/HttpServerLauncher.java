package SpecialTools.controller;

import com.sun.net.httpserver.HttpServer;
import SpecialTools.controller.admin.ListUsersHandler;
import SpecialTools.controller.admin.UpdateOtpConfigHandler;
import SpecialTools.controller.admin.DeleteUserHandler;
import java.net.InetSocketAddress;

public class HttpServerLauncher {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/register",     new RegisterHandler());
        server.createContext("/login",        new LoginHandler());
        server.createContext("/generate-otp", new GenerateOtpHandler());
        server.createContext("/validate-otp", new ValidateOtpHandler());

        server.createContext("/admin/otp-config",   new UpdateOtpConfigHandler());
        server.createContext("/admin/users",        new ListUsersHandler());
        server.createContext("/admin/users/delete", new DeleteUserHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🚀 Server started at http://localhost:8080");
    }
}