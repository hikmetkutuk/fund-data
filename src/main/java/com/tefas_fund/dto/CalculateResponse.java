package com.tefas_fund.dto;

public record CalculateResponse(
    String symbol,
    String index,
    Object oneMonthGrowth,
    Object threeMonthGrowth,
    Object sixMonthGrowth,
    Object ytdGrowth,
    Object oneYearGrowth,
    Object twoYearGrowth,
    Object threeYearGrowth,
    Object fourYearGrowth,
    Object fiveYearGrowth,
    Object sevenYearGrowth,
    Object tenYearGrowth
) {
}
