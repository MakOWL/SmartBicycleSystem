package com.example.smart_bicycle20;

public class UserHelpeClass {
    String fname,uname,email,phone,pass,conPass;

    public UserHelpeClass(String fname, String uname, String email, String phone, String pass, String conPass) {
        this.fname = fname;
        this.uname = uname;
        this.email = email;
        this.phone = phone;
        this.pass = pass;
        this.conPass = conPass;
    }

    public UserHelpeClass() {
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getConPass() {
        return conPass;
    }

    public void setConPass(String conPass) {
        this.conPass = conPass;
    }
}
