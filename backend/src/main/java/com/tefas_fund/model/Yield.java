package com.tefas_fund.model;

import jakarta.persistence.*;

@Entity
@Table(name = "yield")
public class Yield {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String index;
    private Double oneMonth;
    private Double threeMonth;
    private Double sixMonth;
    private Double ytd;
    private Double oneYear;
    private Double twoYear;
    private Double threeYear;
    private Double fourYear;
    private Double fiveYear;
    private Double sixYear;
    private Double sevenYear;
    private Double eightYear;
    private Double nineYear;
    private Double tenYear;
    private double point;


    public Yield() {
    }

    public Yield(
            String symbol,
            String index,
            Double oneMonth,
            Double threeMonth,
            Double sixMonth,
            Double ytd,
            Double oneYear,
            Double twoYear,
            Double threeYear,
            Double fourYear,
            Double fiveYear,
            Double sixYear,
            Double sevenYear,
            Double eightYear,
            Double nineYear,
            Double tenYear,
            double point) {
        this.symbol = symbol;
        this.index = index;
        this.oneMonth = oneMonth;
        this.threeMonth = threeMonth;
        this.sixMonth = sixMonth;
        this.ytd = ytd;
        this.oneYear = oneYear;
        this.twoYear = twoYear;
        this.threeYear = threeYear;
        this.fourYear = fourYear;
        this.fiveYear = fiveYear;
        this.sixYear = sixYear;
        this.sevenYear = sevenYear;
        this.eightYear = eightYear;
        this.nineYear = nineYear;
        this.tenYear = tenYear;
        this.point = point;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Double getOneMonth() {
        return oneMonth;
    }

    public void setOneMonth(Double oneMonth) {
        this.oneMonth = oneMonth;
    }

    public Double getThreeMonth() {
        return threeMonth;
    }

    public void setThreeMonth(Double threeMonth) {
        this.threeMonth = threeMonth;
    }

    public Double getSixMonth() {
        return sixMonth;
    }

    public void setSixMonth(Double sixMonth) {
        this.sixMonth = sixMonth;
    }

    public Double getYtd() {
        return ytd;
    }

    public void setYtd(Double ytd) {
        this.ytd = ytd;
    }

    public Double getOneYear() {
        return oneYear;
    }

    public void setOneYear(Double oneYear) {
        this.oneYear = oneYear;
    }

    public Double getTwoYear() {
        return twoYear;
    }

    public void setTwoYear(Double twoYear) {
        this.twoYear = twoYear;
    }

    public Double getThreeYear() {
        return threeYear;
    }

    public void setThreeYear(Double threeYear) {
        this.threeYear = threeYear;
    }

    public Double getFourYear() {
        return fourYear;
    }

    public void setFourYear(Double fourYear) {
        this.fourYear = fourYear;
    }

    public Double getFiveYear() {
        return fiveYear;
    }

    public void setFiveYear(Double fiveYear) {
        this.fiveYear = fiveYear;
    }

    public Double getSixYear() {
        return sixYear;
    }

    public void setSixYear(Double sixYear) {
        this.sixYear = sixYear;
    }

    public Double getSevenYear() {
        return sevenYear;
    }

    public void setSevenYear(Double sevenYear) {
        this.sevenYear = sevenYear;
    }

    public Double getEightYear() {
        return eightYear;
    }

    public void setEightYear(Double eightYear) {
        this.eightYear = eightYear;
    }

    public Double getNineYear() {
        return nineYear;
    }

    public void setNineYear(Double nineYear) {
        this.nineYear = nineYear;
    }

    public Double getTenYear() {
        return tenYear;
    }

    public void setTenYear(Double tenYear) {
        this.tenYear = tenYear;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }
}
