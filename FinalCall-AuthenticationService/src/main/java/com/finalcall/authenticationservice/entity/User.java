package com.finalcall.authenticationservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "STREET", nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private boolean isSeller;

    // Constructors

    public User() {
    }

    public User(String username, String password, String email,
                String firstName, String lastName, String streetAddress,
                String province, String country, String postalCode,
                boolean isSeller) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetAddress = streetAddress;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.isSeller = isSeller;
    }

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

    public String getPassword() {
        return password;
    }

    // CHANGE ME OH THIS IS AWFULLY UNSAFE
    public void setPassword(String password) {
        this.password = password;
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

    public boolean getIsSeller() {
        return isSeller;
    }

    public void setIsSeller(boolean isSeller) {
        this.isSeller = isSeller;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", isSeller=" + isSeller +
                '}';
    }
}
