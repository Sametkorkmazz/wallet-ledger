import BalanceCard from "@/components/dashboard/BalanceCard";
import BalanceCardSkeleton from "@/components/dashboard/BalanceCardSkeleton";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import useBalances from "@/hooks/useBalances";
import { CircleAlert, Inbox, RefreshCw, Send } from "lucide-react";
import { Link } from "react-router-dom";

const gridClass =
  "grid grid-cols-[repeat(auto-fit,minmax(--spacing(70),1fr))] gap-4";

export default function DashboardPage() {
  const { data, isPending, isError, refetch, dataUpdatedAt, isFetching } =
    useBalances();
  const lastUpdated = new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(dataUpdatedAt);
  return (
    <div className="flex flex-col gap-4">
      <header className="flex items-start justify-between">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
          {dataUpdatedAt > 0 ? (
            <p className="text-sm text-muted-foreground">
              Balances as of {lastUpdated}
            </p>
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
      ) : isError ? (
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
        <Card
          aria-labelledby="empty-title"
          className="mx-auto w-full max-w-md items-center py-10 text-center"
        >
          <span className="flex size-12 items-center justify-center rounded-full bg-primary/12 text-primary">
            <Inbox className="size-6" />
          </span>
          <CardContent className="flex flex-col gap-1.5">
            <h2 id="empty-title" className="font-semibold">
              No money here yet
            </h2>
            <p className="text-sm text-muted-foreground">
              Ask a friend to send you money. Your balances will show up here.
            </p>
          </CardContent>
        </Card>
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
