import BalanceCard from "@/components/dashboard/BalanceCard";
import { Button } from "@/components/ui/button";
import useBalances from "@/hooks/useBalances";
import { Send } from "lucide-react";
import { Link } from "react-router-dom";

export default function DashboardPage() {
  const { data, isPending, isError, error, refetch, dataUpdatedAt } =
    useBalances();
  const lastUpdated = new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(dataUpdatedAt);
  return (
    <div className="flex flex-col gap-4">
      <header className="flex items-start justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
        <Button asChild>
          <Link to="/transfer">
            <Send />
            Send money
          </Link>
        </Button>
      </header>
      {isPending ? (
        <p className="text-muted-foreground">Loading balances…</p>
      ) : isError ? (
        <div role="alert" className="flex flex-col items-start gap-2">
          <p className="text-danger">{error.message}</p>
          <Button variant="outline" size="sm" onClick={() => refetch()}>
            Try again
          </Button>
        </div>
      ) : (
        <>
          <span className="text-muted-foreground ">
            Balances as of {lastUpdated}
          </span>
          <ul className="grid gap-4 sm:grid-cols-3">
            {data.map((b) => (
              <li key={b.currency}>
                <BalanceCard balance={b} />
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
