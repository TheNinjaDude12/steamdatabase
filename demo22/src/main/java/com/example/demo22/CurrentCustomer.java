package com.example.demo22;

public class CurrentCustomer {

    private static CurrentCustomer instance;

    private Integer customerId;
    private String customerEmail;
    private String customerName;

    private CurrentCustomer() {
    }

    public static CurrentCustomer getInstance() {
        if (instance == null) {
            instance = new CurrentCustomer();
        }
        return instance;
    }

    public void login(int customerId, String email, String name) {
        this.customerId = customerId;
        this.customerEmail = email;
        this.customerName = name;
    }

    public void logout() {
        this.customerId = null;
        this.customerEmail = null;
        this.customerName = null;
    }

    public boolean isLoggedIn() {
        return customerId != null;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }
}
