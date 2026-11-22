import EmptyState from "@/components/common/EmptyState";
import ErrorAlert from "@/components/common/ErrorAlert";
import BalanceCard from "@/components/dashboard/BalanceCard";
import BalanceCardSkeleton from "@/components/dashboard/BalanceCardSkeleton";
import RecentActivity from "@/components/dashboard/RecentActivity";
import PageHeader from "@/components/layout/PageHeader";
import { Button } from "@/components/ui/button";
import useBalances from "@/hooks/useBalances";
import { formatDate } from "@/lib/money";
import { Send } from "lucide-react";
import { Link } from "react-router-dom";

const gridClass =
  "grid grid-cols-[repeat(auto-fit,minmax(--spacing(70),1fr))] gap-4";

export default function DashboardPage() {
  const { data, isPending, isError, refetch, dataUpdatedAt, isFetching } =
    useBalances();

  const lastUpdated = dataUpdatedAt > 0 ? formatDate(dataUpdatedAt) : undefined;
  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="Dashboard"
        description={lastUpdated && `Balances as of ${lastUpdated}`}
        loading={isPending}
        action={
          <Button asChild>
            <Link to="/transfer">
              <Send />
              Send money
            </Link>
          </Button>
        }
      />
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
        <ErrorAlert
          title="Couldn't load your balances"
          isRetrying={isFetching}
          onRetry={refetch}
        />
      ) : data.length === 0 ? (
        <EmptyState
          title="No money here yet"
          description="Ask a friend to send you money. Your balances will show up here."
        />
      ) : (
        <ul className={gridClass}>
          {data.map((b) => (
            <li key={b.currency}>
              <BalanceCard balance={b} />
            </li>
          ))}
        </ul>
      )}
      <RecentActivity />
    </div>
  );
}
