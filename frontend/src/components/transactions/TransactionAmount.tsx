import type { Transaction } from "@/api/types";
import { formatMoney } from "@/lib/money";
import { cn } from "cn";

export default function TransactionAmount({
  transaction,
  announceStatus = false,
}: {
  transaction: Transaction;
  /** Read "Failed" to screen readers. Use only where no StatusBadge is shown. */
  announceStatus?: boolean;
}) {
  const isFailed = transaction.status === "FAILED";
  const isIncoming = transaction.type === "TRANSFER_IN";

  return (
    <span
      className={cn(
        "font-medium tabular-nums",
        isFailed
          ? "text-muted-foreground line-through"
          : isIncoming && "text-success",
      )}
    >
      {isFailed && announceStatus && <span className="sr-only">Failed: </span>}
      {!isFailed && (isIncoming ? "+" : "−")}
      {formatMoney(transaction.amount, transaction.currency)}
    </span>
  );
}
