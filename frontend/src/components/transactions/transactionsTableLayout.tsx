import { TableHead, TableHeader, TableRow } from "@/components/ui/table";

export const col = {
  date: "hidden w-[15%] sm:table-cell",
  description: "w-[30%]",
  reference: "hidden w-[20%] lg:table-cell",
  status: "hidden w-[15%] sm:table-cell",
  amount: "w-[20%] text-right",
} as const;

export const transactionsTableHeader = (
  <TableHeader>
    <TableRow>
      <TableHead className={col.date}>Date</TableHead>
      <TableHead className={col.description}>Description</TableHead>
      <TableHead className={col.reference}>Reference</TableHead>
      <TableHead className={col.status}>Status</TableHead>
      <TableHead className={col.amount}>Amount</TableHead>
    </TableRow>
  </TableHeader>
);
