package com.finalcall.catalogueservice.dto;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String streetAddress;
    private String province;
    private String country;
    private String postalCode;
    private boolean isSeller;

    // Constructors
    public UserDTO() {}

    public UserDTO(Long id, String username, String email,
                   String firstName, String lastName, String streetAddress,
                   String province, String country, String postalCode,
                   boolean isSeller) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetAddress = streetAddress;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.isSeller = isSeller;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }
 
    public String getLastName() {
        return lastName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
 
    public String getProvince() {
        return province;
    }
 
    public String getCountry() {
        return country;
    }
 
    public String getPostalCode() {
        return postalCode;
    }
 
    public boolean getIsSeller() {
        return isSeller;
    }

    public void setId(Long id) {
        this.id = id;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
 
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
 
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
 
    public void setProvince(String province) {
        this.province = province;
    }
 
    public void setCountry(String country) {
        this.country = country;
    }
 
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
 
    public void setIsSeller(boolean isSeller) {
        this.isSeller = isSeller;
    }
}
