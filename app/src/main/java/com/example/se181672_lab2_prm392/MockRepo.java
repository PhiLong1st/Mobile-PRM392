package com.example.se181672_lab2_prm392;

public class MockRepo {
public static class UserRepo {
    private String savedUser;
    private String savedPass;

    public boolean exists(String u) { return savedUser != null && savedUser.equals(u); }
    public void save(String u, String p) { savedUser = u; savedPass = p; }
    public boolean verify(String u, String p) { return exists(u) && savedPass.equals(p); }

}
}