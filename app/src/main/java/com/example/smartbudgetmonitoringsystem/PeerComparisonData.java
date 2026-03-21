package com.example.smartbudgetmonitoringsystem;

import java.util.Map;

public class PeerComparisonData {
    private String peerName;
    private double mySpent;
    private double myBudget;
    private double peerSpent;
    private double peerBudget;
    private Map<String, Double> myCategories;
    private Map<String, Double> peerCategories;

    public PeerComparisonData(String peerName, double mySpent, double myBudget, 
                              double peerSpent, double peerBudget, 
                              Map<String, Double> myCategories, 
                              Map<String, Double> peerCategories) {
        this.peerName = peerName;
        this.mySpent = mySpent;
        this.myBudget = myBudget;
        this.peerSpent = peerSpent;
        this.peerBudget = peerBudget;
        this.myCategories = myCategories;
        this.peerCategories = peerCategories;
    }

    public String getPeerName() { return peerName; }
    public double getMySpent() { return mySpent; }
    public double getMyBudget() { return myBudget; }
    public double getPeerSpent() { return peerSpent; }
    public double getPeerBudget() { return peerBudget; }
    public Map<String, Double> getMyCategories() { return myCategories; }
    public Map<String, Double> getPeerCategories() { return peerCategories; }
}
