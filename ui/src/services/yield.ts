import axios from 'axios';

export interface IYield {
    id: number;
    symbol: string;
    index: string;
    oneMonth: number;
    threeMonth: number;
    sixMonth: number;
    ytd: number;
    oneYear: number;
    twoYear: number;
    threeYear: number;
    fourYear: number;
    fiveYear: number;
    sixYear: number;
    sevenYear: number;
    eightYear: number;
    nineYear: number;
    tenYear: number;
    point: number;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
}


export const fetchFundYield = async (
    searchTerm: string,
    page: number,
    size: number,
    sort: string
): Promise<PaginatedResponse<IYield>> => {
    const response = await axios.get<PaginatedResponse<IYield>>('http://localhost:8080/api/fund/yield', {
        params: {
            searchTerm,
            page,
            size,
            sort,
        },
    });
    return response.data;
};
