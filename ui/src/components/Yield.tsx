import React, { useEffect, useState } from "react";
import { Table, InputGroup, FormControl, Pagination } from "react-bootstrap";

import { IYield, fetchFundYield } from "../services/yield";

const Yield = () => {
  const [yields, setYield] = useState<IYield[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [sort, setSort] = useState<string>("id,asc");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await fetchFundYield(searchTerm, page, size, sort);
        setYield(data.content);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error("Error fetching fund yield:", error);
      }
    };
    fetchData();
  }, [searchTerm, page, sort]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    setPage(0); // Reset to first page
  };

  const handleSort = (field: string) => {
    setSort(`${field},${sort.endsWith("asc") ? "desc" : "asc"}`);
  };

  return (
    <div className="container mt-5">
      <InputGroup className="mb-3">
        <FormControl
          placeholder="Search"
          value={searchTerm}
          onChange={handleSearchChange}
        />
      </InputGroup>

      <Table striped bordered hover>
        <thead>
          <tr>
            <th onClick={() => handleSort("symbol")}>Symbol</th>
            <th onClick={() => handleSort("index")}>Index</th>
            <th onClick={() => handleSort("oneMonth")}>One Month</th>
            <th onClick={() => handleSort("threeMonth")}>Three Month</th>
            <th onClick={() => handleSort("sixMonth")}>Six Month</th>
            <th onClick={() => handleSort("ytd")}>YTD</th>
            <th onClick={() => handleSort("oneYear")}>One Year</th>
            <th onClick={() => handleSort("twoYear")}>Two Year</th>
            <th onClick={() => handleSort("threeYear")}>Three Year</th>
            <th onClick={() => handleSort("fourYear")}>Four Year</th>
            <th onClick={() => handleSort("fiveYear")}>Five Year</th>
            <th onClick={() => handleSort("sevenYear")}>Seven Year</th>
            <th onClick={() => handleSort("tenYear")}>Ten Year</th>
            <th onClick={() => handleSort("point")}>Point</th>
          </tr>
        </thead>
        <tbody>
          {yields.map((y) => (
            <tr key={y.id}>
              <td>{y.symbol}</td>
              <td>{y.index}</td>
              <td>{y.oneMonth}</td>
              <td>{y.threeMonth}</td>
              <td>{y.sixMonth}</td>
              <td>{y.ytd}</td>
              <td>{y.oneYear}</td>
              <td>{y.twoYear}</td>
              <td>{y.threeYear}</td>
              <td>{y.fourYear}</td>
              <td>{y.fiveYear}</td>
              <td>{y.sevenYear}</td>
              <td>{y.tenYear}</td>
              <td>{y.point}</td>
            </tr>
          ))}
        </tbody>
      </Table>

      <Pagination>
        {[...Array(totalPages).keys()].map((number) => (
          <Pagination.Item
            key={number}
            active={number === page}
            onClick={() => setPage(number)}
          >
            {number + 1}
          </Pagination.Item>
        ))}
      </Pagination>
    </div>
  );
};

export default Yield;
