package com.lsdzs.lsdzs_tool.ble;

public class ParamModel {
    private String name;
    private int pgn;
    private int value;
    private int min;
    private int max;

    public ParamModel(String name, int pgn) {
        this.name = name;
        this.pgn = pgn;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPgn() {
        return pgn;
    }

    public void setPgn(int pgn) {
        this.pgn = pgn;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
