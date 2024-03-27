package com.example.myapplication;

public class Record {
    private int id;
    private Double timing;
    public Record(int id, Double timing) {
        this.id = id;
        this.timing = timing;
    }
    public int getId() {
        return id;
    }

    public Double getTiming() {
        return timing;
    }
}
