/**
 * Inventory Management App
 * Developed by Gonzalo Patino
 * Software Engineer
 * Southern New Hampshire University
 * CS360 - Software Architecture
 *
 * Description: This app provides an inventory management system with user authentication,
 * SMS notifications for low inventory and upcoming events, and a structured UI
 * following the MVC architecture. It integrates an SQLite database for user authentication
 * and ensures incremental testing with JUnit at each development stage.
 *
 * Date: February 22, 2025
 */



package com.zybooks.inventorymapp;



public class Item {
    private int id;
    private String name;
    private int quantity;

    public Item(int id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
