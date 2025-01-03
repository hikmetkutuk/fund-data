package com.tefas_fund.dto;

public record CalculateResponse(
    String symbol,
    String index,
    double oneMonthGrowth,
    double threeMonthGrowth,
    double sixMonthGrowth,
    double ytdGrowth,
    double oneYearGrowth,
    double twoYearGrowth,
    double threeYearGrowth,
    double fourYearGrowth,
    double fiveYearGrowth,
    double sixYearGrowth,
    double sevenYearGrowth,
    double eightYearGrowth,
    double nineYearGrowth,
    double tenYearGrowth,
    double point
) {}
