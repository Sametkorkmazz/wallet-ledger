import { CURRENCY_NAME, CURRENCY_SYMBOL, type Balance } from "@/api/types";
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatMoney } from "@/lib/money";

type BalanceCardProps = {
  balance: Balance;
};

export default function BalanceCard({ balance }: BalanceCardProps) {
  return (
    <Card className="h-full [--card-spacing:--spacing(6)]">
      <CardHeader>
        <CardDescription>
          {CURRENCY_NAME[balance.currency]} balance
        </CardDescription>
        <CardTitle className="mt-2 text-3xl font-semibold tabular-nums">
          {formatMoney(balance.amount, balance.currency)}
        </CardTitle>
        <CardAction>
          <span className="inline-flex size-9 items-center justify-center rounded-md bg-primary/12 text-base font-semibold text-primary">
            {CURRENCY_SYMBOL[balance.currency]}
          </span>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-1 text-sm">
        <span className="text-muted-foreground">This month</span>
        <div className="flex flex-wrap items-center gap-x-1.5 tabular-nums">
          <span className="font-medium text-success">
            +{formatMoney(balance.inThisMonth, balance.currency)} in
          </span>
          <span aria-hidden="true" className="text-muted-foreground">
            ·
          </span>
          <span className="text-muted-foreground">
            {formatMoney(balance.outThisMonth, balance.currency)} out
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
