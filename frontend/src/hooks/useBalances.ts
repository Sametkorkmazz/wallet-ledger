import { getBalances } from "@/api/balances";
import { useQuery } from "@tanstack/react-query";

const useBalances = () =>
  useQuery({ queryKey: ["balances"], queryFn: getBalances });

export default useBalances;
