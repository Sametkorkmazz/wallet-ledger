import {
  getTransactions,
  type GetTransactionsParams,
} from "@/api/transactions";
import { keepPreviousData, useQuery } from "@tanstack/react-query";

const useTransactions = ({ page, size }: GetTransactionsParams) =>
  useQuery({
    queryKey: ["transactions", { page, size }],
    queryFn: () => getTransactions({ page, size }),
    placeholderData: keepPreviousData, // keeps the old page visible while the next loads
  });

export default useTransactions;
