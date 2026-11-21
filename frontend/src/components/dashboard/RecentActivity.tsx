import StatusBadge from "@/components/dashboard/StatusBadge";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@/components/ui/empty";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import useTransactions from "@/hooks/useTransactions";
import { formatDateShort, formatMoney } from "@/lib/money";
import { cn } from "cn";
import { CircleAlert, Inbox, RefreshCw } from "lucide-react";

const header = (
  <TableHeader>
    <TableRow>
      <TableHead className="w-2/5">Transaction</TableHead>
      <TableHead className="hidden w-1/5 md:table-cell">Date</TableHead>
      <TableHead className="hidden w-1/5 sm:table-cell">Status</TableHead>
      <TableHead className="w-1/5 text-right">Amount</TableHead>
    </TableRow>
  </TableHeader>
);

function SkeletonText({ className }: { className?: string }) {
  return (
    <div className="flex h-lh items-center">
      <Skeleton className={cn("h-3", className)} />
    </div>
  );
}

export default function RecentActivity() {
  const { data, isPending, isError, isFetching, refetch } = useTransactions({
    page: 0,
    size: 5,
  });

  return (
    <Card
      aria-labelledby="recent-activity-title"
      className="[--card-spacing:--spacing(6)]"
    >
      <CardHeader>
        <CardTitle>
          <h2 id="recent-activity-title">Recent activity</h2>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <>
            <span className="sr-only">Loading activity</span>
            <Table aria-busy="true" className="table-fixed">
              {header}
              <TableBody>
                {Array.from({ length: 5 }, (_, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <SkeletonText className="w-24" />
                      <div className="caption">
                        <SkeletonText className="w-33" />
                      </div>
                      <div className="mt-2 sm:hidden">
                        <Skeleton className="h-5.5 w-20 rounded-full" />
                      </div>
                    </TableCell>
                    <TableCell className="hidden md:table-cell">
                      <SkeletonText className="w-20" />
                    </TableCell>
                    <TableCell className="hidden sm:table-cell">
                      <SkeletonText className="w-16" />
                    </TableCell>
                    <TableCell>
                      <SkeletonText className="ml-auto w-16" />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </>
        ) : isError && data === undefined ? (
          <Alert variant="destructive">
            <CircleAlert />
            <AlertTitle>Couldn't load your activity</AlertTitle>
            <AlertDescription className="flex flex-col gap-2">
              <p>
                The server didn't answer. Your money is safe. Check your
                connection and try again.
              </p>
              <Button
                className="self-start"
                variant="outline"
                size="sm"
                onClick={() => refetch()}
                disabled={isFetching}
              >
                <RefreshCw />
                Try again
              </Button>
            </AlertDescription>
          </Alert>
        ) : data.content.length === 0 ? (
          <Empty aria-labelledby="recent-activity-empty-title">
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Inbox className="size-6" />
              </EmptyMedia>
              <EmptyTitle id="recent-activity-empty-title">
                No activity yet
              </EmptyTitle>
              <EmptyDescription>
                Your transfers will show up here.
              </EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          <Table className="table-fixed">
            {header}
            <TableBody>
              {data.content.map((t) => (
                <TableRow key={t.id}>
                  <TableCell>
                    <div className="flex flex-col">
                      <span className="font-medium">{t.counterparty}</span>
                      <span className="caption">{t.reference}</span>
                      <span className="mt-2 sm:hidden">
                        <StatusBadge status={t.status} />
                      </span>
                    </div>
                  </TableCell>
                  <TableCell className="hidden text-muted-foreground md:table-cell">
                    {formatDateShort(Date.parse(t.createdAt))}
                  </TableCell>
                  <TableCell className="hidden sm:table-cell">
                    <StatusBadge status={t.status} />
                  </TableCell>
                  <TableCell
                    className={cn(
                      "text-right tabular-nums",
                      t.type === "TRANSFER_IN" && "text-success",
                    )}
                  >
                    {t.type === "TRANSFER_IN" && "+"}
                    {formatMoney(t.amount, t.currency)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
