package com.example.se181672_lab2_prm392;

public class MockRepo {
    private static String savedUser;
    private static String savedPass;

    public static boolean exists(String u) { return savedUser != null && savedUser.equals(u); }
    public static void save(String u, String p) { savedUser = u; savedPass = p; }
    public static boolean verify(String u, String p) { return exists(u) && savedPass.equals(p); }

}