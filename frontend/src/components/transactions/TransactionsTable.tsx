import type { Transaction } from "@/api/types";
import StatusBadge from "@/components/transactions/StatusBadge";
import TransactionAmount from "@/components/transactions/TransactionAmount";
import {
  col,
  transactionsTableHeader,
} from "@/components/transactions/transactionsTableLayout";
import { Table, TableBody, TableCell, TableRow } from "@/components/ui/table";
import { formatDateShort } from "@/lib/money";
import { cn } from "cn";
import { Link } from "react-router-dom";

export default function TransactionsTable({
  transactions,
}: {
  transactions: Transaction[];
}) {
  return (
    <Table className="table-fixed">
      {transactionsTableHeader}
      <TableBody>
        {transactions.map((t) => (
          <TableRow key={t.id}>
            <TableCell className={cn(col.date, "text-muted-foreground")}>
              {formatDateShort(Date.parse(t.createdAt))}
            </TableCell>
            <TableCell className={col.description}>
              <div className="flex flex-col">
                <Link
                  to={`/transactions/${t.id}`}
                  className="w-fit font-medium hover:underline focus-visible:underline"
                >
                  {t.type === "TRANSFER_IN" ? "Payment from" : "Transfer to"}{" "}
                  {t.counterparty}
                </Link>
                <span className="caption lg:hidden">{t.reference}</span>
                <span className="caption sm:hidden">
                  {formatDateShort(Date.parse(t.createdAt))}
                </span>
                <div className="mt-2 flex sm:hidden">
                  <StatusBadge status={t.status} />
                </div>
              </div>
            </TableCell>
            <TableCell className={cn(col.reference, "text-muted-foreground")}>
              {t.reference}
            </TableCell>
            <TableCell className={col.status}>
              <div className="flex">
                <StatusBadge status={t.status} />
              </div>
            </TableCell>
            <TableCell className={col.amount}>
              <TransactionAmount transaction={t} />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
