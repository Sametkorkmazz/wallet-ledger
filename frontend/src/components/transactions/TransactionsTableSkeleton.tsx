import SkeletonText from "@/components/common/SkeletonText";
import {
  col,
  transactionsTableHeader,
} from "@/components/transactions/transactionsTableLayout";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableRow } from "@/components/ui/table";

export default function TransactionsTableSkeleton({ rows }: { rows: number }) {
  return (
    <>
      <span className="sr-only">Loading transactions</span>
      <Table aria-busy="true" className="table-fixed">
        {transactionsTableHeader}
        <TableBody>
          {Array.from({ length: rows }, (_, i) => (
            <TableRow key={i}>
              <TableCell className={col.date}>
                <SkeletonText className="w-28" />
              </TableCell>
              <TableCell className={col.description}>
                <div className="flex flex-col">
                  <SkeletonText className="w-48" />
                  <div className="caption lg:hidden">
                    <SkeletonText className="w-32" />
                  </div>
                  <div className="mt-2 sm:hidden">
                    <Skeleton className="h-5 w-20 rounded-full" />
                  </div>
                </div>
              </TableCell>
              <TableCell className={col.reference}>
                <SkeletonText className="w-36" />
              </TableCell>
              <TableCell className={col.status}>
                <div className="flex">
                  <Skeleton className="h-5 w-24 rounded-full" />
                </div>
              </TableCell>
              <TableCell className={col.amount}>
                <SkeletonText className="ml-auto w-20" />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </>
  );
}
