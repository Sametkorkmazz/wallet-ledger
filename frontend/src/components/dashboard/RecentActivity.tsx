import EmptyState from "@/components/common/EmptyState";
import ErrorAlert from "@/components/common/ErrorAlert";
import SkeletonText from "@/components/common/SkeletonText";
import TransactionAmount from "@/components/transactions/TransactionAmount";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
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
import { formatDateShort } from "@/lib/money";
import { cn } from "cn";
import { ArrowDownLeft, ArrowUpRight } from "lucide-react";
import { Link } from "react-router-dom";

const RECENT_SIZE = 5;
const header = (
  <TableHeader>
    <TableRow>
      <TableHead className="w-1/2">Transfer</TableHead>
      <TableHead className="hidden w-1/4 sm:table-cell">Date</TableHead>
      <TableHead className="w-1/4 text-right">Amount</TableHead>
    </TableRow>
  </TableHeader>
);

export default function RecentActivity() {
  const { data, isPending, isError, isFetching, refetch } = useTransactions({
    page: 0,
    size: RECENT_SIZE,
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
        <CardDescription>Your last {RECENT_SIZE} transfers</CardDescription>
        <CardAction>
          <Button asChild variant="outline" size="sm">
            <Link to="/transactions">View all</Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <>
            <span className="sr-only">Loading activity</span>
            <Table aria-busy="true" className="table-fixed">
              {header}
              <TableBody>
                {Array.from({ length: RECENT_SIZE }, (_, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Skeleton className="size-8 rounded-full" />
                        <div className="flex flex-col">
                          <div>
                            <SkeletonText className="w-33" />
                          </div>
                          <div className="caption">
                            <SkeletonText className="w-20" />
                          </div>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell className="hidden sm:table-cell">
                      <SkeletonText className="w-20" />
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
          <ErrorAlert
            title="Couldn't load your activity"
            description="The server didn't answer. Your money is safe. Check your connection and try again."
            isRetrying={isFetching}
            onRetry={() => refetch()}
          />
        ) : data.content.length === 0 ? (
          <EmptyState
            title="No activity yet"
            description="Your transfers will show up here."
          />
        ) : (
          <Table className="table-fixed">
            {header}
            <TableBody>
              {data.content.map((t) => (
                <TableRow key={t.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <span
                        className={cn(
                          "flex size-8 shrink-0 items-center justify-center rounded-full bg-muted",
                          t.type === "TRANSFER_IN" &&
                            "bg-success/12 text-success",
                        )}
                      >
                        {t.type === "TRANSFER_IN" ? (
                          <ArrowDownLeft className="size-4" />
                        ) : (
                          <ArrowUpRight className="size-4" />
                        )}
                      </span>
                      <div className="flex flex-col">
                        <span className="font-medium">{t.counterparty}</span>
                        <span className="caption">{t.reference}</span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell className="hidden text-muted-foreground sm:table-cell">
                    {formatDateShort(Date.parse(t.createdAt))}
                  </TableCell>
                  <TableCell className="text-right">
                    <TransactionAmount transaction={t} />
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
