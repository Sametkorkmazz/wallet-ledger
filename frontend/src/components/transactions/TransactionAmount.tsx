import type { Transaction } from "@/api/types";
import { formatMoney } from "@/lib/money";
import { cn } from "cn";

export default function TransactionAmount({
  transaction,
}: {
  transaction: Transaction;
}) {
  const incoming = transaction.type === "TRANSFER_IN";
  return (
    <span
      className={cn("font-medium tabular-nums", incoming && "text-success")}
    >
      {incoming ? "+" : "−"}
      {formatMoney(transaction.amount, transaction.currency)}
    </span>
  );
}
