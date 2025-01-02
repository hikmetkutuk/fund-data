import React, { useEffect, useState } from "react";
import { IYield, fetchFundYield } from "../services/yield";
import { DataTable } from "./Table";
import { ColumnDef } from "@tanstack/react-table";
import { createColumn } from "./Column";
import { Button } from "./ui/button";

const Yield = () => {
  const [yields, setYield] = useState<IYield[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: 20,
  });
  const [totalPages, setTotalPages] = useState<number>(0);
  const [sort, setSort] = useState<string>("id,asc");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await fetchFundYield(
          searchTerm,
          pagination.pageIndex,
          pagination.pageSize,
          sort
        );
        setYield(data.content);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error("Error fetching fund yield:", error);
      }
    };
    fetchData();
  }, [searchTerm, pagination.pageIndex, pagination.pageSize, sort]);

  const columns: ColumnDef<IYield>[] = [
    createColumn("symbol", "Symbol"),
    createColumn("index", "Index"),
    createColumn("oneMonth", "1M"),
    createColumn("threeMonth", "3M"),
    createColumn("sixMonth", "6M"),
    createColumn("ytd", "YTD"),
    createColumn("oneYear", "1Y"),
    createColumn("twoYear", "2Y"),
    createColumn("threeYear", "3Y"),
    createColumn("fourYear", "4Y"),
    createColumn("fiveYear", "5Y"),
    createColumn("sevenYear", "7Y"),
    createColumn("tenYear", "10Y"),
    createColumn("point", "Point"),
  ];

  const serializedData = yields.map((item: IYield) => ({
    ...item,
    id: item.id,
    symbol: item.symbol,
    index: item.index,
    oneMonth: item.oneMonth,
    threeMonth: item.threeMonth,
    sixMonth: item.sixMonth,
    ytd: item.ytd,
    oneYear: item.oneYear,
    twoYear: item.twoYear,
    threeYear: item.threeYear,
    fourYear: item.fourYear,
    fiveYear: item.fiveYear,
    sevenYear: item.sevenYear,
    tenYear: item.tenYear,
    point: item.point,
  }));

  const handlePageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const targetValue = e.target.value;

    if (/^\d+$/.test(targetValue)) {  // Girilen değerin sayısal olduğunu kontrol ediyoruz.
      let targetPage = Number(targetValue) - 1;  // Sayfa numarasını 0 tabanlı hale getiriyoruz.

      targetPage = Math.max(0, Math.min(totalPages - 1, targetPage));

      setPagination((prev) => ({
        ...prev,
        pageIndex: targetPage,
      }));
    }
  };

  return (
    <div className="w-11/12 mx-auto py-10 dark:text-orange-200 text-xs">
      <h1 className="text-2xl font-bold mb-4 dark:text-orange-200 text-center">Fund Yield</h1>
      <DataTable columns={columns} data={serializedData} filterBy="symbol" pageSize={pagination.pageSize} />
      <div className="flex items-center justify-between py-4 dark:bg-slate-800 text-xs">
        <Button
          className="bg-orange-200 text-slate-800"
          variant="outline"
          size="sm"
          onClick={() => setPagination((prev) => ({ ...prev, pageIndex: Math.max(0, prev.pageIndex - 1) }))}
          disabled={pagination.pageIndex <= 0}
        >
          Previous
        </Button>

        <div className="flex items-center gap-2">
          <span>Page</span>
          <input
            type="number"
            value={pagination.pageIndex + 1}
            min={1}
            max={totalPages}
            onChange={handlePageChange}
            className="w-16 border rounded text-center text-slate-800"
          />
          <span>of {totalPages}</span>
        </div>

        <Button
         className="bg-orange-200 text-slate-800"
          variant="outline"
          size="sm"
          onClick={() => setPagination((prev) => ({ ...prev, pageIndex: Math.min(totalPages - 1, prev.pageIndex + 1) }))}
          disabled={pagination.pageIndex >= totalPages - 1}
        >
          Next
        </Button>
      </div>
    </div>
  );
};

export default Yield;
