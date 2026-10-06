package com.chatapp;

import java.util.Scanner;

/**
 * Console application for Part 1: registration and login.
 * No GUI, no JOptionPane - console input and output only.
 */
public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("=== Chat App: Registration ===");

        System.out.print("Enter your first name: ");
        String firstName = input.nextLine().trim();

        System.out.print("Enter your last name: ");
        String lastName = input.nextLine().trim();

        Login user = new Login(firstName, lastName, "", "", "");

        // Keep asking until each value is accepted
        do {
            System.out.print("Enter a username: ");
            user.setUsername(input.nextLine().trim());
            System.out.println(user.usernameMessage());
        } while (!user.checkUserName());

        do {
            System.out.print("Enter a password: ");
            user.setPassword(input.nextLine());
            System.out.println(user.passwordMessage());
        } while (!user.checkPasswordComplexity());

        do {
            System.out.print("Enter your cell phone number (e.g. +27838968976): ");
            user.setCellPhone(input.nextLine().trim());
            System.out.println(user.cellMessage());
        } while (!user.checkCellPhoneNumber());

        System.out.println("\nRegistration complete.");

        System.out.println("\n=== Chat App: Login ===");
        boolean loggedIn;
        do {
            System.out.print("Username: ");
            String enteredUsername = input.nextLine().trim();
            System.out.print("Password: ");
            String enteredPassword = input.nextLine();

            loggedIn = user.loginUser(enteredUsername, enteredPassword);
            System.out.println(user.returnLoginStatus(loggedIn));
        } while (!loggedIn);

        input.close();
    }
}
