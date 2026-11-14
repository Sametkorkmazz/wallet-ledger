import { Button } from "@/components/ui/button";
import useBalances from "@/hooks/useBalances";

export default function DashboardPage() {
  const { data, isPending, isError, error, refetch } = useBalances();
  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
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
        <ul>
          {data.map((b) => (
            <li className="flex max-w-xs justify-between" key={b.currency}>
              {b.currency}
              <span className="tabular-nums">{b.amount}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
