package com.finalcall.auctionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any additional fields
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

    @JsonProperty("isSeller") // Map the JSON field "isSeller" to the Java field "seller"
    private boolean seller;

    // Default constructor
    public UserDTO() {}

    // Parameterized constructor
    public UserDTO(Long id, String username, String email, String firstName, String lastName,
                  String streetAddress, String province, String country, String postalCode, boolean seller) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetAddress = streetAddress;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.seller = seller;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isSeller() {
        return seller;
    }
    
    public void setSeller(boolean seller) {
        this.seller = seller;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", streetAddress='" + streetAddress + '\'' +
               ", province='" + province + '\'' +
               ", country='" + country + '\'' +
               ", postalCode='" + postalCode + '\'' +
               ", seller=" + seller +
               '}';
    }
}
