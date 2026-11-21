import BalanceCard from "@/components/dashboard/BalanceCard";
import BalanceCardSkeleton from "@/components/dashboard/BalanceCardSkeleton";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@/components/ui/empty";
import { Skeleton } from "@/components/ui/skeleton";
import useBalances from "@/hooks/useBalances";
import { formatDate } from "@/lib/money";
import { CircleAlert, Inbox, RefreshCw, Send } from "lucide-react";
import { Link } from "react-router-dom";

const gridClass =
  "grid grid-cols-[repeat(auto-fit,minmax(--spacing(70),1fr))] gap-4";

export default function DashboardPage() {
  const { data, isPending, isError, refetch, dataUpdatedAt, isFetching } =
    useBalances();

  const lastUpdated = dataUpdatedAt > 0 ? formatDate(dataUpdatedAt) : undefined;
  return (
    <div className="flex flex-col gap-4">
      <header className="flex items-start justify-between">
        <div className="flex flex-col gap-1">
          <h1 className="page-title">Dashboard</h1>
          {dataUpdatedAt > 0 ? (
            <p className="caption">Balances as of {lastUpdated}</p>
          ) : (
            isPending && <Skeleton className="h-5 w-48" />
          )}
        </div>
        <Button asChild>
          <Link to="/transfer">
            <Send />
            Send money
          </Link>
        </Button>
      </header>
      {isPending ? (
        <>
          <span className="sr-only">Loading balances</span>
          <ul className={gridClass} aria-busy="true">
            {Array.from({ length: 3 }, (_, i) => (
              <li key={i}>
                <BalanceCardSkeleton />
              </li>
            ))}
          </ul>
        </>
      ) : isError && data === undefined ? (
        <Alert variant="destructive">
          <CircleAlert />
          <AlertTitle>Couldn't load your balances</AlertTitle>
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
      ) : data.length === 0 ? (
        <Empty aria-labelledby="empty-title">
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <Inbox className="size-6" />
            </EmptyMedia>
            <EmptyTitle id="empty-title">No money here yet</EmptyTitle>
            <EmptyDescription>
              Ask a friend to send you money. Your balances will show up here.
            </EmptyDescription>
          </EmptyHeader>
        </Empty>
      ) : (
        <ul className={gridClass}>
          {data.map((b) => (
            <li key={b.currency}>
              <BalanceCard balance={b} />
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
